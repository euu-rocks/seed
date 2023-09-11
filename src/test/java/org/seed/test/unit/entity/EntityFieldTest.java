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

import java.util.Date;

import org.junit.jupiter.api.Test;

import org.seed.core.data.AbstractSystemObject;
import org.seed.core.data.FieldType;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityMetadata;

class EntityFieldTest {
	
	@Test
	void testGetInternalName() {
		final EntityField field = new EntityField();
		assertNull(field.getInternalName());
		
		field.setName("TÄST");
		assertEquals("taest", field.getInternalName());
	}
	
	@Test
	void testGetEffectiveColumnName() {
		final EntityField field = new EntityField();
		assertNull(field.getEffectiveColumnName());
		
		field.setName("TÄST");
		assertEquals("taest", field.getEffectiveColumnName());
		
		field.setColumnName("COL");
		assertEquals("col", field.getEffectiveColumnName());
	}
	
	@Test
	void testGetFieldGroupUid() {
		final EntityField field = new EntityField();
		final EntityFieldGroup group = new EntityFieldGroup();
		group.setUid("group");
		assertNull(field.getFieldGroupUid());
		
		field.setFieldGroupUid("test");
		assertEquals("test", field.getFieldGroupUid());
		
		field.setFieldGroup(group);
		assertEquals("group", field.getFieldGroupUid());
	}
	
	@Test
	void testGetReferenceEntityUid() {
		final EntityField field = new EntityField();
		final Entity referenceEntity = new EntityMetadata();
		referenceEntity.setUid("ref");
		assertNull(field.getReferenceEntityUid());
		
		field.setReferenceEntityUid("test");
		assertEquals("test", field.getReferenceEntityUid());
		
		field.setReferenceEntity(referenceEntity);
		assertEquals("ref", field.getReferenceEntityUid());
	}
	
	@Test
	void testHasDefaultValue() {
		final EntityField field = new EntityField();
		assertFalse(field.hasDefaultValue());
		
		field.setType(FieldType.TEXT);
		field.setDefaultString("test");
		assertTrue(field.hasDefaultValue());
		field.setDefaultString(null);
		
		field.setType(FieldType.DATE);
		field.setDefaultDate(new Date());
		assertTrue(field.hasDefaultValue());
		field.setDefaultDate(null);
		
		field.setType(FieldType.INTEGER);
		field.setDefaultNumber(123);
		assertTrue(field.hasDefaultValue());
		field.setDefaultNumber(null);
		
		field.setType(FieldType.REFERENCE);
		field.setDefaultObject(new AbstractSystemObject() {});
		assertTrue(field.hasDefaultValue());
	}
	
	@Test
	void testIsReferenceField() {
		final EntityField field = new EntityField();
		assertFalse(field.isReferenceField());
		
		field.setType(FieldType.REFERENCE);
		assertTrue(field.isReferenceField());
		
		field.setType(FieldType.TEXT);
		assertFalse(field.isReferenceField());
	}
	
	@Test
	void testIsTextField() {
		final EntityField field = new EntityField();
		assertFalse(field.isTextField());
		
		field.setType(FieldType.TEXT);
		assertTrue(field.isTextField());
		
		field.setType(FieldType.INTEGER);
		assertFalse(field.isTextField());
		
		field.setType(FieldType.TEXTLONG);
		assertTrue(field.isTextField());
	}
	
	@Test
	void testIsUidField() {
		final EntityField field = new EntityField();
		assertFalse(field.isUidField());
		
		field.setName("uid");
		assertTrue(field.isUidField());
		
		field.setName("id");
		assertFalse(field.isUidField());
	}
	
	@Test
	void testIsJsonSerializable() {
		final EntityField field = new EntityField();
		assertFalse(field.isJsonSerializable());
		
		field.setType(FieldType.TEXT);
		assertTrue(field.isJsonSerializable());
		
		field.setType(FieldType.BINARY);
		assertFalse(field.isJsonSerializable());
		
		field.setType(FieldType.DATE);
		assertTrue(field.isJsonSerializable());
		
		field.setType(FieldType.FILE);
		assertFalse(field.isJsonSerializable());
	}
	
	@Test
	void testIsEqual() {
		final EntityField field1 = new EntityField();
		final EntityField field2 = new EntityField();
		assertTrue(field1.isEqual(field2));
		
		field1.setFieldGroupUid("fieldgroup");
		field1.setReferenceEntityUid("referenceEntity");
		field1.setName("name");
		field1.setColumnName("column");
		field1.setType(FieldType.TEXT);
		field1.setLength(123);
		field1.setFormula("formula");
		field1.setAutonumPattern("autonumpattern");
		field1.setAutonumStart(987L);
		field1.setCalculated(true);
		field1.setMandatory(true);
		field1.setIndexed(true);
		field1.setUnique(true);
		field1.setFullTextSearch(true);
		assertFalse(field1.isEqual(field2));
		
		field2.setFieldGroupUid("fieldgroup");
		field2.setReferenceEntityUid("referenceEntity");
		field2.setName("name");
		field2.setColumnName("column");
		field2.setType(FieldType.TEXT);
		field2.setLength(123);
		field2.setFormula("formula");
		field2.setAutonumPattern("autonumpattern");
		field2.setAutonumStart(987L);
		field2.setCalculated(true);
		field2.setMandatory(true);
		field2.setIndexed(true);
		field2.setUnique(true);
		field2.setFullTextSearch(true);
		assertTrue(field1.isEqual(field2));
		
		field2.setFullTextSearch(false);
		assertFalse(field1.isEqual(field2));
	}
}
