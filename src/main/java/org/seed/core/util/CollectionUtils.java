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
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.springframework.util.ObjectUtils;

public interface CollectionUtils {
	
	static <T> boolean anyMatch(@Nullable T[] array, Predicate<T> predicate) {
		return array != null && Arrays.stream(array).anyMatch(predicate);
	}
	
	static <T> boolean anyMatch(@Nullable Collection<T> collection, Predicate<T> predicate) {
		return collection != null && collection.stream().anyMatch(predicate);
	}
	
	static <T> boolean containsObject(@Nullable Collection<T> collection, T object) {
		return collection != null && object != null && collection.contains(object);
	}
	
	static <T,R> List<R> convertedList(@Nullable Collection<T> collection, Function<T,R> function) {
		return notEmpty(collection)
				? collection.stream().map(function).collect(Collectors.toList())
				: Collections.emptyList();
	}
	
	static <T,R> Set<R> convertedSet(@Nullable Collection<T> collection, Function<T,R> function) {
		return notEmpty(collection)
				? collection.stream().map(function).collect(Collectors.toSet())
				: Collections.emptySet();
	}
	
	static <T,K,V> Map<K,V> convertedMap(@Nullable Collection<T> collection,
										 Function<? super T,? extends K> keyFunction,
										 Function<? super T,? extends V> valueFunction) {
		return notEmpty(collection)
				? collection.stream().collect(Collectors.toMap(keyFunction, valueFunction))
				: Collections.emptyMap();
	}
	
	static <T,R> List<R> filterAndConvert(@Nullable Collection<T> collection, 
										  Predicate<T> predicate, 
										  Function<T,R> function) {
		return notEmpty(collection)
				? filterAndConvert(collection.stream(), predicate, function)
				: Collections.emptyList();
	}
	
	static <T,R> List<R> filterAndConvert(@Nullable T[] array, 
			 							  Predicate<T> predicate, 
			 							  Function<T,R> function) {
		return array != null
				? filterAndConvert(Arrays.stream(array), predicate, function)
				: Collections.emptyList();
	}
	
	static <T> long filterAndCount(@Nullable Collection<T> collection, Predicate<T> predicate) {
		return notEmpty(collection)
				? collection.stream().filter(predicate).count()
				: 0;
	}
	
	static <T> void filterAndForEach(@Nullable Collection<T> collection, 
			  						 Predicate<T> predicate,
			  						 Consumer<T> action) {
		if (notEmpty(collection)) {
			filterAndForEach(collection.stream(), predicate, action);
		}
	}
	
	static <T> void filterAndForEach(@Nullable T[] array, 
									 Predicate<T> predicate,
									 Consumer<T> action) {
		if (array != null) {
			filterAndForEach(Arrays.stream(array), predicate, action);
		}
	}
	
	static <T> T firstMatch(@Nullable Collection<T> collection, Predicate<T> predicate) {
		return notEmpty(collection) 
				? firstMatch(collection.stream(), predicate)
				: null;
	}
	
	static <T> T firstMatch(@Nullable T[] array, Predicate<T> predicate) {
		return array != null 
				? firstMatch(Arrays.stream(array), predicate)
				: null;
	}
	
	static <T> void forEach(@Nullable T[] array, Consumer<T> action) {
		if (!ObjectUtils.isEmpty(array)) {
			Arrays.stream(array).forEach(action);
		}
	}
	
	static <T,K,V> Collector<T,?, Map<K,V>> linkedMapCollector(Function<? super T,? extends K> keyFunction,
															   Function<? super T,? extends V> valueFunction) {
        return Collectors.toMap(keyFunction, valueFunction, 
        						(u, v) -> { throw new IllegalStateException("Duplicate map key: " + u); }, 
        						LinkedHashMap::new);
    }
	
	static <T> boolean noneMatch(@Nullable Collection<T> collection, Predicate<T> predicate) {
		return collection == null || collection.stream().noneMatch(predicate);
	}
	
	static <T> Predicate<T> not(Predicate<? super T> target) {
		return Predicate.not(target);
	}
	
	static boolean notEmpty(@Nullable Collection<?> collection) {
		return collection != null && collection.size() > 0;
	}
	
	static boolean notEmpty(@Nullable Map<?,?> map) {
		return map != null && map.size() > 0;
	}
	
	static <T> List<T> subList(@Nullable T[] array, Predicate<T> predicate) {
		return array != null
				? subList(Arrays.stream(array), predicate)
				: Collections.emptyList();
	}
	
	static <T> List<T> subList(@Nullable Collection<T> collection, Predicate<T> predicate) {
		return notEmpty(collection)
				? subList(collection.stream(), predicate)
				: Collections.emptyList();
	}
	
	static <V> Map<String, V> toCaseInsensitiveKeyMap(@Nullable Map<String, V> map) {
		final var caseInsensitiveKeyMap = new TreeMap<String, V>(String.CASE_INSENSITIVE_ORDER);
		if (notEmpty(map)) {
			caseInsensitiveKeyMap.putAll(map);
		}
		return caseInsensitiveKeyMap;
	}
	
	static <T> List<T> valueList(@Nullable Map<?,T> map) {
		return notEmpty(map)
				? map.values().stream().collect(Collectors.toList())
				: Collections.emptyList();
	}
	
	private static <T,R> List<R> filterAndConvert(Stream<T> stream, Predicate<T> predicate, Function<T,R> function) {
		return stream.filter(predicate).map(function).collect(Collectors.toList());
	}
	
	private static <T> void filterAndForEach(Stream<T> stream, Predicate<T> predicate, Consumer<T> action) {
		stream.filter(predicate).forEach(action);
	}
	
	private static <T> T firstMatch(Stream<T> stream, Predicate<T> predicate) {
		return stream.filter(predicate).findFirst().orElse(null);
	}
	
	private static <T> List<T> subList(Stream<T> stream, Predicate<T> predicate) {
		return stream.filter(predicate).collect(Collectors.toList());
	}
	
}
