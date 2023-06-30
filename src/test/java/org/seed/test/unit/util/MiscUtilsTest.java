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

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;

import org.seed.core.util.MiscUtils;

class MiscUtilsTest {
	
	@Test
	void testToArray() {
		assertNotNull(MiscUtils.toArray());
		assertEquals(0, MiscUtils.toArray().length);
		var testarray = MiscUtils.toArray(1, 2, 3);
		
		assertEquals(3, testarray.length);
		assertEquals(1, testarray[0]);
		assertEquals(2, testarray[1]);
		assertEquals(3, testarray[2]);
	}
	
	@Test
	void testToStringBytes() {
		assertNull(MiscUtils.toString(null));
		byte[] test = "test".getBytes();
		assertEquals("test", MiscUtils.toString(test));
	}
	
	@Test
	void testToStringCollection() {
		assertEquals("", MiscUtils.toString(null, null));
		assertEquals("test", MiscUtils.toString(Arrays.asList("t","e","s","t"), null));
		assertEquals("t.e.s.t", MiscUtils.toString(Arrays.asList("t","e","s","t"), "."));
		assertEquals("t, e, s, t", MiscUtils.toString(Arrays.asList("t","e","s","t"), ", "));
	}
	
	@Test
	void testFilterString() {
		assertNull(MiscUtils.filterString(null, null));
		assertNull(MiscUtils.filterString("test", null));
		assertEquals("test", MiscUtils.filterString("§t/e#s@t:", Character::isLetterOrDigit));
		assertEquals("test", MiscUtils.filterString("TtEeSsTt:", Character::isLowerCase));
	}
	
	@Test
	void testReplaceAllIgnoreCase() {
		assertNull(MiscUtils.replaceAllIgnoreCase(null, null, null));
		assertNull(MiscUtils.replaceAllIgnoreCase("test", null, null));
		assertEquals("#test#", MiscUtils.replaceAllIgnoreCase("#BLA#", "bla", "test"));
	}
	
	@Test
	void testFormatDuration() {
		assertEquals("0 ms", MiscUtils.formatDuration(1, 1));
		assertEquals("1 ms", MiscUtils.formatDuration(1, 2));
		assertEquals("4.20 sec", MiscUtils.formatDuration(1, 4201));
		assertEquals("01:00 min", MiscUtils.formatDuration(1, 60001));
		assertEquals("01:00:00", MiscUtils.formatDuration(1, 3600001));
		assertEquals("1:00:00:00", MiscUtils.formatDuration(1, 86400001));
	}
	
	@Test
	void testMaxDate() {
		final Date date1 = new Date();
		final Date date2 = new Date(date1.getTime() + 1);
		assertNull(MiscUtils.maxDate(null, null));
		assertSame(date1, MiscUtils.maxDate(date1, null));
		assertSame(date1, MiscUtils.maxDate(null, date1));
		assertSame(date1, MiscUtils.maxDate(date1, date1));
		assertSame(date2, MiscUtils.maxDate(date1, date2));
		assertSame(date2, MiscUtils.maxDate(date2, date1));
	}
	
	@Test
	void testAddLeadingChars() {
		assertNull(MiscUtils.addLeadingChars(null, (char)0, 0));
		assertEquals("test", MiscUtils.addLeadingChars("test", '#', 4));
		assertEquals("testi", MiscUtils.addLeadingChars("testi", '#', 4));
		assertEquals("#est", MiscUtils.addLeadingChars("est", '#', 4));
		assertEquals("###est", MiscUtils.addLeadingChars("est", '#', 6));
	}
	
	@Test
	void testRemoveHTMLTags() {
		assertNull(MiscUtils.removeHTMLTags(null));
		assertEquals("test", MiscUtils.removeHTMLTags("test"));
		assertEquals("test < 1", MiscUtils.removeHTMLTags("test < 1"));
		assertEquals("this is a test", MiscUtils.removeHTMLTags("<a>this</a> <b>is<b> a <c >test</c >"));
	}
	
}
