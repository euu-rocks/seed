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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterMetadata;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerMetadata;

@Entity
@Table(name = "sys_subform_field")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SubFormField extends AbstractFormField {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subform_id")
	private SubForm subForm;
	
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
	
	private boolean isBandbox;
	
	@Transient
	private String transformerUid;
	
	@Transient
	private String filterUid;
	
	@Transient
	private String detailFormUid;
	
	@XmlTransient
	public SubForm getSubForm() {
		return subForm;
	}

	public void setSubForm(SubForm subForm) {
		this.subForm = subForm;
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
	public boolean isBandbox() {
		return isBandbox;
	}

	public void setBandbox(boolean isBandbox) {
		this.isBandbox = isBandbox;
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

	public String getName() {
		return getLabel() != null ? getLabel() : getEntityField().getName();
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final SubFormField otherField = (SubFormField) other;
		return new EqualsBuilder()
			.append(getEntityFieldUid(), otherField.getEntityFieldUid())
			.append(transformerUid, otherField.getTransformerUid())
			.append(filterUid, otherField.getFilterUid())
			.append(detailFormUid, otherField.getDetailFormUid())
			.append(getLabel(), otherField.getLabel())
			.append(getWidth(), otherField.getWidth())
			.append(getHeight(), otherField.getHeight())
			.append(getHflex(), otherField.getHflex())
			.append(getStyle(), otherField.getStyle())
			.append(getLabelStyle(), otherField.getLabelStyle())
			.append(isReadonly, otherField.isReadonly)
			.append(isBandbox, otherField.isBandbox)
			.isEquals();
	}

}
