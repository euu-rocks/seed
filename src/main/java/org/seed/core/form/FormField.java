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
package org.seed.core.form;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.Seed;
import org.seed.core.data.SystemField;

@Entity
@Table(name = "sys_form_field")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FormField extends AbstractFormField {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
	private FormMetadata form;
	
	private SystemField systemField;
	
	private Integer thumbnailWidth;
	
	private boolean isSelected;
	
	@XmlTransient
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = (FormMetadata) form;
	}
	
	@XmlAttribute
	public SystemField getSystemField() {
		return systemField;
	}

	public void setSystemField(SystemField systemField) {
		this.systemField = systemField;
	}
	
	@XmlAttribute
	public Integer getThumbnailWidth() {
		return thumbnailWidth;
	}

	public void setThumbnailWidth(Integer thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}
	
	@XmlAttribute
	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public boolean isSystem() {
		return systemField != null;
	}
	
	public String getName() {
		if (getLabel() != null) {
			return getLabel();
		}
		if (getEntityField() != null) {
			return getEntityField().getName();
		}
		else if (getSystemField() != null) {
			return Seed.getEnumLabel(getSystemField());
		}
		return null;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !FormField.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final FormField otherField = (FormField) other;
		return new EqualsBuilder()
			.append(getEntityFieldUid(), otherField.getEntityFieldUid())
			.append(getSystemField(), otherField.getSystemField())
			.append(getLabel(), otherField.getLabel())
			.append(getStyle(), otherField.getStyle())
			.append(getLabelStyle(), otherField.getLabelStyle())
			.append(getWidth(), otherField.getWidth())
			.append(getHeight(), otherField.getHeight())
			.append(getHflex(), otherField.getHflex())
			.append(getThumbnailWidth(), otherField.getThumbnailWidth())
			.append(isSelected(), otherField.isSelected())
			.isEquals();
	}
	
}
