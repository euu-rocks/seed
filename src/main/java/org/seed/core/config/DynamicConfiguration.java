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
import org.seed.core.api.Job;
import org.seed.core.codegen.CodeManager;
import org.seed.core.entity.value.ValueEntity;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DynamicConfiguration 
	implements SessionFactoryProvider, UpdatableConfiguration {
	
	private final static Logger log = LoggerFactory.getLogger(DynamicConfiguration.class);
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private CodeManager codeManager;
	
	@Autowired
	private JobScheduler jobScheduler;
	
	@Autowired
	private SchemaManager schemaManager;
	
	private SessionFactory sessionFactory;	// current sesssion factory
	
	private ClassLoader classLoader;		// current CompilerClassLoader
	
	private Dialect dialect;
	
	@PostConstruct
	private void init() {
		// update schema
		schemaManager.updateSchema();
		// build boot session factory
		sessionFactory = createSessionFactoryBuilder(true).build();
	}
	
	// called by ConfigurationInitiator
	void initConfiguration() {
		if (updateSchemaConfiguration()) { // new system schema version detected
			updateConfiguration();
		}
		else {
			buildConfiguration();
		}
	}
	
	@Override
	public SessionFactory getSessionFactory() {
		Assert.state(sessionFactory != null, "no session factory available");
		
		return sessionFactory;
	}
	
	@Override
	public Dialect getDialect() {
		if (dialect == null) {
			dialect = ((SessionFactoryImplementor) getSessionFactory()).getJdbcServices().getDialect();
		}
		return dialect;
	}
	
	@Override
	public void updateConfiguration() {
		log.info("Rebuild Configuration...");
		getSessionFactory().close();
		jobScheduler.unscheduleAllTasks();
		init();
		buildConfiguration();
	}
	
	private void buildConfiguration() {
		final long startTime = System.currentTimeMillis();
		codeManager.generateClasses();
		classLoader = codeManager.getClassLoader();
		// create new hibernate configuration but don't build session factory yet
		final SessionFactoryBuilder sessionFactoryBuilder = createSessionFactoryBuilder(false);
		// close current session factory
		getSessionFactory().close();
		// build new session factory now
		sessionFactory = sessionFactoryBuilder.build();
		scheduleJobs();
		log.info("Configuration created in " + MiscUtils.formatDuration(startTime));
	}
	
	private boolean updateSchemaConfiguration() {
		try (Session session = getSessionFactory().openSession()) {
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
						log.info("System schema updated to " + schemaConfig.getSchemaVersion());
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
			Assert.state(classLoader != null, "class loader not available");
			bootstrapServiceRegistryBuilder.applyClassLoader(classLoader);
		}
		final BootstrapServiceRegistry bootstrapServiceRegistry = 
				bootstrapServiceRegistryBuilder.applyIntegrator(new EventListenerIntegrator()).build();
		final ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder(bootstrapServiceRegistry)
				.applySettings(createSettings(boot))
				.build();
		final MetadataSources metaSources = new MetadataSources(serviceRegistry);
		registerSystemEntities(metaSources);
		if (!boot) {
			registerGeneratedEntities(metaSources);
		}
		return metaSources.getMetadataBuilder().build().getSessionFactoryBuilder();
	}
	
	private void registerSystemEntities(MetadataSources sources) {
		final ClassPathScanningCandidateComponentProvider scanner =
			new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		for (BeanDefinition beanDef : scanner.findCandidateComponents("org.seed.core")) {
			try {
				log.debug("Register " + beanDef.getBeanClassName());
				sources.addAnnotatedClass(Class.forName(beanDef.getBeanClassName()));
			} 
			catch (Exception ex) {
				throw new ConfigurationException("failed to register " + beanDef.getBeanClassName(), ex);
			}
		}
		log.info("System entities registered");
	}
	
	private void registerGeneratedEntities(MetadataSources metadataSources) {
		try {
			for (Class<GeneratedCode> entityClass : codeManager.getGeneratedClasses(ValueEntity.class)) {
				Assert.state(!Modifier.isAbstract(entityClass.getModifiers()), entityClass.getName() + " is abstract");
				
				log.debug("Register " + entityClass.getName());
				metadataSources.addAnnotatedClass(entityClass);
			}
			log.info("Generated entities registered");
		}
		catch (Exception ex) {
			throw new ConfigurationException("failed to register generated entities", ex);
		}
	}
	
	private void scheduleJobs() {
		try {
			for (Class<GeneratedCode> jobClass : codeManager.getGeneratedClasses(Job.class)) {
				final Job job = (Job) jobClass.getDeclaredConstructor().newInstance();
				jobScheduler.scheduleJob(job);
			}
		}	
		catch (Exception ex) {
			throw new ConfigurationException("failed to schedule jobs", ex);
		}
	}
	
	private String applicationProperty(String propertyName) {
		final String property = environment.getProperty(propertyName);
		Assert.state(property != null, "application property '" + propertyName + "' not available");
		return property;
	}
	
	private Map<String, Object> createSettings(boolean boot) {
		final Map<String, Object> settings = new HashMap<>();
		
		// misc settings
		settings.put("hibernate.enable_lazy_load_no_trans", "true");
		// https://stackoverflow.com/questions/43905119/postgres-error-method-org-postgresql-jdbc-pgconnection-createclob-is-not-imple
		settings.put("hibernate.jdbc.lob.non_contextual_creation", "true");
		
		// data source
		settings.put("hibernate.connection.url", applicationProperty("spring.datasource.url"));                                
		settings.put("hibernate.connection.username", applicationProperty("spring.datasource.username"));     
		settings.put("hibernate.connection.password", applicationProperty("spring.datasource.password"));
		settings.put("hibernate.connection.autocommit", "true");
		
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
		}
		return settings;
	}

}
