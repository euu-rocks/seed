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
package org.seed.test.unit.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.seed.core.data.dbobject.DBObjectUtils;

public class DBObjectUtilsTest {
	
	@Test
	void testContainsName() {
		assertFalse(DBObjectUtils.containsName(null, null));
		assertFalse(DBObjectUtils.containsName("test", null));
		assertFalse(DBObjectUtils.containsName(null, "test"));
		assertTrue(DBObjectUtils.containsName("Test", "test"));
		assertTrue(DBObjectUtils.containsName(" test", "test"));
		assertTrue(DBObjectUtils.containsName("test ", "test"));
		assertTrue(DBObjectUtils.containsName("\ttest ", "test"));
		assertTrue(DBObjectUtils.containsName(",test;", "test"));
		assertTrue(DBObjectUtils.containsName("=test()", "test"));
		assertFalse(DBObjectUtils.containsName("atest", "test"));
		assertFalse(DBObjectUtils.containsName(" testa", "test"));
		assertFalse(DBObjectUtils.containsName(" test.", "test"));
		assertFalse(DBObjectUtils.containsName(" test-", "test"));
		assertFalse(DBObjectUtils.containsName(" test_", "test"));
		assertFalse(DBObjectUtils.containsName(".test ", "test"));
		assertFalse(DBObjectUtils.containsName("-test ", "test"));
		assertFalse(DBObjectUtils.containsName("_test ", "test"));
		assertTrue(DBObjectUtils.containsName("create procedure testprocedure(", "Testprocedure"));
		assertFalse(DBObjectUtils.containsName("create procedure 'testprocedure'(", "Testprocedure"));
		assertFalse(DBObjectUtils.containsName("create procedure /*testprocedure*/(", "Testprocedure"));
	}
	
	@Test
	void testRemoveQuotedText() {
		assertNull(DBObjectUtils.removeQuotedText(null));
		assertEquals("this is a test", DBObjectUtils.removeQuotedText("this is a test"));
		assertEquals("this is a test", DBObjectUtils.removeQuotedText("this is a test'end'"));
		assertEquals("this is a test", DBObjectUtils.removeQuotedText("\"start\"this is a test"));
		assertEquals("this is a test", DBObjectUtils.removeQuotedText("this is a 'single-quoted'test"));
		assertEquals("this is a test", DBObjectUtils.removeQuotedText("this is a\" double-quoted\" test"));
		assertEquals("this is a test", DBObjectUtils.removeQuotedText("this is a\" 'double'-'quoted'\" test"));
	}
	
	@Test
	void testRemoveComments() {
		assertNull(DBObjectUtils.removeSqlComments(null));
		assertEquals("this is a test", DBObjectUtils.removeSqlComments("this is a test"));
		assertEquals("this is a test", DBObjectUtils.removeSqlComments("this is a test--comment"));
		assertEquals("this is a test", DBObjectUtils.removeSqlComments("this is/* comment */ a test"));
		assertEquals("this is a test", DBObjectUtils.removeSqlComments("this is a /*\r\n"
																	 + "multiline comment */test--comment"));
	}
	
}
