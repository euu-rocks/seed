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
package org.seed.core.entity.transform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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

import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.data.Order;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityMetadata;
import org.seed.core.entity.EntityStatus;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupMetadata;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

@javax.persistence.Entity
@Table(name = "sys_entity_transform")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TransformerMetadata extends AbstractApplicationEntity 
	implements Transformer {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_entity_id")
	private EntityMetadata sourceEntity;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_entity_id")
	private EntityMetadata targetEntity;
	
	@OneToMany(mappedBy = "transformer",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<TransformerElement> elements;
	
	@OneToMany(mappedBy = "transformer",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OrderBy("order")
	private List<TransformerFunction> functions;
	
	@ManyToMany(fetch = FetchType.LAZY, 
				cascade = CascadeType.ALL)
	@JoinTable(name = "sys_entity_transform_group", 
			   joinColumns = { 
					   @JoinColumn(name = "transformer_id", nullable = false, updatable = false) }, 
			   inverseJoinColumns = { 
					   @JoinColumn(name = "group_id", nullable = false, updatable = false) })
	private Set<UserGroupMetadata> userGroups;
	
	@ManyToMany(fetch = FetchType.LAZY, 
				cascade = CascadeType.ALL)
	@JoinTable(name = "sys_entity_transform_status", 
			   joinColumns = { 
					   @JoinColumn(name = "transformer_id", nullable = false, updatable = false) }, 
			   inverseJoinColumns = { 
					   @JoinColumn(name = "status_id", nullable = false, updatable = false) })
	private Set<EntityStatus> status;
	
	@Transient
	private String sourceEntityUid;
	
	@Transient
	private String targetEntityUid;
	
	@Override
	@XmlTransient
	public Entity getSourceEntity() {
		return sourceEntity;
	}

	public void setSourceEntity(Entity sourceEntity) {
		this.sourceEntity = (EntityMetadata) sourceEntity;
	}
	
	@Override
	@XmlTransient
	public Entity getTargetEntity() {
		return targetEntity;
	}

	public void setTargetEntity(Entity targetEntity) {
		this.targetEntity = (EntityMetadata) targetEntity;
	}
	
	@Override
	@XmlAttribute
	public String getSourceEntityUid() {
		return sourceEntity != null ? sourceEntity.getUid() : sourceEntityUid;
	}

	public void setSourceEntityUid(String sourceEntityUid) {
		this.sourceEntityUid = sourceEntityUid;
	}
	
	@Override
	@XmlAttribute
	public String getTargetEntityUid() {
		return targetEntity != null ? targetEntity.getUid() : targetEntityUid;
	}

	public void setTargetEntityUid(String targetEntityUid) {
		this.targetEntityUid = targetEntityUid;
	}

	@Override
	public boolean hasElements() {
		return !ObjectUtils.isEmpty(getElements());
	}

	@Override
	@XmlElement(name="element")
	@XmlElementWrapper(name="elements")
	public List<TransformerElement> getElements() {
		return elements;
	}
	
	@Override
	public TransformerElement getElementByUid(String uid) {
		return getObjectByUid(getElements(), uid);
	}
	
	@Override
	public TransformerFunction getFunctionByUid(String uid) {
		return getObjectByUid(getFunctions(), uid);
	}
	
	@Override
	public UserGroup getUserGroupByUid(String uid) {
		return getObjectByUid(getUserGroups(), uid);
	}
	
	@Override
	public EntityStatus getStatusByUid(String uid) {
		return getObjectByUid(getStatus(), uid);
	}
	
	@Override
	public boolean containsElement(TransformerElement element) {
		return hasElements() && getElements().contains(element);
	}
	
	@Override
	public void addElement(TransformerElement element) {
		Assert.notNull(element, "element is null");
		
		element.setTransformer(this);
		if (elements == null) {
			elements = new ArrayList<>();
		}
		elements.add(element);
	}
	
	@Override
	public void removeElement(TransformerElement element) {
		Assert.notNull(element, "element is null");
		
		getElements().remove(element);
	}
	
	@Override
	public boolean hasFunctions() {
		return !ObjectUtils.isEmpty(getFunctions());
	}
	
	@Override
	@XmlElement(name="function")
	@XmlElementWrapper(name="functions")
	public List<TransformerFunction> getFunctions() {
		return functions;
	}
	
	public void setFunctions(List<TransformerFunction> functions) {
		this.functions = functions;
	}
	
	@Override
	public void addFunction(TransformerFunction function) {
		Assert.notNull(function, "function is null");
		
		function.setTransformer(this);
		if (functions == null) {
			functions = new ArrayList<>();
		}
		functions.add(function);
	}
	
	@Override
	public void removeFunction(TransformerFunction function) {
		Assert.notNull(function, "function is null");
		
		getFunctions().remove(function);
	}

	@Override
	public boolean hasUserGroups() {
		return !ObjectUtils.isEmpty(getUserGroups());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Set<UserGroup> getUserGroups() {
		final Set<?> groups = userGroups;
		return (Set<UserGroup>) groups;
	}
	
	@Override
	public boolean isAuthorized(User user) {
		Assert.notNull(user, "user is null");
		
		return user.belongsToOneOf(getUserGroups());
	}
	
	@Override
	public boolean isEnabled(EntityStatus entityStatus) {
		Assert.notNull(entityStatus, "entityStatus is null");
		
		return !hasStatus() || getStatus().contains(entityStatus);
	}
	
	@Override
	public boolean hasStatus() {
		return !ObjectUtils.isEmpty(getStatus());
	}
	
	@Override
	public Set<EntityStatus> getStatus() {
		return status;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !Transformer.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final Transformer otherTransformer = (Transformer) other;
		if (!new EqualsBuilder()
			.append(sourceEntityUid, otherTransformer.getSourceEntityUid())
			.append(targetEntityUid, otherTransformer.getTargetEntityUid())
			.isEquals()) {
			return false;
		}
		// check elements
		if (hasElements()) {
			for (TransformerElement element : getElements()) {
				if (!element.isEqual(otherTransformer.getElementByUid(element.getUid()))) {
					return false;
				}
			}
		}
		if (otherTransformer.hasElements()) {
			for (TransformerElement otherElement : otherTransformer.getElements()) {
				if (getElementByUid(otherElement.getUid()) == null) {
					return false;
				}
			}
		}
		// check functions
		if (hasFunctions()) {
			for (TransformerFunction function : getFunctions()) {
				if (!function.isEqual(otherTransformer.getFunctionByUid(function.getUid()))) {
					return false;
				}
			}
		}
		if (otherTransformer.hasFunctions()) {
			for (TransformerFunction otherFunction : otherTransformer.getFunctions()) {
				if (getFunctionByUid(otherFunction.getUid()) == null) {
					return false;
				}
			}
		}
		// check user groups (only uids)
		if (hasUserGroups()) {
			for (UserGroup group : getUserGroups()) {
				if (otherTransformer.getUserGroupByUid(group.getUid()) == null) {
					return false;
				}
			}
		}
		if (otherTransformer.hasUserGroups()) {
			for (UserGroup otherGroup : otherTransformer.getUserGroups()) {
				if (getUserGroupByUid(otherGroup.getUid()) == null) {
					return false;
				}
			}
		}
		// check status (only uids)
		if (hasStatus()) {
			for (EntityStatus status : getStatus()) {
				if (otherTransformer.getStatusByUid(status.getUid()) == null) {
					return false;
				}
			}
		}
		if (otherTransformer.hasStatus()) {
			for (EntityStatus otherStatus : otherTransformer.getStatus()) {
				if (getStatusByUid(otherStatus.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getElements());
		removeNewObjects(getFunctions());
	}
	
	@Override
	public void initUids() {
		super.initUids();
		initUids(getElements());
		initUids(getFunctions());
	}
	
	@Override
	protected void setOrderIndexes() {
		Order.setOrderIndexes(getFunctions());
	}
	
	void createLists() {
		userGroups = new HashSet<>();
		status = new HashSet<>();
	}
	
}
