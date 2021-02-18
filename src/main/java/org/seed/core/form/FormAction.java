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
import org.seed.core.data.AbstractOrderedSystemObject;
import org.seed.core.entity.EntityFunction;

@Entity
@Table(name = "sys_form_action")
public class FormAction extends AbstractOrderedSystemObject
	implements TransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
	private FormMetadata form;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_form_id")
	private FormMetadata targetForm;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_function_id")
	private EntityFunction entityFunction;
	
	private String uid;
	
	private FormActionType type;
	
	private String label;
	
	@Transient
	private String targetFormUid;
	
	@Transient
	private String entityFunctionUid;
	
	@Override
	@XmlAttribute
	public String getUid() {
		return uid;
	}
	
	@Override
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	@XmlAttribute
	public String getTargetFormUid() {
		return targetForm != null ? targetForm.getUid() : targetFormUid;
	}

	public void setTargetFormUid(String targetFormUid) {
		this.targetFormUid = targetFormUid;
	}
	
	@XmlAttribute
	public String getEntityFunctionUid() {
		return entityFunction != null ? entityFunction.getUid() : entityFunctionUid;
	}

	public void setEntityFunctionUid(String entityFunctionUid) {
		this.entityFunctionUid = entityFunctionUid;
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
	
	@XmlTransient
	public EntityFunction getEntityFunction() {
		return entityFunction;
	}

	public void setEntityFunction(EntityFunction entityFunction) {
		this.entityFunction = entityFunction;
	}

	@XmlAttribute
	public FormActionType getType() {
		return type;
	}

	public void setType(FormActionType type) {
		this.type = type;
	}
	
	@XmlAttribute
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean isCustom() {
		return type == FormActionType.CUSTOM;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !FormAction.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final FormAction otherAction = (FormAction) other;
		return new EqualsBuilder()
				.append(targetFormUid, otherAction.getTargetFormUid())
				.append(entityFunctionUid, otherAction.getEntityFunctionUid())
				.append(type, otherAction.type)
				.append(label, otherAction.label)
				.isEquals();
	}
	
}