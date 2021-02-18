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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.TransferableObject;
import org.seed.core.data.AbstractSystemObject;
import org.seed.core.data.FieldAccess;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupMetadata;
import org.seed.core.util.ReferenceJsonSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@javax.persistence.Entity
@Table(name = "sys_entity_field_constraint")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EntityFieldConstraint extends AbstractSystemObject
	implements TransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
	@JsonIgnore
	private EntityMetadata entity;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityField field;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fieldgroup_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityFieldGroup fieldGroup;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityStatus status;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usergroup_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private UserGroupMetadata userGroup;
	
	private String uid;
	
	private FieldAccess access;
	
	private boolean isMandatory;
	
	@Transient
	private String fieldUid;
	
	@Transient
	private String fieldGroupUid;
	
	@Transient
	private String statusUid;
	
	@Transient
	private String userGroupUid;
	
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
	public EntityField getField() {
		return field;
	}
	
	@XmlAttribute
	public String getFieldUid() {
		return field != null ? field.getUid() : fieldUid;
	}
	
	public void setFieldUid(String fieldUid) {
		this.fieldUid = fieldUid;
	}

	public void setField(EntityField field) {
		this.field = field;
	}
	
	@XmlTransient
	public EntityFieldGroup getFieldGroup() {
		return fieldGroup;
	}

	public void setFieldGroup(EntityFieldGroup fieldGroup) {
		this.fieldGroup = fieldGroup;
	}
	
	@XmlAttribute
	public String getFieldGroupUid() {
		return fieldGroup != null ? fieldGroup.getUid() : fieldGroupUid;
	}

	public void setFieldGroupUid(String fieldGroupUid) {
		this.fieldGroupUid = fieldGroupUid;
	}

	@XmlTransient
	public EntityStatus getStatus() {
		return status;
	}
	
	@XmlAttribute
	public String getStatusUid() {
		return status != null ? status.getUid() : statusUid;
	}

	public void setStatusUid(String statusUid) {
		this.statusUid = statusUid;
	}

	public void setStatus(EntityStatus status) {
		this.status = status;
	}
	
	@XmlTransient
	public UserGroup getUserGroup() {
		return userGroup;
	}
	
	@XmlAttribute
	public String getUserGroupUid() {
		return userGroup != null ? userGroup.getUid() : userGroupUid;
	}

	public void setUserGroupUid(String userGroupUid) {
		this.userGroupUid = userGroupUid;
	}

	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = (UserGroupMetadata) userGroup;
	}
	
	@XmlAttribute
	public FieldAccess getAccess() {
		return access;
	}

	public void setAccess(FieldAccess access) {
		this.access = access;
	}
	
	@XmlAttribute
	public boolean isMandatory() {
		return isMandatory;
	}

	public void setMandatory(boolean isMandatory) {
		this.isMandatory = isMandatory;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !EntityFieldConstraint.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final EntityFieldConstraint otherConstraint = (EntityFieldConstraint) other;
		return new EqualsBuilder()
			.append(fieldUid, otherConstraint.getFieldUid())
			.append(fieldGroupUid, otherConstraint.getFieldGroupUid())
			.append(statusUid, otherConstraint.getStatusUid())
			.append(userGroupUid, otherConstraint.getUserGroupUid())
			.append(access,  otherConstraint.access)
			.append(isMandatory, otherConstraint.isMandatory)
			.isEquals();
	}
	
}
