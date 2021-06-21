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

import java.util.HashMap;

import org.seed.core.util.Assert;

public final class TabParameterMap extends HashMap<String, Object> {

	private static final long serialVersionUID = -3098990587395841188L;
	
	public static final String NAME = "name";
	
	public static final String VIEW = "view";
	
	public static final String ICON = "icon";
	
	public static final String PARAMETER = "parameter";
	
	public TabParameterMap(String name, String view, String icon, FormParameter parameter) {
		super(4, 1f);
		Assert.notNull(name, NAME);
		Assert.notNull(view, VIEW);
		
		put(NAME, name);
		put(VIEW, view);
		if (icon != null) {
			put(ICON, icon);
		}
		if (parameter != null) {
			put(PARAMETER, parameter);
		}
	}

}
