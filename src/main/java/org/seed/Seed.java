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
import org.seed.core.application.ApplicationContextProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@Controller
@EnableAsync
@EnableScheduling
@ZKCEApplication
@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class,
								  org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration.class})
public class Seed {
	
	public static final String BASE_APPLICATION_PACKAGE			 = "org.seed";
	
	public static final String PROP_DATASOURCE_URL               = "spring.datasource.url"; 
	public static final String PROP_DATASOURCE_USERNAME          = "spring.datasource.username"; 
	public static final String PROP_DATASOURCE_PASSWORD          = "spring.datasource.password"; 
	
	public static final String PROP_CONNECTIONPOOL_TIMEOUT       = "db.connectionpool.connectionTimeout";
	public static final String PROP_CONNECTIONPOOL_MINIDLE       = "db.connectionpool.minimumIdle";
	public static final String PROP_CONNECTIONPOOL_POOLSIZE      = "db.connectionpool.maximumPoolSize";
	public static final String PROP_CONNECTIONPOOL_IDLE_TIMEOUT  = "db.connectionpool.idleTimeout";
	
	public static final String PROP_BATCH_SIZE                   = "db.batchprocessing.batch_size";
	
	public static final String PROP_EXTERN_API_JAVADOC_URL		 = "extern.apijavadoc.url";
	
	public static final String PROP_SEARCH_SOLR_ENABLE           = "search.solr.enable";
	public static final String PROP_SEARCH_SOLR_URL              = "search.solr.url";
	
	public static final String PROP_CODEGEN_EXT_ROOT_DIR         = "codegen.external.rootdir";
	public static final String PROP_CODEGEN_EXT_DOWNLOAD_SOURCES = "codegen.external.downloadsources";
	public static final String PROP_CODEGEN_EXT_UPLOAD_CHANGES   = "codegen.external.uploadchanges";
	
	public static final String PROP_MODULE_EXT_ROOT_DIR          = "module.external.rootdir";
	
	private static LabelProvider labelProvider;
	
	public static void main(String[] args) {
		SpringApplication.run(Seed.class, args);
	}
	
	@GetMapping("/seed")
	public String seed() {
		return C.SEED;
	}
	
	public static String getLabel(String key, String ...params) {
		return getLabelProvider().getLabel(key, params);
	}
	
	public static String getEnumLabel(Enum<?> enm) {
		return getLabelProvider().getEnumLabel(enm);
	}
	
	public static LabelProvider getLabelProvider() {
		if (labelProvider == null) {
			labelProvider = getBean(LabelProvider.class);
		}
		return labelProvider;
 	}
	
	public static <T> T getBean(Class<T> typeClass) {
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		
		if (typeClass == null) {
			throw new IllegalArgumentException("type class is null");
		}
		// context only available after startup completed
		if (applicationContext == null) {
			throw new IllegalStateException("application context not available");
		}
    	return applicationContext.getBean(typeClass);
	}

}
