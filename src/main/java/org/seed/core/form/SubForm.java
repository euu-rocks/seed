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

import static org.seed.core.util.CollectionUtils.*;

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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.C;
import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.application.AbstractTransferableObject;
import org.seed.core.data.Order;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.util.Assert;

@Entity
@Table(name = "sys_subform")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SubForm extends AbstractTransferableObject {

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
	
	@Transient
	private String nestedEntityUid;
	
	@Transient
	private ValueObject selectedObject;
	
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
	
	@XmlAttribute
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
	
	public void clearSelectedObject() {
		selectedObject = null;
	}

	public void setSelectedObject(ValueObject selectedObject) {
		this.selectedObject = selectedObject;
	}
	
	public boolean hasFields() {
		return notEmpty(getFields());
	}
	
	@XmlElement(name="subformfield")
	@XmlElementWrapper(name="subformfields")
	public List<SubFormField> getFields() {
		return fields;
	}

	public void setFields(List<SubFormField> fields) {
		this.fields = fields;
	}
	
	public void addField(SubFormField field) {
		Assert.notNull(field, C.FIELD);
		
		if (fields == null) {
			fields = new ArrayList<>();
		}
		field.setSubForm(this);
		fields.add(field);
	}
	
	public boolean hasActions() {
		return notEmpty(getActions());
	}
	
	@XmlElement(name="subformaction")
	@XmlElementWrapper(name="subformactions")
	public List<SubFormAction> getActions() {
		return actions;
	}
	
	public void setActions(List<SubFormAction> actions) {
		this.actions = actions;
	}

	public void addAction(SubFormAction action) {
		Assert.notNull(action, C.ACTION);
		
		if (actions == null) {
			actions = new ArrayList<>();
		}
		action.setSubForm(this);
		actions.add(action);
	}
	
	public SubFormField getFieldByUid(String fieldUid) {
		Assert.notNull(fieldUid, "field uid");
		
		return AbstractApplicationEntity.getObjectByUid(getFields(), fieldUid);
	}
	
	public SubFormAction getActionByUid(String actionUid) {
		Assert.notNull(actionUid, "action uid");
		
		return AbstractApplicationEntity.getObjectByUid(getActions(), actionUid);
	}
	
	public SubFormField getFieldByEntityField(EntityField entityField) {
		Assert.notNull(entityField, "entityField");
		
		return firstMatch(getFields(), field -> entityField.equals(field.getEntityField()));
	}
	
	public SubFormField getFieldByEntityFieldUid(String entityFieldUid) {
		Assert.notNull(entityFieldUid, "entity field uid");
		
		return firstMatch(getFields(), field -> entityFieldUid.equals(field.getEntityField().getUid()));
	}
	
	public boolean containsForm(Form form) {
		Assert.notNull(form, C.FORM);
		
		return anyMatch(getFields(), field -> form.equals(field.getDetailForm()));
	}
	
	public boolean containsEntityField(EntityField entityField) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		
		return anyMatch(getFields(), field -> entityField.equals(field.getEntityField()));           
	}
	
	public boolean containsEntityFunction(EntityFunction entityFunction) {
		Assert.notNull(entityFunction, "entity function");
		
		return anyMatch(getActions(), action -> entityFunction.equals(action.getEntityFunction()));
	}
	
	public boolean containsTransformer(Transformer transformer) {
		Assert.notNull(transformer, C.TRANSFORMER);
		
		return anyMatch(getFields(), field -> transformer.equals(field.getTransformer()));
	}
	
	public boolean containsFilter(Filter filter) {
		Assert.notNull(filter, C.FILTER);
		
		return anyMatch(getFields(), field -> filter.equals(field.getFilter()));
	}
	
	public SubFormAction getActionByType(FormActionType actionType) {
		if (hasActions()) {
			return getActionByType(getActions(), actionType);
		}
		return null;
	}
	
	public SubFormAction getActionByType(List<SubFormAction> actions, FormActionType actionType) {
		Assert.notNull(actions, "actions");
		Assert.notNull(actionType, "action type");
		
		return firstMatch(actions, action -> action.getType() == actionType);
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
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
		return isEqualFields(otherSubForm) &&
			   isEqualActions(otherSubForm);
	}
	
	private boolean isEqualFields(SubForm otherSubForm) {
		return !(anyMatch(fields, field -> !field.isEqual(otherSubForm.getFieldByUid(field.getUid()))) || 
				 anyMatch(otherSubForm.getFields(), field -> getFieldByUid(field.getUid()) == null));
	}
	
	private boolean isEqualActions(SubForm otherSubForm) {
		return !(anyMatch(actions, action -> !action.isEqual(otherSubForm.getActionByUid(action.getUid()))) || 
				 anyMatch(otherSubForm.getActions(), action -> getActionByUid(action.getUid()) == null));
	}
	
	@Override
	protected void setOrderIndexes() {
		Order.setOrderIndexes(getFields());
		Order.setOrderIndexes(getActions());
	}
	
}
