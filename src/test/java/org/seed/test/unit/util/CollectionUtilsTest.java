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
package org.seed.test.unit.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.seed.core.util.CollectionUtils;

class CollectionUtilsTest {
	
	@Test
	void testAnyMatch() {
		final List<String> list = new ArrayList<>();
		assertFalse(CollectionUtils.anyMatch((List<String>) null, str -> str.length() > 0));
		assertFalse(CollectionUtils.anyMatch(list, str -> str.length() > 0));
		
		list.add("alpha");
		list.add("beta");
		list.add("gamma");
		assertTrue(CollectionUtils.anyMatch(list, str -> str.length() > 0));
		assertTrue(CollectionUtils.anyMatch(list, str -> str.startsWith("be")));
		assertFalse(CollectionUtils.anyMatch(list, str -> str.endsWith("x")));
		
		final String[] array = list.toArray(new String[list.size()]);
		assertTrue(CollectionUtils.anyMatch(array, str -> str.length() > 0));
		assertTrue(CollectionUtils.anyMatch(array, str -> str.startsWith("be")));
		assertFalse(CollectionUtils.anyMatch(array, str -> str.endsWith("x")));
	}
	
	@Test
	void testContainsObject() {
		final List<String> list = new ArrayList<>();
		assertFalse(CollectionUtils.containsObject((List<String>) null, null));
		assertFalse(CollectionUtils.containsObject((List<String>) null, "test"));
		assertFalse(CollectionUtils.containsObject(list, "test"));
		
		list.add("alpha");
		list.add("beta");
		list.add("gamma");
		
		assertTrue(CollectionUtils.containsObject(list, "gamma"));
		assertFalse(CollectionUtils.containsObject(list, "test"));
	}
	
	@Test
	void testConvertedList() {
		final List<String> list = new ArrayList<>();
		assertTrue(CollectionUtils.convertedList(null, null).isEmpty());
		assertTrue(CollectionUtils.convertedList(list, Wrapper::new).isEmpty());
		
		list.add("test");
		assertFalse(CollectionUtils.convertedList(list, Wrapper::new).isEmpty());
		assertTrue(CollectionUtils.convertedList(list, Wrapper::new).get(0) instanceof Wrapper);
		assertEquals(CollectionUtils.convertedList(list, Wrapper::new).get(0).string, "test");
	}
	
	@Test
	void testConvertedSet() {
		final List<String> list = new ArrayList<>();
		assertTrue(CollectionUtils.convertedList(null, null).isEmpty());
		assertTrue(CollectionUtils.convertedList(list, Wrapper::new).isEmpty());
		
		list.add("test");
		assertFalse(CollectionUtils.convertedSet(list, Wrapper::new).isEmpty());
		assertTrue(CollectionUtils.convertedSet(list, Wrapper::new) instanceof Set);
		assertTrue(CollectionUtils.convertedSet(list, Wrapper::new).iterator().next() instanceof Wrapper);
		assertEquals(CollectionUtils.convertedSet(list, Wrapper::new).iterator().next().string, "test");
	}
	
	@Test
	void testConvertedMap() {
		final List<String> list = new ArrayList<>();
		assertTrue(CollectionUtils.convertedMap(null, null, null).isEmpty());
		assertTrue(CollectionUtils.convertedMap(list, str -> str + "key", str -> str + "value").isEmpty());
	
		list.add("alpha");
		list.add("beta");
		list.add("gamma");
		assertFalse(CollectionUtils.convertedMap(list, str -> str + "key", str -> str + "value").isEmpty());
		assertSame(3, CollectionUtils.convertedMap(list, str -> str + "key", str -> str + "value").size());
		assertTrue(CollectionUtils.convertedMap(list, str -> str + "key", str -> str + "value").containsKey("betakey"));
		assertEquals("betavalue", CollectionUtils.convertedMap(list, str -> str + "key", str -> str + "value").get("betakey"));
	}
	
	@Test
	void testFilterAndConvert() {
		final List<String> list = new ArrayList<>();
		assertTrue(CollectionUtils.filterAndConvert((List<String>) null, str -> str.length() > 0, Wrapper::new).isEmpty());
		assertTrue(CollectionUtils.filterAndConvert(list, str -> str.length() > 0, Wrapper::new).isEmpty());
		
		list.add("alpha");
		list.add("beta");
		list.add("gamma");
		
		assertSame(3, CollectionUtils.filterAndConvert(list, str -> str.length() > 0, Wrapper::new).size());
		assertSame(2, CollectionUtils.filterAndConvert(list, str -> str.length() > 4, Wrapper::new).size());
		assertTrue(CollectionUtils.filterAndConvert(list, str -> str.length() > 4, Wrapper::new).get(0) instanceof Wrapper);
		assertEquals("alpha", CollectionUtils.filterAndConvert(list, str -> str.length() > 4, Wrapper::new).get(0).getString());
		
		final String[] array = list.toArray(new String[list.size()]);
		assertSame(3, CollectionUtils.filterAndConvert(array, str -> str.length() > 0, Wrapper::new).size());
		assertSame(2, CollectionUtils.filterAndConvert(array, str -> str.length() > 4, Wrapper::new).size());
		assertTrue(CollectionUtils.filterAndConvert(array, str -> str.length() > 4, Wrapper::new).get(0) instanceof Wrapper);
		assertEquals("alpha", CollectionUtils.filterAndConvert(array, str -> str.length() > 4, Wrapper::new).get(0).getString());
	}
	
	@Test
	void testForEach() {
		final Integer ints[] = new Integer[] {1,2,3};
		final List<Integer> result = new ArrayList<>();
		
		CollectionUtils.forEach(null, i -> result.add((Integer) i));
		assertTrue(result.isEmpty());
		CollectionUtils.forEach(ints, i -> result.add(i));
		assertSame(3, result.size());
	}
	
	@Test
	void testFilterAndForEach() {
		final List<String> list = new ArrayList<>();
		final List<String> result = new ArrayList<>();
		CollectionUtils.filterAndForEach((List<String>) null, str -> str.length() > 0, str -> result.add(str));
		assertTrue(result.isEmpty());
		CollectionUtils.filterAndForEach(list, str -> str.length() > 0, str -> result.add(str));
		assertTrue(result.isEmpty());
		
		list.add("alpha");
		list.add("beta");
		list.add("gamma");
		
		CollectionUtils.filterAndForEach(list, str -> str.length() > 0, str -> result.add(str));
		assertSame(3, result.size());
		result.clear();
		
		CollectionUtils.filterAndForEach(list, str -> str.length() > 4, str -> result.add(str));
		assertSame(2, result.size());
		result.clear();
		
		CollectionUtils.filterAndForEach(list, str -> str.endsWith("nope"), str -> result.add(str));
		assertTrue(result.isEmpty());
	}
	
	@Test
	void testFirstMatch() {
		final List<String> list = new ArrayList<>();
		assertNull(CollectionUtils.firstMatch((List<String>) null, str -> str.length() > 0));
		assertNull(CollectionUtils.firstMatch(list, str -> str.length() > 0));
		
		list.add("alpha");
		list.add("beta");
		list.add("gamma");
		
		assertEquals("alpha", CollectionUtils.firstMatch(list, str -> str.length() > 0));
		assertEquals("beta", CollectionUtils.firstMatch(list, str -> str.length() < 5));
		assertEquals("gamma", CollectionUtils.firstMatch(list, str -> str.startsWith("g")));
	}
	
	@Test
	void testLinkedMapCollector() {
		final List<String> list = new ArrayList<>();
		list.add("alpha");
		list.add("beta");
		list.add("gamma");
		
		final Map<String, String> map = list.stream().collect(
				CollectionUtils.linkedMapCollector(s1 -> s1 + "key", s2 -> s2 + "value"));
		assertFalse(map.isEmpty());
		assertTrue(map instanceof LinkedHashMap);
	}
	
	@Test
	void testNoneMatch() {
		final List<String> list = new ArrayList<>();
		assertTrue(CollectionUtils.noneMatch((List<String>) null, str -> str.length() > 0));
		assertTrue(CollectionUtils.noneMatch(list, str -> str.length() > 0));
		
		list.add("alpha");
		list.add("beta");
		list.add("gamma");
		
		assertFalse(CollectionUtils.noneMatch(list, str -> str.length() > 0));
		assertFalse(CollectionUtils.noneMatch(list, str -> str.startsWith("be")));
		assertTrue(CollectionUtils.noneMatch(list, str -> str.endsWith("x")));
	}
	
	@Test
	void testNotEmpty() {
		final List<String> list = new ArrayList<>();
		final Map<String, String> map = new HashMap<>();
		assertFalse(CollectionUtils.notEmpty((List<String>) null));
		assertFalse(CollectionUtils.notEmpty(list));
		
		list.add("test");
		assertTrue(CollectionUtils.notEmpty(list));
		
		assertFalse(CollectionUtils.notEmpty((Map<String,String>) null));
		assertFalse(CollectionUtils.notEmpty(map));
		
		map.put("test", "yes");
		assertTrue(CollectionUtils.notEmpty(map));
	}
	
	@Test
	void testSubList() {
		final List<String> list = new ArrayList<>();
		assertTrue(CollectionUtils.subList((List<String>) null, str -> str.length() > 0).isEmpty());
		assertTrue(CollectionUtils.subList(list, str -> str.length() > 0).isEmpty());
		
		list.add("alpha");
		list.add("beta");
		list.add("gamma");
		
		assertSame(3, CollectionUtils.subList(list, str -> str.length() > 0).size());
		assertSame(2, CollectionUtils.subList(list, str -> str.length() > 4).size());
		assertSame(1, CollectionUtils.subList(list, str -> str.startsWith("b")).size());
		
		final String[] array = list.toArray(new String[list.size()]);
		assertSame(3, CollectionUtils.subList(array, str -> str.length() > 0).size());
		assertSame(2, CollectionUtils.subList(array, str -> str.length() > 4).size());
		assertSame(1, CollectionUtils.subList(array, str -> str.startsWith("b")).size());
	}
	
	@Test
	void testValueList() {
		final Map<String,String> map = new HashMap<>();
		assertNotNull(CollectionUtils.valueList(null));
		assertNotNull(CollectionUtils.valueList(map));
		assertTrue(CollectionUtils.valueList(map).isEmpty());
		
		map.put("1", "alpha");
		map.put("2", "beta");
		map.put("3", "gamma");
		
		assertFalse(CollectionUtils.valueList(map).isEmpty());
		assertTrue(CollectionUtils.valueList(map) instanceof List);
		assertSame(3, CollectionUtils.valueList(map).size());
	}
	
	class Wrapper {
		String string;
		
		Wrapper(String string) {
			this.string = string;
		}
		
		public String getString() {
			return string;
		}
	}
	
}
