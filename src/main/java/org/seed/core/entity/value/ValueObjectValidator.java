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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.seed.core.config.Limits;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldConstraint;
import org.seed.core.entity.EntityRepository;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
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
	private List<ValueObjectDependent> valueObjectDependents;
	
	public void validateChangeStatus(ValueObject object, EntityStatus targetStatus) throws ValidationException {
		Assert.notNull(object, "object is null");
		Assert.notNull(targetStatus, "targetStatus is null");
		
		final Entity entity = entityRepository.get(object.getEntityId());
		final Set<ValidationError> errors = new LinkedHashSet<>();
		if (entity.hasFieldConstraints()) {
			for (EntityFieldConstraint constraint : entity.getFieldConstraints()) {
				if (targetStatus.equals(constraint.getStatus()) && constraint.isMandatory()) {
					if (constraint.getField() != null &&
						isEmpty(objectAccess.getValue(object, constraint.getField()))) {
						errors.add(new ValidationError("val.empty.statusfield", constraint.getField().getName()));
					}
					else if (constraint.getFieldGroup() != null) {
						for (EntityField groupField : entity.getGroupFields(constraint.getFieldGroup())) {
							if (isEmpty(objectAccess.getValue(object, groupField))) {
								errors.add(new ValidationError("val.empty.statusfield", groupField.getName()));
							}
						}
					}
					else {
						throw new IllegalStateException("constraint has no field or group");
					}
				}
			}
		}
		
		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
	}
	
	public void validateDelete(ValueObject object) throws ValidationException {
		Assert.notNull(object, "object is null");
		
		final Set<ValidationError> errors = new LinkedHashSet<>();
		for (ValueObjectDependent dependent : valueObjectDependents) {
			for (Entity entity : dependent.findUsage(object)) {
				errors.add(new ValidationError("val.inuse.valueobject", entity.getName()));
			}
		}
		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
	}
	
	public void validateSave(ValueObject object) throws ValidationException {
		Assert.notNull(object, "object is null");
	
		final Entity entity = entityRepository.get(object.getEntityId());
		final Set<ValidationError> errors = new LinkedHashSet<>();
		
		// fields
		if (entity.hasAllFields()) {
			for (EntityField field : entity.getAllFields()) {
				final Object value = objectAccess.getValue(object, field);
				if (field.isMandatory() && !field.getType().isAutonum()) {
					if (isEmpty(value)) {
						errors.add(new ValidationError("val.empty.field", field.getName()));
					}
				}
				else if (field.getType().isText()) {
					final String text = (String) value;
					if (text != null & text.length() > getMaxFieldLength(field)) {
						errors.add(new ValidationError("val.toolong.fieldvalue", field.getName(),
								   					   String.valueOf(getMaxFieldLength(field))));
					}
				}
			}
		}
		
		// nesteds
		if (entity.hasAllNesteds()) {
			for (NestedEntity nested : entity.getAllNesteds()) {
				if (nested.getNestedEntity().hasFields()) {
					final List<ValueObject> nestedObjects = objectAccess.getNestedObjects(object, nested);
					if (nestedObjects != null) {
						for (ValueObject nestedObject : nestedObjects) {
							for (EntityField field : nested.getFields(true)) {
								final Object value = objectAccess.getValue(nestedObject, field);
								if (field.isMandatory() && isEmpty(value)) {
									errors.add(new ValidationError("val.empty.subfield", 
											   field.getName(), nested.getName()));
								}
								else if (field.getType().isText()) {
									final String stringValue = (String) value;
									if (stringValue != null && stringValue.length() > getMaxFieldLength(field)) {
										errors.add(new ValidationError("val.toolong.objectfieldvalue", 
																	   field.getName(), nested.getName(),
																	   String.valueOf(getMaxFieldLength(field))));
									}
								}
							}
						}
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
	
}
