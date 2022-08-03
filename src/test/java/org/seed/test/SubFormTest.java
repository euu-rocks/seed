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
package org.seed.test;

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

public class SubFormTest {
	
	@Test
	public void testAddAction() {
		final SubForm subForm = new SubForm();
		final SubFormAction action = new SubFormAction();
		assertFalse(subForm.hasActions());
		
		subForm.addAction(action);
		
		assertTrue(subForm.hasActions());
		assertSame(subForm.getActions().size(), 1);
		assertSame(subForm.getActions().get(0), action);
	}
	
	@Test
	public void testAddField() {
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		assertFalse(subForm.hasFields());
		
		subForm.addField(field);
		
		assertTrue(subForm.hasFields());
		assertSame(subForm.getFields().size(), 1);
		assertSame(subForm.getFields().get(0), field);
	}
	
	@Test
	public void testContainsEntityField() {
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		final EntityField entityField = new EntityField();
		subForm.addField(field);
		assertFalse(subForm.containsEntityField(entityField));
		
		field.setEntityField(entityField);
		
		assertTrue(subForm.containsEntityField(entityField));
	}
	
	@Test
	public void testContainsEntityFunction() {
		final SubForm subForm = new SubForm();
		final SubFormAction action = new SubFormAction();
		final EntityFunction function = new EntityFunction();
		subForm.addAction(action);
		assertFalse(subForm.containsEntityFunction(function));
		
		action.setEntityFunction(function);
		
		assertTrue(subForm.containsEntityFunction(function));
	}
	
	@Test
	public void testContainsFilter() {
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		final Filter filter = new FilterMetadata();
		subForm.addField(field);
		assertFalse(subForm.containsFilter(filter));
		
		field.setFilter(filter);
		
		assertTrue(subForm.containsFilter(filter));
	}
	
	@Test
	public void testContainsForm() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		subForm.addField(field);
		assertFalse(subForm.containsForm(form));
		
		field.setDetailForm(form);
		
		assertTrue(subForm.containsForm(form));
	}
	
	@Test
	public void testContainsTransformer() {
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		final Transformer transformer = new TransformerMetadata();
		subForm.addField(field);
		assertFalse(subForm.containsTransformer(transformer));
		
		field.setTransformer(transformer);
		
		assertTrue(subForm.containsTransformer(transformer));
	}
	
	@Test
	public void testGetActionByType() {
		final SubForm subForm = new SubForm();
		final SubFormAction action = new SubFormAction();
		subForm.addAction(action);
		action.setType(FormActionType.CUSTOM);
		
		assertNull(subForm.getActionByType((FormActionType.BACKSEARCH)));
		assertSame(subForm.getActionByType((FormActionType.CUSTOM)), action);
	}
	
	@Test
	public void testGetActionByUid() {
		final SubForm subForm = new SubForm();
		final SubFormAction action = new SubFormAction();
		subForm.addAction(action);
		action.setUid("other");
		assertNull(subForm.getActionByUid("test"));
		
		action.setUid("test");
		
		assertSame(subForm.getActionByUid("test"), action);
	}
	
	@Test
	public void testGetFieldByEntityFieldUid() {
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		final EntityField entityField = new EntityField();
		entityField.setUid("other");
		field.setEntityField(entityField);
		subForm.addField(field);
		assertNull(subForm.getFieldByEntityFieldUid("test"));
		
		entityField.setUid("test");
		
		assertSame(subForm.getFieldByEntityFieldUid("test"), field);
	}
	
	@Test
	public void testGetFieldByUid() {
		final SubForm subForm = new SubForm();
		final SubFormField field = new SubFormField();
		subForm.addField(field);
		field.setUid("other");
		assertNull(subForm.getFieldByUid("test"));
		
		field.setUid("test");
		
		assertSame(subForm.getFieldByUid("test"), field);
	}
	
}
