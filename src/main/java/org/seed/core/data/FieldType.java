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

import org.seed.core.util.MiscUtils;

public enum FieldType {
	
	AUTONUM		(String.class,		DataType.VARCHAR),	
	BINARY		(byte[].class,		DataType.BLOB),
	BOOLEAN 	(boolean.class, 	DataType.BOOLEAN),
	DATE		(Date.class,		DataType.DATE),
	DATETIME	(Date.class,		DataType.DATETIME),
	DECIMAL 	(BigDecimal.class,	DataType.DECIMAL),
	DOUBLE		(Double.class,		DataType.DOUBLE),
	FILE		(FileObject.class,	DataType.BIGINT),
	INTEGER 	(Integer.class,		DataType.INT),
	LONG		(Long.class,		DataType.BIGINT),				
	REFERENCE	(Object.class,		DataType.BIGINT),
	TEXT 		(String.class,		DataType.VARCHAR),
	TEXTLONG 	(String.class,		DataType.CLOB);
	
	private static final FieldType[] TRANSFERABLE_TYPES = 
			MiscUtils.toArray(
				FieldType.BOOLEAN,
				FieldType.DATE,
				FieldType.DATETIME,
				FieldType.DECIMAL,
				FieldType.DOUBLE,
				FieldType.INTEGER,
				FieldType.LONG,
				FieldType.TEXT,
				FieldType.TEXTLONG
	); 
	
	private static final FieldType[] NON_AUTONUM_TYPES = 
			Arrays.copyOfRange(values(), 1, values().length);
	
	public final Class<?> typeClass;
	
	public final DataType dataType;

	private FieldType(Class<?> typeClass, DataType dataType) {
		this.typeClass = typeClass;
		this.dataType = dataType;
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
	
	public boolean supportsMinMaxValues() {
		return isDate() || isDateTime() || isDecimal() || 
			   isDouble() || isInteger() || isLong();
	}
	
	public boolean supportsValidation() {
		return isText() || isTextLong();
	}
	
	public Object nullValue() {
		return isBoolean() ? Boolean.FALSE : null;
	}
	
	public static FieldType[] nonAutonumTypes() {
		return NON_AUTONUM_TYPES;
	}
	
	public static FieldType[] transferableTypes() {
		return TRANSFERABLE_TYPES;
	}
	
	public enum DataType {
		
		BIGINT,
		BLOB,
		BOOLEAN,
		CLOB,
		DATE,
		DATETIME,
		DECIMAL,
		DOUBLE,
		INT,
		VARCHAR
		
	}
	
}
