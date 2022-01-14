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

import org.seed.core.application.AbstractTransferableObject;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterMetadata;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerMetadata;

@Entity
@Table(name = "sys_form_field_extra")
public class FormFieldExtra extends AbstractTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
	private FormMetadata form;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_field_id")
	private EntityField entityField;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transform_id")
	private TransformerMetadata transformer;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_id")
	private FilterMetadata filter;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detail_form_id")
	private FormMetadata detailForm;
	
	private boolean isReadonly;
	
	private boolean isUnsortedValues;
	
	@Transient
	private String entityFieldUid;
	
	@Transient
	private String transformerUid;
	
	@Transient
	private String filterUid;
	
	@Transient
	private String detailFormUid;
	
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
	
	@XmlTransient
	public Transformer getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = (TransformerMetadata) transformer;
	}
	
	@XmlTransient
	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = (FilterMetadata) filter;
	}
	
	@XmlTransient
	public Form getDetailForm() {
		return detailForm;
	}

	public void setDetailForm(Form detailForm) {
		this.detailForm = (FormMetadata) detailForm;
	}
	
	@XmlAttribute
	public boolean isReadonly() {
		return isReadonly;
	}

	public void setReadonly(boolean isReadonly) {
		this.isReadonly = isReadonly;
	}
	
	@XmlAttribute
	public boolean isUnsortedValues() {
		return isUnsortedValues;
	}

	public void setUnsortedValues(boolean isUnsortedValues) {
		this.isUnsortedValues = isUnsortedValues;
	}

	@XmlAttribute
	public String getEntityFieldUid() {
		return entityField != null ? entityField.getUid() : entityFieldUid;
	}

	public void setEntityFieldUid(String entityFieldUid) {
		this.entityFieldUid = entityFieldUid;
	}
	
	@XmlAttribute
	public String getTransformerUid() {
		return transformer != null ? transformer.getUid() : transformerUid;
	}

	public void setTransformerUid(String transformerUid) {
		this.transformerUid = transformerUid;
	}
	
	@XmlAttribute
	public String getFilterUid() {
		return filter != null ? filter.getUid() : filterUid;
	}

	public void setFilterUid(String filterUid) {
		this.filterUid = filterUid;
	}
	
	@XmlAttribute
	public String getDetailFormUid() {
		return detailForm != null ? detailForm.getUid() : detailFormUid;
	}

	public void setDetailFormUid(String detailFormUid) {
		this.detailFormUid = detailFormUid;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !FormFieldExtra.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final FormFieldExtra otherExtra = (FormFieldExtra) other;
		return new EqualsBuilder()
				.append(entityFieldUid, otherExtra.getEntityFieldUid())
				.append(transformerUid, otherExtra.getTransformerUid())
				.append(filterUid, otherExtra.getFilterUid())
				.append(detailFormUid, otherExtra.getDetailFormUid())
				.append(isReadonly, otherExtra.isReadonly())
				.append(isUnsortedValues, otherExtra.isUnsortedValues())
				.isEquals();
	}
	
}
