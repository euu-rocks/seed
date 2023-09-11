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

import java.math.BigDecimal;
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

import org.seed.core.application.AbstractOrderedTransferableObject;
import org.seed.core.data.FieldType;
import org.seed.core.data.SystemField;
import org.seed.core.data.SystemObject;
import org.seed.core.util.NameUtils;
import org.seed.core.util.ReferenceJsonSerializer;

import org.springframework.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@javax.persistence.Entity
@Table(name = "sys_entity_field")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EntityField extends AbstractOrderedTransferableObject {
	
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
	
	private String name;
	
	private String columnName;
	
	private FieldType type;
	
	private Integer length;
	
	private String formula;
	
	private String autonumPattern;
	
	private String validationPattern;
	
	private Long autonumStart;
	
	private Date minDate;
	
	private Date maxDate;
	
	private Date minDateTime;
	
	private Date maxDateTime;
	
	private BigDecimal minDecimal;
	
	private BigDecimal maxDecimal;
	
	private Double minDouble;
	
	private Double maxDouble;
	
	private Integer minInt;
	
	private Integer maxInt;
	
	private Long minLong;
	
	private Long maxLong;
	
	private boolean isCalculated;
	
	private boolean isMandatory;
	
	private boolean isIndexed;
	
	private boolean isUnique;
	
	private boolean isFullTextSearch;
	
	@Transient
	@JsonIgnore
	private String fieldGroupUid;
	
	@Transient
	@JsonIgnore
	private String referenceEntityUid;
	
	@Transient
	@JsonIgnore
	private String defaultString;
	
	@Transient
	@JsonIgnore
	private Number defaultNumber;
	
	@Transient
	@JsonIgnore
	private Date defaultDate;
	
	@Transient
	@JsonIgnore
	private SystemObject defaultObject;
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	
	@JsonIgnore
	public String getInternalName() {
		return name != null 
				? NameUtils.getInternalName(name).toLowerCase()
				: null;
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
	
	@JsonIgnore
	public String getEffectiveColumnName() {
		return columnName != null 
				? columnName.toLowerCase()
				: getInternalName();
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
	public String getValidationPattern() {
		return validationPattern;
	}

	public void setValidationPattern(String validationPattern) {
		this.validationPattern = validationPattern;
	}
	
	@XmlAttribute
	public Long getAutonumStart() {
		return autonumStart;
	}

	public void setAutonumStart(Long autonumStart) {
		this.autonumStart = autonumStart;
	}
	
	@XmlAttribute
	public Date getMinDate() {
		return minDate;
	}

	public void setMinDate(Date minDate) {
		this.minDate = minDate;
	}
	
	@XmlAttribute
	public Date getMaxDate() {
		return maxDate;
	}

	public void setMaxDate(Date maxDate) {
		this.maxDate = maxDate;
	}
	
	@XmlAttribute
	public Date getMinDateTime() {
		return minDateTime;
	}

	public void setMinDateTime(Date minDateTime) {
		this.minDateTime = minDateTime;
	}
	
	@XmlAttribute
	public Date getMaxDateTime() {
		return maxDateTime;
	}

	public void setMaxDateTime(Date maxDateTime) {
		this.maxDateTime = maxDateTime;
	}

	@XmlAttribute
	public BigDecimal getMinDecimal() {
		return minDecimal;
	}

	public void setMinDecimal(BigDecimal minDecimal) {
		this.minDecimal = minDecimal;
	}
	
	@XmlAttribute
	public BigDecimal getMaxDecimal() {
		return maxDecimal;
	}

	public void setMaxDecimal(BigDecimal maxDecimal) {
		this.maxDecimal = maxDecimal;
	}
	
	@XmlAttribute
	public Double getMinDouble() {
		return minDouble;
	}

	public void setMinDouble(Double minDouble) {
		this.minDouble = minDouble;
	}
	
	@XmlAttribute
	public Double getMaxDouble() {
		return maxDouble;
	}

	public void setMaxDouble(Double maxDouble) {
		this.maxDouble = maxDouble;
	}
	
	@XmlAttribute
	public Integer getMinInt() {
		return minInt;
	}

	public void setMinInt(Integer minInt) {
		this.minInt = minInt;
	}
	
	@XmlAttribute
	public Integer getMaxInt() {
		return maxInt;
	}

	public void setMaxInt(Integer maxInt) {
		this.maxInt = maxInt;
	}
	
	@XmlAttribute
	public Long getMinLong() {
		return minLong;
	}

	public void setMinLong(Long minLong) {
		this.minLong = minLong;
	}
	
	@XmlAttribute
	public Long getMaxLong() {
		return maxLong;
	}

	public void setMaxLong(Long maxLong) {
		this.maxLong = maxLong;
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
	
	@XmlAttribute
	public boolean isFullTextSearch() {
		return isFullTextSearch;
	}

	public void setFullTextSearch(boolean isFullTextSearch) {
		this.isFullTextSearch = isFullTextSearch;
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
	
	@JsonIgnore
	public boolean isReferenceField() {
		return getType() != null && getType().isReference();
	}
	
	@JsonIgnore
	public boolean isTextField() {
		return getType() != null && (getType().isText() || getType().isTextLong());
	}
	
	@JsonIgnore
	public boolean isJsonSerializable() {
		return getType() != null && !(getType().isBinary() || getType().isFile());
	}
	
	@JsonIgnore
	public boolean isUidField() {
		return SystemField.UID.property.equals(getName());
	}
	
	@JsonIgnore
	public boolean hasDefaultValue() {
		if (getType() == null) {
			return false;
		}
		switch (getType()) {
			case TEXT:
			case TEXTLONG:
				return StringUtils.hasText(getDefaultString());
				
			case DATE:
			case DATETIME:
				return getDefaultDate() != null;
				
			case REFERENCE:
				return getDefaultObject() != null;
				
			case INTEGER:
			case LONG:
			case DECIMAL:
			case DOUBLE:
				return getDefaultNumber() != null;
			default:
				throw new UnsupportedOperationException(getType().name());
		}
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final EntityField otherField = (EntityField) other;
		return new EqualsBuilder()
			.append(getOrder(), otherField.getOrder())
			.append(getFieldGroupUid(), otherField.getFieldGroupUid())
			.append(getReferenceEntityUid(), otherField.getReferenceEntityUid())
			.append(getName(), otherField.getName())
			.append(getColumnName(), otherField.getColumnName())
			.append(getType(), otherField.getType())
			.append(getLength(), otherField.getLength())
			.append(getFormula(), otherField.getFormula())
			.append(getValidationPattern(), otherField.getValidationPattern())
			.append(getAutonumPattern(), otherField.getAutonumPattern())
			.append(getAutonumStart(), otherField.getAutonumStart())
			.append(getMinDate(), otherField.getMinDate())
			.append(getMinDateTime(), otherField.getMinDateTime())
			.append(getMinDecimal(), otherField.getMinDecimal())
			.append(getMinDouble(), otherField.getMinDouble())
			.append(getMinDecimal(), otherField.getMinDecimal())
			.append(getMinInt(), otherField.getMinInt())
			.append(getMinLong(), otherField.getMinLong())
			.append(getMaxDate(), otherField.getMaxDate())
			.append(getMaxDateTime(), otherField.getMaxDateTime())
			.append(getMaxDecimal(), otherField.getMaxDecimal())
			.append(getMaxDouble(), otherField.getMaxDouble())
			.append(getMaxDecimal(), otherField.getMaxDecimal())
			.append(getMaxInt(), otherField.getMaxInt())
			.append(getMaxLong(), otherField.getMaxLong())
			.append(isCalculated(), otherField.isCalculated())
			.append(isMandatory(), otherField.isMandatory())
			.append(isIndexed(), otherField.isIndexed())
			.append(isUnique(), otherField.isUnique())
			.append(isFullTextSearch(), otherField.isFullTextSearch())
			.isEquals();
	}
	
}
