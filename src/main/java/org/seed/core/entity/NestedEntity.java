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

import static org.seed.core.util.CollectionUtils.subList;

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

import org.seed.core.application.AbstractOrderedTransferableObject;
import org.seed.core.util.NameUtils;
import org.seed.core.util.ReferenceJsonSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@javax.persistence.Entity
@Table(name = "sys_entity_nested")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NestedEntity extends AbstractOrderedTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_entity_id")
	@JsonIgnore
	private EntityMetadata parentEntity;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nested_entity_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityMetadata nestedEntityMeta;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_field_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityField referenceField;
	
	private String name;
	
	private boolean isReadonly;
	
	@Transient
	@JsonIgnore
	private String nestedEntityUid;
	
	@Transient
	@JsonIgnore
	private String referenceFieldUid;
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute
	public boolean isReadonly() {
		return isReadonly;
	}

	public void setReadonly(boolean readonly) {
		this.isReadonly = readonly;
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
		return nestedEntityMeta;
	}
	
	@XmlAttribute
	public String getNestedEntityUid() {
		return nestedEntityMeta != null ? nestedEntityMeta.getUid() : nestedEntityUid;
	}

	public void setNestedEntityUid(String nestedEntityUid) {
		this.nestedEntityUid = nestedEntityUid;
	}

	public void setNestedEntity(Entity nestedEntity) {
		this.nestedEntityMeta = (EntityMetadata) nestedEntity;
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
		return nestedEntityMeta.getFieldByUid(entityFieldUid);
	}
	
	public EntityFunction getFunctionByUid(String entityFunctionUid) {
		return nestedEntityMeta.getFunctionByUid(entityFunctionUid);
	}
	
	public List<EntityField> getFields(boolean excludeParentRef) {
		return excludeParentRef
				? subList(nestedEntityMeta.getAllFields(), field -> !field.equals(referenceField))
				: nestedEntityMeta.getAllFields();
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final NestedEntity otherNested = (NestedEntity) other;
		return new EqualsBuilder()
			.append(getOrder(), otherNested.getOrder())
			.append(nestedEntityUid, otherNested.getNestedEntityUid())
			.append(referenceFieldUid, otherNested.getReferenceFieldUid())
			.append(name, otherNested.getName())
			.append(isReadonly, otherNested.isReadonly())
			.isEquals();
	}
	
}
