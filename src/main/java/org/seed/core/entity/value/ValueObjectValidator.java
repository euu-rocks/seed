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
package org.seed.core.entity.value;

import java.util.List;

import org.seed.C;
import org.seed.core.config.Limits;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldConstraint;
import org.seed.core.entity.EntityRepository;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ValueObjectValidator {
	
	@Autowired
	private EntityRepository entityRepository;
	
	@Autowired
	private ValueObjectAccess objectAccess;
	
	@Autowired
	private Limits limits;
	
	@Autowired
	private List<ValueObjectDependent<? extends SystemEntity>> valueObjectDependents;
	
	public void validateChangeStatus(ValueObject object, EntityStatus targetStatus) throws ValidationException {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(targetStatus, "targetStatus");
		
		final Entity entity = entityRepository.get(object.getEntityId());
		final ValidationErrors errors = new ValidationErrors();
		if (entity.hasFieldConstraints()) {
			for (EntityFieldConstraint constraint : entity.getFieldConstraints()) {
				if (targetStatus.equals(constraint.getStatus()) && 
					constraint.isMandatory()) {
					validateMandatoryConstraints(entity, constraint, object, errors);
				}
			}
		}
		
		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
	}
	
	public void validateDelete(ValueObject object) throws ValidationException {
		Assert.notNull(object, C.OBJECT);
		
		final Entity objectEntity = entityRepository.get(object.getEntityId());
		final ValidationErrors errors = new ValidationErrors();
		for (ValueObjectDependent<? extends SystemEntity> dependent : valueObjectDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(object)) {
				if (systemEntity instanceof Entity) {
					final Entity entity = (Entity) systemEntity;
					if (!objectEntity.isNestedEntity(entity)) {
						errors.addError("val.inuse.valueobject", entity.getName());
					}
				}
			}
		}
		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
	}
	
	public void validateSave(ValueObject object) throws ValidationException {
		Assert.notNull(object, C.OBJECT);
	
		final Entity entity = entityRepository.get(object.getEntityId());
		final ValidationErrors errors = new ValidationErrors();
		
		// fields
		if (entity.hasAllFields()) {
			validateFields(entity, object, errors);
		}
		
		// nesteds
		if (entity.hasAllNesteds()) {
			for (NestedEntity nested : entity.getAllNesteds()) {
				if (nested.getNestedEntity().hasFields()) {
					final List<ValueObject> nestedObjects = objectAccess.getNestedObjects(object, nested);
					if (nestedObjects != null) {
						validateNestedObjects(nested, nestedObjects, errors);
					}
				}
			}
		}
		
		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
		
	}
	
	private int getMaxFieldLength(EntityField field) {
		return field.getLength() != null 
				? field.getLength() 
				: limits.getLimit("entity.stringfield.length");
	}
	
	private static boolean isEmpty(Object object) {
		if (object instanceof String) {
			return !StringUtils.hasText((String) object);
		}
		return object == null;
	}
	
	private void validateMandatoryConstraints(Entity entity, EntityFieldConstraint constraint, 
			  								  ValueObject object, ValidationErrors errors) {
		if (constraint.getField() != null &&
			isEmpty(objectAccess.getValue(object, constraint.getField()))) {
			errors.addError("val.empty.statusfield", constraint.getField().getName());
		}
		else if (constraint.getFieldGroup() != null) {
			for (EntityField groupField : entity.getAllFieldsByGroup(constraint.getFieldGroup())) {
				if (isEmpty(objectAccess.getValue(object, groupField))) {
					errors.addError("val.empty.statusfield", groupField.getName());
				}
			}
		}
		else {
			throw new IllegalStateException("constraint has no field or group");
		}
	}
	
	private void validateFields(Entity entity, ValueObject object, ValidationErrors errors) {
		for (EntityField field : entity.getAllFields()) {
			final Object value = objectAccess.getValue(object, field);
			if (field.isMandatory() && !field.getType().isAutonum() && isEmpty(value)) {
				errors.addEmptyField(field.getName());
			}
			if (field.getType().isText()) {
				final String text = (String) value;
				if (text != null && text.length() > getMaxFieldLength(field)) {
					errors.addOverlongField(field.getName(), getMaxFieldLength(field));
				}
			}
		}
	}
	
	private void validateNestedObjects(NestedEntity nested, List<ValueObject> nestedObjects, 
									   ValidationErrors errors) {
		for (ValueObject nestedObject : nestedObjects) {
			for (EntityField field : nested.getFields(true)) {
				final Object value = objectAccess.getValue(nestedObject, field);
				if (field.isMandatory() && isEmpty(value)) {
					errors.addError("val.empty.subfield", field.getName(), nested.getName());
				}
				else if (field.getType().isText()) {
					final String stringValue = (String) value;
					if (stringValue != null && stringValue.length() > getMaxFieldLength(field)) {
						errors.addOverlongObjectField(field.getName(), nested.getName(), getMaxFieldLength(field));
					}
				}
			}
		}
	}
	
}
