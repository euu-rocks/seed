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

import static org.seed.core.util.CollectionUtils.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.seed.C;
import org.seed.core.data.SystemObject;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;

public class ListFilterGroup implements ListFilterListener {
	
	private final Map<String, ListFilter<?>> filterMap = new ConcurrentHashMap<>();
	
	private final ListFilterListener listener;
	
	private final String[] notifyChange;
	
	private boolean visible;
	
	public ListFilterGroup(ListFilterListener listener, String ...notifyChange) {
		Assert.notNull(listener, "listener");
		Assert.notNull(notifyChange, "notifyChange");
		
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
	
	@SuppressWarnings("unchecked")
	public <T extends SystemObject> ListFilter<T> getFilter(String name) {
		Assert.notNull(name, C.NAME);
		
		filterMap.computeIfAbsent(name, f -> new ListFilter<T>(this));
		return (ListFilter<T>) filterMap.get(name);
	}
	
	@Override
	public void filterChanged(ListFilterGroup filterGroup) {
		listener.filterChanged(this);
	}
	
	public <T extends SystemObject> List<T> filter(List<T> list) {
		Assert.notNull(list, C.LIST);
		
		return subList(list, this::acceptObject);
	}
	
	private <T extends SystemObject> boolean acceptObject(T object) {
		Assert.notNull(object, C.OBJECT);
		
		for (String filterName : filterMap.keySet()) {
			final ListFilter<T> filter = getFilter(filterName);
			if (filter.isEmpty()) {
				continue;
			}
			if (filter.isBooleanFilter()) {
				return acceptBooleanFilter(object, filter, filterName);
			}
			
			final Object value = filter.getValueFunction() != null 
					? filter.getValueFunction().apply(object)
					: BeanUtils.callGetter(object, filterName);
			if (value == null) {
				return false;
			}
			// bei Mehrfachauswahl muß der Wert genau stimmen
			if (filter.hasValues()) {
				if (!value.toString().equals(filter.getValue())) {
					return false;
				}
			}
			// sonst reicht es, wenn der Wert mit dem filter value beginnt
			else if (!value.toString().toLowerCase().startsWith(filter.getValue().toLowerCase())) {
				return false;
			}
		}
		return true;
	}
	
	private <T extends SystemObject> boolean acceptBooleanFilter(T object, ListFilter<T> filter, String filterName) {
		if (filter.getValueFunction() != null) {
			final Object result = filter.getValueFunction().apply(object);
			return result instanceof Boolean && ((Boolean) result).booleanValue();
		}
		else {
			final Boolean bool = BeanUtils.callIs(object, filterName);
			return bool != null && bool.booleanValue();
		}
	}
	
}
