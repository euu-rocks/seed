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

public abstract class AbstractFunctionContext implements FunctionContext {
	
	private Map<String, Object> mapProperties;
	
	private String successMessage;
	
	public abstract Session getSession();
	
	@Override
	public String getSuccessMessage() {
		return successMessage;
	}
	
	@Override
	public void setSuccessMessage(String successMessage) {
		this.successMessage = successMessage;
	}
	
	@Override
	public boolean hasProperty(String name) {
		Assert.notNull(name, C.NAME);
		
		return mapProperties != null && mapProperties.containsKey(name);
	}
	
	@Override
	public Object getProperty(String name) {
		Assert.notNull(name, C.NAME);
		
		return mapProperties != null ? mapProperties.get(name) : null;
	}
	
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
