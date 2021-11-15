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

import javax.annotation.Nullable;

import org.springframework.util.StringUtils;

public abstract class Assert {
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 8838296458819421316L;
	
	private Assert() {}
	
	public static void notNull(@Nullable Object object, @Nullable String name) {
		if (object == null) {
			throw new IllegalArgumentException(name != null 
												? name + " is null" 
												: "Object is null");
		}
	}
	
	public static void hasText(@Nullable String text, @Nullable String name) {
		if (!StringUtils.hasText(text)) {
			throw new IllegalArgumentException(name != null 
												? name + " has no content" 
												: "Text has no content");
		}
	}
	
	public static void greaterThanZero(int number, @Nullable String name) {
		if (number <= 0) {
			stateIllegal("Illegal " + (name != null ? name : "number") + ' ' + number);
		}
	}
	
	public static void state(boolean expression, @Nullable String message) {
		if (!expression) {
			stateIllegal(message);
		}
	}
	
	public static void stateAvailable(@Nullable Object object, @Nullable String name) {
		if (object == null) {
			stateIllegal(name != null 
							? name + " not available" 
							: "Object not available");
		}
	}
	
	public static void stateIllegal(@Nullable String message) {
		throw new IllegalStateException(message != null 
											? message 
											: "An invalid state has occurred");
	}
	
}
