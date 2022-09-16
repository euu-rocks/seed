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

import org.junit.jupiter.api.Test;

import org.seed.core.util.ObjectAccess;

class ObjectAccessTest {
	
	@Test
	void testCallBooleanGetter() {
		final TestObject object = new TestObject();
		assertNull(ObjectAccess.callBooleanGetter(object, "bool"));
		
		object.setBool(Boolean.TRUE);
		assertEquals(Boolean.TRUE, ObjectAccess.callBooleanGetter(object, "bool"));
		
		object.setBool(Boolean.FALSE);
		assertEquals(Boolean.FALSE, ObjectAccess.callBooleanGetter(object, "bool"));
	}
	
	@Test
	void testCallGetter() {
		final TestObject object = new TestObject();
		assertNull(ObjectAccess.callGetter(object, "str"));
		
		object.setStr("test");
		assertEquals("test", ObjectAccess.callGetter(object, "str"));
	}
	
	@Test
	void testCallSetter() {
		final TestObject object = new TestObject();
		ObjectAccess.callSetter(object, "str", "test");
		assertEquals("test", object.getStr());
		
		ObjectAccess.callSetter(object, "str", new Object[] {null});
		assertNull(object.getStr());
	}
	
	public static class TestObject {
		
		Boolean bool;
		
		String str;

		public Boolean isBool() {
			return bool;
		}

		public void setBool(Boolean bool) {
			this.bool = bool;
		}

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}
		
	}

}
