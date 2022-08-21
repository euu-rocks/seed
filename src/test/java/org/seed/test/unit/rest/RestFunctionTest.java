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

import org.junit.jupiter.api.Test;
import org.seed.core.api.RestFunction.MethodType;
import org.seed.core.rest.RestFunction;

class RestFunctionTest {
	
	@Test
	void testIsEqual() {
		final RestFunction function1 = new RestFunction();
		final RestFunction function2 = new RestFunction();
		assertTrue(function1.isEqual(function2));
		
		function1.setName("test");
		function1.setMapping("mapping");
		function1.setMethod(MethodType.GET);
		function1.setContent("content");
		assertFalse(function1.isEqual(function2));
		
		function2.setName("test");
		function2.setMapping("mapping");
		function2.setMethod(MethodType.GET);
		function2.setContent("content");
		assertTrue(function1.isEqual(function2));
	}
}
