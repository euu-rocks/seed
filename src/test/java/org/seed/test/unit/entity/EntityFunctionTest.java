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

import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityMetadata;

class EntityFunctionTest {
	
	@Test
	void testGetGeneratedPackage() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		function.setEntity(entity);
		entity.setName("TÄST");
		
		assertEquals("org.seed.generated.entity.taest", function.getGeneratedPackage());
	}
	
	@Test
	void testGetGeneratedClass() {
		final EntityFunction function = new EntityFunction();
		function.setName("tästSomething");
		
		assertEquals("TaestSomething", function.getGeneratedClass());
	}
	
	@Test
	void testIsEqual() {
		final EntityFunction function1 = new EntityFunction();
		final EntityFunction function2 = new EntityFunction();
		assertTrue(function1.isEqual(function2));
		
		function1.setName("name");
		function1.setContent("content");
		function1.setCallback(true);
		function1.setActive(true);
		function1.setActiveOnCreate(true);
		function1.setActiveOnModify(true);
		function1.setActiveOnStatusTransition(true);
		function1.setActiveOnUserAction(true);
		function1.setActiveBeforeInsert(true);
		function1.setActiveAfterInsert(true);
		function1.setActiveBeforeUpdate(true);
		function1.setActiveAfterUpdate(true);
		function1.setActiveBeforeDelete(true);
		function1.setActiveAfterDelete(true);
		assertFalse(function1.isEqual(function2));
		
		function2.setName("name");
		function2.setContent("content");
		function2.setCallback(true);
		function2.setActive(true);
		function2.setActiveOnCreate(true);
		function2.setActiveOnModify(true);
		function2.setActiveOnStatusTransition(true);
		function2.setActiveOnUserAction(true);
		function2.setActiveBeforeInsert(true);
		function2.setActiveAfterInsert(true);
		function2.setActiveBeforeUpdate(true);
		function2.setActiveAfterUpdate(true);
		function2.setActiveBeforeDelete(true);
		function2.setActiveAfterDelete(true);
		assertTrue(function1.isEqual(function2));
		
		function2.setActiveAfterDelete(false);
		assertFalse(function1.isEqual(function2));
	}
	
}
