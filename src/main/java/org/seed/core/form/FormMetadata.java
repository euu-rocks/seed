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

import org.seed.C;
import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.data.FieldAccess;
import org.seed.core.data.Order;
import org.seed.core.data.SystemField;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityAccess;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityMetadata;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.user.User;
import org.seed.core.util.Assert;

import org.springframework.util.ObjectUtils;

@javax.persistence.Entity
@Table(name = "sys_form")
public class FormMetadata extends AbstractApplicationEntity implements Form {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
	private EntityMetadata entity;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layout_id")
	private FormLayout layout;
	
	@OneToMany(mappedBy = "form",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@OrderBy("order")
	private List<FormField> fields;
	
	@OneToMany(mappedBy = "form",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	private List<FormFieldExtra> fieldExtras; 
	
	@OneToMany(mappedBy = "form",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@OrderBy("order")
	private List<FormAction> actions;
	
	@OneToMany(mappedBy = "form",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@OrderBy("order")
	private List<FormTransformer> transformers;
	
	@OneToMany(mappedBy = "form",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@OrderBy("order")
	private List<FormPrintout> printouts;
	
	@OneToMany(mappedBy = "form",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	private List<SubForm> subForms;
	
	private boolean autoLayout;
	
	@Transient
	private String entityUid;
	
	@Transient
	private String layoutUid;
	
	@Override
	@XmlTransient
	public Entity getEntity() {
		return entity;
	}
	
	public void setEntity(Entity entity) {
		this.entity = (EntityMetadata) entity;
	}
	
	@Override
	@XmlAttribute
	public boolean isAutoLayout() {
		return autoLayout;
	}

	public void setAutoLayout(boolean autoLayout) {
		this.autoLayout = autoLayout;
	}

	@Override
	@XmlElement(name="field")
	@XmlElementWrapper(name="fields")
	public List<FormField> getFields() {
		return fields;
	}
	
	public void setFields(List<FormField> fields) {
		this.fields = fields;
	}

	@Override
	@XmlElement(name="fieldextra")
	@XmlElementWrapper(name="fieldextras")
	public List<FormFieldExtra> getFieldExtras() {
		return fieldExtras;
	}
	
	public void setFieldExtras(List<FormFieldExtra> fieldExtras) {
		this.fieldExtras = fieldExtras;
	}

	@Override
	@XmlElement(name="action")
	@XmlElementWrapper(name="actions")
	public List<FormAction> getActions() {
		return actions;
	}
	
	@Override
	public List<FormAction> getActions(boolean isList) {
		final List<FormAction> result = new ArrayList<>();
		if (hasActions()) {
			for (FormAction action : getActions()) {
				if ((isList && action.getType().isVisibleAtList) ||
				   (!isList && action.getType().isVisibleAtDetail)) {
					result.add(action);
				}
			}
		}
		return result;
	}
	
	public void setActions(List<FormAction> actions) {
		this.actions = actions;
	}

	@Override
	@XmlElement(name="transformer")
	@XmlElementWrapper(name="transformers")
	public List<FormTransformer> getTransformers() {
		return transformers;
	}
	
	public void setTransformers(List<FormTransformer> transformers) {
		this.transformers = transformers;
	}

	@Override
	@XmlElement(name="printout")
	@XmlElementWrapper(name="printouts")
	public List<FormPrintout> getPrintouts() {
		return printouts;
	}
	
	public void setPrintouts(List<FormPrintout> printouts) {
		this.printouts = printouts;
	}

	@Override
	@XmlElement(name="subform")
	@XmlElementWrapper(name="subforms")
	public List<SubForm> getSubForms() {
		return subForms;
	}
	
	public void setSubForms(List<SubForm> subForms) {
		this.subForms = subForms;
	}

	@Override
	public FormLayout getLayout() {
		return layout;
	}
	
	public void setLayout(FormLayout layout) {
		this.layout = layout;
	}
	
	@Override
	@XmlAttribute
	public String getEntityUid() {
		return entity != null ? entity.getUid() : entityUid;
	}

	public void setEntityUid(String entityUid) {
		this.entityUid = entityUid;
	}
	
	@Override
	public boolean hasFields() {
		return !ObjectUtils.isEmpty(getFields());
	}
	
	@Override
	public boolean hasFieldExtras() {
		return !ObjectUtils.isEmpty(getFieldExtras());
	}
	
	@Override
	public void addField(FormField field) {
		Assert.notNull(field, C.FIELD);
		
		if (fields == null) {
			fields = new ArrayList<>();
		}
		field.setForm(this);
		fields.add(field);
	}
	
	@Override
	public void removeField(FormField field) {
		Assert.notNull(field, C.FIELD);
		
		getFields().remove(field);
	}
	
	public boolean hasSubForms() {
		return !ObjectUtils.isEmpty(getSubForms());
	}
	
	@Override
	public void addSubForm(SubForm subForm) {
		Assert.notNull(subForm, C.SUBFORM);
		
		if (subForms == null) {
			subForms = new ArrayList<>();
		}
		subForm.setForm(this);
		subForms.add(subForm);
	}
	
	@Override
	public void removeSubForm(SubForm subForm) {
		Assert.notNull(subForm, C.SUBFORM);
		
		subForms.remove(subForm);
	}
	
	@Override
	public boolean hasPrintouts() {
		return !ObjectUtils.isEmpty(getPrintouts());
	}
	
	@Override
	public void addPrintout(FormPrintout printout) {
		Assert.notNull(printout, C.PRINTOUT);
		
		if (printouts == null) {
			printouts = new ArrayList<>();
		}
		printout.setForm(this);
		printouts.add(printout);
	}
	
	@Override
	public void removePrintout(FormPrintout printout) {
		Assert.notNull(printout, C.PRINTOUT);
		
		getPrintouts().remove(printout);
	}
	
	@Override
	public boolean hasActions() {
		return !ObjectUtils.isEmpty(getActions());
	}
	
	@Override
	public void addAction(FormAction action) {
		Assert.notNull(action, C.ACTION);
		
		if (actions == null) {
			actions = new ArrayList<>();
		}
		actions.add(action);
	}
	
	@Override
	public void removeAction(FormAction action) {
		Assert.notNull(action, C.ACTION);
		
		getActions().remove(action);
	}
	
	@Override
	public boolean hasTransformers() {
		return !ObjectUtils.isEmpty(getTransformers());
	}
	
	@Override
	public void addTransformer(FormTransformer transformer) {
		Assert.notNull(transformer, C.TRANSFORMER);
		
		if (transformers == null) {
			transformers = new ArrayList<>();
		}
		transformers.add(transformer);
	}
	
	@Override
	public void removeTransformer(FormTransformer transformer) {
		Assert.notNull(transformer, C.TRANSFORMER);
		
		getTransformers().remove(transformer);
	}
	
	@Override
	public boolean containsEntityField(EntityField entityField) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		
		if (hasFields()) {
			for (FormField formField : getFields()) {
				if (entityField.equals(formField.getEntityField())) {
					return true;
				}
			}
	    }
		return false;
	}
	
	@Override
	public boolean containsSystemField(SystemField systemField) {
		Assert.notNull(systemField, "systemField");
		
		if (hasFields()) {
			for (FormField formField : getFields()) {
				if (systemField == formField.getSystemField()) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean containsEntityFunction(EntityFunction entityFunction) {
		Assert.notNull(entityFunction, "entityFunction");
		
		if (hasActions()) {
			for (FormAction action : getActions()) {
				if (entityFunction.equals(action.getEntityFunction())) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean containsFilter(Filter filter) {
		Assert.notNull(filter, C.FILTER);
		
		if (hasFieldExtras()) {
			for (FormFieldExtra fieldExtra : getFieldExtras()) {
				if (filter.equals(fieldExtra.getFilter())) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean containsTransformer(Transformer transformer) {
		Assert.notNull(transformer, C.TRANSFORMER);
		
		if (hasTransformers()) {
			for (FormTransformer formTransformer : getTransformers()) {
				if (transformer.equals(formTransformer.getTransformer())) {
					return true;
				}
			}
		}
		if (hasFieldExtras()) {
			for (FormFieldExtra fieldExtra : getFieldExtras()) {
				if (transformer.equals(fieldExtra.getTransformer())) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean containsForm(Form form) {
		Assert.notNull(form, C.FORM);
		
		return (hasFieldExtras() && containsFormFieldExtra(form)) || 
			   (hasActions() && containsFormFormAction(form)) || 
			   (hasTransformers() && containsFormFormTransformer(form));
	}
	
	private boolean containsFormFieldExtra(Form form) {
		for (FormFieldExtra fieldExtra : getFieldExtras()) {
			if (form.equals(fieldExtra.getDetailForm())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsFormFormAction(Form form) {
		for (FormAction action : getActions()) {
			if (form.equals(action.getTargetForm())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsFormFormTransformer(Form form) {
		for (FormTransformer transformer : getTransformers()) {
			if (form.equals(transformer.getTargetForm())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public FormAction getActionByType(FormActionType actionType) {
		Assert.notNull(actionType, "actionType");
		
		if (hasActions()) {
			for (FormAction action : getActions()) {
				if (action.getType() == actionType) {
					return action;
				}
			}
		}
		return null;
	}
	
	@Override
	public FormField getFieldById(Long fieldId) {
		Assert.notNull(fieldId, "fieldId");
		
		return getObjectById(getFields(), fieldId);
	}
	
	@Override
	public FormField getFieldByUid(String fieldUid) {
		Assert.notNull(fieldUid, "fieldUid");
		
		return getObjectByUid(getFields(), fieldUid);
	}
	
	@Override
	public FormFieldExtra getFieldExtraByUid(String fieldExtraUid) {
		Assert.notNull(fieldExtraUid, "fieldExtraUid");
		
		return getObjectByUid(getFieldExtras(), fieldExtraUid);
	}
	
	@Override
	public FormAction getActionByUid(String actionUid) {
		Assert.notNull(actionUid, "actionUid");
		
		return getObjectByUid(getActions(), actionUid);
	}
	
	@Override
	public FormTransformer getTransformerByUid(String transformerUid) {
		Assert.notNull(transformerUid, "transformerUid");
		
		return getObjectByUid(getTransformers(), transformerUid);
	}
	
	@Override
	public FormPrintout getPrintoutByUid(String printoutUid) {
		Assert.notNull(printoutUid, "printoutUid");
		
		return getObjectByUid(getPrintouts(), printoutUid);
	}
	
	@Override
	public SubForm getSubFormByUid(String subFormUid) {
		Assert.notNull(subFormUid, "subFormUid");
		
		return getObjectByUid(subForms, subFormUid);
	}
	
	@Override
	public SubForm getSubFormByEntityId(Long entityId) {
		Assert.notNull(entityId, "entityId");
		
		if (hasSubForms()) {
			for (SubForm subForm : subForms) {
				if (entityId.equals(subForm.getNestedEntity().getNestedEntity().getId())) {
					return subForm;
				}
			}
		}
		return null;
	}
	
	@Override
	public SubForm getSubFormByNestedEntityId(Long nestedEntityId) {
		Assert.notNull(nestedEntityId, "nestedEntityId");
		
		if (hasSubForms()) {
			for (SubForm subForm : subForms) {
				if (nestedEntityId.equals(subForm.getNestedEntity().getId())) {
					return subForm;
				}
			}
		}
		return null;
	}
	
	@Override
	public SubForm getSubFormByNestedEntityUid(String nestedEntityUid) {
		Assert.notNull(nestedEntityUid, "nestedEntityUid");
		
		if (hasSubForms()) {
			for (SubForm subForm : subForms) {
				if (nestedEntityUid.equals(subForm.getNestedEntity().getUid())) {
					return subForm;
				}
			}
		}
		return null;
	}
	
	@Override
	public SubFormField getSubFormField(EntityField entityField) {
		if (hasSubForms()) {
			for (SubForm subForm : subForms) {
				if (subForm.hasFields()) {
					for (SubFormField subFormField : subForm.getFields()) {
						if (subFormField.getEntityField().equals(entityField)) {
							return subFormField;
						}
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public FormFieldExtra getFieldExtra(EntityField entityField) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		
		if (hasFieldExtras()) {
			for (FormFieldExtra fieldExtra : getFieldExtras()) {
				if (entityField.equals(fieldExtra.getEntityField())) {
					return fieldExtra;
				}
			}
		}
		return null;
	}
	
	@Override
	public boolean isActionEnabled(FormAction action, User user) {
		Assert.notNull(action, C.ACTION);
		Assert.notNull(user, C.USER);
		
		switch (action.getType()) {
			case DETAIL:
				return entity.checkPermissions(user, EntityAccess.READ);
			case SAVE:
				return entity.checkPermissions(user, EntityAccess.WRITE);
			case NEWOBJECT:
				return entity.checkPermissions(user, EntityAccess.CREATE);
			case DELETE:
				return entity.checkPermissions(user, EntityAccess.DELETE);
			default:
				return true;
		}
	}
	
	@Override
	public boolean isActionEnabled(SubFormAction action, User user) {
		Assert.notNull(action, C.ACTION);
		Assert.notNull(user, C.USER);
		
		switch (action.getType()) {
			case NEWOBJECT:
				return action.getSubForm().getNestedEntity().getNestedEntity()
						.checkPermissions(user, EntityAccess.CREATE);
			case DELETE:
				return action.getSubForm().getNestedEntity().getNestedEntity()
						.checkPermissions(user, EntityAccess.DELETE);
			default:
				return true;
		}
	}
	
	@Override
	public boolean isSubFormVisible(String nestedEntityUid, User user) {
		Assert.notNull(nestedEntityUid, "nestedEntityUid");
		Assert.notNull(user, C.USER);
		
		final SubForm subForm = getSubFormByNestedEntityUid(nestedEntityUid);
		Assert.state(subForm != null, "subForm not found  " + nestedEntityUid);
		return subForm.getNestedEntity().getNestedEntity()
						.checkPermissions(user, EntityAccess.READ);
	}
	
	@Override
	public boolean isFieldVisible(EntityField entityField, EntityStatus status, User user) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		Assert.notNull(user, C.USER);
		
		return entity.checkFieldAccess(entityField, user, status, FieldAccess.READ, FieldAccess.WRITE);
	}
	
	@Override
	public boolean isFieldMandatory(EntityField entityField) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		
		return entityField.isMandatory() && !entityField.getType().isAutonum();
	}
	
	@Override
	public boolean isFieldReadonly(EntityField entityField, EntityStatus status, User user) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		Assert.notNull(user, C.USER);
		
		if (entityField.getType().isAutonum() || 
			entityField.isCalculated()) {
			return true;
		}
		
		final SubFormField subFormField = getSubFormField(entityField);
		// sub form field
		if (subFormField != null) {
			if ((!subFormField.getSubForm().getNestedEntity().getNestedEntity().checkPermissions(user, EntityAccess.WRITE)) ||
				subFormField.isReadonly()) {
				return true;
			}
		}
		// main form field
		else {
			if (!entity.checkPermissions(user, EntityAccess.WRITE)) {
				return true;
			}
			final FormFieldExtra fieldExtra = getFieldExtra(entityField);
			if (fieldExtra != null && fieldExtra.isReadonly()) {
				return true;
			}
		}
		
		// check field constraints
		return !entity.checkFieldAccess(entityField, user, status, FieldAccess.WRITE);
	}
	
	@Override
	public void addFieldExtra(FormFieldExtra fieldExtra) {
		Assert.notNull(fieldExtra, "fieldExtra");
		
		fieldExtra.setForm(this);
		if (fieldExtras == null) {
			fieldExtras = new ArrayList<>();
		}
		fieldExtras.add(fieldExtra);
	}
	
	@Override
	public void removeFieldExtra(FormFieldExtra fieldExtra) {
		Assert.notNull(fieldExtra, "fieldExtra");
		
		getFieldExtras().remove(fieldExtra);
	}
	
	public void setLayoutContent(String content) {
		Assert.notNull(content, C.CONTENT);
		
		if (layout == null) {
			layout = new FormLayout();
		}
		layout.setContent(content);
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !Form.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final Form otherForm = (Form) other;
		if (!new EqualsBuilder()
			.append(getName(), otherForm.getName())
			.append(autoLayout, otherForm.isAutoLayout())
			.isEquals()) {
			return false;
		}
		
		// check layout
		if (layout != null) {
			if (!layout.isEqual(otherForm.getLayout())) {
				return false;
			}
		}
		else if (otherForm.getLayout() != null) {
			return false;
		}
		return isEqualFields(otherForm) &&
			   isEqualFieldExtras(otherForm) &&
			   isEqualActions(otherForm) &&
			   isEqualTransformers(otherForm) &&
			   isEqualPrintouts(otherForm) &&
			   isEqualSubForms(otherForm);

	}
	
	private boolean isEqualFields(Form otherForm) {
		if (hasFields()) {
			for (FormField field : getFields()) {
				if (!field.isEqual(otherForm.getFieldByUid(field.getUid()))) {
					return false;
				}
			}
		}
		if (otherForm.hasFields()) {
			for (FormField otherField : otherForm.getFields()) {
				if (getFieldByUid(otherField.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean isEqualFieldExtras(Form otherForm) {
		if (hasFieldExtras()) {
			for (FormFieldExtra extra : getFieldExtras()) {
				if (!extra.isEqual(otherForm.getFieldExtraByUid(extra.getUid()))) {
					return false;
				}
			}
		}
		if (otherForm.hasFieldExtras()) {
			for (FormFieldExtra otherExtra : otherForm.getFieldExtras()) {
				if (getFieldExtraByUid(otherExtra.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean isEqualActions(Form otherForm) {
		if (hasActions()) {
			for (FormAction action : getActions()) {
				if (!action.isEqual(otherForm.getActionByUid(action.getUid()))) {
					return false;
				}
			}
		}
		if (otherForm.hasActions()) {
			for (FormAction otherAction : otherForm.getActions()) {
				if (getActionByUid(otherAction.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean isEqualTransformers(Form otherForm) {
		if (hasTransformers()) {
			for (FormTransformer transformer : getTransformers()) {
				if (!transformer.isEqual(otherForm.getTransformerByUid(transformer.getUid()))) {
					return false;
				}
			}
		}
		if (otherForm.hasTransformers()) {
			for (FormTransformer otherTransformer : otherForm.getTransformers()) {
				if (getTransformerByUid(otherTransformer.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean isEqualPrintouts(Form otherForm) {
		if (hasPrintouts()) {
			for (FormPrintout printout : getPrintouts()) {
				if (!printout.isEqual(otherForm.getPrintoutByUid(printout.getUid()))) {
					return false;
				}
			}
		}
		if (otherForm.hasPrintouts()) {
			for (FormPrintout otherPrintout : otherForm.getPrintouts()) {
				if (getPrintoutByUid(otherPrintout.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean isEqualSubForms(Form otherForm) {
		if (hasSubForms()) {
			for (SubForm subForm : getSubForms()) {
				if (!subForm.isEqual(otherForm.getSubFormByUid(subForm.getUid()))) {
					return false;
				}
			}
		}
		if (otherForm.hasSubForms()) {
			for (SubForm otherSubForm : otherForm.getSubForms()) {
				if (getSubFormByUid(otherSubForm.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	public void clearSubForms() {
		if (subForms != null) {
			subForms.clear();
		}
	}
	
	void createLists() {
		fields = new ArrayList<>();
		transformers = new ArrayList<>();
	}
	
	@Override
	protected void setOrderIndexes() {
		Order.setOrderIndexes(getFields());
		Order.setOrderIndexes(getActions());
		Order.setOrderIndexes(getTransformers());
		Order.setOrderIndexes(getPrintouts());
		if (hasSubForms()) {
			for (SubForm subForm : getSubForms()) {
				Order.setOrderIndexes(subForm.getFields());
				Order.setOrderIndexes(subForm.getActions());
			}
		}
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getFields());
		removeNewObjects(getFieldExtras());
		removeNewObjects(getActions());
		removeNewObjects(getTransformers());
		removeNewObjects(getPrintouts());
		removeNewObjects(getSubForms());
		if (hasSubForms()) {
			for (SubForm subForm : getSubForms()) {
				removeNewObjects(subForm.getFields());
				removeNewObjects(subForm.getActions());
			}
		}
	}
	
	@Override
	public void initUid() {
		super.initUid();
		initUids(getFields());
		initUids(getFieldExtras());
		initUids(getActions());
		initUids(getTransformers());
		initUids(getPrintouts());
		if (hasSubForms()) {
			initUids(getSubForms());
			for (SubForm subForm : getSubForms()) {
				initUids(subForm.getFields());
				initUids(subForm.getActions());
			}
		}
	}
	
}
