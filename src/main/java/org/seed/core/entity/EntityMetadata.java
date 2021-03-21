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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.data.FieldAccess;
import org.seed.core.data.FieldType;
import org.seed.core.data.Order;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.util.ReferenceJsonSerializer;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@javax.persistence.Entity
@Table(name = "sys_entity")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EntityMetadata extends AbstractApplicationEntity 
	implements Entity {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generic_entity_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityMetadata genericEntity;
	
	@OneToMany(mappedBy = "entity",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OrderBy("order")
    private List<EntityField> fields;
	
	@OneToMany(mappedBy = "entity",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OrderBy("order")
	private List<EntityFieldGroup> fieldGroups;
	
	@OneToMany(mappedBy = "parentEntity",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OrderBy("order")
	private List<NestedEntity> nesteds;
	
	@OneToMany(mappedBy = "entity",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OrderBy("order")
	private List<EntityFunction> functions;
	
	@OneToMany(mappedBy = "entity",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<EntityStatus> statusList;
	
	@OneToMany(mappedBy = "entity",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<EntityStatusTransition> statusTransitions;
	
	@OneToMany(mappedBy = "entity",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<EntityPermission> permissions;
	
	@OneToMany(mappedBy = "entity",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<EntityFieldConstraint> fieldConstraints;
	
	private String tableName;
	
	private String identifierPattern;
	
	private boolean isGeneric;
	
	@Override
	@XmlAttribute
	public boolean isGeneric() {
		return isGeneric;
	}
	
	public void setGeneric(boolean isGeneric) {
		this.isGeneric = isGeneric;
	}
	
	@Override
	public String getGeneratedPackage() {
		return PACKAGE_NAME;
	}

	@Override
	public String getGeneratedClass() {
		return StringUtils.capitalize(getInternalName());
	}
	
	@Override
	@XmlAttribute
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	@Override
	@XmlAttribute
	public String getIdentifierPattern() {
		return identifierPattern;
	}

	public void setIdentifierPattern(String identifierPattern) {
		this.identifierPattern = identifierPattern;
	}

	@Override
	public Date getLastModified() {
		Date lastModified = super.getLastModified();
		if (genericEntity != null && genericEntity.getLastModified().after(lastModified)) {
			lastModified = genericEntity.getLastModified();
		}
		return lastModified;
	}
	
	@Override
	@XmlTransient
	public Entity getGenericEntity() {
		return genericEntity;
	}

	public void setGenericEntity(Entity genericEntity) {
		this.genericEntity = (EntityMetadata) genericEntity;
	}

	@Override
	@XmlElement(name="field")
	@XmlElementWrapper(name="fields")
	public List<EntityField> getFields() {
		return fields;
	}
	
	public void setFields(List<EntityField> fields) {
		this.fields = fields;
	}
	
	// includes generic fields
	@Override
	@JsonIgnore
	public List<EntityField> getAllFields() {
		final List<EntityField> result = new ArrayList<>();
		if (genericEntity != null) {
			result.addAll(genericEntity.getAllFields());
		}
		if (hasFields()) {
			result.addAll(getFields());
		}
		return result;
	}
	
	// includes generic fields
	@Override
	public List<EntityField> getAllFieldsByGroup(EntityFieldGroup fieldGroup) {
		Assert.notNull(fieldGroup, "fieldGroup is null");
		
		return subList(getAllFields(), f -> fieldGroup.equals(f.getFieldGroup()));
	}
	
	@Override
	public List<EntityField> getAllFieldsByType(FieldType fieldType) {
		Assert.notNull(fieldType, "fieldType is null");
		
		return subList(getAllFields(), f -> f.getType() == fieldType);
	}
	
	// get fields that reference to given entity
	@Override
	public List<EntityField> getReferenceFields(Entity entity) {
		Assert.notNull(entity, "nested is null");
		
		return subList(getAllFields(), f -> f.getType().isReference() && 
											f.getReferenceEntity().equals(entity));
	}
	
	@Override
	public List<EntityField> getFullTextSearchFields() {
		return subList(getAllFields(), f -> f.isFullTextSearch());
	}
	
	@Override
	public String getDefaultIdentifierPattern() {
		final EntityField defaultField = findDefaultIdentifierField();
		return defaultField != null 
				? ("{entity} {" + defaultField.getName() + '}') 
				: null;
	}
	
	@Override
	public EntityField findDefaultIdentifierField() {
		// search in all unique fields first
		if (hasAllFields()) {
			for (EntityField field : subList(getAllFields(), f -> f.isUnique())) {
				if (field.getType().isText() || field.getType().isAutonum()) {
					return field;
				}
			}
		}
		// fallback: search current fields
		if (hasFields()) {
			for (EntityField field : getFields()) {
				if (field.getType().isText() || field.getType().isAutonum()) {
					return field;
				}
			}
		}
		return null;
	}
	
	// includes generic fieldgroups
	@Override
	@JsonIgnore
	public List<EntityFieldGroup> getAllFieldGroups() {
		final List<EntityFieldGroup> result = new ArrayList<>();
		if (genericEntity != null) {
			result.addAll(genericEntity.getAllFieldGroups());
		}
		if (hasFieldGroups()) {
			result.addAll(getFieldGroups());
		}
		return result;
	}
	
	@Override
	public boolean hasFieldGroups() {
		return !ObjectUtils.isEmpty(getFieldGroups());
	}
	
	@Override
	@XmlElement(name="fieldgroup")
	@XmlElementWrapper(name="fieldgroups")
	public List<EntityFieldGroup> getFieldGroups() {
		return fieldGroups;
	}

	public void setFieldGroups(List<EntityFieldGroup> fieldGroups) {
		this.fieldGroups = fieldGroups;
	}

	@Override
	public boolean hasFieldConstraints() {
		return !ObjectUtils.isEmpty(getFieldConstraints());
	}

	@Override
	@XmlElement(name="fieldconstraint")
	@XmlElementWrapper(name="fieldconstraints")
	public List<EntityFieldConstraint> getFieldConstraints() {
		return fieldConstraints;
	}

	public void setFieldConstraints(List<EntityFieldConstraint> fieldConstraints) {
		this.fieldConstraints = fieldConstraints;
	}

	@Override
	@XmlElement(name="status")
	@XmlElementWrapper(name="statuslist")
	public List<EntityStatus> getStatusList() {
		return statusList;
	}
	
	public void setStatusList(List<EntityStatus> statusList) {
		this.statusList = statusList;
	}

	@Override
	public boolean hasStatusTransitions() {
		return !ObjectUtils.isEmpty(getStatusTransitions());
	}
	
	@Override
	@XmlElement(name="statustransition")
	@XmlElementWrapper(name="statustransitions")
	public List<EntityStatusTransition> getStatusTransitions() {
		return statusTransitions;
	}

	public void setStatusTransitions(List<EntityStatusTransition> statusTransitions) {
		this.statusTransitions = statusTransitions;
	}

	@Override
	@XmlElement(name="permission")
	@XmlElementWrapper(name="permissions")
	public List<EntityPermission> getPermissions() {
		return permissions;
	}
	
	public void setPermissions(List<EntityPermission> permissions) {
		this.permissions = permissions;
	}
	
	@Override
	public EntityPermission getPermissionByUid(String uid) {
		return getObjectByUid(getPermissions(), uid);
	}

	@Override
	@XmlElement(name="nested")
	@XmlElementWrapper(name="nesteds")
	public List<NestedEntity> getNesteds() {
		return nesteds;
	}

	public void setNesteds(List<NestedEntity> nesteds) {
		this.nesteds = nesteds;
	}
	
	// includes generic fields
	@Override
	@JsonIgnore
	public List<NestedEntity> getAllNesteds() {
		final List<NestedEntity> list = new ArrayList<>();
		if (genericEntity != null) {
			list.addAll(genericEntity.getAllNesteds());
		}
		if (hasNesteds()) {
			list.addAll(getNesteds());
		}
		return list;
	}
	
	@Override
	public boolean hasFields() {
		return !ObjectUtils.isEmpty(getFields());
	}
	
	@Override
	public boolean hasAllFields() {
		if (genericEntity != null && genericEntity.hasAllFields()) {
			return true;
		}
		return hasFields();
	}
	
	// includes nested fields
	@Override
	public boolean hasFullTextSearchFields() {
		if (hasAllFields()) {
			for (EntityField field : getAllFields()) {
				if (field.isFullTextSearch()) {
					return true;
				}
			}
		}
		if (hasAllNesteds()) {
			for (NestedEntity nested : getAllNesteds()) {
				if (nested.getNestedEntity().hasFullTextSearchFields()) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean containsField(EntityField field) {
		Assert.notNull(field, "field is null");
		
		return hasFields() && getFields().contains(field);
	}
	
	@Override
	public EntityField findAutonumField() {
		if (hasAllFields()) {
			for (EntityField field : getAllFields()) {
				if (field.getType().isAutonum()) {
					return field;
				}
			}
		}
		return null;
	}
	
	@Override
	public EntityFieldGroup getFieldGroupByUid(String uid) {
		EntityFieldGroup group = null;
		if (genericEntity != null) {
			group = genericEntity.getFieldGroupByUid(uid);
		}
		return group != null ? group : getObjectByUid(getFieldGroups(), uid);
	}
	
	@Override
	public EntityField getFieldById(Long id) {
		EntityField field = null;
		if (genericEntity != null) {
			field = genericEntity.getFieldById(id);
		}
		return field != null ? field : getObjectById(getFields(), id);
	}
	
	@Override
	public EntityField getFieldByUid(String uid) {
		EntityField field = null;
		if (genericEntity != null) {
			field = genericEntity.getFieldByUid(uid);
		}
		return field != null ? field : getObjectByUid(getFields(), uid);
	}
	
	@Override
	public EntityStatus getStatusById(Long id) {
		return getObjectById(getStatusList(), id);
	}
	
	@Override
	public EntityStatus getStatusByUid(String uid) {
		return getObjectByUid(getStatusList(), uid);
	}
	
	// includes nested fields
	@Override
	public EntityField findFieldById(Long id) {
		EntityField field = getFieldById(id);
		if (field == null && hasNesteds()) {
			for (NestedEntity nested : getNesteds()) {
				field = nested.getNestedEntity().getFieldById(id);
				if (field != null) {
					break;
				}
			}
		}
		return field;
	}
	
	// includes nested fields
	@Override
	public EntityField findFieldByUid(String uid) {
		EntityField field = getFieldByUid(uid);
		if (field == null && hasNesteds()) {
			for (NestedEntity nested : getNesteds()) {
				field = nested.getNestedEntity().getFieldByUid(uid);
				if (field != null) {
					break;
				}
			}
		}
		return field;
	}
	
	@Override
	public NestedEntity getNestedByUid(String uid) {
		NestedEntity nested = null;
		if (genericEntity != null) {
			nested = genericEntity.getNestedByUid(uid);
		}
		return nested != null ? nested : getObjectByUid(getNesteds(), uid);
	}
	
	@Override
	public NestedEntity getNestedByInternalName(String name) {
		Assert.notNull(name, "name is null");
		
		NestedEntity result = null;
		if (genericEntity != null) {
			result = genericEntity.getNestedByInternalName(name);
		}
		if (result == null && hasNesteds()) {
			for (NestedEntity nested : getNesteds()) {
				if (nested.getNestedEntity().getInternalName().equalsIgnoreCase(name)) {
					result = nested;
					break;
				}
			}
		}
		return result;
	}
	
	@Override
	public NestedEntity getNestedByEntityId(Long id) {
		Assert.notNull(id, "id is null");
		
		NestedEntity result = null;
		if (genericEntity != null) {
			result = genericEntity.getNestedByEntityId(id);
		}
		if (result == null && hasNesteds()) {
			for (NestedEntity nested : getNesteds()) {
				if (nested.getNestedEntity().getId().equals(id)) {
					result = nested;
					break;
				}
			}
		}
		return result;
	}
	
	@Override
	public NestedEntity getNestedByEntityField(EntityField field) {
		Assert.notNull(field, "field is null");
		
		NestedEntity result = null;
		if (genericEntity != null) {
			result = genericEntity.getNestedByEntityField(field);
		}
		if (result == null && hasNesteds()) {
			for (NestedEntity nested : getNesteds()) {
				if (nested.getNestedEntity().containsField(field)) {
					result = nested;
					break;
				}
			}
		}
		return result;
	}
	
	@Override
	public boolean hasNesteds() {
		return !ObjectUtils.isEmpty(getNesteds());
	}
	
	@Override
	public boolean hasAllNesteds() {
		if (genericEntity != null && genericEntity.hasAllNesteds()) {
			return true;
		}
		return hasNesteds();
	}
	
	@Override
	public boolean isNestedEntity(Entity entity) {
		Assert.notNull(entity, "entity is null");
		
		if (hasAllNesteds()) {
			for (NestedEntity nested : getAllNesteds()) {
				if (nested.getNestedEntity().equals(entity)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean hasStatus() {
		return !ObjectUtils.isEmpty(getStatusList());
	}
	
	@Override
	public boolean hasPermissions() {
		return !ObjectUtils.isEmpty(getPermissions());
	}
	
	@Override
	public void addFieldGroup(EntityFieldGroup fieldGroup) {
		Assert.notNull(fieldGroup, "fieldGroup is null");
		
		if (fieldGroups == null) {
			fieldGroups = new ArrayList<>();
		}
		fieldGroup.setEntity(this);
		fieldGroups.add(fieldGroup);
	}
	
	@Override
	public void removeFieldGroup(EntityFieldGroup fieldGroup) {
		Assert.notNull(fieldGroup, "fieldGroup is null");
		
		getFieldGroups().remove(fieldGroup);
	}
	
	@Override
	public void addField(EntityField field) {
		Assert.notNull(field, "field is null");
		
		if (fields == null) {
			fields = new ArrayList<>();
		}
		field.setEntity(this);
		fields.add(field);
	}
	
	@Override
	public void removeField(EntityField field) {
		Assert.notNull(field, "field is null");
		
		getFields().remove(field);
	}
	
	@Override
	public void addNested(NestedEntity nested) {
		Assert.notNull(nested, "nested is null");
		
		if (nesteds == null) {
			nesteds = new ArrayList<>();
		}
		nested.setParentEntity(this);
		nesteds.add(nested);
	}
	
	@Override
	public void removeNested(NestedEntity nested) {
		Assert.notNull(nested, "nested is null");
		
		getNesteds().remove(nested);
	}
	
	@Override
	public void addPermission(EntityPermission permission) {
		Assert.notNull(permission, "permission is null");
		
		if (permissions == null) {
			permissions = new ArrayList<>();
		}
		permission.setEntity(this);
		permissions.add(permission);
	}
	
	@Override
	public void removePermission(EntityPermission permission) {
		Assert.notNull(permission, "permission is null");
		
		getPermissions().remove(permission);
	}
	
	@Override
	public void addStatus(EntityStatus status) {
		Assert.notNull(status, "status is null");
		
		if (statusList == null) {
			statusList = new ArrayList<>();
		}
		status.setEntity(this);
		statusList.add(status);
	}
	
	@Override
	public void removeStatus(EntityStatus status) {
		Assert.notNull(status, "status is null");
		
		getStatusList().remove(status);
		// remove transitions containing status
		if (hasStatusTransitions()) {
			getStatusTransitions().removeIf(t -> t.getSourceStatus().equals(status) ||
												 t.getTargetStatus().equals(status));
		}
	}
	
	@Override
	public void addStatusTransition(EntityStatusTransition statusTransition) {
		Assert.notNull(statusTransition, "statusTransition is null");
		
		if (statusTransitions == null) {
			statusTransitions = new ArrayList<>();
		}
		statusTransition.setEntity(this);
		statusTransitions.add(statusTransition);
	}
	
	@Override
	public void removeStatusTransition(EntityStatusTransition statusTransition) {
		Assert.notNull(statusTransition, "statusTransition is null");
		
		getStatusTransitions().remove(statusTransition);
	}
	
	@Override
	public void addFieldConstraint(EntityFieldConstraint constraint) {
		Assert.notNull(constraint, "constraint is null");
		
		if (fieldConstraints == null) {
			fieldConstraints = new ArrayList<>();
		}
		constraint.setEntity(this);
		fieldConstraints.add(constraint);
	}
	
	@Override
	public void removeFieldConstraint(EntityFieldConstraint constraint) {
		Assert.notNull(constraint, "constraint is null");
		
		getFieldConstraints().remove(constraint);
	}
	
	@Override
	public boolean hasFunctions() {
		return !ObjectUtils.isEmpty(getFunctions());
	}
	
	@Override
	@XmlElement(name="function")
	@XmlElementWrapper(name="functions")
	public List<EntityFunction> getFunctions() {
		return functions;
	}
	
	// includes generic functions
	@Override
	@JsonIgnore
	public List<EntityFunction> getAllFunctions() {
		final List<EntityFunction> list = new ArrayList<>();
		if (genericEntity != null) {
			list.addAll(genericEntity.getAllFunctions());
		}
		if (hasFunctions()) {
			list.addAll(getFunctions());
		}
		return list;
	}
	
	public void setFunctions(List<EntityFunction> functions) {
		this.functions = functions;
	}
	
	@Override
	public List<EntityFunction> getCallbackFunctions() {
		return subList(getFunctions(), f -> f.isCallback());
	}
	
	@Override
	public List<EntityFunction> getMemberFunctions() {
		return subList(getFunctions(), f -> !f.isCallback());
	}
	
	@Override
	public List<EntityFunction> getUserActionFunctions() {
		return subList(getFunctions(), f -> f.isActiveOnUserAction());
	}
	
	@Override
	public EntityFunction getFunctionByUid(String uid) {
		return getObjectByUid(getFunctions(), uid);
	}

	@Override
	public void addFunction(EntityFunction function) {
		Assert.notNull(function, "function is null");
		
		if (functions == null) {
			functions = new ArrayList<>();
		}
		function.setEntity(this);
		functions.add(function);
	}
	
	@Override
	public void removeFunction(EntityFunction function) {
		Assert.notNull(function, "function is null");
		
		getFunctions().remove(function);
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !Entity.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final Entity otherEntity = (Entity) other;
		if (!new EqualsBuilder()
				.append(getName(), otherEntity.getName())
				.append(tableName, otherEntity.getTableName())
				.append(identifierPattern, otherEntity.getIdentifierPattern())
				.append(isGeneric, otherEntity.isGeneric())
				.isEquals()) {
			return false;
		}
		// check field groups
		if (hasFieldGroups()) {
			for (EntityFieldGroup group : getFieldGroups()) {
				if (!group.isEqual(otherEntity.getFieldGroupByUid(group.getUid()))) {
					return false;
				}
			}
		}
		if (otherEntity.hasFieldGroups()) {
			for (EntityFieldGroup otherGroup : otherEntity.getFieldGroups()) {
				if (getFieldGroupByUid(otherGroup.getUid()) == null) {
					return false;
				}
			}
				
		}
		// check fields
		if (hasFields()) {
			for (EntityField field : getFields()) {
				if (!field.isEqual(otherEntity.getFieldByUid(field.getUid()))) {
					return false;
				}
			}
		}
		if (otherEntity.hasFields()) {
			for (EntityField otherField : otherEntity.getFields()) {
				if (getFieldByUid(otherField.getUid()) == null) {
					return false;
				}
			}
		}
		// check nesteds
		if (hasNesteds()) {
			for (NestedEntity nested : getNesteds()) {
				if (!nested.isEqual(otherEntity.getNestedByUid(nested.getUid()))) {
					return false;
				}
			}
		}
		if (otherEntity.hasNesteds()) {
			for (NestedEntity otherNested : otherEntity.getNesteds()) {
				if (getNestedByUid(otherNested.getUid()) == null) {
					return false;
				}
			}
		}
		// check functions
		if (hasFunctions()) {
			for (EntityFunction function : getFunctions()) {
				if (!function.isEqual(otherEntity.getFunctionByUid(function.getUid()))) {
					return false;
				}
			}
		}
		if (otherEntity.hasFunctions()) {
			for (EntityFunction otherFunction : otherEntity.getFunctions()) {
				if (getFieldByUid(otherFunction.getUid()) == null) {
					return false;
				}
			}
		}
		// check status
		if (hasStatus()) {
			for (EntityStatus status : getStatusList()) {
				if (!status.isEqual(otherEntity.getStatusByUid(status.getUid()))) {
					return false;
				}
			}
		}
		if (otherEntity.hasStatus()) {
			for (EntityStatus otherStatus : otherEntity.getStatusList()) {
				if (getStatusByUid(otherStatus.getUid()) == null) {
					return false;
				}
			}
		}
		// check status transitions
		if (hasStatusTransitions()) {
			for (EntityStatusTransition transition : getStatusTransitions()) {
				if (!transition.isEqual(otherEntity.getStatusTransitionByUid(transition.getUid()))) {
					return false;
				}
			}
		}
		if (otherEntity.hasStatusTransitions()) {
			for (EntityStatusTransition otherTransition : otherEntity.getStatusTransitions()) {
				if (getStatusTransitionByUid(otherTransition.getUid()) == null) {
					return false;
				}
			}
		}
		// check permissions
		if (hasPermissions()) {
			for (EntityPermission permission : getPermissions()) {
				if (!permission.isEqual(otherEntity.getPermissionByUid(permission.getUid()))) {
					return false;
				}
			}
		}
		if (otherEntity.hasPermissions()) {
			for (EntityPermission otherPermission : otherEntity.getPermissions()) {
				if (getPermissionByUid(otherPermission.getUid()) == null) {
					return false;
				}
			}
		}
		// check field constraints
		if (hasFieldConstraints()) {
			for (EntityFieldConstraint constraint : getFieldConstraints()) {
				if (!constraint.isEqual(otherEntity.getFieldConstraintByUid(constraint.getUid()))) {
					return false;
				}
			}
		}
		if (otherEntity.hasFieldConstraints()) {
			for (EntityFieldConstraint otherConstraint : otherEntity.getFieldConstraints()) {
				if (getFieldConstraintByUid(otherConstraint.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	void createLists() {
		permissions = new ArrayList<>();
	}
	
	@Override
	protected void setOrderIndexes() {
		Order.setOrderIndexes(getFields());
		Order.setOrderIndexes(getFieldGroups());
		Order.setOrderIndexes(getNesteds());
		Order.setOrderIndexes(getFunctions());
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getFields());
		removeNewObjects(getFieldGroups());
		removeNewObjects(getFunctions());
		removeNewObjects(getStatusList());
		removeNewObjects(getStatusTransitions());
		removeNewObjects(getFieldConstraints());
		removeNewObjects(getNesteds());
		removeNewObjects(getPermissions());
		try {
			if (hasStatusTransitions()) {
				for (EntityStatusTransition transition : getStatusTransitions()) {
					removeNewObjects(transition.getFunctions());
					removeNewObjects(transition.getPermissions());
				}
			}
		}
		catch (Exception ex) {
			// ignore
		}
	}
	
	@Override
	public void initUids() {
		super.initUids();
		initUids(getFields());
		initUids(getFieldGroups());
		initUids(getNesteds());
		initUids(getFunctions());
		initUids(getStatusList());
		initUids(getStatusTransitions());
		initUids(getPermissions());
		initUids(getFieldConstraints());
		if (hasStatusTransitions()) {
			for (EntityStatusTransition transition : getStatusTransitions()) {
				initUids(transition.getFunctions());
				initUids(transition.getPermissions());
			}
		}
	}
	
	@Override
	public boolean checkFieldAccess(EntityField field, User user, EntityStatus status, 
									FieldAccess ...fieldAccess) {
		Assert.notNull(field, "field is null");
		Assert.notNull(user, "user is null");
		Assert.notNull(fieldAccess, "fieldAccess is null");
		
		if (hasFieldConstraints()) {
			// check universal status constraints (no user group)
			if (status != null) {
				for (EntityFieldConstraint constraint : getFieldConstraints(field)) {
					if (constraint.getUserGroup() == null &&
						constraint.getStatus().equals(status)) {
						for (FieldAccess access : fieldAccess) {
							if (constraint.getAccess() == access) {
								return true;
							}
						}
						return false;
					}
				}
			}
			
			// check group constraints
			if (hasGroupConstraints(field)) {
				if (user.hasUserGroups()) {
					for (UserGroup group : user.getUserGroups()) {
						final EntityFieldConstraint constraint = getGroupConstraint(field, group, status);
						if (constraint == null) {
							return true;
						}
						for (FieldAccess access : fieldAccess) {
							if (constraint.getAccess() == access) {
								return true;
							}
						}
					}
				}
				return false;
			}
		}
		return true;
	}
	
	@JsonIgnore
	@Override
	public EntityStatus getInitialStatus() {
		Assert.state(hasStatus(), "entity has no status");
		
		for (EntityStatus status : getStatusList()) {
			if (status.isInitial()) {
				return status;
			}
		}
		throw new IllegalStateException("initial status not found");
	}
	
	@Override
	public EntityStatus getStatusByNumber(Integer statusNumber) {
		Assert.notNull(statusNumber, "statusNumber is null");
		
		if (hasStatus()) {
			for (EntityStatus status : getStatusList()) {
				if (statusNumber.equals(status.getStatusNumber())) {
					return status;
				}
			}
		}
		return null;
	}
	
	@Override
	public EntityStatusTransition getStatusTransitionByUid(String uid) {
		return getObjectByUid(getStatusTransitions(), uid);
	}
	
	@Override
	public EntityStatusTransition getStatusTransition(EntityStatus sourceStatus, EntityStatus targetStatus) {
		if (hasStatusTransitions()) {
			for (EntityStatusTransition transition : getStatusTransitions()) {
				if (transition.getSourceStatus().equals(sourceStatus) &&
					transition.getTargetStatus().equals(targetStatus)) {
					return transition;
				}
			}
		}
		return null;
	}
	
	@Override
	public EntityFieldConstraint getFieldConstraintByUid(String uid) {
		return getObjectByUid(getFieldConstraints(), uid);
	}
	
	private List<EntityFieldConstraint> getFieldConstraints(EntityField field) {
		Assert.notNull(field, "field is null");
		
		final List<EntityFieldConstraint> result = new ArrayList<>();
		if (hasFieldConstraints()) {
			for (EntityFieldConstraint constraint : getFieldConstraints()) {
				if (field.equals(constraint.getField()) ||
					(constraint.getFieldGroup() != null && 
					 constraint.getFieldGroup().equals(field.getFieldGroup()))) {
					result.add(constraint);
				}
			}
		}
		return result;
	}
	
	private boolean hasGroupConstraints(EntityField field) {
		Assert.notNull(field, "field is null");
		
		for (EntityFieldConstraint constraint : getFieldConstraints(field)) {
			if (constraint.getUserGroup() != null) {
				return true;
			}
		}
		return false;
	}
	
	private EntityFieldConstraint getGroupConstraint(EntityField field, UserGroup userGroup, EntityStatus status) {
		Assert.notNull(field, "field is null");
		Assert.notNull(userGroup, "userGroup is null");
		
		for (EntityFieldConstraint constraint : getFieldConstraints(field)) {
			if (userGroup.equals(constraint.getUserGroup()) &&
				ObjectUtils.nullSafeEquals(status, constraint.getStatus())) {
				return constraint;
			}
		}
		return null;
	}

}
