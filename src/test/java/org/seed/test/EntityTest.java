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

public class EntityTest {
	
	@Test
	public void testAddField() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		assertFalse(entity.hasFields());
		
		entity.addField(field);
		
		assertEquals(entity, field.getEntity());
		
		assertTrue(entity.hasFields());
		assertSame(entity.getFields().size(), 1);
		assertSame(entity.getFields().get(0), field);
		
		assertTrue(entity.hasAllFields());
		assertSame(entity.getAllFields().size(), 1);
		assertSame(entity.getAllFields().get(0), field);
	}
	
	@Test
	public void testAddFieldConstraint() {
		final Entity entity = new EntityMetadata();
		final EntityFieldConstraint constraint = new EntityFieldConstraint();
		assertFalse(entity.hasFieldConstraints());
		
		entity.addFieldConstraint(constraint);
		
		assertEquals(entity, constraint.getEntity());
		assertTrue(entity.hasFieldConstraints());
		assertSame(entity.getFieldConstraints().size(), 1);
		assertSame(entity.getFieldConstraints().get(0), constraint);
	}
	
	@Test
	public void testAddFieldGroup() {
		final Entity entity = new EntityMetadata();
		final EntityFieldGroup fieldGroup = new EntityFieldGroup();
		assertFalse(entity.hasFieldGroups());
		assertFalse(entity.hasAllFieldGroups());
		
		entity.addFieldGroup(fieldGroup);
		
		assertEquals(entity, fieldGroup.getEntity());
		assertTrue(entity.hasFieldGroups());
		assertSame(entity.getFieldGroups().size(), 1);
		assertSame(entity.getFieldGroups().get(0), fieldGroup);
		
		assertTrue(entity.hasAllFieldGroups());
		assertSame(entity.getAllFieldGroups().size(), 1);
		assertSame(entity.getAllFieldGroups().get(0), fieldGroup);
	}
	
	@Test
	public void testAddFunction() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		assertFalse(entity.hasFunctions());
		assertFalse(entity.hasAllFunctions());
		
		entity.addFunction(function);
		
		assertEquals(entity, function.getEntity());
		assertTrue(entity.hasFunctions());
		assertSame(entity.getFunctions().size(), 1);
		assertSame(entity.getFunctions().get(0), function);
		
		assertTrue(entity.hasAllFunctions());
		assertSame(entity.getAllFunctions().size(), 1);
		assertSame(entity.getAllFunctions().get(0), function);
	}
	
	@Test
	public void testAddNested() {
		final Entity entity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		assertFalse(entity.hasNesteds());
		
		entity.addNested(nested);
		
		assertEquals(entity, nested.getParentEntity());
		assertTrue(entity.hasNesteds());
		assertSame(entity.getNesteds().size(), 1);
		assertSame(entity.getNesteds().get(0), nested);
	}
	
	@Test
	public void testAddPermission() {
		final Entity entity = new EntityMetadata();
		final EntityPermission permission = new EntityPermission();
		assertFalse(entity.hasPermissions());
		
		entity.addPermission(permission);
		
		assertEquals(entity, permission.getEntity());
		assertTrue(entity.hasPermissions());
		assertSame(entity.getPermissions().size(), 1);
		assertSame(entity.getPermissions().get(0), permission);
	}
	
	@Test
	public void testAddRelation() {
		final Entity entity = new EntityMetadata();
		final EntityRelation relation = new EntityRelation();
		assertFalse(entity.hasRelations());
		assertFalse(entity.hasAllRelations());
		
		entity.addRelation(relation);
		
		assertEquals(entity, relation.getEntity());
		assertTrue(entity.hasRelations());
		assertSame(entity.getRelations().size(), 1);
		assertSame(entity.getRelations().get(0), relation);
		
		assertTrue(entity.hasAllRelations());
		assertSame(entity.getAllRelations().size(), 1);
		assertSame(entity.getAllRelations().get(0), relation);
	}
	
	@Test
	public void testAddStatus() {
		final Entity entity = new EntityMetadata();
		final EntityStatus status = new EntityStatus();
		assertFalse(entity.hasStatus());
		
		entity.addStatus(status);
		
		assertEquals(entity, status.getEntity());
		assertTrue(entity.hasStatus());
		assertSame(entity.getStatusList().size(), 1);
		assertSame(entity.getStatusList().get(0), status);
	}
	
	@Test
	public void testAddStatusTransition() {
		final Entity entity = new EntityMetadata();
		final EntityStatusTransition transition = new EntityStatusTransition();
		assertFalse(entity.hasStatusTransitions());
		
		entity.addStatusTransition(transition);
		
		assertEquals(entity, transition.getEntity());
		assertTrue(entity.hasStatusTransitions());
		assertSame(entity.getStatusTransitions().size(), 1);
		assertSame(entity.getStatusTransitions().get(0), transition);
	}
	
	@Test
	public void testCheckFieldAccess() {
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
	public void testContainsField() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		
		assertFalse(entity.containsField(field));
		assertFalse(entity.containsAllField(field));
		
		entity.addField(field);
		
		assertTrue(entity.containsField(field));
		assertTrue(entity.containsAllField(field));
	}
	
	@Test
	public void testContainsFieldGroup() {
		final Entity entity = new EntityMetadata();
		final EntityFieldGroup fieldGroup = new EntityFieldGroup();
		
		assertFalse(entity.containsFieldGroup(fieldGroup));
		
		entity.addFieldGroup(fieldGroup);
		
		assertTrue(entity.containsFieldGroup(fieldGroup));
	}
	
	@Test
	public void testContainsNested() {
		final Entity entity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		
		assertFalse(entity.containsNested(nested));
		
		entity.addNested(nested);
		
		assertTrue(entity.containsNested(nested));
	}
	
	@Test
	public void testFindAutonumField() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		field.setType(FieldType.AUTONUM);
		
		assertNull(entity.findAutonumField());
		
		entity.addField(field);
		
		assertSame(entity.findAutonumField(), field);
	}
	
	@Test
	public void testFindFieldByUid() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		field.setUid("other");
		entity.addField(field);
		
		assertNull(entity.findFieldByUid("test"));
		
		field.setUid("test");
		
		assertSame(entity.findFieldByUid("test"), field);
	}
	
	@Test
	public void testGetNestedByUid() {
		final Entity entity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		nested.setUid("other");
		entity.addNested(nested);
		
		assertNull(entity.getNestedByUid("test"));
		
		nested.setUid("test");
		
		assertSame(entity.getNestedByUid("test"), nested);
	}
	
	@Test
	public void testGetPermissionByUid() {
		final Entity entity = new EntityMetadata();
		final EntityPermission permission = new EntityPermission();
		permission.setUid("other");
		entity.addPermission(permission);
		
		assertNull(entity.getPermissionByUid("test"));
		
		permission.setUid("test");
		
		assertSame(entity.getPermissionByUid("test"), permission);
	}
	
	@Test
	public void testGetRelationByUid() {
		final Entity entity = new EntityMetadata();
		final EntityRelation relation = new EntityRelation();
		relation.setUid("other");
		entity.addRelation(relation);
		
		assertNull(entity.getRelationByUid("test"));
		
		relation.setUid("test");
		
		assertSame(entity.getRelationByUid("test"), relation);
	}
	
	@Test
	public void testGetReferenceFields() {
		final Entity entity = new EntityMetadata();
		final Entity referenceEntity = new EntityMetadata();
		final EntityField textField = new EntityField();
		final EntityField referenceField = new EntityField();
		textField.setType(FieldType.TEXT);
		referenceField.setType(FieldType.REFERENCE);
		referenceField.setReferenceEntity(referenceEntity);
		entity.addField(textField);
		
		assertSame(entity.getReferenceFields(referenceEntity).size(), 0);
		
		entity.addField(referenceField);
		
		assertSame(entity.getReferenceFields(referenceEntity).size(), 1);
		assertSame(entity.getReferenceFields(referenceEntity).get(0), referenceField);
	}
	
	@Test
	public void testGenericEntity() {
		final Entity genericEntity = new EntityMetadata();
		final Entity entity = new EntityMetadata();
		((EntityMetadata) genericEntity).setGeneric(true);
		
		assertTrue(genericEntity.isGeneric());
		assertFalse(entity.isGeneric());
		assertNull(entity.getGenericEntity());
		
		((EntityMetadata) entity).setGenericEntity(genericEntity);
		
		assertSame(entity.getGenericEntity(), genericEntity);
	}
	
	@Test
	public void testGetCallbackFunctions() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		entity.addFunction(function);
		
		final EntityFunction callbackFunction = new EntityFunction();
		callbackFunction.setCallback(true);
		entity.addFunction(callbackFunction);
		
		assertTrue(entity.hasFunctions());
		assertSame(entity.getFunctions().size(), 2);
		assertSame(entity.getCallbackFunctions().size(), 1);
		assertSame(entity.getCallbackFunctions().get(0), callbackFunction);
	}
	
	@Test
	public void testGetMemberFunctions() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		entity.addFunction(function);
		
		final EntityFunction callbackFunction = new EntityFunction();
		callbackFunction.setCallback(true);
		entity.addFunction(callbackFunction);
		
		assertTrue(entity.hasFunctions());
		assertSame(entity.getFunctions().size(), 2);
		assertSame(entity.getMemberFunctions().size(), 1);
		assertSame(entity.getMemberFunctions().get(0), function);
	}
	
	@Test
	public void testGetNestedByEntityField() {
		final Entity entity = new EntityMetadata();
		final Entity nestedEntity = new EntityMetadata();
		final EntityField nestedEntityField = new EntityField();
		final NestedEntity nested = new NestedEntity();
		nested.setNestedEntity(nestedEntity);
		entity.addNested(nested);
		
		assertNull(entity.getNestedByEntityField(nestedEntityField));
		
		nestedEntity.addField(nestedEntityField);
		
		assertSame(entity.getNestedByEntityField(nestedEntityField), nested);
	}
	
	@Test
	public void testGetNestedByEntityId() {
		final Entity entity = new EntityMetadata();
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		((EntityMetadata) nestedEntity).setId(987L);
		nested.setNestedEntity(nestedEntity);
		entity.addNested(nested);
		
		assertNull(entity.getNestedByEntityId(123L));
		
		((EntityMetadata) nestedEntity).setId(123L);
		
		assertSame(entity.getNestedByEntityId(123L), nested);
	}
	
	@Test
	public void testGetNestedByInternalName() {
		final Entity entity = new EntityMetadata();
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		nestedEntity.setName("other");
		nested.setNestedEntity(nestedEntity);
		entity.addNested(nested);
		
		assertNull(entity.getNestedByInternalName("taest"));
		
		nestedEntity.setName("TÄST");
		
		assertEquals(entity.getNestedByInternalName("taest"), nested);
	}
	
	@Test
	public void testGetEffectiveTableName() {
		final Entity entity = new EntityMetadata();
		entity.setName("TÄST");
		
		assertEquals(entity.getEffectiveTableName(), "taest");
		
		((EntityMetadata) entity).setTableName("TESTTABLE");
		
		assertEquals(entity.getEffectiveTableName(), "testtable");
	}
	
	@Test
	public void testGetFieldById() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		field.setId(987L);
		entity.addField(field);
		
		assertNull(entity.getFieldById(123L));
		
		field.setId(123L);
		
		assertSame(entity.getFieldById(123L), field);
	}
	
	@Test
	public void testGetFieldByUId() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		field.setUid("other");
		entity.addField(field);
		
		assertNull(entity.getFieldByUid("test"));
		
		field.setUid("test");
		
		assertSame(entity.getFieldByUid("test"), field);
	}
	
	@Test
	public void testGetFieldGroupById() {
		final Entity entity = new EntityMetadata();
		final EntityFieldGroup fieldGroup = new EntityFieldGroup();
		fieldGroup.setId(987L);
		entity.addFieldGroup(fieldGroup);
		
		assertNull(entity.getFieldGroupById(123L));
		
		fieldGroup.setId(123L);
		
		assertSame(entity.getFieldGroupById(123L), fieldGroup);
	}
	
	@Test
	public void testGetFieldGroupByUid() {
		final Entity entity = new EntityMetadata();
		final EntityFieldGroup fieldGroup = new EntityFieldGroup();
		fieldGroup.setUid("other");
		entity.addFieldGroup(fieldGroup);
		
		assertNull(entity.getFieldGroupByUid("test"));
		
		fieldGroup.setUid("test");
		
		assertSame(entity.getFieldGroupByUid("test"), fieldGroup);
	}
	
	@Test
	public void testGetFunctionById() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		function.setId(987L);
		entity.addFunction(function);
		
		assertNull(entity.getFunctionById(123L));
		
		function.setId(123L);
		
		assertSame(entity.getFunctionById(123L), function);
	}
	
	@Test
	public void testGetFunctionByUid() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		function.setUid("other");
		entity.addFunction(function);
		
		assertNull(entity.getFunctionByUid("test"));
		
		function.setUid("test");
		
		assertSame(entity.getFunctionByUid("test"), function);
	}
	
	@Test
	public void testGetFieldConstraintByUid() {
		final Entity entity = new EntityMetadata();
		final EntityFieldConstraint constraint = new EntityFieldConstraint();
		constraint.setUid("other");
		entity.addFieldConstraint(constraint);
		
		assertNull(entity.getFieldConstraintByUid("test"));
		
		constraint.setUid("test");
		
		assertSame(entity.getFieldConstraintByUid("test"), constraint);
	}
	
	@Test
	public void testGetInitialStatus() {
		final Entity entity = new EntityMetadata();
		final EntityStatus status = new EntityStatus();
		
		IllegalStateException isex = assertThrows(IllegalStateException.class, () -> {
			entity.getInitialStatus();
		});
		assertSame(isex.getMessage(), "entity has no status");
		
		entity.addStatus(status);
		
		isex = assertThrows(IllegalStateException.class, () -> {
			entity.getInitialStatus();
		});
		assertSame(isex.getMessage(), "initial status not found");
		
		status.setInitial(true);
		
		assertSame(entity.getInitialStatus(), status);
	}
	
	@Test
	public void testGetStatusById() {
		final Entity entity = new EntityMetadata();
		final EntityStatus status = new EntityStatus();
		status.setId(123L);
		
		assertNull(entity.getStatusById(123L));
		
		entity.addStatus(status);
		
		assertSame(entity.getStatusById(123L), status);
	}
	
	@Test
	public void testGetStatusByNumber() {
		final Entity entity = new EntityMetadata();
		final EntityStatus status = new EntityStatus();
		status.setStatusNumber(42);
		
		assertNull(entity.getStatusByNumber(42));
		
		entity.addStatus(status);
		
		assertSame(entity.getStatusByNumber(42), status);
	}
	
	@Test
	public void testGetStatusByUid() {
		final Entity entity = new EntityMetadata();
		final EntityStatus status = new EntityStatus();
		status.setUid("other");
		entity.addStatus(status);
		
		assertNull(entity.getStatusByUid("test"));
		
		status.setUid("test");
		
		assertSame(entity.getStatusByUid("test"), status);
	}
	
	@Test
	public void testGetStatusTransition() {
		final Entity entity = new EntityMetadata();
		final EntityStatus sourceStatus = new EntityStatus();
		final EntityStatus targetStatus = new EntityStatus();
		final EntityStatusTransition transition = new EntityStatusTransition();
		transition.setSourceStatus(sourceStatus);
		transition.setTargetStatus(targetStatus);
		
		assertNull(entity.getStatusTransition(sourceStatus, targetStatus));
		
		entity.addStatusTransition(transition);
		
		assertNotNull(entity.getStatusTransition(sourceStatus, targetStatus));
		assertSame(entity.getStatusTransition(sourceStatus, targetStatus), transition);
	}
	
	@Test
	public void testGetStatusTransitionByUid() {
		final Entity entity = new EntityMetadata();
		final EntityStatusTransition transition = new EntityStatusTransition();
		transition.setUid("other");
		entity.addStatusTransition(transition);
		
		assertNull(entity.getStatusTransitionByUid("test"));
		
		transition.setUid("test");
		
		assertSame(entity.getStatusTransitionByUid("test"), transition);
	}
	
	@Test
	public void testGetUserActionFunctions() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function1 = new EntityFunction();
		final EntityFunction function2 = new EntityFunction();
		function2.setActiveOnUserAction(true);
		entity.addFunction(function1);
		
		assertEquals(entity.getUserActionFunctions().size(), 0);
		
		entity.addFunction(function2);
		
		assertEquals(entity.getUserActionFunctions().size(), 1);
		assertSame(entity.getUserActionFunctions().get(0), function2);
	}
	
	@Test
	public void testHasFullTextSearchFields() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		final EntityField fullTextSearchField = new EntityField();
		fullTextSearchField.setFullTextSearch(true);
		entity.addField(field);
		
		assertFalse(entity.hasFullTextSearchFields());
		
		entity.addField(fullTextSearchField);
		
		assertTrue(entity.hasFullTextSearchFields());
	}
	
	@Test
	public void testIsNestedEntity() {
		final Entity entity = new EntityMetadata();
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		nested.setNestedEntity(nestedEntity);
		
		assertFalse(entity.isNestedEntity(nestedEntity));
		
		entity.addNested(nested);
		
		assertTrue(entity.isNestedEntity(nestedEntity));
	}
	
	@Test
	public void testIsRelatedEntity() {
		final Entity entity = new EntityMetadata();
		final Entity relatedEntity = new EntityMetadata();
		final EntityRelation relation = new EntityRelation();
		relation.setRelatedEntity(relatedEntity);
		
		assertFalse(entity.isRelatedEntity(relatedEntity));
		
		entity.addRelation(relation);
		
		assertTrue(entity.isRelatedEntity(relatedEntity));
	}
	
	@Test
	public void testIsUniqueTransition() {
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
	public void testRemoveField() {
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		entity.addField(field);
		assertSame(entity.getFields().size(), 1);
		assertSame(entity.getAllFields().size(), 1);
		
		entity.removeField(field);
		
		assertFalse(entity.hasFields());
		assertFalse(entity.hasAllFields());
		assertSame(entity.getFields().size(), 0);
		assertSame(entity.getAllFields().size(), 0);
	}
	
	@Test
	public void testRemoveFieldConstraint() {
		final Entity entity = new EntityMetadata();
		final EntityFieldConstraint constraint = new EntityFieldConstraint();
		entity.addFieldConstraint(constraint);
		assertSame(entity.getFieldConstraints().size(), 1);
		
		entity.removeFieldConstraint(constraint);
		
		assertFalse(entity.hasFieldConstraints());
		assertSame(entity.getFieldConstraints().size(), 0);
	}
	
	@Test
	public void testRemoveFieldGroup() {
		final Entity entity = new EntityMetadata();
		final EntityFieldGroup fieldGroup = new EntityFieldGroup();
		entity.addFieldGroup(fieldGroup);
		assertSame(entity.getFieldGroups().size(), 1);
		assertSame(entity.getAllFieldGroups().size(), 1);
		
		entity.removeFieldGroup(fieldGroup);
		
		assertFalse(entity.hasFieldGroups());
		assertFalse(entity.hasAllFieldGroups());
		assertSame(entity.getFieldGroups().size(), 0);
		assertSame(entity.getAllFieldGroups().size(), 0);
	}
	
	@Test
	public void testRemoveFunction() {
		final Entity entity = new EntityMetadata();
		final EntityFunction function = new EntityFunction();
		entity.addFunction(function);
		assertSame(entity.getFunctions().size(), 1);
		assertSame(entity.getAllFunctions().size(), 1);
		
		entity.removeFunction(function);
		
		assertFalse(entity.hasFunctions());
		assertFalse(entity.hasAllFunctions());
		assertSame(entity.getFunctions().size(), 0);
		assertSame(entity.getAllFunctions().size(), 0);
	}
	
	@Test
	public void testRemoveNested() {
		final Entity entity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		entity.addNested(nested);
		assertSame(entity.getNesteds().size(), 1);
		
		entity.removeNested(nested);
		
		assertFalse(entity.hasNesteds());
		assertSame(entity.getNesteds().size(), 0);
	}
	
	@Test
	public void testRemovePermission() {
		final Entity entity = new EntityMetadata();
		final EntityPermission permission = new EntityPermission();
		entity.addPermission(permission);
		assertSame(entity.getPermissions().size(), 1);
		
		entity.removePermission(permission);
		
		assertFalse(entity.hasPermissions());
		assertSame(entity.getPermissions().size(), 0);
	}
	
	@Test
	public void testRemoveRelation() {
		final Entity entity = new EntityMetadata();
		final EntityRelation relation = new EntityRelation();
		entity.addRelation(relation);
		assertSame(entity.getRelations().size(), 1);
		assertSame(entity.getAllRelations().size(), 1);
		
		entity.removeRelation(relation);
		
		assertFalse(entity.hasRelations());
		assertFalse(entity.hasAllRelations());
		assertSame(entity.getRelations().size(), 0);
		assertSame(entity.getAllRelations().size(), 0);
	}
	
	@Test
	public void testRemoveStatus() {
		final Entity entity = new EntityMetadata();
		final EntityStatus status = new EntityStatus();
		entity.addStatus(status);
		assertSame(entity.getStatusList().size(), 1);
		
		entity.removeStatus(status);
		
		assertFalse(entity.hasStatus());
		assertSame(entity.getStatusList().size(), 0);
	}
	
	@Test
	public void testRemoveStatustransition() {
		final Entity entity = new EntityMetadata();
		final EntityStatusTransition transition = new EntityStatusTransition();
		entity.addStatusTransition(transition);
		assertSame(entity.getStatusTransitions().size(), 1);
		
		entity.removeStatusTransition(transition);
		
		assertFalse(entity.hasStatusTransitions());
		assertSame(entity.getStatusTransitions().size(), 0);
	}
	
}
