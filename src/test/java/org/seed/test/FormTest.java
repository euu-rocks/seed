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
import org.seed.core.form.Form;
import org.seed.core.form.FormAction;
import org.seed.core.form.FormActionType;
import org.seed.core.form.FormField;
import org.seed.core.form.FormFieldExtra;
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

public class FormTest {
	
	@Test
	public void testAddAction() {
		final Form form = new FormMetadata();
		final FormAction action = new FormAction();
		assertFalse(form.hasActions());
		
		form.addAction(action);
		
		assertSame(action.getForm(), form);
		
		assertTrue(form.hasActions());
		assertSame(form.getActions().size(), 1);
		assertSame(form.getActions().get(0), action);
	}
	
	@Test
	public void testAddField() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		assertFalse(form.hasFields());
		
		form.addField(field);
		
		assertSame(field.getForm(), form);
		
		assertTrue(form.hasFields());
		assertSame(form.getFields().size(), 1);
		assertSame(form.getFields().get(0), field);
	}
	
	@Test
	public void testAddFieldExtra() {
		final Form form = new FormMetadata();
		final FormFieldExtra fieldExtra = new FormFieldExtra();
		assertFalse(form.hasFieldExtras());
		
		form.addFieldExtra(fieldExtra);
		
		assertSame(fieldExtra.getForm(), form);
		
		assertTrue(form.hasFieldExtras());
		assertSame(form.getFieldExtras().size(), 1);
		assertSame(form.getFieldExtras().get(0), fieldExtra);
	}
	
	@Test
	public void testAddPrintout() {
		final Form form = new FormMetadata();
		final FormPrintout printout = new FormPrintout();
		assertFalse(form.hasPrintouts());
		
		form.addPrintout(printout);
		
		assertSame(printout.getForm(), form);
		
		assertTrue(form.hasPrintouts());
		assertSame(form.getPrintouts().size(), 1);
		assertSame(form.getPrintouts().get(0), printout);
	}
	
	@Test
	public void testAddSubForm() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		assertFalse(form.hasSubForms());
		
		form.addSubForm(subForm);
		
		assertSame(subForm.getForm(), form);
		
		assertTrue(form.hasSubForms());
		assertSame(form.getSubForms().size(), 1);
		assertSame(form.getSubForms().get(0), subForm);
	}
	
	@Test
	public void testAddTransformer() {
		final Form form = new FormMetadata();
		final FormTransformer transformer = new FormTransformer();
		assertFalse(form.hasTransformers());
		
		form.addTransformer(transformer);
		
		assertSame(transformer.getForm(), form);
		
		assertTrue(form.hasTransformers());
		assertSame(form.getTransformers().size(), 1);
		assertSame(form.getTransformers().get(0), transformer);
	}
	
	@Test
	public void testContainsEntityField() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		final EntityField entityField = new EntityField();
		form.addField(field);
		assertFalse(form.containsEntityField(entityField));
		
		field.setEntityField(entityField);
		
		assertTrue(form.containsEntityField(entityField));
	}
	
	@Test
	public void testContainsEntityFunction() {
		final Form form = new FormMetadata();
		final FormAction action = new FormAction();
		final EntityFunction function = new EntityFunction();
		form.addAction(action);
		assertFalse(form.containsEntityFunction(function));
		
		action.setEntityFunction(function);
		
		assertTrue(form.containsEntityFunction(function));
	}
	
	@Test
	public void testContainsFilter() {
		final Form form = new FormMetadata();
		final FormFieldExtra fieldExtra = new FormFieldExtra();
		final Filter filter = new FilterMetadata();
		form.addFieldExtra(fieldExtra);
		assertFalse(form.containsFilter(filter));
		
		fieldExtra.setFilter(filter);
		
		assertTrue(form.containsFilter(filter));
	}
	
	@Test
	public void testContainsSystemField() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		final SystemField systemField = SystemField.CREATEDON;
		form.addField(field);
		assertFalse(form.containsSystemField(systemField));
		
		field.setSystemField(systemField);
		
		assertTrue(form.containsSystemField(systemField));
	}
	
	@Test
	public void testContainsTransformer() {
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
	public void testContainsForm() {
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
	public void testGetActionByType() {
		final Form form = new FormMetadata();
		final FormAction action = new FormAction();
		final FormActionType actionType = FormActionType.CUSTOM;
		form.addAction(action);
		
		assertNull(form.getActionByType(actionType));
		
		action.setType(actionType);
		
		assertSame(form.getActionByType(actionType), action);
	}
	
	@Test
	public void testGetActionByUid() {
		final Form form = new FormMetadata();
		final FormAction action = new FormAction();
		form.addAction(action);
		action.setUid("other");
		
		assertNull(form.getActionByUid("test"));
		
		action.setUid("test");
		
		assertSame(form.getActionByUid("test"), action);
	}
	
	@Test
	public void testGetFieldById() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		form.addField(field);
		field.setId(987L);
		
		assertNull(form.getFieldById(123L));
		
		field.setId(123L);
		
		assertSame(form.getFieldById(123L), field);
	}
	
	@Test
	public void testGetFieldByUid() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		form.addField(field);
		field.setUid("other");
		
		assertNull(form.getFieldByUid("test"));
		
		field.setUid("test");
		
		assertSame(form.getFieldByUid("test"), field);
	}
	
	@Test
	public void testGetFieldExtra() {
		final Form form = new FormMetadata();
		final FormFieldExtra extra = new FormFieldExtra();
		final EntityField entityField = new EntityField();
		form.addFieldExtra(extra);
		
		assertNull(form.getFieldExtra(entityField));
		
		extra.setEntityField(entityField);
		
		assertSame(form.getFieldExtra(entityField), extra);
	}
	
	@Test
	public void testGetFieldExtraByUid() {
		final Form form = new FormMetadata();
		final FormFieldExtra extra = new FormFieldExtra();
		form.addFieldExtra(extra);
		extra.setUid("other");
		
		assertNull(form.getFieldExtraByUid("test"));
		
		extra.setUid("test");
		
		assertSame(form.getFieldExtraByUid("test"), extra);
	}
	
	@Test
	public void testGetPrintoutByUid() {
		final Form form = new FormMetadata();
		final FormPrintout printout = new FormPrintout();
		form.addPrintout(printout);
		printout.setUid("other");
		
		assertNull(form.getPrintoutByUid("test"));
		
		printout.setUid("test");
		
		assertSame(form.getPrintoutByUid("test"), printout);
	}
	
	@Test
	public void testGetRelationFormByUid() {
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
		assertSame(relForm.getRelation(), relation);
	}
	
	@Test
	public void testGetSelectedFields() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		final FormField selectedField = new FormField();
		selectedField.setSelected(true);
		form.addField(field);
		form.addField(selectedField);
		
		assertNotNull(form.getSelectedFields(false));
		assertSame(form.getSelectedFields(false).size(), 1);
		assertSame(form.getSelectedFields(false).get(0), field);
		
		assertNotNull(form.getSelectedFields(true));
		assertSame(form.getSelectedFields(true).size(), 1);
		assertSame(form.getSelectedFields(true).get(0), selectedField);
	}
	
	@Test
	public void testGetSubFormByEntityId() {
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
		
		assertSame(form.getSubFormByEntityId(123L), subForm);
	}
	
	@Test
	public void testGetSubFormByNestedEntityId() {
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
		
		assertSame(form.getSubFormByNestedEntityId(123L), subForm);
	}
	
	@Test
	public void testGetSubFormByNestedEntityUid() {
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
		
		assertSame(form.getSubFormByNestedEntityUid("test"), subForm);
	}
	
	@Test
	public void testGetSubFormByUid() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		form.addSubForm(subForm);
		subForm.setUid("other");
		
		assertNull(form.getSubFormByUid("test"));
		
		subForm.setUid("test");
		
		assertSame(form.getSubFormByUid("test"), subForm);
	}
	
	@Test
	public void testGetSubFormField() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		final SubFormField subFormField = new SubFormField();
		final EntityField entityField = new EntityField();
		form.addSubForm(subForm);
		subForm.addField(subFormField);
		
		assertNull(form.getSubFormField(entityField));
		
		subFormField.setEntityField(entityField);
		
		assertSame(form.getSubFormField(entityField), subFormField);
	}
	
	@Test
	public void testGetTransformerByUid() {
		final Form form = new FormMetadata();
		final FormTransformer transformer = new FormTransformer();
		form.addTransformer(transformer);
		transformer.setUid("other");
		
		assertNull(form.getTransformerByUid("test"));
		
		transformer.setUid("test");
		
		assertSame(form.getTransformerByUid("test"), transformer);
	}
	
	@Test
	public void testIsActionEnabled() {
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
	public void testIsActionEnabledSubForm() {
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
	public void testIsFieldMandatory() {
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
	public void testIsFieldReadonly() {
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
	public void testIsFieldVisible() {
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
	public void testIsRelationFormVisible() {
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
	public void testIsSubFormVisible() {
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
	public void testRemoveAction() {
		final Form form = new FormMetadata();
		final FormAction action = new FormAction();
		form.addAction(action);
		assertSame(form.getActions().size(), 1);
		
		form.removeAction(action);
		
		assertFalse(form.hasActions());
		assertSame(form.getActions().size(), 0);
	}
	
	@Test
	public void testRemoveField() {
		final Form form = new FormMetadata();
		final FormField field = new FormField();
		form.addField(field);
		assertSame(form.getFields().size(), 1);
		
		form.removeField(field);
		
		assertFalse(form.hasFields());
		assertSame(form.getFields().size(), 0);
	}
	
	@Test
	public void testRemoveFieldExtra() {
		final Form form = new FormMetadata();
		final FormFieldExtra fieldExtra = new FormFieldExtra();
		form.addFieldExtra(fieldExtra);
		assertSame(form.getFieldExtras().size(), 1);
		
		form.removeFieldExtra(fieldExtra);
		
		assertFalse(form.hasFieldExtras());
		assertSame(form.getFieldExtras().size(), 0);
	}
	
	@Test
	public void testRemovePrintout() {
		final Form form = new FormMetadata();
		final FormPrintout printout = new FormPrintout();
		form.addPrintout(printout);
		assertSame(form.getPrintouts().size(), 1);
		
		form.removePrintout(printout);
		
		assertFalse(form.hasPrintouts());
		assertSame(form.getPrintouts().size(), 0);
	}
	
	@Test
	public void testRemoveRelationForm() {
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
	public void testRemoveSubForm() {
		final Form form = new FormMetadata();
		final SubForm subForm = new SubForm();
		form.addSubForm(subForm);
		assertSame(form.getSubForms().size(), 1);
		
		form.removeSubForm(subForm);
		
		assertFalse(form.hasSubForms());
		assertSame(form.getSubForms().size(), 0);
	}
	
	@Test
	public void testRemoveTransformer() {
		final Form form = new FormMetadata();
		final FormTransformer transformer = new FormTransformer();
		form.addTransformer(transformer);
		assertSame(form.getTransformers().size(), 1);
		
		form.removeTransformer(transformer);
		
		assertFalse(form.hasTransformers());
		assertSame(form.getTransformers().size(), 0);
	}
}
