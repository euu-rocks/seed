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

import org.seed.core.data.SystemField;
import org.seed.core.form.FormField;

class FormFieldTest {
	
	@Test
	void testIsEqual() {
		final FormField field1 = new FormField();
		final FormField field2 = new FormField();
		assertTrue(field1.isEqual(field2));
		
		field1.setEntityFieldUid("entityField");
		field1.setSystemField(SystemField.CREATEDBY);
		field1.setLabel("label");
		field1.setStyle("style");
		field1.setLabelStyle("labelStyle");
		field1.setWidth("width");
		field1.setHeight("height");
		field1.setHflex("hflex");
		field1.setThumbnailWidth(123);
		field1.setSelected(true);
		assertFalse(field1.isEqual(field2));
		
		field2.setEntityFieldUid("entityField");
		field2.setSystemField(SystemField.CREATEDBY);
		field2.setLabel("label");
		field2.setStyle("style");
		field2.setLabelStyle("labelStyle");
		field2.setWidth("width");
		field2.setHeight("height");
		field2.setHflex("hflex");
		field2.setThumbnailWidth(123);
		field2.setSelected(true);
		assertTrue(field1.isEqual(field2));
	}
	
}
