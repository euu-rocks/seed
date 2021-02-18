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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
public class Limits {
	
	private final Properties limits = new Properties();
	
	@Value("classpath:schema-limits.properties")
    private Resource limitsResource;
	
	@PostConstruct
	private void init() {
		Assert.notNull(limitsResource, "schema-limits.properties not found");
		
		try (InputStream inputStream = limitsResource.getInputStream()) {
			limits.load(inputStream);
		}
		catch (IOException ex) {
			throw new ConfigurationException("failed to load schema-limits.properties", ex);
		}
	}
	
	public int getLimit(String limitName) {
		Assert.notNull(limitName, "limitName is null");
		
		final String limit = limits.getProperty(limitName);
		if (!StringUtils.hasText(limit)) {
			throw new ConfigurationException("limit '" + limitName + "' is not available");
		}
		try {
			return Integer.parseInt(limit);
		}
		catch (NumberFormatException nfe) {
			throw new ConfigurationException("limit '" + limitName + "' is not an integer: " + limit);
		}
	}

}
