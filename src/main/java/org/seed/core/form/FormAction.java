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

@Entity
@Table(name = "sys_form_action")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FormAction extends AbstractFormAction {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
	private FormMetadata form;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_form_id")
	private FormMetadata targetForm;
	
	@Transient
	private String targetFormUid;
	
	@XmlAttribute
	public String getTargetFormUid() {
		return targetForm != null ? targetForm.getUid() : targetFormUid;
	}

	public void setTargetFormUid(String targetFormUid) {
		this.targetFormUid = targetFormUid;
	}
	
	@XmlTransient
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = (FormMetadata) form;
	}
	
	@XmlTransient
	public Form getTargetForm() {
		return targetForm;
	}

	public void setTargetForm(Form targetForm) {
		this.targetForm = (FormMetadata) targetForm;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final FormAction otherAction = (FormAction) other;
		return new EqualsBuilder()
				.append(targetFormUid, otherAction.getTargetFormUid())
				.append(getEntityFunctionUid(), otherAction.getEntityFunctionUid())
				.append(getType(), otherAction.getType())
				.append(getLabel(), otherAction.getLabel())
				.isEquals();
	}
	
}