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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.application.TransferableObject;
import org.seed.core.data.AbstractSystemObject;
import org.seed.core.data.Order;
import org.seed.core.user.User;
import org.seed.core.util.ReferenceJsonSerializer;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@javax.persistence.Entity
@Table(name = "sys_entity_statustransition")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EntityStatusTransition extends AbstractSystemObject
	implements TransferableObject {
	
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
	
	private String uid;
	
	@Transient
	private String sourceStatusUid;
	
	@Transient
	private String targetStatusUid;
	
	@Override
	@XmlAttribute
	public String getUid() {
		return uid;
	}
	
	@Override
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	@XmlTransient
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = (EntityMetadata) entity;
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
		return !ObjectUtils.isEmpty(getFunctions());
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
		Assert.notNull(function, "function is null");
		
		if (functions == null) {
			functions = new ArrayList<>();
		}
		functions.add(function);
	}
	
	public void removeFunction(EntityStatusTransitionFunction function) {
		Assert.notNull(function, "function is null");
		
		getFunctions().remove(function);
	}
	
	public boolean hasPermissions() {
		return !ObjectUtils.isEmpty(getPermissions());
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
		Assert.notNull(user, "user is null");
		
		if (hasPermissions()) {
			for (EntityStatusTransitionPermission permission : getPermissions()) {
				if (user.belongsTo(permission.getUserGroup())) {
					return true;
				}
			}
		}
		return false;
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
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !EntityStatusTransition.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final EntityStatusTransition otherTransition = (EntityStatusTransition) other;
		// check functions
		if (hasFunctions()) {
			for (EntityStatusTransitionFunction function : getFunctions()) {
				if (!function.isEqual(otherTransition.getFunctionByUid(function.getUid()))) {
					return false;
				}
			}
		}
		if (otherTransition.hasFunctions()) {
			for (EntityStatusTransitionFunction otherFunction : otherTransition.getFunctions()) {
				if (getFunctionByUid(otherFunction.getUid()) == null) {
					return false;
				}
			}
		}
		// check permissions
		if (hasPermissions()) {
			for (EntityStatusTransitionPermission permission : getPermissions()) {
				if (!permission.isEqual(otherTransition.getPermissionByUid(permission.getUid()))) {
					return false;
				}
			}
		}
		if (otherTransition.hasPermissions()) {
			for (EntityStatusTransitionPermission permission : otherTransition.getPermissions()) {
				if (getPermissionByUid(permission.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	protected void setOrderIndexes() {
		Order.setOrderIndexes(getFunctions());
	}
	
	void createLists() {
		permissions = new ArrayList<>();
	}
	
}
