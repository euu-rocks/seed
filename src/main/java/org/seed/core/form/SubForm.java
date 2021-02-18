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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;

import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.application.TransferableObject;
import org.seed.core.data.AbstractSystemObject;
import org.seed.core.data.Order;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.value.ValueObject;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

@Entity
@Table(name = "sys_subform")
public class SubForm extends AbstractSystemObject
	implements TransferableObject {

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
	private FormMetadata form;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nested_entity_id")
	private NestedEntity nestedEntity;
	
	@OneToMany(mappedBy = "subForm",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@OrderBy("order")
	private List<SubFormField> fields;
	
	@OneToMany(mappedBy = "subForm",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@OrderBy("order")
	private List<SubFormAction> actions;
	
	private String uid;
	
	@Transient
	private String nestedEntityUid;
	
	@Transient
	private ValueObject selectedObject;
	
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
	public NestedEntity getNestedEntity() {
		return nestedEntity;
	}

	public void setNestedEntity(NestedEntity nestedEntity) {
		this.nestedEntity = nestedEntity;
	}
	
	@XmlTransient
	public String getNestedEntityUid() {
		return nestedEntity != null ? nestedEntity.getUid() : nestedEntityUid;
	}

	public void setNestedEntityUid(String nestedEntityUid) {
		this.nestedEntityUid = nestedEntityUid;
	}

	@XmlTransient
	public ValueObject getSelectedObject() {
		return selectedObject;
	}

	public void setSelectedObject(ValueObject selectedObject) {
		this.selectedObject = selectedObject;
	}
	
	public boolean hasFields() {
		return !ObjectUtils.isEmpty(getFields());
	}
	
	public List<SubFormField> getFields() {
		return fields;
	}

	public void setFields(List<SubFormField> fields) {
		this.fields = fields;
	}
	
	public void addField(SubFormField field) {
		Assert.notNull(field, "field is null");
		
		if (fields == null) {
			fields = new ArrayList<>();
		}
		fields.add(field);
	}
	
	public boolean hasActions() {
		return !ObjectUtils.isEmpty(getActions());
	}
	
	public List<SubFormAction> getActions() {
		return actions;
	}
	
	public void addAction(SubFormAction action) {
		Assert.notNull(action, "action is null");
		
		if (actions == null) {
			actions = new ArrayList<>();
		}
		actions.add(action);
	}
	
	public SubFormField getFieldByUid(String fieldUid) {
		Assert.notNull(fieldUid, "fieldUid is null");
		
		return AbstractApplicationEntity.getObjectByUid(getFields(), fieldUid);
	}
	
	public SubFormAction getActionByUid(String actionUid) {
		Assert.notNull(actionUid, "actionUid is null");
		
		return AbstractApplicationEntity.getObjectByUid(getActions(), actionUid);
	}
	
	public SubFormField getFieldByEntityFieldUid(String entityFieldUid) {
		Assert.notNull(entityFieldUid, "entityFieldUid is null");
		
		if (hasFields()) {
			for (SubFormField field : getFields()) {
				if (entityFieldUid.equals(field.getEntityField().getUid())) {
					return field;
				}
			}
		}
		return null;
	}
	
	public SubFormAction getActionByType(FormActionType actionType) {
		if (hasActions()) {
			return getActionByType(getActions(), actionType);
		}
		return null;
	}
	
	public SubFormAction getActionByType(List<SubFormAction> actions, FormActionType actionType) {
		Assert.notNull(actions, "actions is null");
		Assert.notNull(actionType, "actionType is null");
		
		for (SubFormAction action : actions) {
			if (action.getType() == actionType) {
				return action;
			}
		}
		return null;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !SubForm.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final SubForm otherSubForm = (SubForm) other;
		if (!new EqualsBuilder()
			.append(nestedEntityUid, otherSubForm.getNestedEntityUid())
			.isEquals()) {
			return false;
		}
		// check fields
		if (hasFields()) {
			for (SubFormField field : getFields()) {
				if (!field.isEqual(otherSubForm.getFieldByUid(field.getUid()))) {
					return false;
				}
			}
		}
		if (otherSubForm.hasFields()) {
			for (SubFormField otherField : otherSubForm.getFields()) {
				if (getFieldByUid(otherField.getUid()) == null) {
					return false;
				}
			}
 		}
		// check actions
		if (hasActions()) {
			for (SubFormAction action : getActions()) {
				if (!action.isEqual(otherSubForm.getActionByUid(action.getUid()))) {
					return false;
				}
			}
		}
		if (otherSubForm.hasActions()) {
			for (SubFormAction otherAction : otherSubForm.getActions()) {
				if (getActionByUid(otherAction.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	protected void setOrderIndexes() {
		Order.setOrderIndexes(getFields());
		Order.setOrderIndexes(getActions());
	}
	
}
