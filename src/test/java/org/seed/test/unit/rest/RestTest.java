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
package org.seed.test.unit.rest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import org.seed.core.rest.Rest;
import org.seed.core.rest.RestFunction;
import org.seed.core.rest.RestMetadata;
import org.seed.core.rest.RestPermission;

class RestTest {
	
	@Test
	void testAddFunction() {
		final Rest rest = new RestMetadata();
		final RestFunction function = new RestFunction();
		assertFalse(rest.hasFunctions());
		
		rest.addFunction(function);
		
		assertSame(rest, function.getRest());
		assertTrue(rest.hasFunctions());
		assertSame(1, rest.getFunctions().size());
		assertSame(function, rest.getFunctions().get(0));
	}
	
	@Test
	void testGetFunctionByMapping() {
		final Rest rest = new RestMetadata();
		final RestFunction function = new RestFunction();
		function.setName("other");
		function.setMapping("/other");
		rest.addFunction(function);
		
		assertNull(rest.getFunctionByMapping("test"));
		
		function.setMapping("/test");
		
		assertSame(function, rest.getFunctionByMapping("test"));
		
		function.setMapping(null);
		function.setName("test");
		
		assertSame(function, rest.getFunctionByMapping("test"));
	}
	
	@Test
	void testGetFunctionByUid() {
		final Rest rest = new RestMetadata();
		final RestFunction function = new RestFunction();
		function.setUid("other");
		rest.addFunction(function);
		
		assertNull(rest.getFunctionByUid("test"));
		
		function.setUid("test");
		
		assertSame(function, rest.getFunctionByUid("test"));
	}
	
	@Test
	void testGetPermissionByUid() {
		final Rest rest = new RestMetadata();
		final RestPermission permission = new RestPermission();
		final List<RestPermission> permissions = new ArrayList<>();
		permission.setUid("other");
		permissions.add(permission);
		((RestMetadata) rest).setPermissions(permissions);
		
		assertNull(rest.getPermissionByUid("test"));
		
		permission.setUid("test");
		
		assertSame(permission, rest.getPermissionByUid("test"));
	}
	
	@Test
	void testIsEqual() {
		final Rest rest1 = new RestMetadata();
		final Rest rest2 = new RestMetadata();
		assertTrue(rest1.isEqual(rest2));
		
		rest1.setName("test");
		assertFalse(rest1.isEqual(rest2));
		
		rest2.setName("test");
		assertTrue(rest1.isEqual(rest2));
	}
	
	@Test
	void testIsEqualFunctions() {
		final Rest rest1 = new RestMetadata();
		final Rest rest2 = new RestMetadata();
		final RestFunction function1 = new RestFunction();
		final RestFunction function2 = new RestFunction();
		function1.setUid("test");
		function2.setUid("test");
		rest1.addFunction(function1);
		assertFalse(rest1.isEqual(rest2));
		
		rest2.addFunction(function2);
		assertTrue(rest1.isEqual(rest2));
		
		function2.setUid("other");
		assertFalse(rest1.isEqual(rest2));
	}
	
	@Test
	void testIsEqualPermissions() {
		final Rest rest1 = new RestMetadata();
		final Rest rest2 = new RestMetadata();
		final RestPermission permission1 = new RestPermission();
		final RestPermission permission2 = new RestPermission();
		final List<RestPermission> permissions1 = new ArrayList<>();
		final List<RestPermission> permissions2 = new ArrayList<>();
		permission1.setUid("test");
		permission2.setUid("test");
		permissions1.add(permission1);
		permissions2.add(permission2);
		((RestMetadata) rest1).setPermissions(permissions1);
		assertFalse(rest1.isEqual(rest2));
		
		((RestMetadata) rest2).setPermissions(permissions2);
		assertTrue(rest1.isEqual(rest2));
		
		permission2.setUid("other");
		assertFalse(rest1.isEqual(rest2));
	}
	
	@Test
	void testRemoveFunction() {
		final Rest rest = new RestMetadata();
		final RestFunction function = new RestFunction();
		rest.addFunction(function);
		assertSame(1, rest.getFunctions().size());
		
		rest.removeFunction(function);
		
		assertFalse(rest.hasFunctions());
		assertSame(0, rest.getFunctions().size());
	}
	
}
