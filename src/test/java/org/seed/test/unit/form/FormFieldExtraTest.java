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
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterMetadata;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerMetadata;
import org.seed.core.form.Form;
import org.seed.core.form.FormFieldExtra;
import org.seed.core.form.FormMetadata;

class FormFieldExtraTest {
	
	@Test
	void testGetDetailFormUid() {
		final FormFieldExtra extra = new FormFieldExtra();
		final Form detailForm = new FormMetadata();
		assertNull(extra.getDetailFormUid());
		
		extra.setDetailFormUid("test");
		assertEquals("test", extra.getDetailFormUid());
		
		detailForm.setUid("detail");
		extra.setDetailForm(detailForm);
		assertEquals("detail", extra.getDetailFormUid());
	}
	
	@Test
	void testGetEntityFieldUid() {
		final FormFieldExtra extra = new FormFieldExtra();
		final EntityField entityField = new EntityField();
		assertNull(extra.getEntityFieldUid());
		
		extra.setEntityFieldUid("test");
		assertEquals("test", extra.getEntityFieldUid());
		
		entityField.setUid("field");
		extra.setEntityField(entityField);
		assertEquals("field", extra.getEntityFieldUid());
	}
	
	@Test
	void testGetFilterUid() {
		final FormFieldExtra extra = new FormFieldExtra();
		final Filter filter = new FilterMetadata();
		assertNull(extra.getFilterUid());
		
		extra.setFilterUid("test");
		assertEquals("test", extra.getFilterUid());
		
		filter.setUid("filter");
		extra.setFilter(filter);
		assertEquals("filter", extra.getFilterUid());
	}
	
	@Test
	void testGetTransformerUid() {
		final FormFieldExtra extra = new FormFieldExtra();
		final Transformer transformer = new TransformerMetadata();
		assertNull(extra.getTransformerUid());
		
		extra.setTransformerUid("test");
		assertEquals("test", extra.getTransformerUid());
		
		transformer.setUid("transformer");
		extra.setTransformer(transformer);
		assertEquals("transformer", extra.getTransformerUid());
	}
	
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
