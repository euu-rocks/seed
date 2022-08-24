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

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.seed.core.data.FieldAccess;
import org.seed.core.data.FieldType;
import org.seed.core.data.SystemField;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityAccess;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldConstraint;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityMetadata;
import org.seed.core.entity.EntityPermission;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterMetadata;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerMetadata;
import org.seed.core.form.AutolayoutType;
import org.seed.core.form.Form;
import org.seed.core.form.FormAction;
import org.seed.core.form.FormActionType;
import org.seed.core.form.FormField;
import org.seed.core.form.FormFieldExtra;
import org.seed.core.form.FormLayout;
import org.seed.core.form.FormMetadata;
import org.seed.core.form.FormPrintout;
import org.seed.core.form.FormTransformer;
import org.seed.core.form.RelationForm;
import org.seed.core.form.SubForm;
import org.seed.core.form.SubFormAction;
import org.seed.core.form.SubFormField;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupMetadata;
import org.seed.core.user.UserMetadata;

class FormTest {
	
	@Test
	void testAddAction() {
		final Form form = new FormMetadata();
		final FormAction action = new FormAction();
		assertFalse(form.hasActions());
		
		form.addAction(action);
		
		assertSame(form, action.getForm());
		
		assertTrue(form.hasActions());
		assertSame(1, form.getActions().size());
		assertSame(action, form.getActions().get(0));
	}
	
	@Test
	void testAddField() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		assertFalse(form.hasFields());
		
		form.addField(field);
		
		assertSame(form, field.getForm());
		
		assertTrue(form.hasFields());
		assertSame(1, form.getFields().size());
		assertSame(field, form.getFields().get(0));
	}
	
	@Test
	void testAddFieldExtra() {
		final Form form = new FormMetadata();
		final FormFieldExtra fieldExtra = new FormFieldExtra();
		assertFalse(form.hasFieldExtras());
		
		form.addFieldExtra(fieldExtra);
		
		assertSame(form, fieldExtra.getForm());
		
		assertTrue(form.hasFieldExtras());
		assertSame(1, form.getFieldExtras().size());
		assertSame(fieldExtra, form.getFieldExtras().get(0));
	}
	
	@Test
	void testAddPrintout() {
		final Form form = new FormMetadata();
		final FormPrintout printout = new FormPrintout();
		assertFalse(form.hasPrintouts());
		
		form.addPrintout(printout);
		
		assertSame(form, printout.getForm());
		
		assertTrue(form.hasPrintouts());
		assertSame(1, form.getPrintouts().size());
		assertSame(printout, form.getPrintouts().get(0));
	}
	
	@Test
	void testAddSubForm() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		assertFalse(form.hasSubForms());
		
		form.addSubForm(subForm);
		
		assertSame(form, subForm.getForm());
		
		assertTrue(form.hasSubForms());
		assertSame(1, form.getSubForms().size());
		assertSame(subForm, form.getSubForms().get(0));
	}
	
	@Test
	void testAddTransformer() {
		final Form form = new FormMetadata();
		final FormTransformer transformer = new FormTransformer();
		assertFalse(form.hasTransformers());
		
		form.addTransformer(transformer);
		
		assertSame(form, transformer.getForm());
		
		assertTrue(form.hasTransformers());
		assertSame(1, form.getTransformers().size());
		assertSame(transformer, form.getTransformers().get(0));
	}
	
	@Test
	void testContainsEntityField() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		final EntityField entityField = new EntityField();
		form.addField(field);
		assertFalse(form.containsEntityField(entityField));
		
		field.setEntityField(entityField);
		
		assertTrue(form.containsEntityField(entityField));
	}
	
	@Test
	void testContainsEntityFunction() {
		final Form form = new FormMetadata();
		final FormAction action = new FormAction();
		final EntityFunction function = new EntityFunction();
		form.addAction(action);
		assertFalse(form.containsEntityFunction(function));
		
		action.setEntityFunction(function);
		
		assertTrue(form.containsEntityFunction(function));
	}
	
	@Test
	void testContainsFilter() {
		final Form form = new FormMetadata();
		final FormFieldExtra fieldExtra = new FormFieldExtra();
		final Filter filter = new FilterMetadata();
		form.addFieldExtra(fieldExtra);
		assertFalse(form.containsFilter(filter));
		
		fieldExtra.setFilter(filter);
		
		assertTrue(form.containsFilter(filter));
	}
	
	@Test
	void testContainsSystemField() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		final SystemField systemField = SystemField.CREATEDON;
		form.addField(field);
		assertFalse(form.containsSystemField(systemField));
		
		field.setSystemField(systemField);
		
		assertTrue(form.containsSystemField(systemField));
	}
	
	@Test
	void testContainsTransformer() {
		final Form form = new FormMetadata();
		final FormTransformer formTransformer = new FormTransformer();
		final FormFieldExtra extra = new FormFieldExtra();
		final Transformer transformer = new TransformerMetadata();
		form.addTransformer(formTransformer);
		form.addFieldExtra(extra);
		assertFalse(form.containsTransformer(transformer));
		
		formTransformer.setTransformer(transformer);
		
		assertTrue(form.containsTransformer(transformer));
		
		formTransformer.setTransformer(null);
		extra.setTransformer(transformer);
		
		assertTrue(form.containsTransformer(transformer));
	}
	
	@Test
	void testContainsForm() {
		final Form form = new FormMetadata();
		final Form otherForm = new FormMetadata();
		final FormAction action = new FormAction();
		final FormFieldExtra extra = new FormFieldExtra();
		final FormTransformer transformer = new FormTransformer();
		form.addAction(action);
		form.addFieldExtra(extra);
		form.addTransformer(transformer);
		assertFalse(form.containsForm(otherForm));
		
		extra.setDetailForm(otherForm);
		
		assertTrue(form.containsForm(otherForm));
		
		extra.setDetailForm(null);
		action.setTargetForm(otherForm);
		
		assertTrue(form.containsForm(otherForm));
		
		action.setTargetForm(null);
		transformer.setTargetForm(otherForm);
		
		assertTrue(form.containsForm(otherForm));
	}
	
	@Test
	void testGetActionByType() {
		final Form form = new FormMetadata();
		final FormAction action = new FormAction();
		final FormActionType actionType = FormActionType.CUSTOM;
		form.addAction(action);
		
		assertNull(form.getActionByType(actionType));
		
		action.setType(actionType);
		
		assertSame(action, form.getActionByType(actionType));
	}
	
	@Test
	void testGetActionByUid() {
		final Form form = new FormMetadata();
		final FormAction action = new FormAction();
		form.addAction(action);
		action.setUid("other");
		
		assertNull(form.getActionByUid("test"));
		
		action.setUid("test");
		
		assertSame(action, form.getActionByUid("test"));
	}
	
	@Test
	void testGetActions() {
		final Form form = new FormMetadata();
		final FormAction actionList = new FormAction();
		final FormAction actionDetail = new FormAction();
		actionList.setType(FormActionType.DETAIL);
		actionDetail.setType(FormActionType.OVERVIEW);
		assertTrue(form.getActions(true).isEmpty());
		assertTrue(form.getActions(false).isEmpty());
		
		form.addAction(actionList);
		form.addAction(actionDetail);
		assertSame(1, form.getActions(true).size());
		assertSame(actionList, form.getActions(true).get(0));
		
		assertSame(1, form.getActions(false).size());
		assertSame(actionDetail, form.getActions(false).get(0));
	}
	
	@Test
	void testGetFieldById() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		form.addField(field);
		field.setId(987L);
		
		assertNull(form.getFieldById(123L));
		
		field.setId(123L);
		
		assertSame(field, form.getFieldById(123L));
	}
	
	@Test
	void testGetFieldByUid() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		form.addField(field);
		field.setUid("other");
		
		assertNull(form.getFieldByUid("test"));
		
		field.setUid("test");
		
		assertSame(field, form.getFieldByUid("test"));
	}
	
	@Test
	void testGetFieldExtra() {
		final Form form = new FormMetadata();
		final FormFieldExtra extra = new FormFieldExtra();
		final EntityField entityField = new EntityField();
		form.addFieldExtra(extra);
		
		assertNull(form.getFieldExtra(entityField));
		
		extra.setEntityField(entityField);
		
		assertSame(extra, form.getFieldExtra(entityField));
	}
	
	@Test
	void testGetFieldExtraByUid() {
		final Form form = new FormMetadata();
		final FormFieldExtra extra = new FormFieldExtra();
		form.addFieldExtra(extra);
		extra.setUid("other");
		
		assertNull(form.getFieldExtraByUid("test"));
		
		extra.setUid("test");
		
		assertSame(extra, form.getFieldExtraByUid("test"));
	}
	
	@Test
	void testGetPrintoutByUid() {
		final Form form = new FormMetadata();
		final FormPrintout printout = new FormPrintout();
		form.addPrintout(printout);
		printout.setUid("other");
		
		assertNull(form.getPrintoutByUid("test"));
		
		printout.setUid("test");
		
		assertSame(printout, form.getPrintoutByUid("test"));
	}
	
	@Test
	void testGetRelationFormByUid() {
		final Form form = new FormMetadata();
		final Entity entity = new EntityMetadata();
		final EntityRelation relation = new EntityRelation();
		((FormMetadata) form).setEntity(entity);
		entity.addRelation(relation);
		relation.setUid("other");
		
		assertThrows(IllegalStateException.class, () -> {
			form.getRelationFormByUid("test");
		});
		
		relation.setUid("test");
		
		final RelationForm relForm = form.getRelationFormByUid("test");
		assertNotNull(relForm);
		assertSame(relation, relForm.getRelation());
	}
	
	@Test
	void testGetSelectedFields() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		final FormField selectedField = new FormField();
		selectedField.setSelected(true);
		form.addField(field);
		form.addField(selectedField);
		
		assertNotNull(form.getSelectedFields(false));
		assertSame(1, form.getSelectedFields(false).size());
		assertSame(field, form.getSelectedFields(false).get(0));
		
		assertNotNull(form.getSelectedFields(true));
		assertSame(1, form.getSelectedFields(true).size());
		assertSame(selectedField, form.getSelectedFields(true).get(0));
	}
	
	@Test
	void testGetSubFormByEntityId() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		final NestedEntity nested = new NestedEntity();
		final Entity nestedEntity = new EntityMetadata();
		form.addSubForm(subForm);
		subForm.setNestedEntity(nested);
		nested.setNestedEntity(nestedEntity);
		((EntityMetadata) nestedEntity).setId(987L);
		
		assertNull(form.getSubFormByEntityId(123L));
		
		((EntityMetadata) nestedEntity).setId(123L);
		
		assertSame(subForm, form.getSubFormByEntityId(123L));
	}
	
	@Test
	void testGetSubFormByNestedEntityId() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		final NestedEntity nested = new NestedEntity();
		final Entity nestedEntity = new EntityMetadata();
		form.addSubForm(subForm);
		subForm.setNestedEntity(nested);
		nested.setNestedEntity(nestedEntity);
		nested.setId(987L);
		
		assertNull(form.getSubFormByNestedEntityId(123L));
		
		nested.setId(123L);
		
		assertSame(subForm, form.getSubFormByNestedEntityId(123L));
	}
	
	@Test
	void testGetSubFormByNestedEntityUid() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		final NestedEntity nested = new NestedEntity();
		final Entity nestedEntity = new EntityMetadata();
		form.addSubForm(subForm);
		subForm.setNestedEntity(nested);
		nested.setNestedEntity(nestedEntity);
		nested.setUid("other");
		
		assertNull(form.getSubFormByNestedEntityUid("test"));
		
		nested.setUid("test");
		
		assertSame(subForm, form.getSubFormByNestedEntityUid("test"));
	}
	
	@Test
	void testGetSubFormByUid() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		form.addSubForm(subForm);
		subForm.setUid("other");
		
		assertNull(form.getSubFormByUid("test"));
		
		subForm.setUid("test");
		
		assertSame(subForm, form.getSubFormByUid("test"));
	}
	
	@Test
	void testGetSubFormField() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		final SubFormField subFormField = new SubFormField();
		final EntityField entityField = new EntityField();
		form.addSubForm(subForm);
		subForm.addField(subFormField);
		
		assertNull(form.getSubFormField(entityField));
		
		subFormField.setEntityField(entityField);
		
		assertSame(subFormField, form.getSubFormField(entityField));
	}
	
	@Test
	void testGetTransformerByUid() {
		final Form form = new FormMetadata();
		final FormTransformer transformer = new FormTransformer();
		form.addTransformer(transformer);
		transformer.setUid("other");
		
		assertNull(form.getTransformerByUid("test"));
		
		transformer.setUid("test");
		
		assertSame(transformer, form.getTransformerByUid("test"));
	}
	
	@Test
	void testIsActionEnabled() {
		final Form form = new FormMetadata();
		final FormAction action = new FormAction();
		final Entity entity = new EntityMetadata();
		final EntityPermission permission = new EntityPermission();
		final User user = new UserMetadata();
		final UserGroup group = new UserGroupMetadata();
		final Set<UserGroupMetadata> groups = new HashSet<>();
		groups.add((UserGroupMetadata) group);
		((UserMetadata) user).setUserGroups(groups);
		((FormMetadata) form).setEntity(entity);
		permission.setUserGroup(group);
		entity.addPermission(permission);
		permission.setAccess(EntityAccess.READ);
		action.setType(FormActionType.DETAIL);
		assertTrue(form.isActionEnabled(action, user));
		
		action.setType(FormActionType.SAVE);
		assertFalse(form.isActionEnabled(action, user));
		
		permission.setAccess(EntityAccess.WRITE);
		assertTrue(form.isActionEnabled(action, user));
		
		action.setType(FormActionType.NEWOBJECT);
		assertFalse(form.isActionEnabled(action, user));
		
		permission.setAccess(EntityAccess.CREATE);
		assertTrue(form.isActionEnabled(action, user));
		
		action.setType(FormActionType.DELETE);
		assertFalse(form.isActionEnabled(action, user));
		
		permission.setAccess(EntityAccess.DELETE);
		assertTrue(form.isActionEnabled(action, user));
	}
	
	@Test
	void testIsActionEnabledSubForm() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		final SubFormAction action = new SubFormAction();
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		final EntityPermission permission = new EntityPermission();
		final User user = new UserMetadata();
		final UserGroup group = new UserGroupMetadata();
		final Set<UserGroupMetadata> groups = new HashSet<>();
		groups.add((UserGroupMetadata) group);
		((UserMetadata) user).setUserGroups(groups);
		form.addSubForm(subForm);
		permission.setUserGroup(group);
		nestedEntity.addPermission(permission);
		nested.setNestedEntity(nestedEntity);
		subForm.setNestedEntity(nested);
		subForm.addAction(action);
		permission.setAccess(EntityAccess.READ);
		action.setType(FormActionType.NEWOBJECT);
		
		assertFalse(form.isActionEnabled(action, user));
		
		permission.setAccess(EntityAccess.CREATE);
		assertTrue(form.isActionEnabled(action, user));
		
		action.setType(FormActionType.DELETE);
		assertFalse(form.isActionEnabled(action, user));
		
		permission.setAccess(EntityAccess.DELETE);
		assertTrue(form.isActionEnabled(action, user));
	}
	
	@Test
	void testIsEqual() {
		final Form form1 = new FormMetadata();
		final Form form2 = new FormMetadata();
		final FormLayout layout = new FormLayout();
		assertTrue(form1.isEqual(form2));
		
		form1.setName("test");
		((FormMetadata) form1).setFilterUid("filter");
		((FormMetadata) form1).setAutoLayout(true);
		((FormMetadata) form1).setAutolayoutType(AutolayoutType.TWO_COLUMNS_VERTICAL);
		((FormMetadata) form1).setLayout(layout);
		assertFalse(form1.isEqual(form2));
		
		form2.setName("test");
		((FormMetadata) form2).setFilterUid("filter");
		((FormMetadata) form2).setAutoLayout(true);
		((FormMetadata) form2).setAutolayoutType(AutolayoutType.TWO_COLUMNS_VERTICAL);
		((FormMetadata) form2).setLayout(layout);
		assertTrue(form1.isEqual(form2));
	}
	
	@Test
	void testIsEqualFields() {
		final Form form1 = new FormMetadata();
		final Form form2 = new FormMetadata();
		final FormField field1 = new FormField();
		final FormField field2 = new FormField();
		field1.setUid("test");
		field2.setUid("test");
		form1.addField(field1);
		assertFalse(form1.isEqual(form2));
		
		form2.addField(field2);
		assertTrue(form1.isEqual(form2));
		
		field2.setUid("other");
		assertFalse(form1.isEqual(form2));
	}
	
	@Test
	void testIsEqualFieldExtras() {
		final Form form1 = new FormMetadata();
		final Form form2 = new FormMetadata();
		final FormFieldExtra extra1 = new FormFieldExtra();
		final FormFieldExtra extra2 = new FormFieldExtra();
		extra1.setUid("test");
		extra2.setUid("test");
		form1.addFieldExtra(extra1);
		assertFalse(form1.isEqual(form2));
		
		form2.addFieldExtra(extra2);
		assertTrue(form1.isEqual(form2));
		
		extra2.setUid("other");
		assertFalse(form1.isEqual(form2));
	}
	
	@Test
	void testIsEqualActions() {
		final Form form1 = new FormMetadata();
		final Form form2 = new FormMetadata();
		final FormAction action1 = new FormAction();
		final FormAction action2 = new FormAction();
		action1.setUid("test");
		action2.setUid("test");
		form1.addAction(action1);
		assertFalse(form1.isEqual(form2));
		
		form2.addAction(action2);
		assertTrue(form1.isEqual(form2));
		
		action2.setUid("other");
		assertFalse(form1.isEqual(form2));
	}
	
	@Test
	void testIsEqualTransformers() {
		final Form form1 = new FormMetadata();
		final Form form2 = new FormMetadata();
		final FormTransformer transformer1 = new FormTransformer();
		final FormTransformer transformer2 = new FormTransformer();
		transformer1.setUid("test");
		transformer2.setUid("test");
		form1.addTransformer(transformer1);
		assertFalse(form1.isEqual(form2));
		
		form2.addTransformer(transformer2);
		assertTrue(form1.isEqual(form2));
		
		transformer2.setUid("other");
		assertFalse(form1.isEqual(form2));
	}
	
	@Test
	void testIsEqualPrintouts() {
		final Form form1 = new FormMetadata();
		final Form form2 = new FormMetadata();
		final FormPrintout printout1 = new FormPrintout();
		final FormPrintout printout2 = new FormPrintout();
		printout1.setUid("test");
		printout2.setUid("test");
		form1.addPrintout(printout1);
		assertFalse(form1.isEqual(form2));
		
		form2.addPrintout(printout2);
		assertTrue(form1.isEqual(form2));
		
		printout2.setUid("other");
		assertFalse(form1.isEqual(form2));
	}
	
	@Test
	void testIsEqualSubForms() {
		final Form form1 = new FormMetadata();
		final Form form2 = new FormMetadata();
		final SubForm subForm1 = new SubForm();
		final SubForm subForm2 = new SubForm();
		subForm1.setUid("test");
		subForm2.setUid("test");
		form1.addSubForm(subForm1);
		assertFalse(form1.isEqual(form2));
		
		form2.addSubForm(subForm2);
		assertTrue(form1.isEqual(form2));
		
		subForm2.setUid("other");
		assertFalse(form1.isEqual(form2));
	}
	
	@Test
	void testIsFieldMandatory() {
		final Form form = new FormMetadata();
		final EntityField field = new EntityField();
		field.setType(FieldType.TEXT);
		assertFalse(form.isFieldMandatory(field));
		
		field.setMandatory(true);
		assertTrue(form.isFieldMandatory(field));
		
		field.setType(FieldType.AUTONUM);
		assertFalse(form.isFieldMandatory(field));
	}
	
	@Test
	void testIsFieldReadonly() {
		final Form form = new FormMetadata();
		final FormFieldExtra fieldExtra = new FormFieldExtra();
		final SubForm subForm = new SubForm();
		final SubFormField subField = new SubFormField();
		final Entity entity = new EntityMetadata();
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		final EntityField field = new EntityField();
		final EntityPermission permission = new EntityPermission();
		final User user = new UserMetadata();
		final UserGroup group = new UserGroupMetadata();
		final Set<UserGroupMetadata> groups = new HashSet<>();
		groups.add((UserGroupMetadata) group);
		((UserMetadata) user).setUserGroups(groups);
		((FormMetadata) form).setEntity(entity);
		fieldExtra.setEntityField(field);
		form.addFieldExtra(fieldExtra);
		subForm.setNestedEntity(nested);
		subForm.addField(subField);
		subField.setEntityField(field);
		permission.setUserGroup(group);
		entity.addField(field);
		entity.addPermission(permission);
		entity.addNested(nested);
		nestedEntity.addPermission(permission);
		nested.setNestedEntity(nestedEntity);
		field.setType(FieldType.AUTONUM);
		assertTrue(form.isFieldReadonly(field, null, user));
		
		field.setType(FieldType.TEXT);
		field.setCalculated(true);
		assertTrue(form.isFieldReadonly(field, null, user));
		
		field.setCalculated(false);
		permission.setAccess(EntityAccess.READ);
		assertTrue(form.isFieldReadonly(field, null, user));
		
		permission.setAccess(EntityAccess.WRITE);
		assertFalse(form.isFieldReadonly(field, null, user));
		
		fieldExtra.setReadonly(true);
		assertTrue(form.isFieldReadonly(field, null, user));
		
		fieldExtra.setReadonly(false);
		form.addSubForm(subForm);
		assertFalse(form.isFieldReadonly(field, null, user));
		
		permission.setAccess(EntityAccess.READ);
		assertTrue(form.isFieldReadonly(field, null, user));
		
		permission.setAccess(EntityAccess.WRITE);
		assertFalse(form.isFieldReadonly(field, null, user));
		
		subField.setReadonly(true);
		assertTrue(form.isFieldReadonly(field, null, user));
	}
	
	@Test
	void testIsFieldVisible() {
		final Form form = new FormMetadata();
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		final EntityFieldConstraint constraint = new EntityFieldConstraint();
		final EntityPermission permission = new EntityPermission();
		final User user = new UserMetadata();
		final UserGroup group = new UserGroupMetadata();
		final Set<UserGroupMetadata> groups = new HashSet<>();
		((UserMetadata) user).setUserGroups(groups);
		constraint.setUserGroup(group);
		constraint.setField(field);
		constraint.setAccess(FieldAccess.NONE);
		permission.setUserGroup(group);
		permission.setAccess(EntityAccess.READ);
		groups.add((UserGroupMetadata) group);
		entity.addField(field);
		entity.addPermission(permission);
		((FormMetadata) form).setEntity(entity);
		assertTrue(form.isFieldVisible(field, null, user));
		
		entity.addFieldConstraint(constraint);
		assertFalse(form.isFieldVisible(field, null, user));
		
		constraint.setAccess(FieldAccess.READ);
		assertTrue(form.isFieldVisible(field, null, user));
	}
	
	@Test
	void testIsRelationFormVisible() {
		final Form form = new FormMetadata();
		final Entity entity = new EntityMetadata();
		final Entity relatedEntity = new EntityMetadata();
		final EntityPermission permission = new EntityPermission();
		final EntityRelation relation = new EntityRelation();
		final User user = new UserMetadata();
		final UserGroup group = new UserGroupMetadata();
		final UserGroup otherGroup = new UserGroupMetadata();
		final Set<UserGroupMetadata> groups = new HashSet<>();
		groups.add((UserGroupMetadata) group);
		((UserMetadata) user).setUserGroups(groups);
		((FormMetadata) form).setEntity(entity);
		entity.addRelation(relation);
		relation.setRelatedEntity(relatedEntity);
		relation.setUid("test");
		permission.setUserGroup(otherGroup);
		permission.setAccess(EntityAccess.READ);
		assertTrue(form.isRelationFormVisible("test", user));
		
		relatedEntity.addPermission(permission);
		assertFalse(form.isRelationFormVisible("test", user));
		
		permission.setUserGroup(group);
		assertTrue(form.isRelationFormVisible("test", user));
	}
	
	@Test
	void testIsSubFormVisible() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		final Entity nestedEntity = new EntityMetadata();
		final EntityPermission permission = new EntityPermission();
		final NestedEntity nested = new NestedEntity();
		final User user = new UserMetadata();
		final UserGroup group = new UserGroupMetadata();
		final UserGroup otherGroup = new UserGroupMetadata();
		final Set<UserGroupMetadata> groups = new HashSet<>();
		groups.add((UserGroupMetadata) group);
		((UserMetadata) user).setUserGroups(groups);
		permission.setUserGroup(otherGroup);
		permission.setAccess(EntityAccess.READ);
		nested.setNestedEntity(nestedEntity);
		nested.setUid("test");
		form.addSubForm(subForm);
		subForm.setNestedEntity(nested);
		assertTrue(form.isSubFormVisible("test", user));
		
		nestedEntity.addPermission(permission);
		assertFalse(form.isSubFormVisible("test", user));
		
		permission.setUserGroup(group);
		assertTrue(form.isSubFormVisible("test", user));
	}
	
	@Test
	void testRemoveAction() {
		final Form form = new FormMetadata();
		final FormAction action = new FormAction();
		form.addAction(action);
		assertSame(1, form.getActions().size());
		
		form.removeAction(action);
		
		assertFalse(form.hasActions());
		assertSame(0, form.getActions().size());
	}
	
	@Test
	void testRemoveField() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		form.addField(field);
		assertSame(1, form.getFields().size());
		
		form.removeField(field);
		
		assertFalse(form.hasFields());
		assertSame(0, form.getFields().size());
	}
	
	@Test
	void testRemoveFieldExtra() {
		final Form form = new FormMetadata();
		final FormFieldExtra fieldExtra = new FormFieldExtra();
		form.addFieldExtra(fieldExtra);
		assertSame(1, form.getFieldExtras().size());
		
		form.removeFieldExtra(fieldExtra);
		
		assertFalse(form.hasFieldExtras());
		assertSame(0, form.getFieldExtras().size());
	}
	
	@Test
	void testRemovePrintout() {
		final Form form = new FormMetadata();
		final FormPrintout printout = new FormPrintout();
		form.addPrintout(printout);
		assertSame(1, form.getPrintouts().size());
		
		form.removePrintout(printout);
		
		assertFalse(form.hasPrintouts());
		assertSame(0, form.getPrintouts().size());
	}
	
	@Test
	void testRemoveRelationForm() {
		final Form form = new FormMetadata();
		final Entity entity = new EntityMetadata();
		final EntityRelation relation = new EntityRelation();
		((FormMetadata) form).setEntity(entity);
		entity.addRelation(relation);
		relation.setUid("test");
		final RelationForm relForm = form.getRelationFormByUid("test");
		assertNotNull(relForm);
		
		form.removeRelationForm(relForm);
		
		assertFalse(form.hasRelationForms());
	}
	
	@Test
	void testRemoveSubForm() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		form.addSubForm(subForm);
		assertSame(1, form.getSubForms().size());
		
		form.removeSubForm(subForm);
		
		assertFalse(form.hasSubForms());
		assertSame(0, form.getSubForms().size());
	}
	
	@Test
	void testRemoveTransformer() {
		final Form form = new FormMetadata();
		final FormTransformer transformer = new FormTransformer();
		form.addTransformer(transformer);
		assertSame(1, form.getTransformers().size());
		
		form.removeTransformer(transformer);
		
		assertFalse(form.hasTransformers());
		assertSame(0, form.getTransformers().size());
	}
}
