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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.seed.core.data.FieldType;

public enum CriterionOperator {
				  // fieldTypes must be sorted for Arrays.binarySearch
	EMPTY		  (FieldType.AUTONUM, FieldType.BINARY, FieldType.DATE, FieldType.DATETIME, 
				   FieldType.DECIMAL, FieldType.DOUBLE, FieldType.FILE, FieldType.INTEGER, 
				   FieldType.LONG,    FieldType.REFERENCE, FieldType.TEXT, FieldType.TEXTLONG),
	NOT_EMPTY	  (FieldType.AUTONUM, FieldType.BINARY, FieldType.DATE, FieldType.DATETIME, 
				   FieldType.DECIMAL, FieldType.DOUBLE, FieldType.FILE, FieldType.INTEGER, 
				   FieldType.LONG,    FieldType.REFERENCE, FieldType.TEXT, FieldType.TEXTLONG),
	EQUAL 		  (FieldType.AUTONUM, FieldType.BOOLEAN, FieldType.DATE, FieldType.DATETIME, 
				   FieldType.DECIMAL, FieldType.DOUBLE, FieldType.INTEGER, FieldType.LONG,
				   FieldType.REFERENCE, FieldType.TEXT, FieldType.TEXTLONG),
	NOT_EQUAL	  (FieldType.AUTONUM, FieldType.BOOLEAN, FieldType.DATE, FieldType.DATETIME, 
				   FieldType.DECIMAL, FieldType.DOUBLE, FieldType.INTEGER, FieldType.LONG,
				   FieldType.REFERENCE, FieldType.TEXT, FieldType.TEXTLONG),
	GREATER 	  (FieldType.DATE, FieldType.DATETIME, FieldType.DECIMAL, 
				   FieldType.DOUBLE, FieldType.INTEGER, FieldType.LONG),
	GREATER_EQUAL (FieldType.DATE, FieldType.DATETIME, FieldType.DECIMAL, 
				   FieldType.DOUBLE, FieldType.INTEGER, FieldType.LONG),
	LESS 		  (FieldType.DATE, FieldType.DATETIME, FieldType.DECIMAL, 
				   FieldType.DOUBLE, FieldType.INTEGER, FieldType.LONG),
	LESS_EQUAL 	  (FieldType.DATE, FieldType.DATETIME, FieldType.DECIMAL, 
				   FieldType.DOUBLE, FieldType.INTEGER, FieldType.LONG),
	LIKE		  (FieldType.AUTONUM, FieldType.TEXT, FieldType.TEXTLONG),
	NOT_LIKE	  (FieldType.AUTONUM, FieldType.TEXT, FieldType.TEXTLONG);
	
	private final FieldType[] fieldTypes;
	
	private CriterionOperator(FieldType ...fieldTypes) {
		this.fieldTypes = fieldTypes;
	}

	public static CriterionOperator[] getOperators(FieldType fieldType) {
		final List<CriterionOperator> operators = new ArrayList<>();
		for (CriterionOperator operator : values()) {
			if (Arrays.binarySearch(operator.fieldTypes, fieldType) >= 0) {  
				operators.add(operator);
			}
		}
		return operators.toArray(new CriterionOperator[operators.size()]);
	}
	
}
