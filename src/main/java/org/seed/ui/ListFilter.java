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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.seed.C;
import org.seed.core.data.SystemObject;
import org.seed.core.util.Assert;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class ListFilter<T extends SystemObject> {
	
	private final ListFilterListener listener;
	
	private Function<T, Object> valueFunction;
	
	private Set<String> values;
	
	private String value;
	
	private boolean booleanFilter;
	
	private boolean booleanValue; 
	
	ListFilter(ListFilterListener listener) {
		Assert.notNull(listener, "listener");
		
		this.listener = listener;
	}
	
	public Function<T, Object> getValueFunction() {
		return valueFunction;
	}
	
	public void setValueFunction(Function<T, Object> valueFunction) {
		Assert.notNull(valueFunction, "valueFunction");
		
		this.valueFunction = valueFunction;
	}

	public boolean isEmpty() {
		return !StringUtils.hasText(value) && !booleanValue;
	}
	
	public boolean hasValues() {
		return !ObjectUtils.isEmpty(values);
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
		listener.filterChanged(null);
	}
	
	public boolean isBooleanFilter() {
		return booleanFilter;
	}

	public void setBooleanFilter(boolean booleanFilter) {
		this.booleanFilter = booleanFilter;
	}

	public boolean isBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
		listener.filterChanged(null);
	}

	public void clear() {
		if (values != null) {
			values.clear();
		}
		reset();
	}
	
	public void reset() {
		value = null;
	}
	
	public Set<String> getValues() {
		if (values == null) {
			values = new HashSet<>();
		}
		return values;
	}
	
	public void addValue(String value) {
		Assert.notNull(value, C.VALUE);
		
		getValues().add(value);
	}
	
}
