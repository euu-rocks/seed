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

import org.seed.core.application.AbstractTransferableObject;
import org.seed.core.entity.EntityField;
import org.seed.core.util.ReferenceJsonSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@javax.persistence.Entity
@Table(name = "sys_entity_transform_elem")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TransformerElement extends AbstractTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transform_id")
	@JsonIgnore
	private TransformerMetadata transformer;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_field_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityField sourceField;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_field_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityField targetField;
	
	@Transient
	@JsonIgnore
	private String sourceFieldUid;
	
	@Transient
	@JsonIgnore
	private String targetFieldUid;
	
	@XmlTransient
	public Transformer getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = (TransformerMetadata) transformer;
	}
	
	@XmlTransient
	public EntityField getSourceField() {
		return sourceField;
	}

	public void setSourceField(EntityField sourceField) {
		this.sourceField = sourceField;
	}
	
	@XmlTransient
	public EntityField getTargetField() {
		return targetField;
	}

	public void setTargetField(EntityField targetField) {
		this.targetField = targetField;
	}
	
	@XmlAttribute
	public String getSourceFieldUid() {
		return sourceField != null ? sourceField.getUid() : sourceFieldUid;
	}

	public void setSourceFieldUid(String sourceFieldUid) {
		this.sourceFieldUid = sourceFieldUid;
	}
	
	@XmlAttribute
	public String getTargetFieldUid() {
		return targetField != null ? targetField.getUid() : targetFieldUid;
	}

	public void setTargetFieldUid(String targetFieldUid) {
		this.targetFieldUid = targetFieldUid;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !TransformerElement.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final TransformerElement otherElement = (TransformerElement) other;
		return new EqualsBuilder()
			.append(sourceFieldUid, otherElement.getSourceFieldUid())
			.append(targetFieldUid, otherElement.getTargetFieldUid())
			.isEquals();
	}
	
}
