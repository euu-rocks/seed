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

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.application.ApplicationEntity;
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
import org.seed.core.entity.filter.Filter;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ValueObjectValidator implements ApplicationContextAware {
	
	@Autowired
	private EntityRepository entityRepository;
	
	@Autowired
	private ValueObjectAccess objectAccess;
	
	@Autowired
	private Limits limits;
	
	private ApplicationContext applicationContext;
	
	private List<ValueObjectDependent<? extends ApplicationEntity>> valueObjectDependents;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	public void validateChangeStatus(ValueObject object, EntityStatus targetStatus, Session session) throws ValidationException {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(targetStatus, "targetStatus");
		
		final Entity entity = entityRepository.get(object.getEntityId(), session);
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
	
	public void validateDelete(Session session, ValueObject object) throws ValidationException {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(object, C.OBJECT);
		
		final Entity objectEntity = entityRepository.get(object.getEntityId(), session);
		final ValidationErrors errors = new ValidationErrors();
		for (ValueObjectDependent<? extends SystemEntity> dependent : getValueObjectDependents()) {
			for (SystemEntity systemEntity : dependent.findUsage(session, object)) {
				if (systemEntity instanceof Entity) {
					final Entity entity = (Entity) systemEntity;
					if (!objectEntity.isNestedEntity(entity)) {
						errors.addError("val.inuse.valueobject", entity.getName());
					}
				}
				else if (systemEntity instanceof Filter) {
					errors.addError("val.inuse.valueobjectfilter", systemEntity.getName());
				}
			}
		}
		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
	}
	
	public void validateSave(Session session, ValueObject object) throws ValidationException {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(object, C.OBJECT);
	
		final Entity entity = entityRepository.get(object.getEntityId(), session);
		final ValidationErrors errors = new ValidationErrors();
		
		// fields
		if (entity.hasAllFields()) {
			validateFields(entity, object, errors);
		}
		
		// nesteds
		if (entity.hasNesteds()) {
			for (NestedEntity nested : entity.getNesteds()) {
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
				: limits.getLimit(Limits.LIMIT_TEXT_LENGTH);
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
			nested.getFields(true).stream().filter(field -> !field.getType().isAutonum())
				  .forEach(field -> validateNestedObjectField(nestedObject, nested, field, errors));
		}
	}
	
	private void validateNestedObjectField(ValueObject nestedObject, NestedEntity nested, EntityField field, 
										   ValidationErrors errors) {
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
	
	private List<ValueObjectDependent<? extends ApplicationEntity>> getValueObjectDependents() {
		if (valueObjectDependents == null) {
			valueObjectDependents = MiscUtils.castList(
					BeanUtils.getBeans(applicationContext, ValueObjectDependent.class));
		}
		return valueObjectDependents;
	}
	
}
