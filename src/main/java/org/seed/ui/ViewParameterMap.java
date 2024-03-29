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

import javax.annotation.Nullable;

import org.seed.C;
import org.seed.core.util.Assert;

public final class ViewParameterMap extends HashMap<String, Object> {
	
	private static final long serialVersionUID = -901328042280559618L;

	public ViewParameterMap(String view, @Nullable Object parameter) {
		super(2, 1f);
		Assert.notNull(view, C.VIEW);
		
		put(C.VIEW, view);
		if (parameter != null) {
			put(C.PARAMETER, parameter);
		}
	}
	
}
