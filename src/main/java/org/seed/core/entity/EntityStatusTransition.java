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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.C;
import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.application.AbstractTransferableObject;
import org.seed.core.data.Order;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.util.Assert;
import org.seed.core.util.ReferenceJsonSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@javax.persistence.Entity
@Table(name = "sys_entity_statustransition")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EntityStatusTransition extends AbstractTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
	@JsonIgnore
	private EntityMetadata entity;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_status_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityStatus sourceStatus;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_status_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityStatus targetStatus;
	
	@OneToMany(mappedBy = "statusTransition",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OrderBy("order")
	private List<EntityStatusTransitionFunction> functions;
	
	@OneToMany(mappedBy = "statusTransition",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<EntityStatusTransitionPermission> permissions;
	
	private String description;
	
	@Transient
	@JsonIgnore
	private String sourceStatusUid;
	
	@Transient
	@JsonIgnore
	private String targetStatusUid;
	
	@XmlTransient
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = (EntityMetadata) entity;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlTransient
	public EntityStatus getSourceStatus() {
		return sourceStatus;
	}
	
	@XmlAttribute
	public String getSourceStatusUid() {
		return sourceStatus != null ? sourceStatus.getUid() : sourceStatusUid;
	}

	public void setSourceStatusUid(String sourceStatusUid) {
		this.sourceStatusUid = sourceStatusUid;
	}

	public void setSourceStatus(EntityStatus sourceStatus) {
		this.sourceStatus = sourceStatus;
	}
	
	@XmlTransient
	public EntityStatus getTargetStatus() {
		return targetStatus;
	}
	
	@XmlAttribute
	public String getTargetStatusUid() {
		return targetStatus != null ? targetStatus.getUid() : targetStatusUid;
	}

	public void setTargetStatusUid(String targetStatusUid) {
		this.targetStatusUid = targetStatusUid;
	}

	public void setTargetStatus(EntityStatus targetStatus) {
		this.targetStatus = targetStatus;
	}
	
	public boolean hasFunctions() {
		return notEmpty(getFunctions());
	}
	
	@XmlElement(name="function")
	@XmlElementWrapper(name="functions")
	public List<EntityStatusTransitionFunction> getFunctions() {
		return functions;
	}
	
	public void setFunctions(List<EntityStatusTransitionFunction> functions) {
		this.functions = functions;
	}

	public EntityStatusTransitionFunction getFunctionByUid(String uid) {
		return AbstractApplicationEntity.getObjectByUid(getFunctions(), uid);
	}
	
	public void addFunction(EntityStatusTransitionFunction function) {
		Assert.notNull(function, C.FUNCTION);
		
		if (functions == null) {
			functions = new ArrayList<>();
		}
		function.setStatusTransition(this);
		functions.add(function);
	}
	
	public boolean containsEntityFunction(EntityFunction function) {
		Assert.notNull(function, C.FUNCTION);
		
		return anyMatch(functions, func -> function.equals(func.getFunction()));
	}
	
	public boolean containsPermission(UserGroup group) {
		Assert.notNull(group, C.USERGROUP);
		
		return anyMatch(permissions, perm -> group.equals(perm.getUserGroup()));
	}
	
	public void removeFunction(EntityStatusTransitionFunction function) {
		Assert.notNull(function, C.FUNCTION);
		
		getFunctions().remove(function);
	}
	
	public boolean hasPermissions() {
		return notEmpty(getPermissions());
	}
	
	public EntityStatusTransitionPermission getPermissionByUid(String uid) {
		return AbstractApplicationEntity.getObjectByUid(getPermissions(), uid);
	}
	
	@XmlElement(name="permission")
	@XmlElementWrapper(name="permissions")
	public List<EntityStatusTransitionPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<EntityStatusTransitionPermission> permissions) {
		this.permissions = permissions;
	}

	public boolean isAuthorized(User user) {
		Assert.notNull(user, C.USER);
		
		if (!hasPermissions()) {
			return true;
		}
		return anyMatch(permissions, permission -> user.belongsTo(permission.getUserGroup()));
	}
	
	@JsonIgnore
	public String getName() {
		final StringBuilder buf = new StringBuilder();
		if (sourceStatus != null) {
			buf.append(sourceStatus.getNumberAndName());
		}
		buf.append(" -> ");
		if (targetStatus != null) {
			buf.append(targetStatus.getNumberAndName());
		}
		return buf.toString();
	}
	
	@JsonIgnore
	public String getInternalName() {
		final StringBuilder buf = new StringBuilder();
		if (sourceStatus != null) {
			buf.append(sourceStatus.getInternalName());
		}
		buf.append("-");
		if (targetStatus != null) {
			buf.append(targetStatus.getInternalName());
		}
		return buf.toString();
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final EntityStatusTransition otherTransition = (EntityStatusTransition) other;
		if (!new EqualsBuilder()
			.append(description, otherTransition.getDescription())
			.append(sourceStatusUid, otherTransition.getSourceStatusUid())
			.append(targetStatusUid, otherTransition.getTargetStatusUid())
			.isEquals()) {
			return false;
		}
		return isEqualFunctions(otherTransition) && 
			   isEqualPermissions(otherTransition);
	}
	
	private boolean isEqualFunctions(EntityStatusTransition otherTransition) {
		return !(anyMatch(functions, function -> !function.isEqual(otherTransition.getFunctionByUid(function.getUid()))) || 
				 anyMatch(otherTransition.getFunctions(), otherFunction -> getFunctionByUid(otherFunction.getUid()) == null));
	}
	
	private boolean isEqualPermissions(EntityStatusTransition otherTransition) {
		return !(anyMatch(permissions, permission -> !permission.isEqual(otherTransition.getPermissionByUid(permission.getUid()))) || 
				 anyMatch(otherTransition.getPermissions(), otherPermission -> getPermissionByUid(otherPermission.getUid()) == null));
	}
	
	@Override
	protected void setOrderIndexes() {
		Order.setOrderIndexes(getFunctions());
	}
	
	void createLists() {
		functions = new ArrayList<>();
		permissions = new ArrayList<>();
	}
	
}
