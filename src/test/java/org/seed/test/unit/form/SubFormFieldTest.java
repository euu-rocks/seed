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
package org.seed.test.unit.form;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.seed.core.entity.EntityField;
import org.seed.core.form.SubFormField;

class SubFormFieldTest {

	@Test
	void testGetName() {
		final SubFormField field = new SubFormField();
		final EntityField entityField = new EntityField();
		field.setEntityField(entityField);
		entityField.setName("field");
		assertEquals("field", field.getName());
		
		field.setLabel("label");
		assertEquals("label", field.getName());
	}
	
	@Test
	void testIsEqual() {
		final SubFormField field1 = new SubFormField();
		final SubFormField field2 = new SubFormField();
		assertTrue(field1.isEqual(field2));
		
		field1.setEntityFieldUid("entityField");
		field1.setTransformerUid("transformer");
		field1.setFilterUid("filter");
		field1.setDetailFormUid("detailForm");
		field1.setLabel("test");
		field1.setWidth("width");
		field1.setHeight("height");
		field1.setHflex("hflex");
		field1.setStyle("style");
		field1.setLabelStyle("labelStyle");
		field1.setReadonly(true);
		field1.setBandbox(true);
		assertFalse(field1.isEqual(field2));
		
		field2.setEntityFieldUid("entityField");
		field2.setTransformerUid("transformer");
		field2.setFilterUid("filter");
		field2.setDetailFormUid("detailForm");
		field2.setLabel("test");
		field2.setWidth("width");
		field2.setHeight("height");
		field2.setHflex("hflex");
		field2.setStyle("style");
		field2.setLabelStyle("labelStyle");
		field2.setReadonly(true);
		field2.setBandbox(true);
		assertTrue(field1.isEqual(field2));
	}
}
