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

import org.springframework.util.Assert;

public class TabParameterMap extends HashMap<String, Object> {

	private static final long serialVersionUID = -3098990587395841188L;
	
	public final static String NAME = "name";
	
	public final static String VIEW = "view";
	
	public final static String ICON = "icon";
	
	public final static String PARAMETER = "parameter";
	
	public TabParameterMap(String name, String view, String icon, FormParameter parameter) {
		super(4, 1f);
		Assert.notNull(name, "name is null");
		Assert.notNull(view, "view is null");
		
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
