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
import java.util.Set;

import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

@Component
public class EntityValidator extends AbstractSystemEntityValidator<Entity> {
	
	@Autowired
	private List<EntityDependent> entityDependents;
	
	@SuppressWarnings("unchecked")
	@Override
	public void validateSave(Entity entity) throws ValidationException {
		Assert.notNull(entity, "entity is null");
		final Set<ValidationError> errors = createErrorList();
		
		// name / table name
		if (isEmpty(entity.getName())) {
			errors.add(new ValidationError("val.empty.field", "label.name"));
		}
		else if (!isNameLengthAllowed(entity.getName())) {
			errors.add(new ValidationError("val.toolong.name", String.valueOf(getMaxNameLength())));
		}
		else if (!isNameAllowed(entity.getInternalName())) {
			errors.add(new ValidationError("val.illegal.field", "label.name", entity.getName()));
		}
		if (entity.getTableName() != null && !isNameLengthAllowed(entity.getTableName())) {
			errors.add(new ValidationError("val.toolong.fieldvalue", "label.tablename", 
										   String.valueOf(getMaxNameLength())));
		}
		
		// field groups
		if (entity.hasFieldGroups()) {
			for (EntityFieldGroup fieldGroup : entity.getFieldGroups()) {
				if (isEmpty(fieldGroup.getName())) {
					errors.add(new ValidationError("val.empty.field", "label.fieldgroupname"));
				}
				else if (!isNameLengthAllowed(fieldGroup.getName())) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.name", "label.fieldgroup",
												   String.valueOf(getMaxNameLength())));
				}
				else if (!isNameUnique(fieldGroup.getName(), entity.getAllFieldGroups())) {
					errors.add(new ValidationError("val.ambiguous.fieldgroup", fieldGroup.getName()));
				}
			}
		}
		
		// fields
		if (entity.hasFields()) {
			boolean foundAutonum = false;
			for (EntityField field : entity.getFields()) {
				if (isEmpty(field.getName())) {
					errors.add(new ValidationError("val.empty.field", "label.fieldname"));
				}
				else if (!isNameLengthAllowed(field.getName())) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.name", "label.field", 
							   					   String.valueOf(getMaxNameLength())));
				}
				else if (!isNameAllowed(field.getInternalName())) {
					errors.add(new ValidationError("val.illegal.field", "label.fieldname", field.getName()));
				}
 				else if (!isNameUnique(field.getName(), entity.getAllFields(), entity.getAllNesteds())) {
					errors.add(new ValidationError("val.ambiguous.fieldornested", field.getName()));
				}
				if (field.getColumnName() != null) {
					if (!isNameLengthAllowed(field.getColumnName())) {
						errors.add(new ValidationError("val.toolong.fieldvalue", "label.columnname", 
													   String.valueOf(getMaxNameLength())));
					}
					else if (!isUnique(field.getColumnName(), "columnName", entity.getAllFields())) {
						errors.add(new ValidationError("val.ambiguous.columnname", field.getColumnName()));
					}
				}
				if (field.getFormula() != null &&
					field.getFormula().length() > getLimit("entity.stringfield.length")) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.calculationformula", "label.field",
												   String.valueOf(getLimit("entity.stringfield.length"))));
				}
				if (field.getAutonumPattern() != null &&
					field.getAutonumPattern().length() > getLimit("entity.stringfield.length")) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.pattern", "label.field",
												   String.valueOf(getLimit("entity.stringfield.length"))));
				}
				
				if (isEmpty(field.getType())) {
					errors.add(new ValidationError("val.empty.field", "label.datatype"));
				}
				else if (field.getType().isAutonum()) {
					if (!foundAutonum) {
						foundAutonum = true;
					}
					else {
						errors.add(new ValidationError("val.ambiguous.autonum"));
					}
				}
				else if (field.getType().isReference()) {
					if (isEmpty(field.getReferenceEntity())) {
						errors.add(new ValidationError("val.empty.field", "label.refentity"));
					}
					else if (isEmpty(field.getReferenceEntityField())) {
						errors.add(new ValidationError("val.empty.field", "label.reffield"));
					}
				}
				if (field.isCalculated() && isEmpty(field.getFormula())) {
					errors.add(new ValidationError("val.empty.field", "label.calculationformula"));
				}
			}
		}
		
		// functions
		if (entity.hasFunctions()) {
			for (EntityFunction function : entity.getFunctions()) {
				if (isEmpty(function.getName())) {
					errors.add(new ValidationError("val.empty.field", "label.functionname"));
				}
				else if (!isNameLengthAllowed(function.getName())) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.name", "label.function", 
												   String.valueOf(getMaxNameLength())));
				}
				else if (!isNameAllowed(function.getInternalName())) {
					errors.add(new ValidationError("val.illegal.field", "label.functionname", function.getName()));
				}
 				else if (!isNameUnique(function.getName(), entity.getAllFunctions())) {
					errors.add(new ValidationError("val.ambiguous.functionname", function.getName()));
				}
			}
		}
		
		// nesteds
		if (entity.hasNesteds()) {
			for (NestedEntity nested : entity.getNesteds()) {
				if (isEmpty(nested.getName())) {
					errors.add(new ValidationError("val.empty.field", "label.nestedname"));
				}
				else if (!isNameLengthAllowed(nested.getName())) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.name", "label.nested", 
												   String.valueOf(getMaxNameLength())));
				}
				else if (!isNameAllowed(nested.getInternalName())) {
					errors.add(new ValidationError("val.illegal.field", "label.nestedname", nested.getName()));
				}
				else if (!isNameUnique(nested.getName(), entity.getAllNesteds(), entity.getAllFields())) {
					errors.add(new ValidationError("val.ambiguous.fieldornested", nested.getName()));
				}
				if (isEmpty(nested.getNestedEntity())) {
					errors.add(new ValidationError("val.empty.field", "label.nested"));
				}
				if (isEmpty(nested.getReferenceField())) {
					errors.add(new ValidationError("val.empty.field", "label.reffield"));
				}
			}
		}
		
		// status
		if (entity.hasStatus()) {
			int counterInitialStatus = 0;
			for (EntityStatus status : entity.getStatusList()) {
				if (status.isInitial()) {
					counterInitialStatus++;
				}
				if (isEmpty(status.getName())) {
					errors.add(new ValidationError("val.empty.field", "label.statusname"));
				}
				else if (!isNameLengthAllowed(status.getName())) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.name", "label.status", 
												   String.valueOf(getMaxNameLength())));
				}
				else if (!isNameUnique(status.getName(), entity.getStatusList())) {
					errors.add(new ValidationError("val.ambiguous.statusname", status.getName()));
				}
				if (isEmpty(status.getStatusNumber())) {
					errors.add(new ValidationError("val.empty.field", "label.statusnumber"));
				}
				else if (!isUnique(status.getStatusNumber(), "statusNumber", entity.getStatusList())) {
					errors.add(new ValidationError("val.ambiguous.statusnumber", status.getStatusNumber().toString()));
				}
			}
			if (counterInitialStatus == 0) {
				errors.add(new ValidationError("val.empty.initialstate"));
			}
			else if (counterInitialStatus > 1) {
				errors.add(new ValidationError("val.ambiguous.initialstate"));
			}
		}
		
		// status transitions
		if (entity.hasStatusTransitions()) {
			for (EntityStatusTransition transition : entity.getStatusTransitions()) {
				boolean fieldMissing = false;
				if (isEmpty(transition.getSourceStatus())) {
					errors.add(new ValidationError("val.empty.field", "label.sourcestatus"));
					fieldMissing = true;
				}
				if (isEmpty(transition.getTargetStatus())) {
					errors.add(new ValidationError("val.empty.field", "label.targetstatus"));
					fieldMissing = true;
				}
				if (fieldMissing) {
					continue;
				}
				if (transition.getSourceStatus().equals(transition.getTargetStatus())) {
					errors.add(new ValidationError("val.same.status"));
					continue;
				}
				for (EntityStatusTransition tran : entity.getStatusTransitions()) {
					if (transition != tran && 
						ObjectUtils.nullSafeEquals(transition.getSourceStatus(), tran.getSourceStatus()) &&
						ObjectUtils.nullSafeEquals(transition.getTargetStatus(), tran.getTargetStatus())) {
						errors.add(new ValidationError("val.ambiguous.statustransition", 
													   transition.getSourceStatus().getStatusNumber().toString(),
													   transition.getTargetStatus().getStatusNumber().toString()));
						break;
					}
				}
 			}
		}
		
		// field constraints
		if (entity.hasFieldConstraints()) {
			for (EntityFieldConstraint constraint : entity.getFieldConstraints()) {
				boolean fieldMissing = false;
				if (isEmpty(constraint.getField()) && isEmpty(constraint.getFieldGroup())) {
					errors.add(new ValidationError("val.empty.constraintfieldorgroup"));
					fieldMissing = true;
				}
				else if (!isEmpty(constraint.getField()) && !isEmpty(constraint.getFieldGroup())) {
					errors.add(new ValidationError("val.ambiguous.constraintfieldorgroup"));
					fieldMissing = true;
				}
				if (isEmpty(constraint.getAccess())) {
					errors.add(new ValidationError("val.empty.constraintfield", "label.access"));
					fieldMissing = true;
				}
				if (isEmpty(constraint.getStatus()) && isEmpty(constraint.getUserGroup())) {
					errors.add(new ValidationError("val.empty.constraintincomplete"));
					fieldMissing = true;
				}
				// find duplicate
				if (!fieldMissing) {
					for (EntityFieldConstraint constr : entity.getFieldConstraints()) {
						if (constraint != constr &&
							ObjectUtils.nullSafeEquals(constraint.getField(), constr.getField()) &&
							ObjectUtils.nullSafeEquals(constraint.getFieldGroup(), constr.getFieldGroup()) &&
							ObjectUtils.nullSafeEquals(constraint.getStatus(), constr.getStatus()) &&
							ObjectUtils.nullSafeEquals(constraint.getUserGroup(), constr.getUserGroup())) {
							errors.add(new ValidationError("val.ambiguous.fieldconstraint", 
														   constraint.getField().getName()));
							break;
						}
					}
				}
			}
		}
		
		validate(errors);
	}
	
	@Override
	public void validateDelete(Entity entity) throws ValidationException {
		Assert.notNull(entity, "entity is null");
		final Set<ValidationError> errors = createErrorList();
		
		for (EntityDependent dependent : entityDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(entity)) {
				switch (getEntityType(systemEntity)) {
					case "datasource":
						errors.add(new ValidationError("val.inuse.entitydatasource", systemEntity.getName()));
						break;
					case "entity":
						errors.add(new ValidationError("val.inuse.entityentity", systemEntity.getName()));
						break;
					case "filter":
						errors.add(new ValidationError("val.inuse.entityfilter", systemEntity.getName()));
						break;
					case "form":
						errors.add(new ValidationError("val.inuse.entityform", systemEntity.getName()));
						break;
					case "transform":
						errors.add(new ValidationError("val.inuse.entitytransform", systemEntity.getName()));
						break;
					case "value":
						errors.add(new ValidationError("val.inuse.entityvalue"));
						break;
					default:
						throw new IllegalStateException("unhandled entity: " + getEntityType(systemEntity));
				}
			}
		}
		
		validate(errors);
	}
	
	public void validateRemoveField(EntityField field) throws ValidationException {
		Assert.notNull(field, "field is null");
		final Set<ValidationError> errors = createErrorList();
		
		for (EntityDependent dependent : entityDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(field)) {
				switch (getEntityType(systemEntity)) {
					case "entity":
						errors.add(new ValidationError("val.inuse.fieldentity", systemEntity.getName()));
						break;
					case "filter":
						errors.add(new ValidationError("val.inuse.fieldfilter", systemEntity.getName()));
						break;
					case "form":
						errors.add(new ValidationError("val.inuse.fieldform", systemEntity.getName()));
						break;
					case "transform":
						errors.add(new ValidationError("val.inuse.fieldtransform", systemEntity.getName()));
						break;
					default:
						throw new IllegalStateException("unhandled entity: " + getEntityType(systemEntity));
				}
			}
		}
		
		validate(errors);
	}
	
	public void validateRemoveFieldGroup(EntityFieldGroup fieldGroup) throws ValidationException {
		Assert.notNull(fieldGroup, "fieldGroup is null");
		final Set<ValidationError> errors = createErrorList();
		
		for (EntityDependent dependent : entityDependents) {
			if (dependent instanceof EntityService) {
				for (SystemEntity systemEntity : dependent.findUsage(fieldGroup)) {
					errors.add(new ValidationError("val.inuse.fieldgroupentity", systemEntity.getName()));
				}
				break;
			}
		}
		
		validate(errors);
	}
	
	public void validateRemoveFunction(EntityFunction function) throws ValidationException {
		Assert.notNull(function, "function is null");
		final Set<ValidationError> errors = createErrorList();
		
		for (EntityDependent dependent : entityDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(function)) {
				switch (getEntityType(systemEntity)) {
					case "form":
						errors.add(new ValidationError("val.inuse.functionform", systemEntity.getName()));
						break;
					default:
						throw new IllegalStateException("unhandled entity: " + getEntityType(systemEntity));
				}
			}
		}
		
		validate(errors);
	}
	
	public void validateRemoveStatus(EntityStatus status) throws ValidationException {
		Assert.notNull(status, "status is null");
		final Set<ValidationError> errors = createErrorList();
		
		for (EntityDependent dependent : entityDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(status)) {
				switch (getEntityType(systemEntity)) {
					case "value":
						errors.add(new ValidationError("val.inuse.status"));
						break;
					default:
						throw new IllegalStateException("unhandled entity: " + getEntityType(systemEntity));
				}
			}
		}
		
		validate(errors);
	}
	
	public void validateRemoveNested(NestedEntity nestedEntity) throws ValidationException {
		Assert.notNull(nestedEntity, "nestedEntity is null");
		final Set<ValidationError> errors = createErrorList();
		
		for (EntityDependent dependent : entityDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(nestedEntity)) {
				switch (getEntityType(systemEntity)) {
					case "form":
						errors.add(new ValidationError("val.inuse.nestedform", systemEntity.getName()));
						break;
				}
			}
		}
		
		validate(errors);
	}

}
