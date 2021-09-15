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

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;
import org.seed.core.util.NameUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class EntityValidator extends AbstractSystemEntityValidator<Entity> {
	
	@Autowired
	private EntityRepository repository;
	
	@Autowired
	private List<EntityDependent<? extends SystemEntity>> entityDependents;
	
	@Override
	public void validateSave(Entity entity) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		final ValidationErrors errors = new ValidationErrors();
		
		// name
		validateName(entity, errors);
		
		// identifier
		if (entity.getIdentifierPattern() != null && 
			!isNameLengthAllowed(entity.getIdentifierPattern())) {
			errors.addOverlongField("label.identifier", getMaxNameLength());
		}
		
		// field groups
		if (entity.hasFieldGroups()) {
			validateFieldGroups(entity, errors);
		}
		
		// fields
		if (entity.hasFields()) {
			validateFields(entity, errors);
		}
		
		// functions
		if (entity.hasFunctions()) {
			validateFunctions(entity, errors);
		}
		
		// nesteds
		if (entity.hasNesteds()) {
			validateNesteds(entity, errors);
		}
		
		// status
		if (entity.hasStatus()) {
			validateStatus(entity, errors);
		}
		
		// status transitions
		if (entity.hasStatusTransitions()) {
			validateStatusTransitions(entity, errors);
		}
		
		// field constraints
		if (entity.hasFieldConstraints()) {
			validateFieldConstraints(entity, errors);
		}
		
		validate(errors);
	}
	
	@Override
	public void validateDelete(Entity entity) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		final ValidationErrors errors = new ValidationErrors();
		
		for (EntityDependent<? extends SystemEntity> dependent : entityDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(entity)) {
				switch (getEntityType(systemEntity)) {
					case "datasource":
						errors.addError("val.inuse.entitydatasource", systemEntity.getName());
						break;
						
					case "entity":
						errors.addError("val.inuse.entityentity", systemEntity.getName());
						break;
						
					case "filter":
						errors.addError("val.inuse.entityfilter", systemEntity.getName());
						break;
						
					case "form":
						errors.addError("val.inuse.entityform", systemEntity.getName());
						break;
						
					case "transform":
						errors.addError("val.inuse.entitytransform", systemEntity.getName());
						break;
						
					case "value":
						errors.addError("val.inuse.entityvalue");
						break;
						
					default:
						unhandledEntity(systemEntity);
				}
			}
		}
		
		validate(errors);
	}
	
	public void validateRemoveField(EntityField field) throws ValidationException {
		Assert.notNull(field, C.FIELD);
		final ValidationErrors errors = new ValidationErrors();
		
		// check if field is used in field formula
		for (EntityField entityField : field.getEntity().getFields()) {
			if (!entityField.equals(field) && 
				entityField.getFormula() != null &&
				entityField.getFormula().contains(EntityChangeLogBuilder.getColumnName(field))) {
					errors.addError("val.inuse.fieldformula", entityField.getName());
			}
		}
		for (EntityDependent<? extends SystemEntity> dependent : entityDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(field)) {
				switch (getEntityType(systemEntity)) {
					case "entity":
						errors.addError("val.inuse.fieldentity", systemEntity.getName());
						break;
						
					case "filter":
						errors.addError("val.inuse.fieldfilter", systemEntity.getName());
						break;
						
					case "form":
						errors.addError("val.inuse.fieldform", systemEntity.getName());
						break;
						
					case "transform":
						errors.addError("val.inuse.fieldtransform", systemEntity.getName());
						break;
						
					default:
						unhandledEntity(systemEntity);
				}
			}
		}
		
		validate(errors);
	}
	
	public void validateRemoveFieldGroup(EntityFieldGroup fieldGroup) throws ValidationException {
		Assert.notNull(fieldGroup, C.FIELDGROUP);
		final ValidationErrors errors = new ValidationErrors();
		
		for (EntityDependent<? extends SystemEntity> dependent : entityDependents) {
			if (dependent instanceof EntityService) {
				for (SystemEntity systemEntity : dependent.findUsage(fieldGroup)) {
					errors.addError("val.inuse.fieldgroupentity", systemEntity.getName());
				}
				break;
			}
		}
		
		validate(errors);
	}
	
	public void validateRemoveFunction(EntityFunction function) throws ValidationException {
		Assert.notNull(function, C.FUNCTION);
		final ValidationErrors errors = new ValidationErrors();
		
		for (EntityDependent<? extends SystemEntity> dependent : entityDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(function)) {
				if (C.FORM.equals(getEntityType(systemEntity))) {
					errors.addError("val.inuse.functionform", systemEntity.getName());
				}
			}
		}
		
		validate(errors);
	}
	
	public void validateRemoveStatus(EntityStatus status) throws ValidationException {
		Assert.notNull(status, C.STATUS);
		final ValidationErrors errors = new ValidationErrors();
		
		for (EntityDependent<? extends SystemEntity> dependent : entityDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(status)) {
				if (C.VALUE.equals(getEntityType(systemEntity))) {
					errors.addError("val.inuse.status");
				}
			}
		}
		
		validate(errors);
	}
	
	public void validateRemoveNested(NestedEntity nestedEntity) throws ValidationException {
		Assert.notNull(nestedEntity, C.NESTEDENTITY);
		final ValidationErrors errors = new ValidationErrors();
		
		for (EntityDependent<? extends SystemEntity> dependent : entityDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(nestedEntity)) {
				if (C.FORM.equals(getEntityType(systemEntity))) {
					errors.addError("val.inuse.nestedform", systemEntity.getName());
				}
			}
		}
		
		validate(errors);
	}
	
	private void validateFields(Entity entity, final ValidationErrors errors) {
		int numAutonums = 0;
		for (EntityField field : entity.getFields()) {
			validateFieldName(entity, field, errors);
			if (field.getColumnName() != null) {
				validateColumnName(entity, field, errors);
			}
			if (field.isCalculated()) {
				validateCalculatedField(entity, field, errors);
			}
			if (isEmpty(field.getType())) {
				errors.addEmptyField("label.datatype");
			}
			else if (field.getType().isAutonum()) {
				if (++numAutonums > 1) {
					errors.addError("val.ambiguous.autonum");
				}
			}
			else if (field.getType().isReference() && 
				     isEmpty(field.getReferenceEntity())) {
				errors.addEmptyField("label.refentity");
			}
		}
	}
	
	private void validateName(Entity entity, final ValidationErrors errors) {
		// name
		if (isEmpty(entity.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(entity.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		else if ((entity.getTableName() == null && entity.getInternalName().toLowerCase().startsWith("sys_")) || 
				 !isNameAllowed(entity.getInternalName())) {
			errors.addIllegalName(entity.getName());
		}
		
		// table name
		if (entity.getTableName() != null) {
			if (entity.getTableName().toLowerCase().startsWith("sys_")) {
				errors.addIllegalField("label.tablename", entity.getTableName());
			}
			else if (!isNameLengthAllowed(entity.getTableName())) {
				errors.addOverlongField("label.tablename", getMaxNameLength());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateFieldGroups(Entity entity, final ValidationErrors errors) {
		for (EntityFieldGroup fieldGroup : entity.getFieldGroups()) {
			if (isEmpty(fieldGroup.getName())) {
				errors.addEmptyField("label.fieldgroupname");
			}
			else if (!isNameLengthAllowed(fieldGroup.getName())) {
				errors.addOverlongObjectName("label.fieldgroup", getMaxNameLength());
			}
			else if (!isNameUnique(fieldGroup.getName(), entity.getAllFieldGroups())) {
				errors.addError("val.ambiguous.fieldgroup", fieldGroup.getName());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateFieldName(Entity entity, EntityField field, ValidationErrors errors) {
		if (isEmpty(field.getName())) {
			errors.addEmptyField("label.fieldname");
		}
		else if (!isNameLengthAllowed(field.getName())) {
			errors.addOverlongObjectName("label.field", getMaxNameLength());
		}
		else if (!isNameAllowed(field.getInternalName()) ||
				 NameUtils.isIllegalFieldName(field.getInternalName())) {
			errors.addIllegalField("label.fieldname", field.getName());
		}
		else if (!isNameUnique(field.getName(), entity.getAllFields(), entity.getAllNesteds())) {
			errors.addError("val.ambiguous.fieldornested", field.getName());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateColumnName(Entity entity, EntityField field, final ValidationErrors errors) {
		if (!isNameLengthAllowed(field.getColumnName())) {
			errors.addOverlongField("label.columnname", getMaxNameLength());
		}
		else if (!isUnique(field.getColumnName(), "columnName", entity.getAllFields())) {
			errors.addError("val.ambiguous.columnname", field.getColumnName());
		}
		else if (NameUtils.isIllegalFieldName(field.getColumnName())) {
			errors.addIllegalField("label.columnname", field.getColumnName());
		}
	}
	
	private void validateCalculatedField(Entity entity, EntityField field, final ValidationErrors errors) {
		if (isEmpty(field.getFormula())) {
			errors.addEmptyField("label.calculationformula");
		}
		else if (field.getFormula().length() > getMaxStringLength()) {
			errors.addOverlongField("label.calculationformula", getMaxStringLength());
		}
		else if (!testFormula(entity, field.getFormula())) {
			errors.addError("val.illegal.formula", field.getName());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateFunctions(Entity entity, final ValidationErrors errors) {
		for (EntityFunction function : entity.getFunctions()) {
			if (isEmpty(function.getName())) {
				errors.addEmptyField("label.functionname");
			}
			else if (!isNameLengthAllowed(function.getName())) {
				errors.addOverlongObjectName("label.function", getMaxNameLength());
			}
			else if (!isNameAllowed(function.getInternalName())) {
				errors.addIllegalField("label.functionname", function.getName());
			}
			else if (!isNameUnique(function.getName(), entity.getAllFunctions())) {
				errors.addError("val.ambiguous.functionname", function.getName());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateNesteds(Entity entity, final ValidationErrors errors) {
		for (NestedEntity nested : entity.getNesteds()) {
			if (isEmpty(nested.getName())) {
				errors.addEmptyField("label.nestedname");
			}
			else if (!isNameLengthAllowed(nested.getName())) {
				errors.addOverlongObjectName("label.nested", getMaxNameLength());
			}
			else if (!isNameAllowed(nested.getInternalName())) {
				errors.addIllegalField("label.nestedname", nested.getName());
			}
			else if (!isNameUnique(nested.getName(), entity.getAllNesteds(), entity.getAllFields())) {
				errors.addError("val.ambiguous.fieldornested", nested.getName());
			}
			if (isEmpty(nested.getNestedEntity())) {
				errors.addEmptyField("label.nested");
			}
			if (isEmpty(nested.getReferenceField())) {
				errors.addEmptyField("label.reffield");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateStatus(Entity entity, final ValidationErrors errors) {
		int counterInitialStatus = 0;
		for (EntityStatus status : entity.getStatusList()) {
			if (status.isInitial()) {
				counterInitialStatus++;
			}
			if (isEmpty(status.getName())) {
				errors.addEmptyField("label.statusname");
			}
			else if (!isNameLengthAllowed(status.getName())) {
				errors.addOverlongObjectName("label.status", getMaxNameLength());
			}
			else if (!isNameUnique(status.getName(), entity.getStatusList())) {
				errors.addError("val.ambiguous.statusname", status.getName());
			}
			if (isEmpty(status.getStatusNumber())) {
				errors.addEmptyField("label.statusnumber");
			}
			else if (!isUnique(status.getStatusNumber(), "statusNumber", entity.getStatusList())) {
				errors.addError("val.ambiguous.statusnumber", status.getStatusNumber().toString());
			}
		}
		if (counterInitialStatus == 0) {
			errors.addError("val.empty.initialstate");
		}
		else if (counterInitialStatus > 1) {
			errors.addError("val.ambiguous.initialstate");
		}
	}
	
	private void validateStatusTransitions(Entity entity, ValidationErrors errors) {
		for (EntityStatusTransition transition : entity.getStatusTransitions()) {
			boolean fieldMissing = false;
			if (isEmpty(transition.getSourceStatus())) {
				errors.addEmptyField("label.sourcestatus");
				fieldMissing = true;
			}
			if (isEmpty(transition.getTargetStatus())) {
				errors.addEmptyField("label.targetstatus");
				fieldMissing = true;
			}
			if (fieldMissing) {
				continue;
			}
			if (transition.getSourceStatus().equals(transition.getTargetStatus())) {
				errors.addError("val.same.status");
			}
			else if (!entity.isUnique(transition)) {
				errors.addError("val.ambiguous.statustransition", 
						   transition.getSourceStatus().getStatusNumber().toString(),
						   transition.getTargetStatus().getStatusNumber().toString());
			}
		}
	}
	
	private void validateFieldConstraints(Entity entity, ValidationErrors errors) {
		for (EntityFieldConstraint constraint : entity.getFieldConstraints()) {
			boolean fieldMissing = false;
			if (isEmpty(constraint.getField()) && isEmpty(constraint.getFieldGroup())) {
				errors.addError("val.empty.constraintfieldorgroup");
				fieldMissing = true;
			}
			else if (!isEmpty(constraint.getField()) && !isEmpty(constraint.getFieldGroup())) {
				errors.addError("val.ambiguous.constraintfieldorgroup");
				fieldMissing = true;
			}
			if (isEmpty(constraint.getAccess())) {
				errors.addError("val.empty.constraintfield", "label.access");
				fieldMissing = true;
			}
			if (isEmpty(constraint.getStatus()) && isEmpty(constraint.getUserGroup())) {
				errors.addError("val.empty.constraintincomplete");
				fieldMissing = true;
			}
			// find duplicate
			if (!fieldMissing) {
				validateContraintDuplicate(entity, constraint, errors);
			}
		}
	}
	
	private void validateContraintDuplicate(Entity entity, EntityFieldConstraint constraint, ValidationErrors errors) {
		for (EntityFieldConstraint constr : entity.getFieldConstraints()) {
			if (constraint != constr &&
				ObjectUtils.nullSafeEquals(constraint.getField(), constr.getField()) &&
				ObjectUtils.nullSafeEquals(constraint.getFieldGroup(), constr.getFieldGroup()) &&
				ObjectUtils.nullSafeEquals(constraint.getStatus(), constr.getStatus()) &&
				ObjectUtils.nullSafeEquals(constraint.getUserGroup(), constr.getUserGroup())) {
				errors.addError("val.ambiguous.fieldconstraint", 
								constraint.getField().getName());
				break;
			}
		}
	}
	
	private boolean testFormula(Entity entity, String formula) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(formula, "formula");
		
		final StringBuilder buf = new StringBuilder()
									.append("select ").append(formula).append(" from ")
									.append(EntityChangeLogBuilder.getTableName(entity));
		try (Session session = repository.getSession()) {
			session.createSQLQuery(buf.toString())
					.setMaxResults(0).list();
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}
	
}
