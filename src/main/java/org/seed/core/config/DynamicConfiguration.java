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

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.persistence.Entity;

import org.seed.core.codegen.GeneratedCode;
import org.seed.core.codegen.CodeManager;
import org.seed.core.entity.value.ValueEntity;
import org.seed.core.task.JobScheduler;
import org.seed.core.user.UserService;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DynamicConfiguration implements UpdatableConfiguration {
	
	private static final Logger log = LoggerFactory.getLogger(DynamicConfiguration.class);
	
	@Autowired
	private Environment environment;
	
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
	
	private ClassLoader classLoader;	// current class loader
	
	@PostConstruct
	private void init() {
		buildBootSessionFactory();
		userService.createDefaultUserAndGroup();
	}
	
	@EventListener(ApplicationReadyEvent.class)
	public void initConfiguration() {
		if (updateSchemaConfiguration()) { // new system schema version detected
			updateConfiguration();
		}
		else {
			buildConfiguration();
		}
	}
	
	@Override
	public synchronized void updateConfiguration() {
		log.info("Updating configuration...");
		sessionProvider.close();
		jobScheduler.unscheduleAllTasks();
		buildBootSessionFactory();
		buildConfiguration();
	}
	
	private void buildBootSessionFactory() {
		schemaManager.updateSchema();
		sessionProvider.setSessionFactory(createSessionFactoryBuilder(true).build());
	}
	
	private void buildConfiguration() {
		log.info("Creating configuration...");
		final long startTime = System.currentTimeMillis();
		codeManager.generateClasses();
		classLoader = codeManager.getClassLoader();
		// create new hibernate configuration but don't build session factory yet
		final SessionFactoryBuilder sessionFactoryBuilder = createSessionFactoryBuilder(false);
		// close current session factory
		sessionProvider.close();
		// build new session factory now
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
				boolean saveConfig = false;
				SchemaConfiguration schemaConfig = schemaManager.loadSchemaConfiguration(session);
				// config is new
				if (schemaConfig == null) {
					schemaConfig = new SchemaConfiguration();
					if (SchemaVersion.existUpdates()) {
						schemaManager.updateSchemaConfiguration(SchemaVersion.firstVersion(), session);
					}
					saveConfig = true;
				}
				// config is not up to date
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
		final BootstrapServiceRegistryBuilder bootstrapServiceRegistryBuilder = new BootstrapServiceRegistryBuilder();
		if (!boot) {
			Assert.stateAvailable(classLoader, "class loader");
			bootstrapServiceRegistryBuilder.applyClassLoader(classLoader);
		}
		final BootstrapServiceRegistry bootstrapServiceRegistry = 
				bootstrapServiceRegistryBuilder.applyIntegrator(new EventListenerIntegrator()).build();
		final ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder(bootstrapServiceRegistry)
				.applySettings(createSettings(boot))
				.build();
		final MetadataSources metaSources = new MetadataSources(serviceRegistry);
		
		// register system entities
		for (Class<?> annotatedClass : BeanUtils.getAnnotatedClasses(Entity.class)) {
			metaSources.addAnnotatedClass(annotatedClass);
		}
		log.info("System entities registered");
		
		// register generated entities
		if (!boot) {
			registerGeneratedEntities(metaSources);
		}
		return metaSources.getMetadataBuilder().build().getSessionFactoryBuilder();
	}
	
	private void registerGeneratedEntities(MetadataSources metadataSources) {
		try {
			for (Class<GeneratedCode> entityClass : codeManager.getGeneratedClasses(ValueEntity.class)) {
				Assert.state(!Modifier.isAbstract(entityClass.getModifiers()), entityClass.getName() + " is abstract");
				
				log.debug("Register {}", entityClass.getName());
				metadataSources.addAnnotatedClass(entityClass);
			}
			log.info("Generated entities registered");
		}
		catch (Exception ex) {
			throw new ConfigurationException("failed to register generated entities", ex);
		}
	}
	
	private String applicationProperty(String propertyName) {
		final String property = environment.getProperty(propertyName);
		Assert.stateAvailable(property, "application property '" + propertyName + "'");
		
		return property;
	}
	
	private Map<String, Object> createSettings(boolean boot) {
		final Map<String, Object> settings = new HashMap<>();
		
		// misc settings
		settings.put("hibernate.enable_lazy_load_no_trans", "true");
		
		// data source
		settings.put("hibernate.connection.url", applicationProperty("spring.datasource.url"));                                
		settings.put("hibernate.connection.username", applicationProperty("spring.datasource.username"));     
		settings.put("hibernate.connection.password", applicationProperty("spring.datasource.password"));
		
		// connection pool
		settings.put("hibernate.hikari.connectionTimeout", applicationProperty("connectionpool.connectionTimeout"));
		settings.put("hibernate.hikari.minimumIdle", applicationProperty("connectionpool.minimumIdle"));
		settings.put("hibernate.hikari.maximumPoolSize", applicationProperty("connectionpool.maximumPoolSize"));
		settings.put("hibernate.hikari.idleTimeout", applicationProperty("connectionpool.idleTimeout"));
		
		// cache
		settings.put("hibernate.cache.use_second_level_cache", String.valueOf(!boot));
		if (!boot) {
			settings.put("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory");
			settings.put("hibernate.javax.cache.missing_cache_strategy", "create");
			// statistics
			settings.put("hibernate.generate_statistics", "true");
		}
		return settings;
	}

}
