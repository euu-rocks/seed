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
import java.util.List;

import org.junit.jupiter.api.Test;

import org.seed.core.util.NameUtils;

class NameUtilsTest {
	
	@Test
	void testIsKeyword() {
		assertFalse(NameUtils.isKeyword(null));
		assertFalse(NameUtils.isKeyword(""));
		assertFalse(NameUtils.isKeyword("test"));
		
		assertTrue(NameUtils.isKeyword("class"));
		assertTrue(NameUtils.isKeyword("PUBLIC"));
	}
	
	@Test
	void testIsSqlKeyword() {
		assertFalse(NameUtils.isSqlKeyword(null));
		assertFalse(NameUtils.isSqlKeyword(""));
		assertFalse(NameUtils.isSqlKeyword("test"));
		
		assertTrue(NameUtils.isSqlKeyword("table"));
		assertTrue(NameUtils.isSqlKeyword("FROM"));
	}
	
	@Test
	void testIsJavaLangClassName() {
		assertFalse(NameUtils.isJavaLangClassName(null));
		assertFalse(NameUtils.isJavaLangClassName(""));
		assertFalse(NameUtils.isJavaLangClassName("Test"));
		assertFalse(NameUtils.isJavaLangClassName("List"));
		
		assertTrue(NameUtils.isJavaLangClassName("String"));
		assertTrue(NameUtils.isJavaLangClassName("Exception"));
		assertTrue(NameUtils.isJavaLangClassName("WeakPairMap"));
	}
	
	@Test
	void testIsIllegalEntityName() {
		assertFalse(NameUtils.isIllegalEntityName(null));
		assertFalse(NameUtils.isIllegalEntityName(""));
		assertFalse(NameUtils.isIllegalEntityName("test"));
		
		assertTrue(NameUtils.isIllegalEntityName("String"));
		assertTrue(NameUtils.isIllegalEntityName("Exception"));
		assertTrue(NameUtils.isIllegalEntityName("AbstractValueObject"));
		assertTrue(NameUtils.isIllegalEntityName("List"));
		assertTrue(NameUtils.isIllegalEntityName("id"));
		assertTrue(NameUtils.isIllegalEntityName("UID"));
	}
	
	@Test
	void testIsIllegalFieldName() {
		assertFalse(NameUtils.isIllegalFieldName(null));
		assertFalse(NameUtils.isIllegalFieldName(""));
		assertFalse(NameUtils.isIllegalFieldName("test"));
		assertFalse(NameUtils.isIllegalFieldName("column"));
		
		assertTrue(NameUtils.isIllegalFieldName("id"));
		assertTrue(NameUtils.isIllegalFieldName("UID"));
		assertTrue(NameUtils.isIllegalFieldName("class"));
	}
	
	@Test
	void testIsIllegalColumnName() {
		assertFalse(NameUtils.isIllegalColumnName(null));
		assertFalse(NameUtils.isIllegalColumnName(""));
		assertFalse(NameUtils.isIllegalColumnName("test"));
		assertFalse(NameUtils.isIllegalColumnName("class"));
		
		assertTrue(NameUtils.isIllegalColumnName("id"));
		assertTrue(NameUtils.isIllegalColumnName("UID"));
		assertTrue(NameUtils.isIllegalColumnName("column"));
	}
	
	@Test
	void testBooleanValue() {
		assertFalse(NameUtils.booleanValue(null));
		assertFalse(NameUtils.booleanValue(""));
		assertFalse(NameUtils.booleanValue("0"));
		assertFalse(NameUtils.booleanValue("n"));
		assertFalse(NameUtils.booleanValue("NO"));
		assertFalse(NameUtils.booleanValue("other"));
		
		assertTrue(NameUtils.booleanValue("1"));
		assertTrue(NameUtils.booleanValue("TRUE"));
		assertTrue(NameUtils.booleanValue("ja"));
		assertTrue(NameUtils.booleanValue("yes"));
		assertTrue(NameUtils.booleanValue("j"));
		assertTrue(NameUtils.booleanValue("ON"));
	}
	
	@Test
	void testGetInternalName() {
		assertNull(NameUtils.getInternalName(null));
		assertEquals("", NameUtils.getInternalName(""));
		assertEquals("test", NameUtils.getInternalName("test"));
		assertEquals("Test", NameUtils.getInternalName("Test"));
		assertEquals("spa_ce", NameUtils.getInternalName("spa ce"));
		assertEquals("test", NameUtils.getInternalName("^t$e:s@t"));
		assertEquals("abcdefghijklmnopqrstuvwxyz", NameUtils.getInternalName("abcdefghijklmnopqrstuvwxyz"));
		assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", NameUtils.getInternalName("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
		assertEquals("aeoeueAeOeUess", NameUtils.getInternalName("äöüÄÖÜß"));
		assertEquals("0_1_2_3_4_5_6_7_8_9", NameUtils.getInternalName("!'0 §1.2³_3 *4;-5/-6 >|7_~8.&9'"));
	}
	
	@Test
	void testSplitAndTrim() {
		assertNotNull(NameUtils.splitAndTrim(null, null));
		assertSame(0, NameUtils.splitAndTrim(null, null).length);
		
		assertSame(1, NameUtils.splitAndTrim("test", null).length);
		assertEquals("test", NameUtils.splitAndTrim("test", null)[0]);
		
		assertSame(1, NameUtils.splitAndTrim(" te, st ", ";").length);
		assertSame(2, NameUtils.splitAndTrim(" te, st ", ",").length);
		assertEquals("te",  NameUtils.splitAndTrim(" te, s t ", ",")[0]);
		assertEquals("s t", NameUtils.splitAndTrim(" te, s t ", ",")[1]);
	}
	
	@Test
	void testStartsWithNumber() {
		assertFalse(NameUtils.startsWithNumber(null));
		assertFalse(NameUtils.startsWithNumber(""));
		assertTrue(NameUtils.startsWithNumber("0"));
		assertTrue(NameUtils.startsWithNumber("1abc"));
		assertFalse(NameUtils.startsWithNumber("a"));
		assertFalse(NameUtils.startsWithNumber("abc"));
	}
	
	@Test
	void testContainsAlphabet() {
		assertFalse(NameUtils.containsAlphabet(null));
		assertFalse(NameUtils.containsAlphabet(""));
		assertFalse(NameUtils.containsAlphabet("_"));
		assertFalse(NameUtils.containsAlphabet("1_2"));
		assertTrue(NameUtils.containsAlphabet("a"));
		assertTrue(NameUtils.containsAlphabet("ABC"));
		assertTrue(NameUtils.containsAlphabet("_a_"));
	}
	
	@Test
	void testGetRandomName() {
		assertNotNull(NameUtils.getRandomName());
		assertFalse(NameUtils.getRandomName().isEmpty());
		
		final int numRuns = 100;
		final List<String> names = new ArrayList<>(numRuns);
		for (int i = 0; i < numRuns; i++) {
			final String randomName = NameUtils.getRandomName();
			assertFalse(names.contains(randomName));
			names.add(randomName);
		}
		
	}
	
}
