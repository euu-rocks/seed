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
import org.seed.core.data.AbstractOrderedSystemObject;
import org.seed.core.util.NameUtils;
import org.seed.core.util.ReferenceJsonSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@javax.persistence.Entity
@Table(name = "sys_entity_nested")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NestedEntity extends AbstractOrderedSystemObject
	implements TransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_entity_id")
	@JsonIgnore
	private EntityMetadata parentEntity;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nested_entity_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityMetadata nestedEntity;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_field_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityField referenceField;
	
	private String uid;
	
	private String name;
	
	@Transient
	@JsonIgnore
	private String nestedEntityUid;
	
	@Transient
	@JsonIgnore
	private String referenceFieldUid;
	
	@Override
	@XmlAttribute
	public String getUid() {
		return uid;
	}
	
	@Override
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@JsonIgnore
	public String getInternalName() {
		return getName() != null ? NameUtils.getInternalName(getName()).toLowerCase() : null;
	}
	
	@XmlTransient
	public Entity getParentEntity() {
		return parentEntity;
	}

	public void setParentEntity(Entity parentEntity) {
		this.parentEntity = (EntityMetadata) parentEntity;
	}
	
	@XmlTransient
	public Entity getNestedEntity() {
		return nestedEntity;
	}
	
	@XmlAttribute
	public String getNestedEntityUid() {
		return nestedEntity != null ? nestedEntity.getUid() : nestedEntityUid;
	}

	public void setNestedEntityUid(String nestedEntityUid) {
		this.nestedEntityUid = nestedEntityUid;
	}

	public void setNestedEntity(Entity nestedEntity) {
		this.nestedEntity = (EntityMetadata) nestedEntity;
	}
	
	@XmlTransient
	public EntityField getReferenceField() {
		return referenceField;
	}
	
	@XmlAttribute
	public String getReferenceFieldUid() {
		return referenceField != null ? referenceField.getUid() : referenceFieldUid;
	}

	public void setReferenceFieldUid(String referenceFieldUid) {
		this.referenceFieldUid = referenceFieldUid;
	}

	public void setReferenceField(EntityField referenceField) {
		this.referenceField = referenceField;
	}
	
	public EntityField getFieldByUid(String entityFieldUid) {
		return nestedEntity.getFieldByUid(entityFieldUid);
	}
	
	public EntityFunction getFunctionByUid(String entityFunctionUid) {
		return nestedEntity.getFunctionByUid(entityFunctionUid);
	}
	
	public List<EntityField> getFields(boolean excludeParentRef) {
		final List<EntityField> fields = new ArrayList<>();
		if (nestedEntity.hasAllFields()) {
			for (EntityField field : nestedEntity.getAllFields()) {
				if ((!excludeParentRef) || (!field.equals(referenceField))) {
					fields.add(field);
				}
			}
		}
		return fields;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !NestedEntity.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final NestedEntity otherNested = (NestedEntity) other;
		return new EqualsBuilder()
			.append(nestedEntityUid, otherNested.getNestedEntityUid())
			.append(referenceFieldUid, otherNested.getReferenceFieldUid())
			.append(name, otherNested.name)
			.isEquals();
	}
	
}
