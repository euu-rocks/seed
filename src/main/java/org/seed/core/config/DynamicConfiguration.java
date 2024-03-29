/**
 * Seed
 * Copyright (C) 2021 EUU⛰ROCKS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.seed.core.config;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.persistence.Entity;

import org.seed.C;
import org.seed.Seed;
import org.seed.core.data.SystemObjectEventListener;
import org.seed.core.data.revision.RevisionField;
import org.seed.core.codegen.CodeManager;
import org.seed.core.entity.value.ValueEntity;
import org.seed.core.task.job.JobScheduler;
import org.seed.core.user.UserService;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;
import org.seed.core.util.MiscUtils;
import org.seed.ui.zk.UIUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.internal.MetadataImpl;
import org.hibernate.boot.internal.SessionFactoryBuilderImpl;
import org.hibernate.boot.internal.SessionFactoryOptionsBuilder;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DynamicConfiguration implements UpdatableConfiguration, Integrator {
	
	private static final Logger log = LoggerFactory.getLogger(DynamicConfiguration.class);
	
	private class DynamicSessionFactoryBuilder extends SessionFactoryBuilderImpl {
		
		private DynamicSessionFactoryBuilder(MetadataImpl metaImpl) {
			super(metaImpl,
				  new SessionFactoryOptionsBuilder(
						metaImpl.getMetadataBuildingOptions().getServiceRegistry(),
						metaImpl.getBootstrapContext()
				  ) 
				  {
						@Override
						public String getUuid() {
							return C.SEED;
						}
				  }
			);
		}
		
	}
	
	@Autowired
	private ApplicationProperties appProperties;
	
	@Autowired
	private CodeManager codeManager;
	
	@Autowired
	private DefaultSessionProvider sessionProvider;
	
	@Autowired
	private JobScheduler jobScheduler;
	
	@Autowired
	private SchemaManager schemaManager;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private SystemLog systemLog;
	
	private ClassLoader classLoader;
	
	@PostConstruct
	private void init() {
		schemaManager.checkLiquibaseLock();
		buildBootSessionFactory();
	}
	
	@EventListener(ApplicationReadyEvent.class)
	private void initConfiguration() {
		if (updateSchemaConfiguration()) {
			systemLog.logInfo("systemlog.info.schemaupdated", SchemaVersion.currentVersion().name());
			updateConfiguration(false);
			userService.initDefaults();
		}
		else {
			buildConfiguration(false);
			systemLog.logInfo("systemlog.info.configcreated");
		}
	}
	
	@Override
	public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory,
						  SessionFactoryServiceRegistry serviceRegistry) {
		serviceRegistry.getService(EventListenerRegistry.class)
							.getEventListenerGroup(EventType.SAVE_UPDATE)
							.appendListener(new SystemObjectEventListener());
	}

	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
		// do nothing
	}
	
	@Override
	public void updateConfiguration() {
		updateConfiguration(false);
	}
	
	@Override
	public synchronized void updateConfiguration(boolean compileAllClasses) {
		log.info("Updating configuration...");
		sessionProvider.close();
		jobScheduler.unscheduleAllTasks();
		buildBootSessionFactory();
		buildConfiguration(compileAllClasses);
		systemLog.logInfo("systemlog.info.configupdated");
	}
	
	private void buildBootSessionFactory() {
		final boolean updateResult = schemaManager.updateSchema();
		sessionProvider.setSessionFactory(createSessionFactoryBuilder(true).build());
		if (!updateResult) {
			schemaManager.checkLiquibaseLock();
			schemaManager.repairSchema();
			UIUtils.showSchemaErrorWarning();
		}
	}
	
	private void buildConfiguration(boolean buildAllClasses) {
		log.info("Creating configuration...");
		final long startTime = System.currentTimeMillis();
		codeManager.generateClasses(buildAllClasses);
		classLoader = codeManager.getClassLoader();
		final var sessionFactoryBuilder = createSessionFactoryBuilder(false);
		sessionProvider.close();
		sessionProvider.setSessionFactory(sessionFactoryBuilder.build());
		jobScheduler.scheduleAllTasks();
		if (log.isInfoEnabled()) {
			log.info("Configuration created in {}", MiscUtils.formatDuration(startTime));
		}
	}
	
	private boolean updateSchemaConfiguration() {
		try (Session session = sessionProvider.getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				var saveConfig = false;
				var schemaConfig = schemaManager.loadSchemaConfiguration(session);
				if (schemaConfig == null) {
					schemaConfig = new SchemaConfiguration();
					if (SchemaVersion.existUpdates()) {
						schemaManager.updateSchemaConfiguration(SchemaVersion.firstVersion(), session);
					}
					saveConfig = true;
				}
				else if (!schemaConfig.isUpToDate()) {
					schemaManager.updateSchemaConfiguration(schemaConfig.getSchemaVersion(), session);
					saveConfig = true;
				}
				if (saveConfig) {
					schemaConfig.setSchemaVersion(SchemaVersion.lastVersion());
					session.saveOrUpdate(schemaConfig);
					if (SchemaVersion.existUpdates()) {
						log.info("System schema updated to {}", schemaConfig.getSchemaVersion());
					}
				}
				tx.commit();
				return saveConfig && SchemaVersion.existUpdates();
			}
			catch (Exception ex) {
				if (tx != null) {
					tx.rollback();
				}
				throw ex;
			}
		}
	}
	
	private SessionFactoryBuilder createSessionFactoryBuilder(boolean boot) {
		final var bootstrapServiceRegistryBuilder = new BootstrapServiceRegistryBuilder().applyIntegrator(this);
		if (!boot) {
			Assert.stateAvailable(classLoader, "class loader");
			bootstrapServiceRegistryBuilder.applyClassLoader(classLoader);
		}
		final var metaSources = new MetadataSources(
				new StandardServiceRegistryBuilder(bootstrapServiceRegistryBuilder.build())
					 .applySettings(createSettings(boot)).build());
		
		BeanUtils.getAnnotatedClasses(Entity.class).forEach(metaSources::addAnnotatedClass);
		log.info("System entities registered");
		
		if (!boot) {
			codeManager.getGeneratedClasses(ValueEntity.class).forEach(metaSources::addAnnotatedClass);
			log.info("Generated entities registered");
		}
		return new DynamicSessionFactoryBuilder((MetadataImpl) metaSources.getMetadataBuilder().build());
	}
	
	private Map<String, Object> createSettings(boolean boot) {
		final var settings = new HashMap<String, Object>();
		
		// data source
		settings.put("hibernate.connection.url", appProperties.getRequiredProperty(Seed.PROP_DATASOURCE_URL));                                
		settings.put("hibernate.connection.username", appProperties.getRequiredProperty(Seed.PROP_DATASOURCE_USERNAME));     
		settings.put("hibernate.connection.password", appProperties.getRequiredProperty(Seed.PROP_DATASOURCE_PASSWORD));
		
		// connection pool
		settings.put("hibernate.hikari.connectionTimeout", appProperties.getRequiredProperty(Seed.PROP_CONNECTIONPOOL_TIMEOUT));
		settings.put("hibernate.hikari.minimumIdle", appProperties.getRequiredProperty(Seed.PROP_CONNECTIONPOOL_MINIDLE));
		settings.put("hibernate.hikari.maximumPoolSize", appProperties.getRequiredProperty(Seed.PROP_CONNECTIONPOOL_POOLSIZE));
		settings.put("hibernate.hikari.idleTimeout", appProperties.getRequiredProperty(Seed.PROP_CONNECTIONPOOL_IDLE_TIMEOUT));
		
		// cache
		settings.put("hibernate.cache.use_second_level_cache", String.valueOf(!boot));
		if (!boot) {
			settings.put("hibernate.cache.use_query_cache", C.TRUE);
			settings.put("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory");
			settings.put("hibernate.javax.cache.missing_cache_strategy", "create");
		}
		
		// batch processing
		if (!boot && appProperties.hasProperty(Seed.PROP_BATCH_SIZE)) {
			settings.put("hibernate.jdbc.batch_size", appProperties.getProperty(Seed.PROP_BATCH_SIZE));
			settings.put("hibernate.order_inserts", C.TRUE);
			settings.put("hibernate.order_updates", C.TRUE);
			settings.put("hibernate.batch_versioned_data", C.TRUE);
		}
		
		// statistics
		if (!boot) {
			settings.put("hibernate.generate_statistics", C.TRUE);
		}
		
		// misc settings
		settings.put("hibernate.enable_lazy_load_no_trans", C.TRUE);
		settings.put("org.hibernate.envers.revision_field_name", RevisionField.REV.columName);
		settings.put("org.hibernate.envers.revision_type_field_name", RevisionField.REVTYPE.columName);
		return settings;
	}
	
}
