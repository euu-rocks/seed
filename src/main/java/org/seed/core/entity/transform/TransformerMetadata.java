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
import org.seed.core.data.Order;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityMetadata;
import org.seed.core.entity.EntityStatus;
import org.seed.core.util.Assert;
import org.seed.core.util.ReferenceJsonSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@javax.persistence.Entity
@Table(name = "sys_entity_transform")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TransformerMetadata extends AbstractApplicationEntity 
	implements Transformer {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_entity_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityMetadata sourceEntity;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_entity_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
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
	
	@OneToMany(mappedBy = "transformer",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<TransformerPermission> permissions;
	
	@OneToMany(mappedBy = "transformer",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<TransformerStatus> statusList;
	
	@Transient
	@JsonIgnore
	private String sourceEntityUid;
	
	@Transient
	@JsonIgnore
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
		return notEmpty(getElements());
	}

	@Override
	@XmlElement(name="element")
	@XmlElementWrapper(name="elements")
	public List<TransformerElement> getElements() {
		return elements;
	}
	
	public void setElements(List<TransformerElement> elements) {
		this.elements = elements;
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
	public TransformerPermission getPermissionByUid(String uid) {
		return getObjectByUid(getPermissions(), uid);
	}
	
	@Override
	public TransformerStatus getStatusByUid(String uid) {
		return getObjectByUid(getStatus(), uid);
	}
	
	@Override
	public boolean containsElement(TransformerElement element) {
		Assert.notNull(element, C.ELEMENT);
		
		return containsObject(getElements(), element);
	}
	
	@Override
	public boolean containsField(EntityField entityField) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		
		return anyMatch(getElements(), 
						elem -> entityField.equals(elem.getSourceField()) ||
								entityField.equals(elem.getTargetField()));
	}
	
	@Override
	public boolean containsStatus(EntityStatus status) {
		Assert.notNull(status, C.STATUS);
		
		return anyMatch(getStatus(), stat -> status.equals(stat.getStatus()));
	}
	
	@Override
	public void addElement(TransformerElement element) {
		Assert.notNull(element, C.ELEMENT);
		
		element.setTransformer(this);
		if (elements == null) {
			elements = new ArrayList<>();
		}
		elements.add(element);
	}
	
	@Override
	public void removeElement(TransformerElement element) {
		Assert.notNull(element, C.ELEMENT);
		
		getElements().remove(element);
	}
	
	@Override
	public boolean hasFunctions() {
		return notEmpty(getFunctions());
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
		Assert.notNull(function, C.FUNCTION);
		
		function.setTransformer(this);
		if (functions == null) {
			functions = new ArrayList<>();
		}
		functions.add(function);
	}
	
	@Override
	public void removeFunction(TransformerFunction function) {
		Assert.notNull(function, C.FUNCTION);
		
		getFunctions().remove(function);
	}
	
	@Override
	public boolean hasPermissions() {
		return notEmpty(getPermissions());
	}
	
	@Override
	@XmlElement(name="permission")
	@XmlElementWrapper(name="permissions")
	public List<TransformerPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<TransformerPermission> permissions) {
		this.permissions = permissions;
	}
	
	@Override
	public boolean hasStatus() {
		return notEmpty(getStatus());
	}
	
	@Override
	@XmlElement(name="status")
	@XmlElementWrapper(name="statuses")
	public List<TransformerStatus> getStatus() {
		return statusList;
	}
	
	public void setStatus(List<TransformerStatus> statusList) {
		this.statusList = statusList;
	}

	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
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
		return isEqualElements(otherTransformer) &&
			   isEqualFunctions(otherTransformer) &&
			   isEqualPermissions(otherTransformer) &&
			   isEqualStatus(otherTransformer);
	}
	
	private boolean isEqualElements(Transformer otherTransformer) {
		return !(anyMatch(elements, elem -> !elem.isEqual(otherTransformer.getElementByUid(elem.getUid()))) || 
				 anyMatch(otherTransformer.getElements(), elem -> getElementByUid(elem.getUid()) == null));
	}
	
	private boolean isEqualFunctions(Transformer otherTransformer) {
		return !(anyMatch(functions, function -> !function.isEqual(otherTransformer.getFunctionByUid(function.getUid()))) || 
				 anyMatch(otherTransformer.getFunctions(), function -> getFunctionByUid(function.getUid()) == null));
	}
	
	private boolean isEqualPermissions(Transformer otherTransformer) {
		return !(anyMatch(permissions, perm -> !perm.isEqual(otherTransformer.getPermissionByUid(perm.getUid()))) || 
				 anyMatch(otherTransformer.getPermissions(), perm -> getPermissionByUid(perm.getUid()) == null));
	}
	
	private boolean isEqualStatus(Transformer otherTransformer) {
		return !(anyMatch(statusList, status -> !status.isEqual(otherTransformer.getStatusByUid(status.getUid()))) || 
				 anyMatch(otherTransformer.getStatus(), status -> getStatusByUid(status.getUid()) == null));
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getElements());
		removeNewObjects(getFunctions());
		removeNewObjects(getPermissions());
		removeNewObjects(getStatus());
	}
	
	@Override
	public void initUid() {
		super.initUid();
		initUids(getElements());
		initUids(getFunctions());
		initUids(getPermissions());
		initUids(getStatus());
	}
	
	@Override
	protected void setOrderIndexes() {
		Order.setOrderIndexes(getFunctions());
	}
	
	void createLists() {
		statusList = new ArrayList<>();
		permissions = new ArrayList<>();
	}
	
}
