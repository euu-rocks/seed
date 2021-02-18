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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

public enum FieldType {
	
	AUTONUM		(String.class,		"VARCHAR"),	
	BINARY		(byte[].class,		"BLOB"),
	BOOLEAN 	(boolean.class, 	"BOOLEAN"),
	DATE		(Date.class,		"DATE"),
	DATETIME	(Date.class,		"DATETIME"),
	DECIMAL 	(BigDecimal.class,	"DECIMAL"),
	DOUBLE		(Double.class,		"DOUBLE"),
	FILE		(FileObject.class,	"BIGINT"),
	INTEGER 	(Integer.class,		"INT"),
	LONG		(Long.class,		"BIGINT"),				
	REFERENCE	(Object.class,		"BIGINT"),
	TEXT 		(String.class,		"VARCHAR"),
	TEXTLONG 	(String.class,		"CLOB");
	
	public final Class<?> typeClass;
	
	public final String dbType;

	private FieldType(Class<?> typeClass, String dbType) {
		this.typeClass = typeClass;
		this.dbType = dbType;
	}
	
	public boolean isAutonum() {
		return this == AUTONUM;
	}
	
	public boolean isBinary() {
		return this == BINARY;
	}
	
	public boolean isBoolean() {
		return this == BOOLEAN;
	}
	
	public boolean isDate() {
		return this == DATE;
	}
	
	public boolean isDateTime() {
		return this == DATETIME;
	}
	
	public boolean isDecimal() {
		return this == DECIMAL;
	}
	
	public boolean isDouble() {
		return this == DOUBLE;
	}
	
	public boolean isFile() {
		return this == FILE;
	}
	
	public boolean isInteger() {
		return this == INTEGER;
	}
	
	public boolean isLong() {
		return this == LONG;
	}
	
	public boolean isReference() {
		return this == REFERENCE;
	}
	
	public boolean isText() {
		return this == TEXT;
	}
	
	public boolean isTextLong() {
		return this == TEXTLONG;
	}
	
	public static FieldType[] valuesWithoutAutonum() {
		return Arrays.copyOfRange(values(), 1, values().length);
	}
	
}
