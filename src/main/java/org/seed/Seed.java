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
package org.seed;

import org.seed.config.ZKCEApplication;
import org.seed.core.config.UpdatableConfiguration;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@EnableAsync
@EnableScheduling
@ZKCEApplication
@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class,
								  org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration.class})
public class Seed {
	
	public static final String PROP_DATASOURCE_URL               = "spring.datasource.url"; 
	public static final String PROP_DATASOURCE_USERNAME          = "spring.datasource.username"; 
	public static final String PROP_DATASOURCE_PASSWORD          = "spring.datasource.password"; 
	
	public static final String PROP_CONNECTIONPOOL_TIMEOUT       = "db.connectionpool.connectionTimeout";
	public static final String PROP_CONNECTIONPOOL_MINIDLE       = "db.connectionpool.minimumIdle";
	public static final String PROP_CONNECTIONPOOL_POOLSIZE      = "db.connectionpool.maximumPoolSize";
	public static final String PROP_CONNECTIONPOOL_IDLE_TIMEOUT  = "db.connectionpool.idleTimeout";
	
	public static final String PROP_BATCH_SIZE                   = "db.batchprocessing.batch_size";
	
	public static final String PROP_SEARCH_SOLR_ENABLE           = "search.solr.enable";
	public static final String PROP_SEARCH_SOLR_URL              = "search.solr.url";
	
	public static final String PROP_CODEGEN_EXT_ROOTDIR          = "codegen.external.rootdir";
	public static final String PROP_CODEGEN_EXT_DOWNLOAD_SOURCES = "codegen.external.downloadsources";
	public static final String PROP_CODEGEN_EXT_UPOAD_CHANGES    = "codegen.external.downloadsources";
	
	private static ApplicationContext applicationContext;
	
	private static Seed instance;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private BuildProperties buildProperties;
	
	public static void main(String[] args) {
		applicationContext = SpringApplication.run(Seed.class, args);
	}
	
	@GetMapping("/seed")
	public String seed() {
		return C.SEED;
	}
	
	public String applicationProperty(String propertyName) {
		Assert.notNull(propertyName, "property name");
		
		return environment.getProperty(propertyName);
	}
	
	public static String getApplicationProperty(String propertyName) {
		return getInstance().applicationProperty(propertyName);
	}
	
	public String getVersion() {
		return buildProperties != null
				? buildProperties.getVersion()
				: null;
	}
	
	public static <T> T getBean(Class<T> typeClass) {
		Assert.notNull(typeClass, C.TYPECLASS);
    	// context only available after startup completed
		Assert.stateAvailable(applicationContext, "applicationContext");
		
    	return applicationContext.getBean(typeClass);
	}
	
	public static void updateConfiguration() {
		getBean(UpdatableConfiguration.class).updateConfiguration();
	}
	
	private static Seed getInstance() {
		if (instance == null) {
			instance = getBean(Seed.class);
			Assert.stateAvailable(instance, "instance");
		}
		return instance;
	}
	
}
