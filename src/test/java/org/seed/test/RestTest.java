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
package org.seed.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import org.seed.core.rest.Rest;
import org.seed.core.rest.RestFunction;
import org.seed.core.rest.RestMetadata;
import org.seed.core.rest.RestPermission;

public class RestTest {
	
	@Test
	public void testAddFunction() {
		final Rest rest = new RestMetadata();
		final RestFunction function = new RestFunction();
		assertFalse(rest.hasFunctions());
		
		rest.addFunction(function);
		
		assertSame(function.getRest(), rest);
		assertTrue(rest.hasFunctions());
		assertSame(rest.getFunctions().size(), 1);
		assertSame(rest.getFunctions().get(0), function);
	}
	
	@Test
	public void testGetFunctionByMapping() {
		final Rest rest = new RestMetadata();
		final RestFunction function = new RestFunction();
		function.setName("other");
		function.setMapping("/other");
		rest.addFunction(function);
		
		assertNull(rest.getFunctionByMapping("test"));
		
		function.setMapping("/test");
		
		assertSame(rest.getFunctionByMapping("test"), function);
		
		function.setMapping(null);
		function.setName("test");
		
		assertSame(rest.getFunctionByMapping("test"), function);
	}
	
	@Test
	public void testGetFunctionByUid() {
		final Rest rest = new RestMetadata();
		final RestFunction function = new RestFunction();
		function.setUid("other");
		rest.addFunction(function);
		
		assertNull(rest.getFunctionByUid("test"));
		
		function.setUid("test");
		
		assertSame(rest.getFunctionByUid("test"), function);
	}
	
	@Test
	public void testGetPermissionByUid() {
		final Rest rest = new RestMetadata();
		final RestPermission permission = new RestPermission();
		final List<RestPermission> permissions = new ArrayList<>();
		permission.setUid("other");
		permissions.add(permission);
		((RestMetadata) rest).setPermissions(permissions);
		
		assertNull(rest.getPermissionByUid("test"));
		
		permission.setUid("test");
		
		assertSame(rest.getPermissionByUid("test"), permission);
	}
	
	@Test
	public void testRemoveFunction() {
		final Rest rest = new RestMetadata();
		final RestFunction function = new RestFunction();
		rest.addFunction(function);
		assertSame(rest.getFunctions().size(), 1);
		
		rest.removeFunction(function);
		
		assertFalse(rest.hasFunctions());
		assertSame(rest.getFunctions().size(), 0);
	}
	
}
