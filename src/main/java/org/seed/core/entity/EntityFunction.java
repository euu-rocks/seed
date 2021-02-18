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

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.TransferableObject;
import org.seed.core.codegen.GeneratedObject;
import org.seed.core.data.AbstractOrderedSystemObject;
import org.seed.core.util.CDATAXmlAdapter;
import org.seed.core.util.NameUtils;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

@javax.persistence.Entity
@Table(name = "sys_entity_function")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EntityFunction extends AbstractOrderedSystemObject
	implements GeneratedObject, TransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
	@JsonIgnore
	private EntityMetadata entity;
	
	private String uid;
	
	private String name;
	
	private String content;
	
	private boolean isCallback;	
	
	private boolean isActive;
	
	private boolean isActiveOnCreate;
	
	private boolean isActiveOnModify;
	
	private boolean isActiveBeforeInsert;
	
	private boolean isActiveAfterInsert;
	
	private boolean isActiveBeforeUpdate;
	
	private boolean isActiveAfterUpdate;
	
	private boolean isActiveBeforeDelete;
	
	private boolean isActiveAfterDelete;
	
	private boolean isActiveOnUserAction;
	
	private boolean isActiveOnStatusTransition;
	
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
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlJavaTypeAdapter(CDATAXmlAdapter.class)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@XmlAttribute
	public boolean isCallback() {
		return isCallback;
	}

	public void setCallback(boolean isCallback) {
		this.isCallback = isCallback;
	}

	@XmlAttribute
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	@XmlAttribute
	public boolean isActiveOnCreate() {
		return isActiveOnCreate;
	}

	public void setActiveOnCreate(boolean isActiveOnCreate) {
		this.isActiveOnCreate = isActiveOnCreate;
	}
	
	@XmlAttribute
	public boolean isActiveOnModify() {
		return isActiveOnModify;
	}
	
	public void setActiveOnModify(boolean isActiveOnModify) {
		this.isActiveOnModify = isActiveOnModify;
	}
	
	@XmlAttribute
	public boolean isActiveOnStatusTransition() {
		return isActiveOnStatusTransition;
	}

	public void setActiveOnStatusTransition(boolean isActiveOnStatusTransition) {
		this.isActiveOnStatusTransition = isActiveOnStatusTransition;
	}
	
	@XmlAttribute
	public boolean isActiveBeforeInsert() {
		return isActiveBeforeInsert;
	}

	public void setActiveBeforeInsert(boolean isActiveBeforeInsert) {
		this.isActiveBeforeInsert = isActiveBeforeInsert;
	}
	
	@XmlAttribute
	public boolean isActiveAfterInsert() {
		return isActiveAfterInsert;
	}

	public void setActiveAfterInsert(boolean isActiveAfterInsert) {
		this.isActiveAfterInsert = isActiveAfterInsert;
	}
	
	@XmlAttribute
	public boolean isActiveBeforeUpdate() {
		return isActiveBeforeUpdate;
	}

	public void setActiveBeforeUpdate(boolean isActiveBeforeUpdate) {
		this.isActiveBeforeUpdate = isActiveBeforeUpdate;
	}
	
	@XmlAttribute
	public boolean isActiveAfterUpdate() {
		return isActiveAfterUpdate;
	}

	public void setActiveAfterUpdate(boolean isActiveAfterUpdate) {
		this.isActiveAfterUpdate = isActiveAfterUpdate;
	}
	
	@XmlAttribute
	public boolean isActiveBeforeDelete() {
		return isActiveBeforeDelete;
	}

	public void setActiveBeforeDelete(boolean isActiveBeforeDelete) {
		this.isActiveBeforeDelete = isActiveBeforeDelete;
	}
	
	@XmlAttribute
	public boolean isActiveAfterDelete() {
		return isActiveAfterDelete;
	}

	public void setActiveAfterDelete(boolean isActiveAfterDelete) {
		this.isActiveAfterDelete = isActiveAfterDelete;
	}
	
	@XmlAttribute
	public boolean isActiveOnUserAction() {
		return isActiveOnUserAction;
	}

	public void setActiveOnUserAction(boolean isActiveOnUserAction) {
		this.isActiveOnUserAction = isActiveOnUserAction;
	}
	
	@JsonIgnore
	public String getInternalName() {
		return NameUtils.getInternalName(getName());
	}
	
	@Override
	@JsonIgnore
	public String getGeneratedPackage() {
		return getEntity().getGeneratedPackage() + '.' + getEntity().getInternalName().toLowerCase();
	}
	
	@Override
	@JsonIgnore
	public String getGeneratedClass() {
		return StringUtils.capitalize(getInternalName());
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !EntityFunction.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final EntityFunction otherFunction = (EntityFunction) other;
		return new EqualsBuilder()
			.append(name, otherFunction.name)
			.append(content, otherFunction.content)
			.append(isCallback, otherFunction.isCallback)
			.append(isActive, otherFunction.isActive)
			.append(isActiveOnCreate, otherFunction.isActiveOnCreate)
			.append(isActiveOnModify, otherFunction.isActiveOnModify)
			.append(isActiveOnStatusTransition, otherFunction.isActiveOnStatusTransition)
			.append(isActiveOnUserAction, otherFunction.isActiveOnUserAction)
			.append(isActiveBeforeInsert, otherFunction.isActiveBeforeInsert)
			.append(isActiveAfterInsert, otherFunction.isActiveAfterInsert)
			.append(isActiveBeforeUpdate, otherFunction.isActiveBeforeUpdate)
			.append(isActiveAfterUpdate, otherFunction.isActiveAfterUpdate)
			.append(isActiveBeforeDelete, otherFunction.isActiveBeforeDelete)
			.append(isActiveAfterDelete, otherFunction.isActiveAfterDelete)
			.isEquals();
	}

}
