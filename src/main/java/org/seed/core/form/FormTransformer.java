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
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractOrderedTransferableObject;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerMetadata;

@javax.persistence.Entity
@Table(name = "sys_form_transformer")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FormTransformer extends AbstractOrderedTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
	private FormMetadata form;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transformer_id")
	private TransformerMetadata transformer;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_form_id")
	private FormMetadata targetForm;
	
	private String label;
	
	@Transient
	private String transformerUid;
	
	@Transient
	private String targetFormUid;
	
	@XmlTransient
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = (FormMetadata) form;
	}
	
	@XmlTransient
	public Transformer getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = (TransformerMetadata) transformer;
	}
	
	@XmlTransient
	public Form getTargetForm() {
		return targetForm;
	}

	public void setTargetForm(Form targetForm) {
		this.targetForm = (FormMetadata) targetForm;
	}
	
	@XmlTransient
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	@XmlAttribute
	public String getTransformerUid() {
		return transformer != null ? transformer.getUid() : transformerUid;
	}

	public void setTransformerUid(String transformerUid) {
		this.transformerUid = transformerUid;
	}
	
	@XmlAttribute
	public String getTargetFormUid() {
		return targetForm != null ? targetForm.getUid() : targetFormUid;
	}

	public void setTargetFormUid(String targetFormUid) {
		this.targetFormUid = targetFormUid;
	}

	public String getName() {
		return label != null ? label : transformer.getName();
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final FormTransformer otherTransformer = (FormTransformer) other;
		return new EqualsBuilder()
			.append(getOrder(), otherTransformer.getOrder())
			.append(transformerUid, otherTransformer.getTransformerUid())
			.append(targetFormUid, otherTransformer.getTargetFormUid())
			.append(label, otherTransformer.label)
			.isEquals();
	}
}
