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

import org.seed.core.form.FormFieldExtra;

class FormFieldExtraTest {
	
	@Test
	void testIsEqual() {
		final FormFieldExtra extra1 = new FormFieldExtra();
		final FormFieldExtra extra2 = new FormFieldExtra();
		assertTrue(extra1.isEqual(extra2));
		
		extra1.setEntityFieldUid("entityField");
		extra1.setTransformerUid("transformer");
		extra1.setFilterUid("filter");
		extra1.setDetailFormUid("detailForm");
		extra1.setReadonly(true);
		extra1.setUnsortedValues(true);
		assertFalse(extra1.isEqual(extra2));
		
		extra2.setEntityFieldUid("entityField");
		extra2.setTransformerUid("transformer");
		extra2.setFilterUid("filter");
		extra2.setDetailFormUid("detailForm");
		extra2.setReadonly(true);
		extra2.setUnsortedValues(true);
		assertTrue(extra1.isEqual(extra2));
	}
}
