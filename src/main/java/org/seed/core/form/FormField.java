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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;

import org.seed.core.application.TransferableObject;
import org.seed.core.config.ApplicationContextProvider;
import org.seed.core.data.AbstractOrderedSystemObject;
import org.seed.core.data.SystemField;
import org.seed.core.entity.EntityField;

@Entity
@Table(name = "sys_form_field")
public class FormField extends AbstractOrderedSystemObject
	implements TransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
	private FormMetadata form;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_field_id")
	private EntityField entityField;
	
	private String uid;
	
	private SystemField systemField;
	
	private String label;
	
	private String style;
	
	private String labelStyle;
	
	private String width;
	
	private String height;
	
	private String hflex;
	
	private Integer thumbnailWidth;
	
	@Transient
	private String entityFieldUid;
	
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
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = (FormMetadata) form;
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
	
	@XmlAttribute
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	@XmlAttribute
	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}
	
	@XmlAttribute
	public String getLabelStyle() {
		return labelStyle;
	}

	public void setLabelStyle(String labelStyle) {
		this.labelStyle = labelStyle;
	}
	
	@XmlAttribute
	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}
	
	@XmlAttribute
	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	@XmlAttribute
	public Integer getThumbnailWidth() {
		return thumbnailWidth;
	}

	public void setThumbnailWidth(Integer thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}
	
	@XmlAttribute
	public String getHflex() {
		return hflex;
	}

	public void setHflex(String hflex) {
		this.hflex = hflex;
	}

	public boolean isSystem() {
		return systemField != null;
	}
	
	@XmlAttribute
	public String getEntityFieldUid() {
		return entityField != null ? entityField.getUid() : entityFieldUid;
	}

	public void setEntityFieldUid(String entityFieldUid) {
		this.entityFieldUid = entityFieldUid;
	}
	
	public String getName() {
		if (label != null) {
			return label;
		}
		if (entityField != null) {
			return entityField.getName();
		}
		return ApplicationContextProvider.getBean(LabelProvider.class)
										 .getEnumLabel(systemField);
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
			.append(entityFieldUid, otherField.getEntityFieldUid())
			.append(systemField, otherField.systemField)
			.append(label, otherField.label)
			.append(style, otherField.style)
			.append(labelStyle, otherField.labelStyle)
			.append(width, otherField.width)
			.append(height, otherField.height)
			.append(hflex, otherField.hflex)
			.append(thumbnailWidth, otherField.thumbnailWidth)
			.isEquals();
	}
	
}
