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
package org.seed.test.unit.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityStatusTransitionFunction;

class EntityStatusTransitionFunctionTest {
	
	@Test
	void testGetFunctionUid() {
		final EntityStatusTransitionFunction function = new EntityStatusTransitionFunction();
		final EntityFunction entityFunction = new EntityFunction();
		function.setFunctionUid("test");
		assertEquals("test", function.getFunctionUid());
		
		entityFunction.setUid("function");
		function.setFunction(entityFunction);
		assertEquals("function", function.getFunctionUid());
	}
	
	@Test
	void testIsEqual() {
		final EntityStatusTransitionFunction function1 = new EntityStatusTransitionFunction();
		final EntityStatusTransitionFunction function2 = new EntityStatusTransitionFunction();
		assertTrue(function1.isEqual(function2));
		
		function1.setFunctionUid("function");
		function1.setActiveBeforeTransition(true);
		function1.setActiveAfterTransition(true);
		assertFalse(function1.isEqual(function2));
		
		function2.setFunctionUid("function");
		function2.setActiveBeforeTransition(true);
		function2.setActiveAfterTransition(true);
		assertTrue(function1.isEqual(function2));
	}
}
