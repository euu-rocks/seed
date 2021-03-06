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
package org.seed.core.entity;

import java.util.List;

import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApprovableObject;
import org.seed.core.codegen.GeneratedObject;
import org.seed.core.data.FieldAccess;
import org.seed.core.data.FieldType;
import org.seed.core.user.User;

public interface Entity 
	extends ApplicationEntity, GeneratedObject, ApprovableObject<EntityPermission> {
	
	final static String PACKAGE_NAME = "org.seed.generated.entity";
	
	String getInternalName();
	
	boolean isGeneric();
	
	boolean hasFields();
	
	boolean hasAllFields(); // includes generic fields
	
	boolean hasFullTextSearchFields();
	
	boolean hasFieldGroups();
	
	boolean hasNesteds();
	
	boolean hasAllNesteds(); // includes generic nesteds
	
	boolean hasStatus();
	
	boolean hasFunctions(); 
	
	boolean containsField(EntityField field);
	
	String getTableName();
	
	Entity getGenericEntity();
	
	List<EntityField> getFields();
	
	List<EntityField> getAllFields(); // includes generic fields
	
	List<EntityField> getReferenceFields(Entity entity);
	
	List<EntityField> getAllFieldsByType(FieldType fieldType);
	
	List<EntityField> getAllFieldsByGroup(EntityFieldGroup fieldGroup);
	
	List<EntityField> getFullTextSearchFields();
	
	EntityFieldGroup getFieldGroupByUid(String uid);
	
	EntityField getFieldById(Long id);
	
	EntityField getFieldByUid(String uid);
	
	EntityField findFieldById(Long id); // search in nestends too
	
	EntityField findFieldByUid(String uid); // search in nestends too
	
	EntityField findAutonumField();
	
	List<EntityFieldGroup> getFieldGroups();
	
	List<EntityFieldGroup> getAllFieldGroups();
	
	EntityStatus getInitialStatus();
	
	void addFieldGroup(EntityFieldGroup fieldGroup);
	
	void removeFieldGroup(EntityFieldGroup fieldGroup);
	
	void addField(EntityField field);
	
	void removeField(EntityField field);
	
	List<NestedEntity> getNesteds();
	
	List<NestedEntity> getAllNesteds(); // includes generic fields
	
	NestedEntity getNestedByEntityId(Long id);
	
	NestedEntity getNestedByUid(String uid);
	
	NestedEntity getNestedByInternalName(String name);
	
	NestedEntity getNestedByEntityField(EntityField field);
	
	boolean isNestedEntity(Entity entity);
	
	void addNested(NestedEntity nested);
	
	void removeNested(NestedEntity nested);
	
	List<EntityStatus> getStatusList();
	
	EntityStatus getStatusById(Long id);
	
	EntityStatus getStatusByUid(String uid);
	
	EntityStatus getStatusByNumber(Integer statusNumber);
	
	void addStatus(EntityStatus status);
	
	void removeStatus(EntityStatus status);
	
	boolean hasStatusTransitions();
	
	EntityStatusTransition getStatusTransitionByUid(String uid);
	
	EntityStatusTransition getStatusTransition(EntityStatus sourceStatus, EntityStatus targetStatus);
	
	List<EntityStatusTransition> getStatusTransitions();
	
	void addStatusTransition(EntityStatusTransition statusTransition);
	
	void removeStatusTransition(EntityStatusTransition statusTransition);
	
	EntityPermission getPermissionByUid(String uid);
	
	boolean hasFieldConstraints();
	
	List<EntityFieldConstraint> getFieldConstraints();
	
	EntityFieldConstraint getFieldConstraintByUid(String uid);
	
	boolean checkFieldAccess(EntityField field, User user, EntityStatus status, FieldAccess ...fieldAccess);
	
	void addFieldConstraint(EntityFieldConstraint constraint);
	
	void removeFieldConstraint(EntityFieldConstraint constraint);
	
	void addPermission(EntityPermission permission);
	
	void removePermission(EntityPermission permission);
	
	List<EntityFunction> getFunctions();
	
	List<EntityFunction> getAllFunctions();
	
	List<EntityFunction> getCallbackFunctions();
	
	List<EntityFunction> getMemberFunctions();
	
	List<EntityFunction> getUserActionFunctions();
	
	EntityFunction getFunctionByUid(String uid);
	
	void addFunction(EntityFunction function);
	
	void removeFunction(EntityFunction function);
	
}
