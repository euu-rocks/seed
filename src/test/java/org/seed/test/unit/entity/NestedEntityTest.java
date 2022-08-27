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
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityMetadata;
import org.seed.core.entity.NestedEntity;

class NestedEntityTest {
	
	@Test
	void testGetFieldByUid() {
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		final EntityField field = new EntityField();
		nested.setNestedEntity(nestedEntity);
		nestedEntity.addField(field);
		field.setUid("other");
		assertNull(nested.getFieldByUid("test"));
		
		field.setUid("test");
		assertSame(field, nested.getFieldByUid("test"));
	}
	
	@Test
	void testGetFunctionByUid() {
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		final EntityFunction function = new EntityFunction();
		nested.setNestedEntity(nestedEntity);
		nestedEntity.addFunction(function);
		function.setUid("other");
		assertNull(nested.getFunctionByUid("test"));
		
		function.setUid("test");
		assertSame(function, nested.getFunctionByUid("test"));
	}
	
	@Test
	void testGetFieldsExcludeParentRef() {
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		final EntityField field = new EntityField();
		final EntityField referenceField = new EntityField();
		nested.setNestedEntity(nestedEntity);
		nested.setReferenceField(referenceField);
		nestedEntity.addField(field);
		nestedEntity.addField(referenceField);
		assertSame(2, nested.getFields(false).size());
		
		assertSame(1, nested.getFields(true).size());
	}
	
	@Test
	void testGetInternalName() {
		final NestedEntity nested = new NestedEntity();
		assertNull(nested.getInternalName());
		
		nested.setName("TÄST");
		assertEquals("taest", nested.getInternalName());
	}
	
	@Test
	void testGetNestedEntityUid() {
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		nested.setNestedEntityUid("test");
		assertEquals("test", nested.getNestedEntityUid());
		
		nestedEntity.setUid("nested");
		nested.setNestedEntity(nestedEntity);
		assertEquals("nested", nested.getNestedEntityUid());
	}
	
	@Test
	void testGetReferenceFieldUid() {
		final NestedEntity nested = new NestedEntity();
		final EntityField referenceField = new EntityField();
		nested.setReferenceFieldUid("test");
		assertEquals("test", nested.getReferenceFieldUid());
		
		referenceField.setUid("ref");
		nested.setReferenceField(referenceField);
		assertEquals("ref", nested.getReferenceFieldUid());
	}
	
	@Test
	void testIsEqual() {
		final NestedEntity nested1 = new NestedEntity();
		final NestedEntity nested2 = new NestedEntity();
		assertTrue(nested1.isEqual(nested2));
		
		nested1.setName("test");
		nested1.setNestedEntityUid("nestedEntity");
		nested1.setReferenceFieldUid("referenceField");
		nested1.setReadonly(true);
		assertFalse(nested1.isEqual(nested2));
		
		nested2.setName("test");
		nested2.setNestedEntityUid("nestedEntity");
		nested2.setReferenceFieldUid("referenceField");
		nested2.setReadonly(true);
		assertTrue(nested1.isEqual(nested2));
	}
}
