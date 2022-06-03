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
package org.seed.core.entity.filter;

import org.seed.core.data.FieldType;
import org.seed.core.data.SystemField;
import org.seed.core.entity.EntityField;

public class FilterElement {
	
	private EntityField entityField;
	
	private SystemField systemField;

	public EntityField getEntityField() {
		return entityField;
	}

	public void setEntityField(EntityField entityField) {
		this.entityField = entityField;
	}

	public SystemField getSystemField() {
		return systemField;
	}

	public void setSystemField(SystemField systemField) {
		this.systemField = systemField;
	}
	
	public boolean isField() {
		return (systemField != null && systemField.type != FieldType.REFERENCE) || 
			   (entityField != null && !getType().isReference());
	}
	
	public boolean isReference() {
		return (systemField != null && systemField.type == FieldType.REFERENCE) ||
			   (entityField != null && getType().isReference());
	}
	
	public boolean isDateTimeField() {
		return getType() == FieldType.DATETIME;
	}
	
	public FieldType getType() {
		return entityField != null 
				? entityField.getType() 
				: getSystemFieldType();
	}
	
	private FieldType getSystemFieldType() {
		return systemField != null 
				? systemField.type 
				: null;
	}
	
}
