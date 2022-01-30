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

import org.seed.core.application.ApplicationEntityService;
import org.seed.core.data.FieldType;
import org.seed.core.data.ValidationException;
import org.seed.core.user.User;

public interface EntityService extends ApplicationEntityService<Entity> {
	
	EntityField createField(Entity entity);
	
	EntityStatusTransition createStatusTransition(Entity entity);
	
	EntityFunction createFunction(Entity entity, boolean isCallback);
	
	NestedEntity createNested(Entity entity);
	
	EntityRelation createRelation(Entity entity);
	
	EntityStatus createStatus(Entity entity);
	
	EntityFieldConstraint createFieldConstraint(Entity entity);
	
	EntityFieldGroup createFieldGroup(Entity entity);
	
	boolean existGenericEntities();
	
	List<Entity> findGenericEntities();
	
	List<Entity> findNonGenericEntities();
	
	List<Entity> findTransferableEntities();
	
	List<Entity> findDescendants(Entity genericEntity);
	
	List<Entity> getAvailableNestedEntities(Entity entity);
	
	FieldType[] getAvailableFieldTypes(Entity entity, EntityField field, boolean existObjects);
	
	List<EntityPermission> getAvailablePermissions(Entity entity);
	
	List<EntityStatus> getAvailableStatusList(Entity entity, EntityStatus currentStatus, User user);
	
	List<EntityStatusTransitionFunction> getAvailableStatusTransitionFunctions(Entity entity, EntityStatusTransition transition);
	
	List<EntityStatusTransitionPermission> getAvailableStatusTransitionPermissions(EntityStatusTransition transition);
	
	void removeField(Entity entity, EntityField field) throws ValidationException;
	
	void removeFieldGroup(Entity entity, EntityFieldGroup fieldGroup) throws ValidationException;
	
	void removeFunction(Entity entity, EntityFunction function) throws ValidationException;
	
	void removeStatus(Entity entity, EntityStatus status) throws ValidationException;
	
	void removeStatusTransition(Entity entity, EntityStatusTransition statusTransition) throws ValidationException;
	
	void removeNested(Entity entity, NestedEntity nested) throws ValidationException;
	
	void removeRelation(Entity entity, EntityRelation relation) throws ValidationException;
	
}
