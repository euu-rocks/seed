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

import org.seed.core.data.FieldType;
import org.seed.core.entity.EntityField;

class EntityFieldTest {
	
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
