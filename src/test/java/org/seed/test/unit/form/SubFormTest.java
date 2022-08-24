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
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterMetadata;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerMetadata;
import org.seed.core.form.Form;
import org.seed.core.form.FormActionType;
import org.seed.core.form.FormMetadata;
import org.seed.core.form.SubForm;
import org.seed.core.form.SubFormAction;
import org.seed.core.form.SubFormField;

class SubFormTest {
	
	@Test
	void testAddAction() {
		final SubForm subForm = new SubForm();
		final SubFormAction action = new SubFormAction();
		assertFalse(subForm.hasActions());
		
		subForm.addAction(action);
		
		assertTrue(subForm.hasActions());
		assertSame(1, subForm.getActions().size());
		assertSame(action, subForm.getActions().get(0));
	}
	
	@Test
	void testAddField() {
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		assertFalse(subForm.hasFields());
		
		subForm.addField(field);
		
		assertTrue(subForm.hasFields());
		assertSame(1, subForm.getFields().size());
		assertSame(field, subForm.getFields().get(0));
	}
	
	@Test
	void testContainsEntityField() {
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		final EntityField entityField = new EntityField();
		subForm.addField(field);
		assertFalse(subForm.containsEntityField(entityField));
		
		field.setEntityField(entityField);
		
		assertTrue(subForm.containsEntityField(entityField));
	}
	
	@Test
	void testContainsEntityFunction() {
		final SubForm subForm = new SubForm();
		final SubFormAction action = new SubFormAction();
		final EntityFunction function = new EntityFunction();
		subForm.addAction(action);
		assertFalse(subForm.containsEntityFunction(function));
		
		action.setEntityFunction(function);
		
		assertTrue(subForm.containsEntityFunction(function));
	}
	
	@Test
	void testContainsFilter() {
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		final Filter filter = new FilterMetadata();
		subForm.addField(field);
		assertFalse(subForm.containsFilter(filter));
		
		field.setFilter(filter);
		
		assertTrue(subForm.containsFilter(filter));
	}
	
	@Test
	void testContainsForm() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		subForm.addField(field);
		assertFalse(subForm.containsForm(form));
		
		field.setDetailForm(form);
		
		assertTrue(subForm.containsForm(form));
	}
	
	@Test
	void testContainsTransformer() {
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		final Transformer transformer = new TransformerMetadata();
		subForm.addField(field);
		assertFalse(subForm.containsTransformer(transformer));
		
		field.setTransformer(transformer);
		
		assertTrue(subForm.containsTransformer(transformer));
	}
	
	@Test
	void testGetActionByType() {
		final SubForm subForm = new SubForm();
		final SubFormAction action = new SubFormAction();
		subForm.addAction(action);
		action.setType(FormActionType.CUSTOM);
		
		assertNull(subForm.getActionByType((FormActionType.BACKSEARCH)));
		assertSame(action, subForm.getActionByType((FormActionType.CUSTOM)));
	}
	
	@Test
	void testGetActionByUid() {
		final SubForm subForm = new SubForm();
		final SubFormAction action = new SubFormAction();
		subForm.addAction(action);
		action.setUid("other");
		assertNull(subForm.getActionByUid("test"));
		
		action.setUid("test");
		
		assertSame(action, subForm.getActionByUid("test"));
	}
	
	@Test
	void testGetFieldByEntityFieldUid() {
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		final EntityField entityField = new EntityField();
		entityField.setUid("other");
		field.setEntityField(entityField);
		subForm.addField(field);
		assertNull(subForm.getFieldByEntityFieldUid("test"));
		
		entityField.setUid("test");
		
		assertSame(field, subForm.getFieldByEntityFieldUid("test"));
	}
	
	@Test
	void testGetFieldByUid() {
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		subForm.addField(field);
		field.setUid("other");
		assertNull(subForm.getFieldByUid("test"));
		
		field.setUid("test");
		
		assertSame(field, subForm.getFieldByUid("test"));
	}
	
	@Test
	void testIsEqual() {
		final SubForm subForm1 = new SubForm();
		final SubForm subForm2 = new SubForm();
		assertTrue(subForm1.isEqual(subForm2));
		
		subForm1.setNestedEntityUid("test");
		assertFalse(subForm1.isEqual(subForm2));
		
		subForm2.setNestedEntityUid("test");
		assertTrue(subForm1.isEqual(subForm2));
		
		subForm2.setNestedEntityUid("other");
		assertFalse(subForm1.isEqual(subForm2));
	}
	
	@Test
	void testIsEqualFields() {
		final SubForm subForm1 = new SubForm();
		final SubForm subForm2 = new SubForm();
		final SubFormField field1 = new SubFormField();
		final SubFormField field2 = new SubFormField();
		field1.setUid("test");
		field2.setUid("test");
		subForm1.addField(field1);
		assertFalse(subForm1.isEqual(subForm2));
		
		subForm2.addField(field2);
		assertTrue(subForm1.isEqual(subForm2));
		
		field2.setUid("other");
		assertFalse(subForm1.isEqual(subForm2));
	}
	
	@Test
	void testIsEqualActions() {
		final SubForm subForm1 = new SubForm();
		final SubForm subForm2 = new SubForm();
		final SubFormAction action1 = new SubFormAction();
		final SubFormAction action2 = new SubFormAction();
		action1.setUid("test");
		action2.setUid("test");
		subForm1.addAction(action1);
		assertFalse(subForm1.isEqual(subForm2));
		
		subForm2.addAction(action2);
		assertTrue(subForm1.isEqual(subForm2));
		
		action2.setUid("other");
		assertFalse(subForm1.isEqual(subForm2));
	}
}
