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

import org.seed.C;
import org.seed.core.util.Assert;

public final class ViewModelProperty {
	
	private static final String PREFIX = "custom.";
	
	private final Tab tab;
	
	private final String propertyName;

	public ViewModelProperty(Tab tab, String name) {
		Assert.notNull(tab, C.TAB);
		Assert.notNull(name, C.NAME);
		
		this.tab = tab;
		propertyName = PREFIX.concat(name);
	}
	
	public Object getValue() {
		return tab.getProperty(propertyName);
	}
	
	public void setValue(Object value) {
		if (value != null) {
			tab.setProperty(propertyName, value);
		}
		else {
			tab.removeProperty(propertyName);
		}
	}
	
}
