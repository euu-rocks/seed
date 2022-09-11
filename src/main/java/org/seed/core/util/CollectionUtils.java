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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public abstract class CollectionUtils {
	
	private CollectionUtils() {}
	
	public static <T> boolean anyMatch(@Nullable T[] array, Predicate<T> predicate) {
		return array != null && Arrays.stream(array).anyMatch(predicate);
	}
	
	public static <T> boolean anyMatch(@Nullable Collection<T> collection, Predicate<T> predicate) {
		return collection != null && collection.stream().anyMatch(predicate);
	}
	
	public static <T> boolean containsObject(@Nullable Collection<T> collection, T object) {
		return collection != null && collection.contains(object);
	}
	
	public static <T,R> List<R> filterAndConvert(@Nullable Collection<T> collection, 
												 Predicate<T> predicate, 
												 Function<T,R> function) {
		return collection != null
				? filterAndConvert(collection.stream(), predicate, function)
				: Collections.emptyList();
	}
	
	public static <T,R> List<R> filterAndConvert(@Nullable T[] array, 
			 									 Predicate<T> predicate, 
			 									 Function<T,R> function) {
		return array != null
				? filterAndConvert(Arrays.stream(array), predicate, function)
				: Collections.emptyList();
	}
	
	public static <T> void filterAndForEach(@Nullable Collection<T> collection, 
			  								Predicate<T> predicate,
			  								Consumer<T> action) {
		if (collection != null) {
			collection.stream().filter(predicate).forEach(action);
		}
	}
	
	public static <T> T firstMatch(@Nullable Collection<T> collection, Predicate<T> predicate) {
		return collection != null 
				? collection.stream().filter(predicate).findFirst().orElse(null) 
				: null;
	}
	
	public static <T,K,V> Collector<T,?, Map<K,V>> linkedMapCollector(Function<? super T,? extends K> keyFunction,
																	  Function<? super T,? extends V> valueFunction) {
        return Collectors.toMap(keyFunction, valueFunction, (u, v) -> u, LinkedHashMap::new);
    }
	
	public static <T> boolean noneMatch(@Nullable Collection<T> collection, Predicate<T> predicate) {
		return collection == null || collection.stream().noneMatch(predicate);
	}
	
	public static boolean notEmpty(@Nullable Collection<?> collection) {
		return collection != null && !collection.isEmpty();
	}
	
	public static boolean notEmpty(@Nullable Map<?,?> map) {
		return map != null && !map.isEmpty();
	}
	
	public static <T> List<T> subList(@Nullable Collection<T> collection, Predicate<T> predicate) {
		return collection != null
				? collection.stream().filter(predicate).collect(Collectors.toList())
				: Collections.emptyList();
	}
	
	private static <T,R> List<R> filterAndConvert(Stream<T> stream, Predicate<T> predicate, Function<T,R> function) {
		return stream.filter(predicate).map(function).collect(Collectors.toList());
	}
	
}
