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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class CollectionUtils {
	
	private CollectionUtils() {}
	
	public static <T> boolean anyMatch(T[] array, Predicate<T> predicate) {
		return array != null && Arrays.stream(array).anyMatch(predicate);
	}
	
	public static <T> boolean anyMatch(Collection<T> collection, Predicate<T> predicate) {
		return collection != null && collection.stream().anyMatch(predicate);
	}
	
	public static <T,R> List<R> filterConvert(Collection<T> collection, 
											  Predicate<T> predicate, 
											  Function<T,R> function) {
		return collection != null
				? collection.stream().filter(predicate)
									 .map(function)
									 .collect(Collectors.toList())
				: Collections.emptyList();
	}
	
	public static <T> T firstMatch(Collection<T> collection, Predicate<T> predicate) {
		return collection != null 
				? collection.stream().filter(predicate).findFirst().orElse(null) 
				: null;
	}
	
	public static <T> boolean noneMatch(Collection<T> collection, Predicate<T> predicate) {
		return collection == null || collection.stream().noneMatch(predicate);
	}
	
	public static boolean notEmpty(Collection<?> collection) {
		return collection != null && !collection.isEmpty();
	}
	
	public static boolean notEmpty(Map<?,?> map) {
		return map != null && !map.isEmpty();
	}
	
	public static <T,K,V> Collector<T,?, Map<K,V>> linkedMapCollector(
			Function<? super T,? extends K> keyFunction,
	        Function<? super T,? extends V> valueFunction) {
        return Collectors.toMap(keyFunction, valueFunction, (u, v) -> u, LinkedHashMap::new);
    }
	
	public static <T> List<T> subList(Collection<T> collection, Predicate<T> predicate) {
		return collection != null
				? collection.stream().filter(predicate).collect(Collectors.toList())
				: Collections.emptyList();
	}
	
}
