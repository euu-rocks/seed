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
package org.seed.test.unit.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.seed.core.data.FieldAccess;
import org.seed.core.data.FieldType;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldConstraint;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityMetadata;
import org.seed.core.entity.EntityPermission;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.EntityStatusTransition;
import org.seed.core.entity.NestedEntity;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupMetadata;
import org.seed.core.user.UserMetadata;

class EntityTest {
	
	@Test
	void testAddField() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		assertFalse(entity.hasFields());
		
		entity.addField(field);
		
		assertEquals(entity, field.getEntity());
		
		assertTrue(entity.hasFields());
		assertSame(1, entity.getFields().size());
		assertSame(field, entity.getFields().get(0));
		
		assertTrue(entity.hasAllFields());
		assertSame(1, entity.getAllFields().size());
		assertSame(field, entity.getAllFields().get(0));
	}
	
	@Test
	void testAddFieldConstraint() {
		final Entity entity = new EntityMetadata();
		final EntityFieldConstraint constraint = new EntityFieldConstraint();
		assertFalse(entity.hasFieldConstraints());
		
		entity.addFieldConstraint(constraint);
		
		assertEquals(entity, constraint.getEntity());
		assertTrue(entity.hasFieldConstraints());
		assertSame(1, entity.getFieldConstraints().size());
		assertSame(constraint, entity.getFieldConstraints().get(0));
	}
	
	@Test
	void testAddFieldGroup() {
		final Entity entity = new EntityMetadata();
		final EntityFieldGroup fieldGroup = new EntityFieldGroup();
		assertFalse(entity.hasFieldGroups());
		assertFalse(entity.hasAllFieldGroups());
		
		entity.addFieldGroup(fieldGroup);
		
		assertEquals(entity, fieldGroup.getEntity());
		assertTrue(entity.hasFieldGroups());
		assertSame(1, entity.getFieldGroups().size());
		assertSame(fieldGroup, entity.getFieldGroups().get(0));
		
		assertTrue(entity.hasAllFieldGroups());
		assertSame(1, entity.getAllFieldGroups().size());
		assertSame(fieldGroup, entity.getAllFieldGroups().get(0));
	}
	
	@Test
	void testAddFunction() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		assertFalse(entity.hasFunctions());
		assertFalse(entity.hasAllFunctions());
		
		entity.addFunction(function);
		
		assertEquals(entity, function.getEntity());
		assertTrue(entity.hasFunctions());
		assertSame(1, entity.getFunctions().size());
		assertSame(function, entity.getFunctions().get(0));
		
		assertTrue(entity.hasAllFunctions());
		assertSame(1, entity.getAllFunctions().size());
		assertSame(function, entity.getAllFunctions().get(0));
	}
	
	@Test
	void testAddNested() {
		final Entity entity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		assertFalse(entity.hasNesteds());
		
		entity.addNested(nested);
		
		assertEquals(entity, nested.getParentEntity());
		assertTrue(entity.hasNesteds());
		assertSame(1, entity.getNesteds().size());
		assertSame(nested, entity.getNesteds().get(0));
	}
	
	@Test
	void testAddPermission() {
		final Entity entity = new EntityMetadata();
		final EntityPermission permission = new EntityPermission();
		assertFalse(entity.hasPermissions());
		
		entity.addPermission(permission);
		
		assertEquals(entity, permission.getEntity());
		assertTrue(entity.hasPermissions());
		assertSame(1, entity.getPermissions().size());
		assertSame(permission, entity.getPermissions().get(0));
	}
	
	@Test
	void testAddRelation() {
		final Entity entity = new EntityMetadata();
		final EntityRelation relation = new EntityRelation();
		assertFalse(entity.hasRelations());
		assertFalse(entity.hasAllRelations());
		
		entity.addRelation(relation);
		
		assertEquals(entity, relation.getEntity());
		assertTrue(entity.hasRelations());
		assertSame(1, entity.getRelations().size());
		assertSame(relation, entity.getRelations().get(0));
		
		assertTrue(entity.hasAllRelations());
		assertSame(1, entity.getAllRelations().size());
		assertSame(relation, entity.getAllRelations().get(0));
	}
	
	@Test
	void testAddStatus() {
		final Entity entity = new EntityMetadata();
		final EntityStatus status = new EntityStatus();
		assertFalse(entity.hasStatus());
		
		entity.addStatus(status);
		
		assertEquals(entity, status.getEntity());
		assertTrue(entity.hasStatus());
		assertSame(1, entity.getStatusList().size());
		assertSame(status, entity.getStatusList().get(0));
	}
	
	@Test
	void testAddStatusTransition() {
		final Entity entity = new EntityMetadata();
		final EntityStatusTransition transition = new EntityStatusTransition();
		assertFalse(entity.hasStatusTransitions());
		
		entity.addStatusTransition(transition);
		
		assertEquals(entity, transition.getEntity());
		assertTrue(entity.hasStatusTransitions());
		assertSame(1, entity.getStatusTransitions().size());
		assertSame(transition, entity.getStatusTransitions().get(0));
	}
	
	@Test
	void testCheckFieldAccess() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		final EntityFieldConstraint constraint = new EntityFieldConstraint();
		final EntityStatus status = new EntityStatus();
		final User user = new UserMetadata();
		final UserGroup userGroup = new UserGroupMetadata();
		final Set<UserGroupMetadata> groups = new HashSet<>();
		groups.add((UserGroupMetadata) userGroup);
		((UserMetadata) user).setUserGroups(groups); 
		constraint.setField(field);
		constraint.setAccess(FieldAccess.READ);
		constraint.setStatus(status);
		
		// no constraint -> always permitted
		assertTrue(entity.checkFieldAccess(field, user, status, FieldAccess.WRITE));
		
		entity.addFieldConstraint(constraint);
		
		// check status constraints (no user group)
		assertTrue(entity.checkFieldAccess(field, user, status, FieldAccess.READ));
		assertFalse(entity.checkFieldAccess(field, user, status, FieldAccess.WRITE));
		
		constraint.setUserGroup(userGroup);
		
		// check user group constraints
		assertTrue(entity.checkFieldAccess(field, user, status, FieldAccess.READ));
		assertFalse(entity.checkFieldAccess(field, user, status, FieldAccess.WRITE));
	}
	
	@Test
	void testContainsField() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		
		assertFalse(entity.containsField(field));
		assertFalse(entity.containsAllField(field));
		
		entity.addField(field);
		
		assertTrue(entity.containsField(field));
		assertTrue(entity.containsAllField(field));
	}
	
	@Test
	void testContainsFieldGroup() {
		final Entity entity = new EntityMetadata();
		final EntityFieldGroup fieldGroup = new EntityFieldGroup();
		
		assertFalse(entity.containsFieldGroup(fieldGroup));
		
		entity.addFieldGroup(fieldGroup);
		
		assertTrue(entity.containsFieldGroup(fieldGroup));
	}
	
	@Test
	void testContainsNested() {
		final Entity entity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		
		assertFalse(entity.containsNested(nested));
		
		entity.addNested(nested);
		
		assertTrue(entity.containsNested(nested));
	}
	
	@Test
	void testFindAutonumField() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		field.setType(FieldType.AUTONUM);
		
		assertNull(entity.findAutonumField());
		
		entity.addField(field);
		
		assertSame(field, entity.findAutonumField());
	}
	
	@Test
	void testFindFieldByUid() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		field.setUid("other");
		entity.addField(field);
		
		assertNull(entity.findFieldByUid("test"));
		
		field.setUid("test");
		
		assertSame(field, entity.findFieldByUid("test"));
	}
	
	@Test
	void testFindDefaultIdentifierField() {
		final Entity entity = new EntityMetadata();
		final EntityField field1 = new EntityField();
		final EntityField field2 = new EntityField();
		entity.addField(field1);
		entity.addField(field2);
		field1.setType(FieldType.DATE);
		field2.setType(FieldType.BOOLEAN);
		assertNull(entity.findDefaultIdentifierField());
		
		field1.setType(FieldType.TEXT);
		assertSame(field1, entity.findDefaultIdentifierField());
		
		field2.setType(FieldType.AUTONUM);
		assertSame(field1, entity.findDefaultIdentifierField());
		
		field2.setUnique(true);
		assertSame(field2, entity.findDefaultIdentifierField());
	}
	
	@Test
	void testGetNestedByUid() {
		final Entity entity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		nested.setUid("other");
		entity.addNested(nested);
		
		assertNull(entity.getNestedByUid("test"));
		
		nested.setUid("test");
		
		assertSame(nested, entity.getNestedByUid("test"));
	}
	
	@Test
	void testGetParentEntity() {
		final EntityMetadata entity = new EntityMetadata();
		final Entity parent = new EntityMetadata();
		assertNull(entity.getParentEntity());
		
		entity.setParentEntity(parent);
		assertSame(parent, entity.getParentEntity());
	}
	
	@Test
	void testGetPermissionByUid() {
		final Entity entity = new EntityMetadata();
		final EntityPermission permission = new EntityPermission();
		permission.setUid("other");
		entity.addPermission(permission);
		
		assertNull(entity.getPermissionByUid("test"));
		
		permission.setUid("test");
		
		assertSame(permission, entity.getPermissionByUid("test"));
	}
	
	@Test
	void testGetRelationByUid() {
		final Entity entity = new EntityMetadata();
		final EntityRelation relation = new EntityRelation();
		relation.setUid("other");
		entity.addRelation(relation);
		
		assertNull(entity.getRelationByUid("test"));
		
		relation.setUid("test");
		
		assertSame(relation, entity.getRelationByUid("test"));
	}
	
	@Test
	void testGetReferenceFields() {
		final Entity entity = new EntityMetadata();
		final Entity referenceEntity = new EntityMetadata();
		final EntityField textField = new EntityField();
		final EntityField referenceField = new EntityField();
		textField.setType(FieldType.TEXT);
		referenceField.setType(FieldType.REFERENCE);
		referenceField.setReferenceEntity(referenceEntity);
		entity.addField(textField);
		
		assertSame(0, entity.getReferenceFields(referenceEntity).size());
		
		entity.addField(referenceField);
		
		assertSame(1, entity.getReferenceFields(referenceEntity).size());
		assertSame(referenceField, entity.getReferenceFields(referenceEntity).get(0));
	}
	
	@Test
	void testGenericEntity() {
		final Entity genericEntity = new EntityMetadata();
		final Entity entity = new EntityMetadata();
		((EntityMetadata) genericEntity).setGeneric(true);
		
		assertTrue(genericEntity.isGeneric());
		assertFalse(entity.isGeneric());
		assertNull(entity.getGenericEntity());
		
		((EntityMetadata) entity).setGenericEntity(genericEntity);
		
		assertSame(genericEntity, entity.getGenericEntity());
	}
	
	@Test
	void testGetAllFields() {
		final Entity entity = new EntityMetadata();
		final Entity genericEntity = new EntityMetadata();
		final EntityField entityField = new EntityField();
		final EntityField genericField = new EntityField();
		assertTrue(entity.getAllFields().isEmpty());
		
		entity.addField(entityField);
		assertSame(1, entity.getAllFields().size());
		assertSame(entityField, entity.getAllFields().get(0));
		
		genericEntity.addField(genericField);
		((EntityMetadata) entity).setGenericEntity(genericEntity);
		assertSame(2, entity.getAllFields().size());
		assertSame(genericField, entity.getAllFields().get(0));
	}
	
	@Test
	void testGetAllFieldGroups() {
		final Entity entity = new EntityMetadata();
		final Entity genericEntity = new EntityMetadata();
		final EntityFieldGroup groupEntity = new EntityFieldGroup();
		final EntityFieldGroup groupGeneric = new EntityFieldGroup();
		assertTrue(entity.getAllFieldGroups().isEmpty());
		
		entity.addFieldGroup(groupEntity);
		assertSame(1, entity.getAllFieldGroups().size());
		assertSame(groupEntity, entity.getAllFieldGroups().get(0));
		
		genericEntity.addFieldGroup(groupGeneric);
		((EntityMetadata) entity).setGenericEntity(genericEntity);
		assertSame(2, entity.getAllFieldGroups().size());
		assertSame(groupGeneric, entity.getAllFieldGroups().get(0));
	}
	
	@Test
	void testGetAllFunctions() {
		final Entity entity = new EntityMetadata();
		final Entity genericEntity = new EntityMetadata();
		final EntityFunction functionEntity = new EntityFunction(); 
		final EntityFunction functionGeneric = new EntityFunction();
		assertTrue(entity.getAllFunctions().isEmpty());
		
		entity.addFunction(functionEntity);
		assertSame(1, entity.getAllFunctions().size());
		assertSame(functionEntity, entity.getAllFunctions().get(0));
		
		genericEntity.addFunction(functionGeneric);
		((EntityMetadata) entity).setGenericEntity(genericEntity);
		assertSame(2, entity.getAllFunctions().size());
		assertSame(functionGeneric, entity.getAllFunctions().get(0));
	}
	
	@Test
	void testGetAllRelations() {
		final Entity entity = new EntityMetadata();
		final Entity genericEntity = new EntityMetadata();
		final EntityRelation relationEntity = new EntityRelation();
		final EntityRelation relationGeneric = new EntityRelation();
		assertTrue(entity.getAllRelations().isEmpty());
		
		entity.addRelation(relationEntity);
		assertSame(1, entity.getAllRelations().size());
		assertSame(relationEntity, entity.getAllRelations().get(0));
		
		genericEntity.addRelation(relationGeneric);
		((EntityMetadata) entity).setGenericEntity(genericEntity);
		assertSame(2, entity.getAllRelations().size());
		assertSame(relationGeneric, entity.getAllRelations().get(0));
	}
	
	@Test
	void testGetAllFieldsByGroup() {
		final Entity entity = new EntityMetadata();
		final EntityField field1 = new EntityField();
		final EntityField field2 = new EntityField();
		final EntityFieldGroup group1 = new EntityFieldGroup();
		final EntityFieldGroup group2 = new EntityFieldGroup();
		entity.addField(field1);
		entity.addField(field2);
		assertTrue(entity.getAllFieldsByGroup(group1).isEmpty());
		
		field1.setFieldGroup(group1);
		field2.setFieldGroup(group2);
		assertSame(1, entity.getAllFieldsByGroup(group1).size());
		assertSame(1, entity.getAllFieldsByGroup(group2).size());
		assertSame(field1, entity.getAllFieldsByGroup(group1).get(0));
		assertSame(field2, entity.getAllFieldsByGroup(group2).get(0));
	}
	
	@Test
	void testGetAllFieldsByType() {
		final Entity entity = new EntityMetadata();
		final EntityField field1 = new EntityField();
		final EntityField field2 = new EntityField();
		entity.addField(field1);
		entity.addField(field2);
		assertTrue(entity.getAllFieldsByType(FieldType.TEXT).isEmpty());
		
		field1.setType(FieldType.TEXT);
		field2.setType(FieldType.DATE);
		assertSame(1, entity.getAllFieldsByType(FieldType.TEXT).size());
		assertSame(1, entity.getAllFieldsByType(FieldType.DATE).size());
		assertSame(field1, entity.getAllFieldsByType(FieldType.TEXT).get(0));
		assertSame(field2, entity.getAllFieldsByType(FieldType.DATE).get(0));
	}
	
	@Test
	void testGetCallbackFunctions() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		entity.addFunction(function);
		
		final EntityFunction callbackFunction = new EntityFunction();
		callbackFunction.setCallback(true);
		entity.addFunction(callbackFunction);
		
		assertTrue(entity.hasFunctions());
		assertSame(2, entity.getFunctions().size());
		assertSame(1, entity.getCallbackFunctions().size());
		assertSame(callbackFunction, entity.getCallbackFunctions().get(0));
	}
	
	@Test
	void testGetDefaultIdentifierPattern() {
		final Entity entity = new EntityMetadata();
		final EntityField field1 = new EntityField();
		final EntityField field2 = new EntityField();
		field1.setName("field1");
		field2.setName("field2");
		field1.setType(FieldType.INTEGER);
		field2.setType(FieldType.BOOLEAN);
		entity.addField(field1);
		entity.addField(field2);
		assertNull(entity.getDefaultIdentifierPattern());
		
		field1.setType(FieldType.TEXT);
		assertEquals("{field1}", entity.getDefaultIdentifierPattern());
		
		field2.setType(FieldType.AUTONUM);
		field2.setUnique(true);
		assertEquals("{field2}", entity.getDefaultIdentifierPattern());
	}
	
	@Test
	void testGetMemberFunctions() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		entity.addFunction(function);
		
		final EntityFunction callbackFunction = new EntityFunction();
		callbackFunction.setCallback(true);
		entity.addFunction(callbackFunction);
		
		assertTrue(entity.hasFunctions());
		assertSame(2, entity.getFunctions().size());
		assertSame(1, entity.getMemberFunctions().size());
		assertSame(function, entity.getMemberFunctions().get(0));
	}
	
	@Test
	void testGetNestedByEntityField() {
		final Entity entity = new EntityMetadata();
		final Entity nestedEntity = new EntityMetadata();
		final EntityField nestedEntityField = new EntityField();
		final NestedEntity nested = new NestedEntity();
		nested.setNestedEntity(nestedEntity);
		entity.addNested(nested);
		
		assertNull(entity.getNestedByEntityField(nestedEntityField));
		
		nestedEntity.addField(nestedEntityField);
		
		assertSame(nested, entity.getNestedByEntityField(nestedEntityField));
	}
	
	@Test
	void testGetNestedByEntityId() {
		final Entity entity = new EntityMetadata();
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		((EntityMetadata) nestedEntity).setId(987L);
		nested.setNestedEntity(nestedEntity);
		entity.addNested(nested);
		
		assertNull(entity.getNestedByEntityId(123L));
		
		((EntityMetadata) nestedEntity).setId(123L);
		
		assertSame(nested, entity.getNestedByEntityId(123L));
	}
	
	@Test
	void testGetNestedByInternalName() {
		final Entity entity = new EntityMetadata();
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		nestedEntity.setName("other");
		nested.setNestedEntity(nestedEntity);
		entity.addNested(nested);
		
		assertNull(entity.getNestedByInternalName("taest"));
		
		nestedEntity.setName("TÄST");
		
		assertEquals(nested, entity.getNestedByInternalName("taest"));
	}
	
	@Test
	void testGetEffectiveTableName() {
		final Entity entity = new EntityMetadata();
		entity.setName("TÄST");
		
		assertEquals("taest", entity.getEffectiveTableName());
		
		((EntityMetadata) entity).setTableName("TESTTABLE");
		
		assertEquals("testtable", entity.getEffectiveTableName());
	}
	
	@Test
	void testGetFieldById() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		field.setId(987L);
		entity.addField(field);
		
		assertNull(entity.getFieldById(123L));
		
		field.setId(123L);
		
		assertSame(field, entity.getFieldById(123L));
	}
	
	@Test
	void testGetFieldByUId() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		field.setUid("other");
		entity.addField(field);
		
		assertNull(entity.getFieldByUid("test"));
		
		field.setUid("test");
		
		assertSame(field, entity.getFieldByUid("test"));
	}
	
	@Test
	void testGetFieldGroupById() {
		final Entity entity = new EntityMetadata();
		final EntityFieldGroup fieldGroup = new EntityFieldGroup();
		fieldGroup.setId(987L);
		entity.addFieldGroup(fieldGroup);
		
		assertNull(entity.getFieldGroupById(123L));
		
		fieldGroup.setId(123L);
		
		assertSame(fieldGroup, entity.getFieldGroupById(123L));
	}
	
	@Test
	void testGetFieldGroupByUid() {
		final Entity entity = new EntityMetadata();
		final EntityFieldGroup fieldGroup = new EntityFieldGroup();
		fieldGroup.setUid("other");
		entity.addFieldGroup(fieldGroup);
		
		assertNull(entity.getFieldGroupByUid("test"));
		
		fieldGroup.setUid("test");
		
		assertSame(fieldGroup, entity.getFieldGroupByUid("test"));
	}
	
	@Test
	void testGetFullTextSearchFields() {
		final Entity entity = new EntityMetadata();
		final EntityField field1 = new EntityField();
		final EntityField field2 = new EntityField();
		entity.addField(field1);
		entity.addField(field2);
		assertTrue(entity.getFullTextSearchFields().isEmpty());
		
		field1.setFullTextSearch(true);
		assertSame(1, entity.getFullTextSearchFields().size());
		assertSame(field1, entity.getFullTextSearchFields().get(0));
		
		field2.setFullTextSearch(true);
		assertSame(2, entity.getFullTextSearchFields().size());
		assertSame(field2, entity.getFullTextSearchFields().get(1));
	}
	
	@Test
	void testGetFunctionById() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		function.setId(987L);
		entity.addFunction(function);
		
		assertNull(entity.getFunctionById(123L));
		
		function.setId(123L);
		
		assertSame(function, entity.getFunctionById(123L));
	}
	
	@Test
	void testGetFunctionByUid() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		function.setUid("other");
		entity.addFunction(function);
		
		assertNull(entity.getFunctionByUid("test"));
		
		function.setUid("test");
		
		assertSame(function, entity.getFunctionByUid("test"));
	}
	
	@Test
	void testGetGeneratedClass() {
		final Entity entity = new EntityMetadata();
		entity.setName("tästThing");
		
		assertEquals("TaestThing", entity.getGeneratedClass());
	}
	
	@Test
	void testGetGenericEntityUid() {
		final Entity entity = new EntityMetadata();
		final Entity genericEntity = new EntityMetadata();
		genericEntity.setUid("generic");
		assertNull(entity.getGenericEntityUid());
		
		((EntityMetadata) entity).setGenericEntityUid("test");
		assertEquals("test", entity.getGenericEntityUid());
		
		((EntityMetadata) entity).setGenericEntity(genericEntity);
		assertEquals("generic", entity.getGenericEntityUid());
	}
	
	@Test
	void testGetFieldConstraintByUid() {
		final Entity entity = new EntityMetadata();
		final EntityFieldConstraint constraint = new EntityFieldConstraint();
		constraint.setUid("other");
		entity.addFieldConstraint(constraint);
		
		assertNull(entity.getFieldConstraintByUid("test"));
		
		constraint.setUid("test");
		
		assertSame(constraint, entity.getFieldConstraintByUid("test"));
	}
	
	@Test
	void testGetInitialStatus() {
		final Entity entity = new EntityMetadata();
		final EntityStatus status = new EntityStatus();
		
		IllegalStateException isex = assertThrows(IllegalStateException.class, () -> {
			entity.getInitialStatus();
		});
		assertEquals("entity has no status", isex.getMessage());
		
		entity.addStatus(status);
		
		isex = assertThrows(IllegalStateException.class, () -> {
			entity.getInitialStatus();
		});
		assertEquals("initial status not available", isex.getMessage());
		
		status.setInitial(true);
		
		assertSame(status, entity.getInitialStatus());
	}
	
	@Test
	void testGetStatusById() {
		final Entity entity = new EntityMetadata();
		final EntityStatus status = new EntityStatus();
		status.setId(123L);
		
		assertNull(entity.getStatusById(123L));
		
		entity.addStatus(status);
		
		assertSame(status, entity.getStatusById(123L));
	}
	
	@Test
	void testGetStatusByNumber() {
		final Entity entity = new EntityMetadata();
		final EntityStatus status = new EntityStatus();
		status.setStatusNumber(42);
		
		assertNull(entity.getStatusByNumber(42));
		
		entity.addStatus(status);
		
		assertSame(status, entity.getStatusByNumber(42));
	}
	
	@Test
	void testGetStatusByUid() {
		final Entity entity = new EntityMetadata();
		final EntityStatus status = new EntityStatus();
		status.setUid("other");
		entity.addStatus(status);
		
		assertNull(entity.getStatusByUid("test"));
		
		status.setUid("test");
		
		assertSame(status, entity.getStatusByUid("test"));
	}
	
	@Test
	void testGetStatusTransition() {
		final Entity entity = new EntityMetadata();
		final EntityStatus sourceStatus = new EntityStatus();
		final EntityStatus targetStatus = new EntityStatus();
		final EntityStatusTransition transition = new EntityStatusTransition();
		transition.setSourceStatus(sourceStatus);
		transition.setTargetStatus(targetStatus);
		
		assertNull(entity.getStatusTransition(sourceStatus, targetStatus));
		
		entity.addStatusTransition(transition);
		
		assertNotNull(entity.getStatusTransition(sourceStatus, targetStatus));
		assertSame(transition, entity.getStatusTransition(sourceStatus, targetStatus));
	}
	
	@Test
	void testGetStatusTransitionByUid() {
		final Entity entity = new EntityMetadata();
		final EntityStatusTransition transition = new EntityStatusTransition();
		transition.setUid("other");
		entity.addStatusTransition(transition);
		
		assertNull(entity.getStatusTransitionByUid("test"));
		
		transition.setUid("test");
		
		assertSame(transition, entity.getStatusTransitionByUid("test"));
	}
	
	@Test
	void testGetUserActionFunctions() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function1 = new EntityFunction();
		final EntityFunction function2 = new EntityFunction();
		function2.setActiveOnUserAction(true);
		entity.addFunction(function1);
		
		assertEquals(0, entity.getUserActionFunctions().size());
		
		entity.addFunction(function2);
		
		assertEquals(1, entity.getUserActionFunctions().size());
		assertSame(function2, entity.getUserActionFunctions().get(0));
	}
	
	@Test
	void testGetUidField() {
		final Entity entity = new EntityMetadata();
		IllegalStateException isex = assertThrows(IllegalStateException.class, () -> {
			entity.getUidField();
		});
		assertSame("entity is not transferable", isex.getMessage());
		
		((EntityMetadata) entity).setTransferable(true);
		assertNotNull(entity.getUidField());
		assertEquals("uid", entity.getUidField().getName());
	}
	
	@Test
	void testHasAllFieldGroups() {
		final Entity entity = new EntityMetadata();
		final Entity genericEntity = new EntityMetadata();
		final EntityFieldGroup group = new EntityFieldGroup();
		((EntityMetadata) entity).setGenericEntity(genericEntity);
		assertFalse(entity.hasAllFieldGroups());
		
		entity.addFieldGroup(group);
		assertTrue(entity.hasAllFieldGroups());
		entity.removeFieldGroup(group);
		assertFalse(entity.hasAllFieldGroups());
		
		genericEntity.addFieldGroup(group);
		assertTrue(entity.hasAllFieldGroups());
	}
	
	@Test
	void testHasAllFields() {
		final Entity entity = new EntityMetadata();
		final Entity genericEntity = new EntityMetadata();
		final EntityField field = new EntityField();
		((EntityMetadata) entity).setGenericEntity(genericEntity);
		assertFalse(entity.hasAllFields());
		
		entity.addField(field);
		assertTrue(entity.hasAllFields());
		entity.removeField(field);
		assertFalse(entity.hasAllFields());
		
		genericEntity.addField(field);
		assertTrue(entity.hasAllFields());
	}
	
	@Test
	void testHasAllFunctions() {
		final Entity entity = new EntityMetadata();
		final Entity genericEntity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		((EntityMetadata) entity).setGenericEntity(genericEntity);
		assertFalse(entity.hasAllFunctions());
		
		entity.addFunction(function);
		assertTrue(entity.hasAllFunctions());
		entity.removeFunction(function);
		assertFalse(entity.hasAllFunctions());
		
		genericEntity.addFunction(function);
		assertTrue(entity.hasAllFunctions());
	}
	
	@Test
	void testHasAllRelations() {
		final Entity entity = new EntityMetadata();
		final Entity genericEntity = new EntityMetadata();
		final EntityRelation relation = new EntityRelation();
		((EntityMetadata) entity).setGenericEntity(genericEntity);
		assertFalse(entity.hasAllRelations());
		
		entity.addRelation(relation);
		assertTrue(entity.hasAllRelations());
		entity.removeRelation(relation);
		assertFalse(entity.hasAllRelations());
		
		genericEntity.addRelation(relation);
		assertTrue(entity.hasAllRelations());
	}
	
	@Test
	void testHasFullTextSearchFields() {
		final Entity entity = new EntityMetadata();
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		final EntityField field = new EntityField();
		final EntityField fullTextSearchField = new EntityField();
		entity.addNested(nested);
		nested.setNestedEntity(nestedEntity);
		fullTextSearchField.setFullTextSearch(true);
		entity.addField(field);
		assertFalse(entity.hasFullTextSearchFields());
		
		entity.addField(fullTextSearchField);
		assertTrue(entity.hasFullTextSearchFields());
		
		entity.removeField(fullTextSearchField);
		assertFalse(entity.hasFullTextSearchFields());
		
		nestedEntity.addField(fullTextSearchField);
		assertTrue(entity.hasFullTextSearchFields());
	}
	
	@Test
	void testIsNestedEntity() {
		final Entity entity = new EntityMetadata();
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		nested.setNestedEntity(nestedEntity);
		
		assertFalse(entity.isNestedEntity(nestedEntity));
		
		entity.addNested(nested);
		
		assertTrue(entity.isNestedEntity(nestedEntity));
	}
	
	@Test
	void testIsRelatedEntity() {
		final Entity entity = new EntityMetadata();
		final Entity relatedEntity = new EntityMetadata();
		final EntityRelation relation = new EntityRelation();
		relation.setRelatedEntity(relatedEntity);
		
		assertFalse(entity.isRelatedEntity(relatedEntity));
		
		entity.addRelation(relation);
		
		assertTrue(entity.isRelatedEntity(relatedEntity));
	}
	
	@Test
	void testIsUniqueTransition() {
		final Entity entity = new EntityMetadata();
		final EntityStatus sourceStatus = new EntityStatus();
		final EntityStatus targetStatus = new EntityStatus();
		final EntityStatusTransition transition1 = new EntityStatusTransition();
		final EntityStatusTransition transition2 = new EntityStatusTransition();
		transition1.setSourceStatus(sourceStatus);
		transition1.setTargetStatus(targetStatus);
		transition2.setSourceStatus(sourceStatus);
		transition2.setTargetStatus(targetStatus);
		assertTrue(entity.isUnique(transition1));
		
		entity.addStatusTransition(transition1);
		
		assertFalse(entity.isUnique(transition2));
	}
	
	@Test
	void testIsTransferable() {
		final Entity entity = new EntityMetadata();
		assertFalse(entity.isTransferable());
		
		((EntityMetadata) entity).setTransferable(true);
		assertTrue(entity.isTransferable());
	}
	
	@Test
	void testIsEqual() {
		final Entity entity1 = new EntityMetadata();
		final Entity entity2 = new EntityMetadata();
		assertTrue(entity1.isEqual(entity2));
		
		// entity
		entity1.setName("test");
		((EntityMetadata) entity1).setTableName("tableName");
		((EntityMetadata) entity1).setIdentifierPattern("identifier");
		((EntityMetadata) entity1).setGenericEntityUid("genericUid");
		((EntityMetadata) entity1).setGeneric(true);
		((EntityMetadata) entity1).setTransferable(true);
		((EntityMetadata) entity1).setAudited(true);
		assertFalse(entity1.isEqual(entity2));
		
		entity2.setName("test");
		((EntityMetadata) entity2).setTableName("tableName");
		((EntityMetadata) entity2).setIdentifierPattern("identifier");
		((EntityMetadata) entity2).setGenericEntityUid("genericUid");
		((EntityMetadata) entity2).setGeneric(true);
		((EntityMetadata) entity2).setTransferable(true);
		((EntityMetadata) entity2).setAudited(true);
		assertTrue(entity1.isEqual(entity2));
	}
	
	@Test
	void testIsEqualFields() {
		final Entity entity1 = new EntityMetadata();
		final Entity entity2 = new EntityMetadata();
		final EntityField field1 = new EntityField();
		final EntityField field2 = new EntityField();
		field1.setUid("test");
		field2.setUid("test");
		entity1.addField(field1);
		assertFalse(entity1.isEqual(entity2));
		
		entity2.addField(field2);
		assertTrue(entity1.isEqual(entity2));
		
		field2.setUid("other");
		assertFalse(entity1.isEqual(entity2));
	}
	
	@Test
	void testIsEqualFieldGroups() {
		final Entity entity1 = new EntityMetadata();
		final Entity entity2 = new EntityMetadata();
		final EntityFieldGroup fieldGroup1 = new EntityFieldGroup();
		final EntityFieldGroup fieldGroup2 = new EntityFieldGroup();
		fieldGroup1.setUid("test");
		fieldGroup2.setUid("test");
		entity1.addFieldGroup(fieldGroup1);
		assertFalse(entity1.isEqual(entity2));
		
		entity2.addFieldGroup(fieldGroup2);
		assertTrue(entity1.isEqual(entity2));
		
		fieldGroup2.setUid("other");
		assertFalse(entity1.isEqual(entity2));
	}
	
	@Test
	void testIsEqualFunctions() {
		final Entity entity1 = new EntityMetadata();
		final Entity entity2 = new EntityMetadata();
		final EntityFunction function1 = new EntityFunction();
		final EntityFunction function2 = new EntityFunction();
		function1.setUid("test");
		function2.setUid("test");
		entity1.addFunction(function1);
		assertFalse(entity1.isEqual(entity2));
		
		entity2.addFunction(function2);
		assertTrue(entity1.isEqual(entity2));
		
		function2.setUid("other");
		assertFalse(entity1.isEqual(entity2));
	}
	
	@Test
	void testIsEqualStatus() {
		final Entity entity1 = new EntityMetadata();
		final Entity entity2 = new EntityMetadata();
		final EntityStatus status1 = new EntityStatus();
		final EntityStatus status2 = new EntityStatus();
		status1.setUid("test");
		status2.setUid("test");
		entity1.addStatus(status1);
		assertFalse(entity1.isEqual(entity2));
		
		entity2.addStatus(status2);
		assertTrue(entity1.isEqual(entity2));
		
		status2.setUid("other");
		assertFalse(entity1.isEqual(entity2));
	}
	
	@Test
	void testIsEqualStatusTransitions() {
		final Entity entity1 = new EntityMetadata();
		final Entity entity2 = new EntityMetadata();
		final EntityStatusTransition transition1 = new EntityStatusTransition();
		final EntityStatusTransition transition2 = new EntityStatusTransition();
		transition1.setUid("test");
		transition2.setUid("test");
		entity1.addStatusTransition(transition1);
		assertFalse(entity1.isEqual(entity2));
		
		entity2.addStatusTransition(transition2);
		assertTrue(entity1.isEqual(entity2));
		
		transition2.setUid("other");
		assertFalse(entity1.isEqual(entity2));
	}
	
	@Test
	void testIsEqualNesteds() {
		final Entity entity1 = new EntityMetadata();
		final Entity entity2 = new EntityMetadata();
		final NestedEntity nested1 = new NestedEntity();
		final NestedEntity nested2 = new NestedEntity();
		nested1.setUid("test");
		nested2.setUid("test");
		entity1.addNested(nested1);
		assertFalse(entity1.isEqual(entity2));
		
		entity2.addNested(nested2);
		assertTrue(entity1.isEqual(entity2));
		
		nested2.setUid("other");
		assertFalse(entity1.isEqual(entity2));
	}
	
	@Test
	void testIsEqualRelations() {
		final Entity entity1 = new EntityMetadata();
		final Entity entity2 = new EntityMetadata();
		final EntityRelation relation1 = new EntityRelation();
		final EntityRelation relation2 = new EntityRelation();
		relation1.setUid("test");
		relation2.setUid("test");
		entity1.addRelation(relation1);
		assertFalse(entity1.isEqual(entity2));
		
		entity2.addRelation(relation2);
		assertTrue(entity1.isEqual(entity2));
		
		relation2.setUid("other");
		assertFalse(entity1.isEqual(entity2));
	}
	
	@Test
	void testIsEqualPermissions() {
		final Entity entity1 = new EntityMetadata();
		final Entity entity2 = new EntityMetadata();
		final EntityPermission permission1 = new EntityPermission();
		final EntityPermission permission2 = new EntityPermission();
		permission1.setUid("test");
		permission2.setUid("test");
		entity1.addPermission(permission1);
		assertFalse(entity1.isEqual(entity2));
		
		entity2.addPermission(permission2);
		assertTrue(entity1.isEqual(entity2));
		
		permission2.setUid("other");
		assertFalse(entity1.isEqual(entity2));
	}
	
	@Test
	void testIsEqualConstraints() {
		final Entity entity1 = new EntityMetadata();
		final Entity entity2 = new EntityMetadata();
		final EntityFieldConstraint constraint1 = new EntityFieldConstraint();
		final EntityFieldConstraint constraint2 = new EntityFieldConstraint();
		constraint1.setUid("test");
		constraint2.setUid("test");
		entity1.addFieldConstraint(constraint1);
		assertFalse(entity1.isEqual(entity2));
		
		entity2.addFieldConstraint(constraint2);
		assertTrue(entity1.isEqual(entity2));
		
		constraint2.setUid("other");
		assertFalse(entity1.isEqual(entity2));
	}
	
	@Test
	void testRemoveField() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		entity.addField(field);
		assertSame(1, entity.getFields().size());
		assertSame(1, entity.getAllFields().size());
		
		entity.removeField(field);
		
		assertFalse(entity.hasFields());
		assertFalse(entity.hasAllFields());
		assertSame(0, entity.getFields().size());
		assertSame(0, entity.getAllFields().size());
	}
	
	@Test
	void testRemoveFieldConstraint() {
		final Entity entity = new EntityMetadata();
		final EntityFieldConstraint constraint = new EntityFieldConstraint();
		entity.addFieldConstraint(constraint);
		assertSame(1, entity.getFieldConstraints().size());
		
		entity.removeFieldConstraint(constraint);
		
		assertFalse(entity.hasFieldConstraints());
		assertSame(0, entity.getFieldConstraints().size());
	}
	
	@Test
	void testRemoveFieldGroup() {
		final Entity entity = new EntityMetadata();
		final EntityFieldGroup fieldGroup = new EntityFieldGroup();
		entity.addFieldGroup(fieldGroup);
		assertSame(1, entity.getFieldGroups().size());
		assertSame(1, entity.getAllFieldGroups().size());
		
		entity.removeFieldGroup(fieldGroup);
		
		assertFalse(entity.hasFieldGroups());
		assertFalse(entity.hasAllFieldGroups());
		assertSame(0, entity.getFieldGroups().size());
		assertSame(0, entity.getAllFieldGroups().size());
	}
	
	@Test
	void testRemoveFunction() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		entity.addFunction(function);
		assertSame(1, entity.getFunctions().size());
		assertSame(1, entity.getAllFunctions().size());
		
		entity.removeFunction(function);
		
		assertFalse(entity.hasFunctions());
		assertFalse(entity.hasAllFunctions());
		assertSame(0, entity.getFunctions().size());
		assertSame(0, entity.getAllFunctions().size());
	}
	
	@Test
	void testRemoveNested() {
		final Entity entity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		entity.addNested(nested);
		assertSame(1, entity.getNesteds().size());
		
		entity.removeNested(nested);
		
		assertFalse(entity.hasNesteds());
		assertSame(0, entity.getNesteds().size());
	}
	
	@Test
	void testRemovePermission() {
		final Entity entity = new EntityMetadata();
		final EntityPermission permission = new EntityPermission();
		entity.addPermission(permission);
		assertSame(1, entity.getPermissions().size());
		
		entity.removePermission(permission);
		
		assertFalse(entity.hasPermissions());
		assertSame(0, entity.getPermissions().size());
	}
	
	@Test
	void testRemoveRelation() {
		final Entity entity = new EntityMetadata();
		final EntityRelation relation = new EntityRelation();
		entity.addRelation(relation);
		assertSame(1, entity.getRelations().size());
		assertSame(1, entity.getAllRelations().size());
		
		entity.removeRelation(relation);
		
		assertFalse(entity.hasRelations());
		assertFalse(entity.hasAllRelations());
		assertSame(0, entity.getRelations().size());
		assertSame(0, entity.getAllRelations().size());
	}
	
	@Test
	void testRemoveStatus() {
		final Entity entity = new EntityMetadata();
		final EntityStatus status = new EntityStatus();
		entity.addStatus(status);
		assertSame(1, entity.getStatusList().size());
		
		entity.removeStatus(status);
		
		assertFalse(entity.hasStatus());
		assertSame(0, entity.getStatusList().size());
	}
	
	@Test
	void testRemoveStatustransition() {
		final Entity entity = new EntityMetadata();
		final EntityStatusTransition transition = new EntityStatusTransition();
		entity.addStatusTransition(transition);
		assertSame(1, entity.getStatusTransitions().size());
		
		entity.removeStatusTransition(transition);
		
		assertFalse(entity.hasStatusTransitions());
		assertSame(0, entity.getStatusTransitions().size());
	}
	
}
