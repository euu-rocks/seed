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

import java.util.concurrent.atomic.AtomicLong;

public final class TinyId {
	
	private final AtomicLong id;
	
	private final String prefix;
	
	public TinyId(String prefix) {
		this(1L, prefix);
	}
	
	public TinyId(long value, String prefix) {
		id = new AtomicLong(value);
		this.prefix = prefix;
	}
	
	public String next() {
		final String strId = get(id.getAndIncrement());
		return prefix != null 
				? prefix + strId 
				: strId;
	}
	
	public static String get(long id) {
		return Long.toString(id, Character.MAX_RADIX);
	}
	
}
