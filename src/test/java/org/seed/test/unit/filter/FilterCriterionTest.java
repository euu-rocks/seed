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
import org.seed.core.entity.filter.CriterionOperator;
import org.seed.core.entity.filter.FilterCriterion;

public class FilterCriterionTest {
	
	@Test
	public void testIsEqual() {
		final FilterCriterion criterion1 = new FilterCriterion();
		final FilterCriterion criterion2 = new FilterCriterion();
		final EntityField entityField1 = new EntityField();
		final EntityField entityField2 = new EntityField();
		entityField1.setType(FieldType.TEXT);
		entityField2.setType(FieldType.TEXT);
		criterion1.setEntityField(entityField1);
		criterion2.setEntityField(entityField2);
		assertTrue(criterion1.isEqual(criterion2));
		
		criterion1.setEntityFieldUid("test");
		criterion1.setSystemField(SystemField.CREATEDBY);
		criterion1.setOperator(CriterionOperator.EQUAL);
		criterion1.setValue("test");
		assertFalse(criterion1.isEqual(criterion2));
		
		entityField2.setUid("test");
		criterion2.setSystemField(SystemField.CREATEDBY);
		criterion2.setOperator(CriterionOperator.EQUAL);
		criterion2.setValue("test");
		assertTrue(criterion1.isEqual(criterion2));
	}
	
	@Test
	public void testNeedsValue() {
		final FilterCriterion criterion = new FilterCriterion();
		assertFalse(criterion.needsValue());
		
		criterion.setOperator(CriterionOperator.EQUAL);
		assertTrue(criterion.needsValue());
		
		criterion.setOperator(CriterionOperator.EMPTY);
		assertFalse(criterion.needsValue());
	}
	
	@Test
	public void testHasValue() {
		final FilterCriterion criterion = new FilterCriterion();
		final EntityField entityField = new EntityField();
		entityField.setType(FieldType.TEXT);
		criterion.setEntityField(entityField);
		assertFalse(criterion.hasValue());
		
		criterion.setValue("test");
		assertTrue(criterion.hasValue());
	}
	
	@Test
	public void testGetLike() {
		final FilterCriterion criterion = new FilterCriterion();
		final EntityField entityField = new EntityField();
		entityField.setType(FieldType.TEXT);
		criterion.setEntityField(entityField);
		assertNull(criterion.getLike());
		
		criterion.setValue("test");
		assertEquals("%test%", criterion.getLike());
		
		criterion.setValue("test*");
		assertEquals("test%", criterion.getLike());
		
		criterion.setValue("*test");
		assertEquals("%test", criterion.getLike());
		
		criterion.setValue("*test*");
		assertEquals("%test%", criterion.getLike());
		
		criterion.setValue("test%");
		assertEquals("test%", criterion.getLike());
		
		criterion.setValue("%test");
		assertEquals("%test", criterion.getLike());
		
		criterion.setValue("%test%");
		assertEquals("%test%", criterion.getLike());
	}
}
