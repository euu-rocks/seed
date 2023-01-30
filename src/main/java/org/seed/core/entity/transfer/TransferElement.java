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
package org.seed.core.entity.transfer;

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
import org.seed.core.entity.EntityField;

@javax.persistence.Entity
@Table(name = "sys_entity_transfer_elem")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TransferElement extends AbstractOrderedTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id")
	private TransferMetadata transfer;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_field_id")
	private EntityField entityField;
	
	private boolean isIdentifier;
	
	private String name;
	
	private String format;
	
	private String valueTrue;
	
	private String valueFalse;
	
	@Transient
	private SystemField systemField;
	
	@Transient
	private String fieldUid;
	
	@XmlAttribute
	public boolean isIdentifier() {
		return isIdentifier;
	}

	public void setIdentifier(boolean isIdentifier) {
		this.isIdentifier = isIdentifier;
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	@XmlAttribute
	public String getValueTrue() {
		return valueTrue;
	}

	public void setValueTrue(String valueTrue) {
		this.valueTrue = valueTrue;
	}
	
	@XmlAttribute
	public String getValueFalse() {
		return valueFalse;
	}

	public void setValueFalse(String valueFalse) {
		this.valueFalse = valueFalse;
	}

	@XmlTransient
	public Transfer getTransfer() {
		return transfer;
	}
	
	public void setTransfer(Transfer transfer) {
		this.transfer = (TransferMetadata) transfer;
	}
	
	@XmlTransient
	public EntityField getEntityField() {
		return entityField;
	}
	
	@XmlTransient
	public FieldType getFieldType() {
		return entityField != null
				? entityField.getType()
				: systemField.type;
	}
	
	@XmlAttribute
	public String getFieldUid() {
		return entityField != null ? entityField.getUid() : fieldUid;
	}

	public void setFieldUid(String fieldUid) {
		this.fieldUid = fieldUid;
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

	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final TransferElement otherElement = (TransferElement) other;
		return new EqualsBuilder()
			.append(fieldUid, otherElement.getFieldUid())
			.append(isIdentifier, otherElement.isIdentifier)
			.append(name, otherElement.name)
			.append(format, otherElement.format)
			.append(valueTrue, otherElement.valueTrue)
			.append(valueFalse, otherElement.valueFalse)
			.isEquals();
	}

}
