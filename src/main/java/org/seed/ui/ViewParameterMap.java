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

public class ViewParameterMap extends HashMap<String, Object> {
	
	private static final long serialVersionUID = -901328042280559618L;

	public final static String VIEW = "view";
	
	public final static String PARAM = "param";
	
	public ViewParameterMap(String view, Object param) {
		super(2, 1f);
		Assert.notNull(view, "view is null");
		
		put(VIEW, view);
		if (param != null) {
			put(PARAM, param);
		}
	}
	
}
