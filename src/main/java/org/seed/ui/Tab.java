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
package org.seed.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.seed.C;
import org.seed.core.util.Assert;
import org.seed.core.util.NameUtils;

import org.zkoss.util.resource.Labels;

public final class Tab {
	
	private String tabName;
	
	private String name;
	
	private String path;
	
	private String iconClass;
	
	private Object parameter;
	
	private Long objectId;
	
	private Map<String, Object> properties;
	
	public Tab(String name, Object parameter) {
		this(name, null, null, parameter);
	}
	
	public Tab(String name, String path, String iconClass) {
		this(name, path, iconClass, null);
	}
	
	public Tab(String name, String path, String iconClass, Object parameter) {
		Assert.notNull(name, C.NAME);
		
		this.name = name;
		this.tabName = name;
		this.path = path;
		this.iconClass = iconClass;
		this.parameter = parameter;
	}

	public String getName() {
		return name;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String name) {
		Assert.notNull(name, C.NAME);
		
		return properties != null 
				? (T) properties.get(name) 
				: null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T removeProperty(String name) {
		Assert.notNull(name, C.NAME);
		
		return properties != null
				? (T) properties.remove(name)
				: null;
	}
	
	public void setProperty(String name, Object value) {
		Assert.notNull(name, C.NAME);
		Assert.notNull(value, C.VALUE);
		
		if (properties == null) {
			properties = new ConcurrentHashMap<>();
		}
		properties.put(name, value);
	}
	
	public void setName(String name) {
		this.name = tabName + ": " + (name != null ? name : Labels.getLabel("label.new"));
	}
	
	public String getTestClass() {
		return NameUtils.getInternalName(tabName).toLowerCase().replace('_', '-') + "-tab";
	}
	
	public void resetName() {
		name = tabName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getIconClass() {
		return iconClass;
	}

	public void setIconClass(String iconClass) {
		this.iconClass = iconClass;
	}

	public Object getParameter() {
		return parameter;
	}

	public void setParameter(Object parameter) {
		this.parameter = parameter;
	}

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}

}
