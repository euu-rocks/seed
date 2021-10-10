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
package org.seed.core.data;

import java.util.Arrays;

public enum SystemField {
	
    ID           (FieldType.LONG,      "id",           "id"),
    VERSION      (FieldType.INTEGER,   "version",      "version"),
    CREATEDON    (FieldType.DATETIME,  "createdOn",    "createdon"),
    CREATEDBY    (FieldType.TEXT,      "createdBy",    "createdby"),
    MODIFIEDON   (FieldType.DATETIME,  "modifiedOn",   "modifiedon"),
    MODIFIEDBY   (FieldType.TEXT,      "modifiedBy",   "modifiedby"),
    ENTITYSTATUS (FieldType.REFERENCE, "entityStatus", "status_id");
	
	public final FieldType type;
	
	public final String property;
	
	public final String columName;
	
	private SystemField(FieldType type, String property, String columName) {
		this.type = type;
		this.property = property;
		this.columName = columName;
	}
	
	public static SystemField[] valuesWithoutIdAndVersion() {
		return Arrays.copyOfRange(values(), 2, values().length);
	}
	
}
