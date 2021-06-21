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

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.seed.core.application.AbstractOrderedTransferableObject;
import org.seed.core.entity.EntityField;

@MappedSuperclass
public abstract class AbstractFormField extends AbstractOrderedTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_field_id")
	private EntityField entityField;
	
	private String label;
	
	private String width;
	
	private String height;
	
	private String hflex;
	
	private String style;
	
	private String labelStyle;
	
	@Transient
	private String entityFieldUid;
	
	@XmlTransient
	public EntityField getEntityField() {
		return entityField;
	}

	public void setEntityField(EntityField entityField) {
		this.entityField = entityField;
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
	public String getHflex() {
		return hflex;
	}

	public void setHflex(String hflex) {
		this.hflex = hflex;
	}
	
	@XmlAttribute
	public String getEntityFieldUid() {
		return entityField != null ? entityField.getUid() : entityFieldUid;
	}

	public void setEntityFieldUid(String entityFieldUid) {
		this.entityFieldUid = entityFieldUid;
	}
	
}
