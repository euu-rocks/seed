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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seed.core.data.SystemObject;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class ListFilterGroup implements ListFilterListener {
	
	private Map<String, ListFilter> filterMap;
	
	private final ListFilterListener listener;
	
	private final String[] notifyChange;
	
	private boolean visible;
	
	public ListFilterGroup(ListFilterListener listener, String ...notifyChange) {
		Assert.notNull(listener, "listener is null");
		Assert.notNull(notifyChange, "notifyChange is null");
		
		this.listener = listener;
		this.notifyChange = notifyChange;
	}

	public String[] getNotifyChange() {
		return notifyChange;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public ListFilter getFilter(String name) {
		Assert.notNull(name, "name is null");
		
		if (filterMap == null) {
			filterMap = new HashMap<>();
		}
		ListFilter filter = filterMap.get(name);
		if (filter == null) {
			filter = new ListFilter(this);
			filterMap.put(name, filter);
		}
		return filter;
	}
	
	@Override
	public void filterChanged(ListFilterGroup filterGroup) {
		listener.filterChanged(this);
	}
	
	public <T extends SystemObject> List<T> filter(List<T> list) {
		Assert.notNull(list, "list is null");
		
		final List<T> result = new ArrayList<>();
		for (T object : list) {
			if (acceptObject(object)) {
				result.add(object);
			}
		}
		return result;
	}
	
	private <T extends SystemObject> boolean acceptObject(T object) {
		Assert.notNull(object, "object is null");
		
		boolean accept = true;
		final Class<?> objectClass = object.getClass();
		try {
			for (String filterName : filterMap.keySet()) {
				final ListFilter filter = getFilter(filterName);
				if (filter.isEmpty()) {
					continue;
				}
				
				Object value = null;
				// value lambda function
				if (filter.getValueFunction() != null) {
					value = filter.getValueFunction().apply(object);
					if (filter.isBooleanFilter()) {
						if (value == null) {
							value = false;
						}
						accept = (boolean) value;
						break;
					}
				}
				// boolean filter
				else if (filter.isBooleanFilter()) {
					if (filter.isBooleanValue()) {
						final Method getter = objectClass.getMethod("is" + StringUtils.capitalize(filterName));
						Assert.state(getter != null, "no getter available for " + filterName);
						accept = (boolean) getter.invoke(object);
						break;
					}
				}
				// simple value filter
				else {
					final Method getter = objectClass.getMethod("get" + StringUtils.capitalize(filterName));
					Assert.state(getter != null, "no getter available for " + filterName);
					value = getter.invoke(object);
				}
				
				if (value == null) {
					accept = false;
					break;
				}
				// bei Mehrfachauswahl muß der Wert genau stimmen
				if (filter.hasValues()) {
					if (!value.toString().equals(filter.getValue())) {
						accept = false;
						break;
					}
				}
				// sonst reicht es, wenn der Wert mit dem filter value beginnt
				else if (!value.toString().toLowerCase().startsWith(filter.getValue().toLowerCase())) {
					accept = false;
					break;
				}
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return accept;
	}
	
}
