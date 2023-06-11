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

import static org.seed.core.util.CollectionUtils.filterAndForEach;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.config.Limits;
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
		Assert.notNull(session, C.SESSION);
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(targetStatus, "targetStatus");
		final Entity entity = entityRepository.get(object.getEntityId(), session);
		final var errors = new ValidationErrors();
		
		filterAndForEach(entity.getFieldConstraints(), 
						 constraint -> constraint.isMandatory() && targetStatus.equals(constraint.getStatus()), 
						 constraint -> validateMandatoryConstraints(entity, constraint, object, errors));
		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
	}
	
	public void validateDelete(Session session, ValueObject object) throws ValidationException {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(object, C.OBJECT);
		final Entity objectEntity = entityRepository.get(object.getEntityId(), session);
		final var errors = new ValidationErrors();
		
		for (var dependent : getValueObjectDependents()) {
			for (var dependentEntity : dependent.findUsage(session, object)) {
				if (dependentEntity instanceof Entity) {
					final Entity entity = (Entity) dependentEntity;
					if (!objectEntity.isNestedEntity(entity)) {
						errors.addError("val.inuse.valueobject", entity.getName());
					}
				}
				else if (dependentEntity instanceof Filter) {
					errors.addError("val.inuse.valueobjectfilter", dependentEntity.getName());
				}
				else {
					throw new UnsupportedOperationException(dependentEntity.getName());
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
		final var errors = new ValidationErrors();
		
		// fields
		if (entity.hasAllFields()) {
			validateFields(entity, object, errors);
		}
		
		// nesteds
		if (entity.hasNesteds()) {
			for (NestedEntity nested : entity.getNesteds()) {
				if (nested.getNestedEntity().hasAllFields()) {
					final var nestedObjects = objectAccess.getNestedObjects(object, nested);
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
	
	private void validateMandatoryConstraints(Entity entity, EntityFieldConstraint constraint, 
			  								  ValueObject object, ValidationErrors errors) {
		if (constraint.getField() != null && isEmpty(objectAccess.getValue(object, constraint.getField()))) {
			errors.addError("val.empty.statusfield", constraint.getField().getName());
		}
		else if (constraint.getFieldGroup() != null) {
			filterAndForEach(entity.getAllFieldsByGroup(constraint.getFieldGroup()), 
							 field -> isEmpty(objectAccess.getValue(object, field)), 
							 field -> errors.addError("val.empty.statusfield", field.getName()));
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
			else if (!isEmpty(value)) {
				validateField(field, value, errors);
			}
		}
	}
	
	private void validateNestedObjects(NestedEntity nested, List<ValueObject> nestedObjects, 
									   ValidationErrors errors) {
		for (ValueObject nestedObject : nestedObjects) {
			filterAndForEach(nested.getFields(true), 
							 field -> !field.getType().isAutonum(), 
							 field -> validateNestedObjectField(nestedObject, nested, field, errors));
		}
	}
	
	private void validateNestedObjectField(ValueObject nestedObject, NestedEntity nested, EntityField field, 
										   ValidationErrors errors) {
		final Object value = objectAccess.getValue(nestedObject, field);
		if (field.isMandatory() && !field.getType().isAutonum() && isEmpty(value)) {
			errors.addError("val.empty.subfield", field.getName(), nested.getName());
		}
		else if (!isEmpty(value)) {
			validateField(field, value, errors);
		}
	}
	
	private List<ValueObjectDependent<? extends ApplicationEntity>> getValueObjectDependents() {
		if (valueObjectDependents == null) {
			valueObjectDependents = MiscUtils.castList(
					BeanUtils.getBeans(applicationContext, ValueObjectDependent.class));
		}
		return valueObjectDependents;
	}
	
	private void validateField(EntityField field, Object value, ValidationErrors errors) {
		if (field.getType().isText() && ((String) value).length() > getMaxFieldLength(field)) {
			errors.addOverlongField(field.getName(), getMaxFieldLength(field));
		}
		if (field.getType().supportsValidation() && field.getValidationPattern() != null && 
			!value.toString().matches(field.getValidationPattern())) {
			errors.addIllegalFieldValue(field.getName());
		}
		if (field.getType().supportsMinMaxValues()) {
			validateMinMaxValue(field, value, errors);
		}
	}
	
	private static boolean isEmpty(Object object) {
		return object instanceof String
				? !StringUtils.hasText((String) object)
				: object == null;
	}
	
	private static void validateMinMaxValue(EntityField field, Object value, ValidationErrors errors) {
		final int result;
		switch (field.getType()) {
			case DATE:
				result = validateMinMaxDate(field, field.getMinDate(), field.getMaxDate(), value);
				break;
			
			case DATETIME:
				result = validateMinMaxDate(field, field.getMinDateTime(), field.getMaxDateTime(), value);
				break;
				
			case DECIMAL:
				result = validateMinMaxNumber(field, field.getMinDecimal(), field.getMaxDecimal(), value);
				break;
				
			case DOUBLE:
				result = validateMinMaxNumber(field, field.getMinDouble(), field.getMaxDouble(), value);
				break;
				
			case INTEGER:
				result = validateMinMaxNumber(field, field.getMinInt(), field.getMaxInt(), value);
				break;
				
			case LONG:
				result = validateMinMaxNumber(field, field.getMinLong(), field.getMaxLong(), value);
				break;
				
			default:
				throw new UnsupportedOperationException(field.getType().name());
		}
		if (result < 0) {
			errors.addError("val.toolow.fieldvalue", field.getName());
		}
		else if (result > 0) {
			errors.addError("val.toobig.fieldvalue", field.getName());
		}
	}
	
	private static int validateMinMaxDate(EntityField field, Date minDate, Date maxDate, Object value) {
		if (value instanceof Date) {
			if (minDate != null && minDate.after((Date) value)) {
				return -1;
			}
			else if (maxDate != null && maxDate.before((Date) value)) {
				return 1;
			}
			else {
				return 0;
			}
		}
		else {
			throw new IllegalStateException("value of '"+ field.getName() + "' is not a date: " + value.getClass() + ' '+ value);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static int validateMinMaxNumber(EntityField field, Comparable minNumber, Comparable maxNumber, Object value) {
		if (value instanceof Number) {
			if (minNumber != null && minNumber.compareTo(value) > 0) {
				return -1;
			}
			else if (maxNumber != null && maxNumber.compareTo(value) < 0) {
				return 1;
			}
			else {
				return 0;
			}
		}
		else {
			throw new IllegalStateException("value of '"+ field.getName() + "' is not a number: " + value.getClass() + ' '+ value);
		}
	}
	
}
