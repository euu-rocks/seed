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
package org.seed.core.entity.filter;

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

import org.seed.C;
import org.seed.core.application.AbstractTransferableObject;
import org.seed.core.application.TransferableObject;
import org.seed.core.data.FieldType;
import org.seed.core.data.SystemField;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.util.Assert;
import org.seed.core.util.ReferenceJsonSerializer;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@javax.persistence.Entity
@Table(name = "sys_entity_filter_criterion")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FilterCriterion extends AbstractTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_id")
	@JsonIgnore
	private FilterMetadata filter;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_field_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityField entityField;
	
	private SystemField systemField;
	
	private CriterionOperator operator;
	
	private String stringValue;
	
	private Integer integerValue;
	
	private Long longValue;
	
	private Double doubleValue;
	
	private BigDecimal decimalValue;
	
	private Boolean booleanValue;
	
	private Date dateValue;
	
	private Date dateTimeValue;
	
	private String referenceUid;
	
	@Transient
	@JsonIgnore
	private String entityFieldUid;
	
	@Transient
	@JsonIgnore
	private FilterElement element;
	
	@Transient
	@JsonIgnore
	private TransferableObject reference;
	
	@Transient
	@JsonIgnore
	private ValueObject valueObject;
	
	@XmlAttribute
	public String getEntityFieldUid() {
		return entityField != null ? entityField.getUid() : entityFieldUid;
	}

	public void setEntityFieldUid(String entityFieldUid) {
		this.entityFieldUid = entityFieldUid;
	}

	@XmlTransient
	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = (FilterMetadata) filter;
	}
	
	@XmlTransient
	public EntityField getEntityField() {
		return entityField;
	}

	public void setEntityField(EntityField entityField) {
		this.entityField = entityField;
	}
	
	@XmlAttribute
	public SystemField getSystemField() {
		return systemField;
	}

	public void setSystemField(SystemField systemField) {
		this.systemField = systemField;
	}

	public FilterElement getElement() {
		return element;
	}

	public void setElement(FilterElement element) {
		this.element = element;
	}
	
	@XmlAttribute
	public CriterionOperator getOperator() {
		return operator;
	}

	public void setOperator(CriterionOperator operator) {
		this.operator = operator;
	}
	
	@XmlAttribute
	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	
	@XmlAttribute
	public Integer getIntegerValue() {
		return integerValue;
	}

	public void setIntegerValue(Integer integerValue) {
		this.integerValue = integerValue;
	}
	
	@XmlAttribute
	public Long getLongValue() {
		return longValue;
	}

	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}
	
	@XmlAttribute
	public Double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(Double doubleValue) {
		this.doubleValue = doubleValue;
	}
	
	@XmlAttribute
	public BigDecimal getDecimalValue() {
		return decimalValue;
	}

	public void setDecimalValue(BigDecimal decimalValue) {
		this.decimalValue = decimalValue;
	}
	
	@XmlAttribute
	public Boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}
	
	@XmlAttribute
	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}
	
	@XmlAttribute
	public Date getDateTimeValue() {
		return dateTimeValue;
	}

	public void setDateTimeValue(Date dateTimeValue) {
		this.dateTimeValue = dateTimeValue;
	}
	
	@XmlAttribute
	public String getReferenceUid() {
		return referenceUid;
	}

	public void setReferenceUid(String referenceUid) {
		this.referenceUid = referenceUid;
	}
	
	@XmlTransient
	public TransferableObject getReference() {
		return reference;
	}

	public void setReference(TransferableObject reference) {
		this.reference = reference;
		referenceUid = reference != null ? reference.getUid() : null;
	}
	
	@JsonIgnore
	public String getInternalName() {
		if (entityField != null) {
			return entityField.getInternalName();
		}
		else if (systemField != null) {
			return systemField.property;
		}
		return null;
	}
	
	@JsonIgnore
	public boolean needsValue() {
		return operator != null &&
			   operator != CriterionOperator.EMPTY &&
			   operator != CriterionOperator.NOT_EMPTY;
	}
	
	public boolean hasValue() {
		return StringUtils.hasText(stringValue) ||
			   booleanValue != null ||
			   dateTimeValue != null ||
			   dateValue != null ||
			   decimalValue != null ||
			   doubleValue != null ||
			   integerValue != null ||
			   longValue != null ||
			   referenceUid != null;
	}
	
	public String getLike() {
		if (stringValue != null) {
			final String value = stringValue.replace('*', '%');
			return value.contains("%") ? value : '%' + value + '%';
		}
		return null;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final FilterCriterion otherCriterion = (FilterCriterion) other;
		return new EqualsBuilder()
					.append(entityFieldUid, otherCriterion.getEntityFieldUid())
					.append(systemField, otherCriterion.systemField)
					.append(operator, otherCriterion.operator)
					.append(getValue(), otherCriterion.getValue())
					.isEquals();
	}
	
	@XmlTransient
	@JsonIgnore
	public Object getValue() {
		final FieldType fieldType = getType();
		if (fieldType == null) {
			return null;
		}
		switch (fieldType) {
			case AUTONUM:
			case TEXT:
			case TEXTLONG:
				if (entityField != null && entityField.isUidField()) {
					return referenceUid;
				}
				return stringValue;
				
			case BOOLEAN:
				return booleanValue;
				
			case INTEGER:
				return integerValue;
				
			case LONG:
				return longValue;
				
			case DOUBLE:
				return doubleValue;
				
			case DECIMAL:
				return decimalValue;
				
			case DATE:
				return dateValue;
				
			case DATETIME:
				return dateTimeValue;
				
			case REFERENCE:
				if (reference != null) {
					return reference.getId();
				}
				if (valueObject != null) {
					return valueObject;
				}
				return null;
			
			case FILE:
				return null;
				
			default:
				throw new UnsupportedOperationException(fieldType.name());
		}
	}
	
	public void setValue(Object value) {
		Assert.notNull(value, C.VALUE);
		
		final FieldType fieldType = getType();
		if (fieldType == null) {
			return;
		}
		switch (fieldType) {
			case AUTONUM:
			case TEXT:
			case TEXTLONG:
				if (entityField != null && entityField.isUidField()) {
					referenceUid = (String) value;
				}
				stringValue = (String) value;
				break;
				
			case BOOLEAN:
				booleanValue = (Boolean) value;
				break;
				
			case INTEGER:
				integerValue = (Integer) value;
				break;
				
			case LONG:
				longValue = (Long) value;
				break;
				
			case DOUBLE:
				doubleValue = (Double) value;
				break;
				
			case DECIMAL:
				decimalValue = (BigDecimal) value;
				break;
				
			case DATE:
				dateValue = (Date) value;
				break;
				
			case DATETIME:
				dateTimeValue = (Date) value;
				break;
				
			case REFERENCE:
				if (value instanceof TransferableObject) {
					referenceUid = ((TransferableObject) value).getUid();
				}
				else if (value instanceof ValueObject) {
					valueObject = (ValueObject) value; 
				}
				else {
					referenceUid = (String) value;
				}
				break;
				
			default:
				throw new UnsupportedOperationException(fieldType.name());
		}
	}
	
	private FieldType getType() {
		return element != null 
				? element.getType() 
				: getFieldType();
	}
	
	private FieldType getFieldType() {
		return entityField != null 
				? entityField.getType() 
				: getSystemFieldType();
	}
	
	private FieldType getSystemFieldType() {
		return systemField != null 
				? systemField.type
				: null;
	}
	
	void cleanup() {
		final Object value = getValue();
		final TransferableObject referenceObject = getReference();
		
		// clean all values
		stringValue= null;
		integerValue = null;
		longValue = null;
		doubleValue = null;
		decimalValue = null;
		booleanValue = null;
		dateValue = null;
		dateTimeValue = null;
		referenceUid = null;
		this.reference = null;
		this.valueObject = null;
		
		// set value again
		if (needsValue()) {
			if (referenceObject != null) {
				setReference(referenceObject);
			}
			else if (value != null) {
				setValue(value);
			}
		}
	}
	
}
