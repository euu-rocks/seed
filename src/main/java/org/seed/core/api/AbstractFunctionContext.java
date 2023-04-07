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
package org.seed.core.api;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.util.Assert;

/**
 * Abstract base class of all function contexts and an implementation of {@link FunctionContext}.
 * It provides basic functionality for setting and reading context properties.
 * 
 * @author seed-master
 *
 */
public abstract class AbstractFunctionContext implements FunctionContext {
	
	private Map<String, Object> mapProperties;
	
	private String successMessage;
	
	public abstract Session getSession();
	
	/**
	 * Returns the success message or null if none
	 * @return the success message or null if none
	 */
	@Override
	public String getSuccessMessage() {
		return successMessage;
	}
	
	/**
	 * Sets the success message
	 * @param successMessage the success message
	 */
	@Override
	public void setSuccessMessage(String successMessage) {
		Assert.notNull(successMessage, "success massage");
		
		this.successMessage = successMessage;
	}
	
	/**
	 * Checks if there is a context property with the given name
	 * @param name the name of the context property
	 * @return <code>true</code> if there is a property with the given name
	 */
	@Override
	public boolean hasProperty(String name) {
		Assert.notNull(name, C.NAME);
		
		return mapProperties != null && mapProperties.containsKey(name);
	}
	
	/**
	 * Returns the value of a context property with the given name
	 * @param name the name of the context property
	 * @return the value of the context property or null if the property doesn't exist
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String name) {
		Assert.notNull(name, C.NAME);
		
		return mapProperties != null ? (T) mapProperties.get(name) : null;
	}
	
	/**
	 * Sets the value of a context property. By setting it to null, the property can be removed.
	 * @param name the name of the context property
	 * @param value the value of a context property. Can also be null
	 */
	@Override
	public void setProperty(String name, Object object) {
		Assert.notNull(name, C.NAME);
		
		if (object != null) {
			if (mapProperties == null) {
				mapProperties = new HashMap<>();
			}
			mapProperties.put(name, object);
		}
		else if (mapProperties != null) {
			mapProperties.remove(name);
		}
	}
	
}
