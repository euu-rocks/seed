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

import static org.seed.core.util.CollectionUtils.firstMatch;

import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public abstract class ObjectAccess {
	
	protected ObjectAccess() {}
	
	private static final String PRE_IS  = "is";
	private static final String PRE_GET = "get";
	private static final String PRE_SET = "set";
	
	public static Boolean callBooleanGetter(Object object, String propertyName) {
		return callMethod(object, PRE_IS.concat(StringUtils.capitalize(propertyName)));
	}
	
	public static <T> T callGetter(Object object, String propertyName) {
		return callMethod(object, PRE_GET.concat(StringUtils.capitalize(propertyName)));
	}
	
	public static void callSetter(Object object, String propertyName, Object ...parameters) {
		callMethod(object, PRE_SET.concat(StringUtils.capitalize(propertyName)), parameters);
	}
	
	@SuppressWarnings("unchecked")
	protected static <T> T callMethod(Object object, String methodName, Object ...parameters) {
		final Method method = firstMatch(object.getClass().getMethods(), 
										 methd -> methd.getName().equals(methodName));
		if (method != null) {
			return (T) ReflectionUtils.invokeMethod(method, object, parameters);
		}
        throw new IllegalStateException("method not found: " + object.getClass().getName() + '.' + methodName);
	}
	
}