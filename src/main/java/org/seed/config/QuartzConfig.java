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
package org.seed.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {
	
	@Value("classpath:quartz.properties")
    private Resource propertiyResource;
	
	@Bean
	SchedulerFactoryBean quartzScheduler() throws IOException {
		final var quartzScheduler = new SchedulerFactoryBean();
		
		// load properties
		final var properties = new Properties();
		try (final var inputStream = propertiyResource.getInputStream()) {
			properties.load(inputStream);
		}
		quartzScheduler.setQuartzProperties(properties);
		return quartzScheduler;
	}
	
}
