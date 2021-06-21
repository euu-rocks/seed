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
package org.seed.core.util;

import java.util.Arrays;

import org.springframework.util.ObjectUtils;

public class MultiKey {
	
	private final Object[] objects;

	private MultiKey(Object[] objects) {
		this.objects = objects;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(objects);
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof MultiKey && 
			   Arrays.equals(objects, ((MultiKey) object).objects);
	}
	
	public static MultiKey valueOf(Object ...objects) {
		Assert.state(!ObjectUtils.isEmpty(objects), "no objects");
		
		return new MultiKey(objects);
	}
	
}
