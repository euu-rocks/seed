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

import org.springframework.util.Assert;
import org.zkoss.util.resource.Labels;

public class Tab {
	
	private String tabName;
	
	private String name;
	
	private String path;
	
	private String iconClass;
	
	private Object parameter;
	
	private Long objectId;
	
	public Tab(String name, Object parameter) {
		this(name, null, null, parameter);
	}
	
	public Tab(String name, String path, String iconClass, Object parameter) {
		Assert.notNull(name, "name is null");
		
		this.name = name;
		this.tabName = name;
		this.path = path;
		this.iconClass = iconClass;
		this.parameter = parameter;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = tabName + ": " + (name != null ? name : Labels.getLabel("label.new"));
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
