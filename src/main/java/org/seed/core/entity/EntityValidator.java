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

import static org.seed.core.util.CollectionUtils.*;

import java.util.List;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.codegen.CodeUtils;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.data.dbobject.DBObjectService;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.NameUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class EntityValidator extends AbstractSystemEntityValidator<Entity> {
	
	@Autowired
	private EntityRepository repository;
	
	@Autowired
	private DBObjectService dbObjectService;
	
	private List<EntityDependent<? extends SystemEntity>> entityDependents;
	
	void validateCreateNested(Entity entity) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		final var errors = createValidationErrors(entity);
		
		if (isEmpty(((EntityMetadata) entity).getParentEntity())) {
			errors.addError("val.empty.parententity");
		}
		
		validate(errors);
	}
		
	@Override
	public void validateSave(Entity entity) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		final var errors = createValidationErrors(entity);
		
		// name
		validateName(entity, errors);
		// table name
		validateTableName(entity, errors);
		
		// parent dependencies
		if (entity.isNew()) {
			validateParentCreate(entity, errors);
		}
		else {
			validateParentEdit(entity, errors);
		}
		
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
			if (!entity.isNew()) {
				validateFieldAttributeChanges(entity, errors);
			}
		}
		
		// functions
		if (entity.hasFunctions()) {
			validateFunctions(entity, errors);
		}
		
		// nesteds
		if (entity.hasNesteds()) {
			validateNesteds(entity, errors);
		}
		
		// relations
		if (entity.hasRelations()) {
			validateRelations(entity, errors);
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
		final var errors = createValidationErrors(entity);
		
		if (entity.hasNesteds()) {
			errors.addError("val.delete.nestedexist");
		}
		else {
			try (Session session = repository.getSession()) {
				for (var dependent : getEntityDependents()) {
					validateDeleteEntityDependent(entity, dependent, errors, session);
				}
			}
		}
		validate(errors);
	}
	
	public void validateRemoveField(EntityField field) throws ValidationException {
		Assert.notNull(field, C.FIELD);
		final var errors = createValidationErrors(field.getEntity());
		
		// check if field is used in field formula
		for (EntityField entityField : field.getEntity().getFields()) {
			if (!entityField.equals(field) && 
				entityField.getFormula() != null &&
				entityField.getFormula().contains(field.getEffectiveColumnName())) {
					errors.addError("val.inuse.fieldformula", entityField.getName());
			}
		}
		try (Session session = repository.getSession()) {
			for (var dependent : getEntityDependents()) {
				validateRemoveFieldDependent(field, dependent, errors, session);
			}
		}
		validate(errors);
	}
	
	public void validateRemoveFieldGroup(EntityFieldGroup fieldGroup) throws ValidationException {
		Assert.notNull(fieldGroup, C.FIELDGROUP);
		final var errors = createValidationErrors(fieldGroup.getEntity());
		
		for (var dependent : getEntityDependents()) {
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
		final var errors = createValidationErrors(function.getEntity());
		
		try (Session session = repository.getSession()) {
			for (var dependent : getEntityDependents()) {
				for (SystemEntity systemEntity : dependent.findUsage(function, session)) {
					switch (getEntityType(systemEntity)) {
						case C.ENTITY: 
							errors.addError("val.inuse.functionstatus");
							break;
							
						case C.FORM:
							errors.addError("val.inuse.functionform", systemEntity.getName());
							break;
							
						default:
							unhandledEntity(systemEntity);
					}
				}
			}
		}
		validate(errors);
	}
	
	public void validateRemoveStatus(EntityStatus status) throws ValidationException {
		Assert.notNull(status, C.STATUS);
		final var errors = createValidationErrors(status.getEntity());
		
		try (Session session = repository.getSession()) {
			for (var dependent : getEntityDependents()) {
				validateRemoveStatusDependent(dependent, status, errors, session);
			}
		}
		validate(errors);
	}
	
	public void validateRemoveNested(NestedEntity nestedEntity) throws ValidationException {
		Assert.notNull(nestedEntity, C.NESTEDENTITY);
		final var errors = createValidationErrors(nestedEntity.getParentEntity());
		
		try (Session session = repository.getSession()) {
			for (var dependent : getEntityDependents()) {
				for (SystemEntity systemEntity : dependent.findUsage(nestedEntity, session)) {
					switch (getEntityType(systemEntity)) {
						case C.ENTITY:
							errors.addError("val.inuse.nestedconstraint");
							break;
							
						case C.FORM:
							errors.addError("val.inuse.nestedform", systemEntity.getName());
							break;
							
						default:
							unhandledEntity(systemEntity);
					}
				}
			}
		}
		validate(errors);
	}
	
	public void validateRemoveRelation(EntityRelation relation) throws ValidationException {
		Assert.notNull(relation, C.RELATION);
		final var errors = createValidationErrors(relation.getEntity());
		
		try (Session session = repository.getSession()) {
			for (var dependent : getEntityDependents()) {
				for (SystemEntity systemEntity : dependent.findUsage(relation, session)) {
					if (C.FORM.equals(getEntityType(systemEntity))) {
						errors.addError("val.inuse.relationform", systemEntity.getName());
					}
					else {
						unhandledEntity(systemEntity);
					}
				}
			}
		}
		validate(errors);
	}
	
	private void validateFields(Entity entity, final ValidationErrors errors) {
		int numAutonums = 0;
		for (EntityField field : entity.getFields()) {
			validateFieldName(entity, field, errors);
			validateValidationPattern(field, errors);
			if (field.getColumnName() != null) {
				validateColumnName(entity, field, errors);
			}
			else if (field.getInternalName() != null) {
				validateDerivedColumnName(field, errors);
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
	
	private void validateFieldAttributeChanges(Entity entity, final ValidationErrors errors) {
		try (Session session = repository.getSession()) {
			final Entity currentVersionEntity = repository.get(entity.getId(), session);
			if (currentVersionEntity != null) {
				validateFieldAttributeChange(entity, currentVersionEntity, session, errors);
			}
		}
	}
	
	private void validateFieldAttributeChange(Entity entity, Entity currentVersionEntity, 
											  Session session, final ValidationErrors errors) {
		for (EntityField field : subList(entity.getFields(), not(EntityField::isNew))) {
			final EntityField currentVersionField = currentVersionEntity.getFieldById(field.getId());
			Assert.stateAvailable(currentVersionField, "current version field");
			
			// field type change
			if (field.getType() != currentVersionField.getType()) {
				dbObjectService.findViewsContains(entity.getInternalName())
							   .forEach(view -> errors.addError("val.inuse.entityview", view.getName()));
			}
			// change to unique
			if (field.isUnique() && !currentVersionField.isUnique() &&
				!repository.areColumnValuesUnique(currentVersionEntity, currentVersionField, session)) {
				errors.addError("val.illegal.uniquechange", field.getName());
			}
			// unique and change to mandatory
			if (field.isUnique() && field.isMandatory() && !currentVersionField.isMandatory()) {
				if (repository.countEmptyColumnValues(currentVersionEntity, currentVersionField, session) > 1) {
					errors.addError("val.illegal.mandatorychange", field.getName());
				}
				else if (field.getDefaultNumber() != null &&
						 repository.countColumnValue(currentVersionEntity, currentVersionField, field.getDefaultNumber(), session) > 0) {
					errors.addError("val.illegal.mandatorydefault", field.getDefaultNumber().toString(), field.getName());
				}
			}
		}
	}
	
	private void validateParentCreate(Entity entity, final ValidationErrors errors) {
		final Entity parent = ((EntityMetadata) entity).getParentEntity();
		if (parent != null) {
			// ref field to parent
			if (entity.getReferenceFields(parent).isEmpty()) {
				errors.addError("val.missing.fieldtoparent", parent.getName());
			}
			// audited state
			if (parent.isAudited() && !entity.isAudited()) {
				errors.addError("val.illegal.parentaudited", parent.getName());
			}
		}
	}
	
	private void validateParentEdit(Entity entity, final ValidationErrors errors) {
		for (Entity parent : repository.findParentEntities(entity)) {
			// audited state
			if (parent.isAudited() && !entity.isAudited()) {
				errors.addError("val.illegal.parentaudited", parent.getName());
			}
		}
	}
	
	private void validateName(Entity entity, final ValidationErrors errors) {
		if (isEmpty(entity.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(entity.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		else if (NameUtils.startsWithNumber(entity.getName())) {
			errors.addError("val.illegal.namestartswithnumber");
		}
	}
	
	private void validateTableName(Entity entity, final ValidationErrors errors) {
		if (entity.getTableName() != null) {
			if (!entity.getTableName().equals(NameUtils.getInternalName(entity.getTableName())) ||
				entity.getTableName().toLowerCase().startsWith("sys_") ||
				NameUtils.isSqlKeyword(entity.getTableName().toLowerCase())) {
				errors.addIllegalField("label.tablename", entity.getTableName());
			}
			else if (!isNameLengthAllowed(entity.getTableName())) {
				errors.addOverlongField("label.tablename", getMaxNameLength());
			}
		}
		else if (entity.getInternalName() != null &&
				 (entity.getInternalName().toLowerCase().startsWith("sys_") || 
				  NameUtils.isSqlKeyword(entity.getInternalName().toLowerCase()))) {
			errors.addError("val.illegal.tablename", entity.getName(), entity.getInternalName().toLowerCase());
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
		else if (!isNameUnique(field.getName(), entity.getAllFields(), entity.getNesteds(), entity.getAllRelations())) {
			errors.addError(AMBIGUOUS_FIELD_OR_NESTED, field.getName());
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
		else if (!field.getColumnName().equals(NameUtils.getInternalName(field.getColumnName())) ||
				 NameUtils.isIllegalColumnName(field.getColumnName())) {
			errors.addIllegalField("label.columnname", field.getColumnName());
		}
	}
	
	private void validateDerivedColumnName(EntityField field, final ValidationErrors errors) {
		if (NameUtils.isIllegalColumnName(field.getInternalName())) {
			errors.addError("val.illegal.columnname", field.getName(), field.getInternalName());
		}
	}
	
	private void validateValidationPattern(EntityField field, final ValidationErrors errors) {
		if (field.getType() != null && field.getType().supportsValidation() && 
			field.getValidationPattern() != null && field.getValidationPattern().length() > getMaxStringLength()) {
			errors.addOverlongField(field.getName(), getMaxStringLength());
		}
	}
	
	private void validateCalculatedField(Entity entity, EntityField field, final ValidationErrors errors) {
		if (isEmpty(field.getFormula())) {
			errors.addEmptyField("label.calculationformula");
		}
		else if (field.getFormula().length() > getMaxStringLength()) {
			errors.addOverlongField("label.calculationformula", getMaxStringLength());
		}
		else if (!repository.testFormula(entity, field.getFormula())) {
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
			else if (isEmpty(function.getContent())) {
				errors.addError("val.empty.functioncode", function.getName());
			}
			else if (function.isCallback()) {
				final String className = CodeUtils.extractClassName(
						CodeUtils.extractQualifiedName(function.getContent()));
				if (!function.getGeneratedClass().equals(className)) {
					errors.addError("val.illegal.functionclassname", function.getName(), function.getGeneratedClass());
				}
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
			else if (!isNameUnique(nested.getName(), entity.getNesteds(), 
								   entity.getAllFields(), entity.getAllRelations())) {
				errors.addError(AMBIGUOUS_FIELD_OR_NESTED, nested.getName());
			}
			if (isEmpty(nested.getNestedEntity())) {
				errors.addEmptyField("label.nested");
			}
			else if (entity.isAudited() && !nested.getNestedEntity().isAudited()) {
				errors.addError("val.illegal.nestedaudited", nested.getName());
			}
			if (isEmpty(nested.getReferenceField())) {
				errors.addEmptyField("label.reffield");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateRelations(Entity entity, final ValidationErrors errors) {
		for (EntityRelation relation : entity.getRelations()) {
			if (isEmpty(relation.getName())) {
				errors.addEmptyField("label.relationname");
			}
			else if (!isNameLengthAllowed(relation.getName())) {
				errors.addOverlongObjectName("label.relation", getMaxNameLength());
			}
			else if (!isNameAllowed(relation.getInternalName())) {
				errors.addIllegalField("label.relationname", relation.getName());
			}
			else if (!isNameUnique(relation.getName(), entity.getNesteds(), 
								   entity.getAllFields(), entity.getAllRelations())) {
				errors.addError(AMBIGUOUS_FIELD_OR_NESTED, relation.getName());
			}
			if (isEmpty(relation.getRelatedEntity())) {
				errors.addEmptyField("label.relatedentity");
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
	
	private void validateDeleteEntityDependent(Entity entity, EntityDependent<? extends SystemEntity> dependent,
			 ValidationErrors errors, Session session) {
		for (SystemEntity systemEntity : dependent.findUsage(entity, session)) {
			switch (getEntityType(systemEntity)) {
				case "datasource":
					errors.addError("val.inuse.entitydatasource", systemEntity.getName());
					break;
	
				case C.ENTITY:
					errors.addError("val.inuse.entityentity", systemEntity.getName());
					break;
	
				case C.FILTER:
					errors.addError("val.inuse.entityfilter", systemEntity.getName());
					break;
	
				case C.FORM:
					errors.addError("val.inuse.entityform", systemEntity.getName());
					break;
					
				case C.TRANSFER:
					errors.addError("val.inuse.entitytransfer", systemEntity.getName());
					break;
	
				case "transform":
					errors.addError("val.inuse.entitytransform", systemEntity.getName());
					break;
	
				case C.VALUE:
					errors.addError("val.inuse.entityvalue");
					break;
	
				default:
					unhandledEntity(systemEntity);
			}
		}
	}
	
	private void validateRemoveFieldDependent(EntityField field,EntityDependent<? extends SystemEntity> dependent,
			  ValidationErrors errors, Session session) {
		for (SystemEntity systemEntity : dependent.findUsage(field, session)) {
			switch (getEntityType(systemEntity)) {
				case C.ENTITY:
					errors.addError("val.inuse.fieldentity", systemEntity.getName());
					break;
	
				case C.FILTER:
					errors.addError("val.inuse.fieldfilter", systemEntity.getName());
					break;
	
				case C.FORM:
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
	
	private void validateRemoveStatusDependent(EntityDependent<? extends SystemEntity> dependent, EntityStatus status, ValidationErrors errors, Session session) {
		for (SystemEntity systemEntity : dependent.findUsage(status, session)) {
			switch (getEntityType(systemEntity)) {
				case C.ENTITY:
					errors.addError("val.inuse.statusentity");
					break;
					
				case C.FILTER:
					errors.addError("val.inuse.statusfilter", systemEntity.getName());
					break;
					
				case C.VALUE:
					errors.addError("val.inuse.status");
					break;
					
				default:
					unhandledEntity(systemEntity);
			}
		}
	}
	
	private List<EntityDependent<? extends SystemEntity>> getEntityDependents() {
		if (entityDependents == null) {
			entityDependents = MiscUtils.castList(getBeans(EntityDependent.class));
		}
		return entityDependents;
	}
	
	private static final String AMBIGUOUS_FIELD_OR_NESTED = "val.ambiguous.fieldornested";
	
}
