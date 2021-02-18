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

import java.util.Date;

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
import org.seed.core.data.FieldType;
import org.seed.core.data.SystemObject;
import org.seed.core.util.NameUtils;
import org.seed.core.util.ReferenceJsonSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@javax.persistence.Entity
@Table(name = "sys_entity_field")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EntityField extends AbstractOrderedSystemObject
	implements TransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
	@JsonIgnore
	private EntityMetadata entity;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fieldgroup_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityFieldGroup fieldGroup;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_entity_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityMetadata referenceEntity;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_entity_field_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityField referenceEntityField;
	
	private String uid;
	
	private String name;
	
	private String columnName;
	
	private FieldType type;
	
	private Integer length;
	
	private String formula;
	
	private String autonumPattern;
	
	private Long autonumStart;
	
	private boolean isCalculated;
	
	private boolean isMandatory;
	
	private boolean isIndexed;
	
	private boolean isUnique;
	
	@Transient
	private String fieldGroupUid;
	
	@Transient
	private String referenceEntityUid;
	
	@Transient
	private String referenceEntityFieldUid;
	
	@Transient
	private String defaultString;
	
	@Transient
	private Number defaultNumber;
	
	@Transient
	private Date defaultDate;
	
	@Transient
	private SystemObject defaultObject;
	
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
	
	@JsonIgnore
	public String getInternalName() {
		return name != null ? NameUtils.getInternalName(name).toLowerCase() : null;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	@XmlAttribute
	public FieldType getType() {
		return type;
	}

	public void setType(FieldType type) {
		this.type = type;
	}
	
	@XmlTransient
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = (EntityMetadata) entity;
	}
	
	@XmlAttribute
	public String getFieldGroupUid() {
		return fieldGroup != null ? fieldGroup.getUid() : fieldGroupUid;
	}

	public void setFieldGroupUid(String fieldGroupUid) {
		this.fieldGroupUid = fieldGroupUid;
	}

	@XmlTransient
	public EntityFieldGroup getFieldGroup() {
		return fieldGroup;
	}

	public void setFieldGroup(EntityFieldGroup fieldGroup) {
		this.fieldGroup = fieldGroup;
	}

	@XmlTransient
	public Entity getReferenceEntity() {
		return referenceEntity;
	}
	
	@XmlAttribute
	public String getReferenceEntityUid() {
		return referenceEntity != null ? referenceEntity.getUid() : referenceEntityUid;
	}

	public void setReferenceEntityUid(String referenceEntityUid) {
		this.referenceEntityUid = referenceEntityUid;
	}

	public void setReferenceEntity(Entity referenceEntity) {
		this.referenceEntity = (EntityMetadata) referenceEntity;
	}
	
	@XmlTransient
	public EntityField getReferenceEntityField() {
		return referenceEntityField;
	}
	
	@XmlAttribute
	public String getReferenceEntityFieldUid() {
		return referenceEntityField != null ? referenceEntityField.getUid() : referenceEntityFieldUid;
	}

	public void setReferenceEntityFieldUid(String referenceEntityFieldUid) {
		this.referenceEntityFieldUid = referenceEntityFieldUid;
	}

	public void setReferenceEntityField(EntityField referenceEntityField) {
		this.referenceEntityField = referenceEntityField;
	}
	
	@XmlAttribute
	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}
	
	@XmlAttribute
	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}
	
	@XmlAttribute
	public String getAutonumPattern() {
		return autonumPattern;
	}
	
	public void setAutonumPattern(String autonumPattern) {
		this.autonumPattern = autonumPattern;
	}
	
	@XmlAttribute
	public Long getAutonumStart() {
		return autonumStart;
	}

	public void setAutonumStart(Long autonumStart) {
		this.autonumStart = autonumStart;
	}

	@XmlAttribute
	public boolean isCalculated() {
		return isCalculated;
	}

	public void setCalculated(boolean isCalculated) {
		this.isCalculated = isCalculated;
	}

	@XmlAttribute
	public boolean isMandatory() {
		return isMandatory;
	}

	public void setMandatory(boolean isMandatory) {
		this.isMandatory = isMandatory;
	}
	
	@XmlAttribute
	public boolean isIndexed() {
		return isIndexed;
	}

	public void setIndexed(boolean isIndexed) {
		this.isIndexed = isIndexed;
	}
	
	@XmlAttribute
	public boolean isUnique() {
		return isUnique;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}
	
	@XmlTransient
	public String getDefaultString() {
		return defaultString;
	}

	public void setDefaultString(String defaultString) {
		this.defaultString = defaultString;
	}
	
	@XmlTransient
	public Number getDefaultNumber() {
		return defaultNumber;
	}

	public void setDefaultNumber(Number defaultNumber) {
		this.defaultNumber = defaultNumber;
	}
	
	@XmlTransient
	public Date getDefaultDate() {
		return defaultDate;
	}

	public void setDefaultDate(Date defaultDate) {
		this.defaultDate = defaultDate;
	}
	
	@XmlTransient
	public SystemObject getDefaultObject() {
		return defaultObject;
	}

	public void setDefaultObject(SystemObject defaultObject) {
		this.defaultObject = defaultObject;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !EntityField.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final EntityField otherField = (EntityField) other;
		return new EqualsBuilder()
			.append(fieldGroupUid, otherField.getFieldGroupUid())
			.append(referenceEntityUid, otherField.getReferenceEntityUid())
			.append(referenceEntityFieldUid, otherField.getReferenceEntityFieldUid())
			.append(name, otherField.name)
			.append(columnName, otherField.columnName)
			.append(type, otherField.type)
			.append(length, otherField.length)
			.append(formula, otherField.formula)
			.append(autonumPattern, otherField.autonumPattern)
			.append(autonumStart, otherField.autonumStart)
			.append(isCalculated, otherField.isCalculated)
			.append(isMandatory, otherField.isMandatory)
			.append(isIndexed, otherField.isIndexed)
			.append(isUnique, otherField.isUnique)
			.isEquals();
	}
	
}
