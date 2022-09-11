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
package org.seed.test.unit.filter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.seed.core.data.FieldType;
import org.seed.core.data.SystemField;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.filter.FilterElement;

class FilterElementTest {
	
	@Test
	void testIsField() {
		final FilterElement element = new FilterElement();
		final EntityField field = new EntityField();
		assertFalse(element.isField());
		
		field.setType(FieldType.TEXT);
		element.setEntityField(field);
		assertTrue(element.isField());
		
		field.setType(FieldType.REFERENCE);
		assertFalse(element.isField());
		
		element.setEntityField(null);
		element.setSystemField(SystemField.ID);
		assertTrue(element.isField());
		
		element.setSystemField(SystemField.ENTITYSTATUS);
		assertFalse(element.isField());
	}
	
	@Test
	void testIsReference() {
		final FilterElement element = new FilterElement();
		final EntityField field = new EntityField();
		assertFalse(element.isReference());
		
		field.setType(FieldType.REFERENCE);
		element.setEntityField(field);
		assertTrue(element.isReference());
		
		field.setType(FieldType.TEXT);
		assertFalse(element.isReference());
		
		element.setEntityField(null);
		element.setSystemField(SystemField.ENTITYSTATUS);
		assertTrue(element.isReference());
		
		element.setSystemField(SystemField.ID);
		assertFalse(element.isReference());
	}
	
	@Test
	void testGetType() {
		final FilterElement element = new FilterElement();
		final EntityField field = new EntityField();
		assertNull(element.getType());
		
		field.setType(FieldType.TEXT);
		element.setEntityField(field);
		assertSame(FieldType.TEXT, element.getType());
		
		element.setEntityField(null);
		assertNull(element.getType());
		
		element.setSystemField(SystemField.ID);
		assertSame(FieldType.LONG, element.getType());
	}
	
}
