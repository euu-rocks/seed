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

import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Component;

@Component
public class ApplicationProperties {
	
	@Autowired
	private PropertyResolver propertyResolver;
	
	public boolean hasProperty(String propertyName) {
		return getProperty(propertyName) != null;
	}
	
	public String getProperty(String propertyName) {
		Assert.notNull(propertyName, "property name");
		
		return propertyResolver.getProperty(propertyName);
	}
	
	public String getRequiredProperty(String propertyName) {
		final String property = getProperty(propertyName);
		if (property == null) {
			throw new ConfigurationException("application property '" + propertyName + 
					 						 "' is not available");
		}
		return property;
	}
	
	public Integer getIntegerProperty(String propertyName) {
		final String property = getProperty(propertyName);
		if (property != null) {
			try {
				return Integer.parseInt(property);
			}
			catch (NumberFormatException nfex) {
				throw new ConfigurationException("application property '" + propertyName + 
												 "' is not an integer value: " + property);
			}
		}
		return null;
	}
	
}
