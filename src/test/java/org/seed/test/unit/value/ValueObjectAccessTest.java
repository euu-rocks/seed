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
package org.seed.test.unit.value;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.seed.core.data.SystemField;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.value.AbstractValueObject;
import org.seed.core.entity.value.ValueObjectAccess;

class ValueObjectAccessTest {
	
	@Test
	void testGetValue() {
		final ValueObjectAccess access = new ValueObjectAccess();
		final EntityField field = new EntityField();
		final TestObject object = new TestObject();
		field.setName("name");
		assertNull(access.getValue(object, field));
		
		object.setName("test");
		assertEquals("test", access.getValue(object, field));
	}
	
	@Test
	void testGetValueSystemField() {
		final ValueObjectAccess access = new ValueObjectAccess();
		final TestObject object = new TestObject();
		assertNull(access.getValue(object, SystemField.ID));
		
		object.setId(123L);
		assertSame(123L, access.getValue(object, SystemField.ID));
	}
	
	@Test
	void testSetValue() {
		final ValueObjectAccess access = new ValueObjectAccess();
		final EntityField field = new EntityField();
		final TestObject object = new TestObject();
		field.setName("name");
		access.setValue(object, field, "test");
		assertEquals("test", object.getName());
		
		access.setValue(object, field, null);
		assertNull(object.getName());
	}
	
	@Test
	void testSetValueSystemField() {
		final ValueObjectAccess access = new ValueObjectAccess();
		final TestObject object = new TestObject();
		access.setValue(object, SystemField.ID, 123L);
		assertSame(123L, object.getId());
		
		access.setValue(object, SystemField.ID, null);
		assertNull(object.getId());
	}
	
	@Test
	void testHasNestedObjects() {
		final ValueObjectAccess access = new ValueObjectAccess();
		final TestObject object = new TestObject();
		final NestedEntity nestedEntity = new NestedEntity();
		nestedEntity.setName("nested");
		assertFalse(access.hasNestedObjects(object, nestedEntity));
		
		object.addNested(new TestNestedObject());
		assertTrue(access.hasNestedObjects(object, nestedEntity));
	}
	
	@Test
	void testGetNestedObjects() {
		final ValueObjectAccess access = new ValueObjectAccess();
		final TestObject object = new TestObject();
		final TestNestedObject nestedObject = new TestNestedObject();
		final NestedEntity nestedEntity = new NestedEntity();
		nestedEntity.setName("nested");
		assertNull(access.getNestedObjects(object, nestedEntity));
		
		object.addNested(nestedObject);
		assertNotNull(access.getNestedObjects(object, nestedEntity));
		assertSame(nestedObject, access.getNestedObjects(object, nestedEntity).get(0));
	}
	
	@Test
	void testHasRelatedObjects() {
		final ValueObjectAccess access = new ValueObjectAccess();
		final TestObject object = new TestObject();
		final EntityRelation relation = new EntityRelation();
		relation.setName("related");
		assertFalse(access.hasRelatedObjects(object, relation));
		
		object.addRelated(new TestRelatedObject());
		assertTrue(access.hasRelatedObjects(object, relation));
	}
	
	@Test
	void testGetRelatedObjects() {
		final ValueObjectAccess access = new ValueObjectAccess();
		final TestObject object = new TestObject();
		final TestRelatedObject relatedObject = new TestRelatedObject();
		final EntityRelation relation = new EntityRelation();
		relation.setName("related");
		assertNull(access.getRelatedObjects(object, relation));
		
		object.addRelated(relatedObject);
		assertNotNull(access.getRelatedObjects(object, relation));
		assertSame(relatedObject, access.getRelatedObjects(object, relation).iterator().next());
	}
	
	@Test
	void testAddRelatedObject() {
		final ValueObjectAccess access = new ValueObjectAccess();
		final TestObject object = new TestObject();
		final TestRelatedObject relatedObject = new TestRelatedObject();
		final EntityRelation relation = new EntityRelation();
		relation.setName("related");
		assertNull(object.getRelated());
		
		access.addRelatedObject(object, relation, relatedObject);
		assertNotNull(object.getRelated());
	}
	
	@Test
	void testRemoveRelatedObject() {
		final ValueObjectAccess access = new ValueObjectAccess();
		final TestObject object = new TestObject();
		final TestRelatedObject relatedObject = new TestRelatedObject();
		final EntityRelation relation = new EntityRelation();
		relation.setName("related");
		object.addRelated(relatedObject);
		assertNotNull(object.getRelated());
		
		access.removeRelatedObject(object, relation, relatedObject);
		assertTrue(object.getRelated().isEmpty());
	}
	
	public static class TestObject extends AbstractValueObject {
		
		private String name;

		private List<TestNestedObject> nested;
		
		private Set<TestRelatedObject> related;
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public Long getEntityId() {
			return null;
		}

		public List<TestNestedObject> getNested() {
			return nested;
		}
		
		public Set<TestRelatedObject> getRelated() {
			return related;
		}

		public void addNested(TestNestedObject _nested) {
			if (nested == null) {
				nested = new ArrayList<>();
			}
			nested.add(_nested);
		}
		
		public void addRelated(TestRelatedObject _related) {
			if (related == null) {
				related = new HashSet<>();
			}
			related.add(_related);
		}
		
		public void removeRelated(TestRelatedObject _related) {
			if (related != null) {
				related.remove(_related);
			}
		}
		
	}
	
	public static class TestRelatedObject extends AbstractValueObject {

		@Override
		public Long getEntityId() {
			return null;
		}
		
	}
	
	public static class TestNestedObject extends AbstractValueObject {
		
		@Override
		public Long getEntityId() {
			return null;
		}
		
	}
}
