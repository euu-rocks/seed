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
package org.seed.test.unit.transformer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.seed.core.entity.EntityField;
import org.seed.core.entity.transform.TransformerElement;

class TransformerElementTest {
	
	@Test
	void testGetSourceFieldUid() {
		final TransformerElement element = new TransformerElement();
		final EntityField sourceField = new EntityField();
		assertNull(element.getSourceFieldUid());
		
		element.setSourceFieldUid("test");
		assertEquals("test", element.getSourceFieldUid());
		
		sourceField.setUid("source");
		element.setSourceField(sourceField);
		assertEquals("source", element.getSourceFieldUid());
	}
	
	@Test
	void testGetTargetFieldUid() {
		final TransformerElement element = new TransformerElement();
		final EntityField targetField = new EntityField();
		assertNull(element.getTargetFieldUid());
		
		element.setTargetFieldUid("test");
		assertEquals("test", element.getTargetFieldUid());
		
		targetField.setUid("target");
		element.setTargetField(targetField);
		assertEquals("target", element.getTargetFieldUid());
	}
	
	@Test
	void testIsEqual() {
		final TransformerElement element1 = new TransformerElement();
		final TransformerElement element2 = new TransformerElement();
		assertTrue(element1.isEqual(element2));
		
		element1.setSourceFieldUid("source");
		element1.setTargetFieldUid("target");
		assertFalse(element1.isEqual(element2));
		
		element2.setSourceFieldUid("source");
		element2.setTargetFieldUid("target");
		assertTrue(element1.isEqual(element2));
	}
	
 }
