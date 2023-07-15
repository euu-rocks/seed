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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

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
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterMetadata;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.user.User;
import org.seed.core.util.Assert;

@javax.persistence.Entity
@Table(name = "sys_form")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FormMetadata extends AbstractApplicationEntity implements Form {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
	private EntityMetadata entity;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layout_id")
	private FormLayout layout;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_id")
	private FilterMetadata filter;
	
	@OneToMany(mappedBy = "form",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OrderBy("order")
	private List<FormField> fields;
	
	@OneToMany(mappedBy = "form",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<FormFieldExtra> fieldExtras; 
	
	@OneToMany(mappedBy = "form",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OrderBy("order")
	private List<FormAction> actions;
	
	@OneToMany(mappedBy = "form",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<FormFunction> functions;
	
	@OneToMany(mappedBy = "form",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OrderBy("order")
	private List<FormTransformer> transformers;
	
	@OneToMany(mappedBy = "form",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OrderBy("order")
	private List<FormPrintout> printouts;
	
	@OneToMany(mappedBy = "form",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<SubForm> subForms;
	
	private boolean autoLayout;
	
	private boolean expertMode;
	
	private AutolayoutType autolayoutType;
	
	@Transient
	private String entityUid;
	
	@Transient
	private String layoutUid;
	
	@Transient
	private String filterUid;
	
	@Transient
	private Map<String, RelationForm> mapRelations;
	
	@Override
	@XmlTransient
	public Entity getEntity() {
		return entity;
	}
	
	public void setEntity(Entity entity) {
		this.entity = (EntityMetadata) entity;
	}
	
	@Override
	public FormLayout getLayout() {
		return layout;
	}
	
	public void setLayout(FormLayout layout) {
		this.layout = layout;
	}
	
	@Override
	@XmlTransient
	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = (FilterMetadata) filter;
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
	@XmlAttribute
	public boolean isExpertMode() {
		return expertMode;
	}

	public void setExpertMode(boolean expertMode) {
		this.expertMode = expertMode;
	}

	@Override
	@XmlAttribute
	public AutolayoutType getAutolayoutType() {
		return autolayoutType;
	}

	public void setAutolayoutType(AutolayoutType autolayoutType) {
		this.autolayoutType = autolayoutType;
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
		return subList(getActions(), action -> (isList && action.getType().isVisibleAtList) ||
				   							   (!isList && action.getType().isVisibleAtDetail));
	}
	
	public void setActions(List<FormAction> actions) {
		this.actions = actions;
	}
	
	@Override
	@XmlElement(name="function")
	@XmlElementWrapper(name="functions")
	public List<FormFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(List<FormFunction> functions) {
		this.functions = functions;
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
	@XmlAttribute
	public String getEntityUid() {
		return entity != null ? entity.getUid() : entityUid;
	}

	public void setEntityUid(String entityUid) {
		this.entityUid = entityUid;
	}
	
	@Override
	@XmlAttribute
	public String getFilterUid() {
		return filter != null ? filter.getUid() : filterUid;
	}

	public void setFilterUid(String filterUid) {
		this.filterUid = filterUid;
	}

	@Override
	public boolean hasFields() {
		return notEmpty(getFields());
	}
	
	@Override
	public boolean hasFunctions() {
		return notEmpty(getFunctions());
	}
	
	@Override
	public boolean hasFieldExtras() {
		return notEmpty(getFieldExtras());
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
	
	@Override
	public void addFunction(FormFunction function) {
		Assert.notNull(function, C.FUNCTION);
		
		if (functions == null) {
			functions = new ArrayList<>();
		}
		function.setForm(this);
		functions.add(function);
	}
	
	@Override
	public void removeFunction(FormFunction function) {
		Assert.notNull(function, C.FUNCTION);
		
		getFunctions().remove(function);
	}
	
	@Override
	public boolean hasSubForms() {
		return notEmpty(getSubForms());
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
	public boolean hasRelationForms() {
		return mapRelations != null && !mapRelations.isEmpty();
	}
	
	@Override
	public void removeSubForm(SubForm subForm) {
		Assert.notNull(subForm, C.SUBFORM);
		
		subForms.remove(subForm);
	}
	
	@Override
	public void removeRelationForm(RelationForm relationForm) {
		Assert.notNull(relationForm, "relation form");
		
		if (mapRelations != null) {
			mapRelations.remove(relationForm.getUid());
		}
	}
	
	@Override
	public boolean hasPrintouts() {
		return notEmpty(getPrintouts());
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
		return notEmpty(getActions());
	}
	
	@Override
	public void addAction(FormAction action) {
		Assert.notNull(action, C.ACTION);
		
		if (actions == null) {
			actions = new ArrayList<>();
		}
		action.setForm(this);
		actions.add(action);
	}
	
	@Override
	public void removeAction(FormAction action) {
		Assert.notNull(action, C.ACTION);
		
		getActions().remove(action);
	}
	
	@Override
	public boolean hasTransformers() {
		return notEmpty(getTransformers());
	}
	
	@Override
	public void addTransformer(FormTransformer transformer) {
		Assert.notNull(transformer, C.TRANSFORMER);
		
		if (transformers == null) {
			transformers = new ArrayList<>();
		}
		transformer.setForm(this);
		transformers.add(transformer);
	}
	
	@Override
	public void removeTransformer(FormTransformer transformer) {
		Assert.notNull(transformer, C.TRANSFORMER);
		
		getTransformers().remove(transformer);
	}
	
	@Override
	public RelationForm getRelationFormByUid(String relationUid) {
		if (mapRelations == null) {
			mapRelations = new HashMap<>();
		}
		RelationForm relationForm = mapRelations.get(relationUid);
		if (relationForm == null) {
			final EntityRelation relation = getEntity().getRelationByUid(relationUid);
			Assert.stateAvailable(relation, "relation " + relationUid);
			
			relationForm = new RelationForm(relation);
			mapRelations.put(relationUid, relationForm);
		}
		return relationForm;
	}
	
	@Override
	public boolean containsEntityField(EntityField entityField) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		
		return anyMatch(fields, field -> entityField.equals(field.getEntityField()));
	}
	
	@Override
	public boolean containsSystemField(SystemField systemField) {
		Assert.notNull(systemField, "system field");
		
		return anyMatch(fields, field -> systemField == field.getSystemField());
	}
	
	@Override
	public boolean containsEntityFunction(EntityFunction entityFunction) {
		Assert.notNull(entityFunction, "entity function");
		
		return anyMatch(actions, action -> entityFunction.equals(action.getEntityFunction()));
	}
	
	@Override
	public boolean containsFilter(Filter filter) {
		Assert.notNull(filter, C.FILTER);
		
		return filter.equals(this.filter) || 
			   anyMatch(fieldExtras, extra -> filter.equals(extra.getFilter()));
	}
	
	@Override
	public boolean containsTransformer(Transformer transformer) {
		Assert.notNull(transformer, C.TRANSFORMER);
		
		return anyMatch(transformers, trans -> transformer.equals(trans.getTransformer())) ||
			   anyMatch(fieldExtras, extra -> transformer.equals(extra.getTransformer()));
	}
	
	@Override
	public boolean containsForm(Form form) {
		Assert.notNull(form, C.FORM);
		
		return anyMatch(fieldExtras, extra -> form.equals(extra.getDetailForm())) ||
			   anyMatch(actions, action -> form.equals(action.getTargetForm())) ||
			   anyMatch(transformers, trans -> form.equals(trans.getTargetForm()));
	}
	
	@Override
	public FormAction getActionByType(FormActionType actionType) {
		Assert.notNull(actionType, "action type");
		
		return firstMatch(getActions(), action -> action.getType() == actionType);
	}
	
	@Override
	public List<FormField> getSelectedFields(boolean selected) {
		return subList(getFields(), field -> field.isSelected() == selected);
	}
	
	@Override
	public FormField getFieldById(Long fieldId) {
		Assert.notNull(fieldId, "field id");
		
		return getObjectById(getFields(), fieldId);
	}
	
	@Override
	public FormField getFieldByUid(String fieldUid) {
		Assert.notNull(fieldUid, "field uid");
		
		return getObjectByUid(getFields(), fieldUid);
	}
	
	@Override
	public FormFunction getFunctionByUid(String functionUid) {
		Assert.notNull(functionUid, "function uid");
		
		return getObjectByUid(getFunctions(), functionUid);
	}
	
	@Override
	public FormFunction getFunctionByName(String functionName) {
		Assert.notNull(functionName, "function name");
		
		return getObjectByName(getFunctions(), functionName, true);
	}
	
	@Override
	public FormFieldExtra getFieldExtraByUid(String fieldExtraUid) {
		Assert.notNull(fieldExtraUid, "field extra uid");
		
		return getObjectByUid(getFieldExtras(), fieldExtraUid);
	}
	
	@Override
	public FormAction getActionByUid(String actionUid) {
		Assert.notNull(actionUid, "action uid");
		
		return getObjectByUid(getActions(), actionUid);
	}
	
	@Override
	public FormTransformer getTransformerByUid(String transformerUid) {
		Assert.notNull(transformerUid, "transformer uid");
		
		return getObjectByUid(getTransformers(), transformerUid);
	}
	
	@Override
	public FormPrintout getPrintoutByUid(String printoutUid) {
		Assert.notNull(printoutUid, "printout uid");
		
		return getObjectByUid(getPrintouts(), printoutUid);
	}
	
	@Override
	public SubForm getSubFormByUid(String subFormUid) {
		Assert.notNull(subFormUid, "sub form uid");
		
		return getObjectByUid(subForms, subFormUid);
	}
	
	@Override
	public SubForm getSubFormByEntityId(Long entityId) {
		Assert.notNull(entityId, "entity id");
		
		return firstMatch(getSubForms(), sub -> entityId.equals(sub.getNestedEntity().getNestedEntity().getId()));
	}
	
	@Override
	public SubForm getSubFormByNestedEntityId(Long nestedEntityId) {
		Assert.notNull(nestedEntityId, "nested entity id");
		
		return firstMatch(getSubForms(), sub -> nestedEntityId.equals(sub.getNestedEntity().getId()));
	}
	
	@Override
	public SubForm getSubFormByNestedEntityUid(String nestedEntityUid) {
		Assert.notNull(nestedEntityUid, "nested entity uid");
		
		return firstMatch(getSubForms(), sub -> nestedEntityUid.equals(sub.getNestedEntity().getUid()));
	}
	
	@Override
	public SubFormField getSubFormField(EntityField entityField) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		
		final SubForm subForm = firstMatch(getSubForms(), sub -> sub.getFieldByEntityField(entityField) != null);
		return subForm != null ? subForm.getFieldByEntityField(entityField) : null;
	}
	
	@Override
	public FormFieldExtra getFieldExtra(EntityField entityField) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		
		return firstMatch(getFieldExtras(), extra -> entityField.equals(extra.getEntityField()));
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
		Assert.stateAvailable(subForm, "subForm " + nestedEntityUid);
		return subForm.getNestedEntity().getNestedEntity()
						.checkPermissions(user, EntityAccess.READ);
	}
	
	@Override
	public boolean isRelationFormVisible(String relationEntityUid, User user) {
		Assert.notNull(relationEntityUid, "relationEntityUid");
		Assert.notNull(user, C.USER);
		
		final EntityRelation relation = getEntity().getRelationByUid(relationEntityUid);
		Assert.stateAvailable(relation, "relation " + relationEntityUid);
		return relation.getRelatedEntity().checkPermissions(user, EntityAccess.READ);
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
		
		if (fieldExtras == null) {
			fieldExtras = new ArrayList<>();
		}
		fieldExtra.setForm(this);
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
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final Form otherForm = (Form) other;
		if (!new EqualsBuilder()
			.append(getName(), otherForm.getName())
			.append(getFilterUid(), otherForm.getFilterUid())
			.append(expertMode, otherForm.isExpertMode())
			.append(autoLayout, otherForm.isAutoLayout())
			.append(autolayoutType, otherForm.getAutolayoutType())
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
			   isEqualFunctions(otherForm) &&
			   isEqualActions(otherForm) &&
			   isEqualTransformers(otherForm) &&
			   isEqualPrintouts(otherForm) &&
			   isEqualSubForms(otherForm);

	}
	
	private boolean isEqualFields(Form otherForm) {
		return !(anyMatch(fields, field -> !field.isEqual(otherForm.getFieldByUid(field.getUid()))) || 
				 anyMatch(otherForm.getFields(), field -> getFieldByUid(field.getUid()) == null));
	}
	
	private boolean isEqualFunctions(Form otherForm) {
		return !(anyMatch(functions, function -> !function.isEqual(otherForm.getFunctionByUid(function.getUid()))) || 
				 anyMatch(otherForm.getFunctions(), function -> getFunctionByUid(function.getUid()) == null));
	}
	
	private boolean isEqualFieldExtras(Form otherForm) {
		return !(anyMatch(fieldExtras, extra -> !extra.isEqual(otherForm.getFieldExtraByUid(extra.getUid()))) || 
				 anyMatch(otherForm.getFieldExtras(), extra -> getFieldExtraByUid(extra.getUid()) == null));
	}
	
	private boolean isEqualActions(Form otherForm) {
		return !(anyMatch(actions, action -> !action.isEqual(otherForm.getActionByUid(action.getUid()))) || 
				 anyMatch(otherForm.getActions(), action -> getActionByUid(action.getUid()) == null));
	}
	
	private boolean isEqualTransformers(Form otherForm) {
		return !(anyMatch(transformers, transformer -> !transformer.isEqual(otherForm.getTransformerByUid(transformer.getUid()))) || 
				 anyMatch(otherForm.getTransformers(), transformer -> getTransformerByUid(transformer.getUid()) == null));
	}
	
	private boolean isEqualPrintouts(Form otherForm) {
		return !(anyMatch(printouts, printout -> !printout.isEqual(otherForm.getPrintoutByUid(printout.getUid()))) || 
				 anyMatch(otherForm.getPrintouts(), printout -> getPrintoutByUid(printout.getUid()) == null));
	}
	
	private boolean isEqualSubForms(Form otherForm) {
		return !(anyMatch(subForms, subForm -> !subForm.isEqual(otherForm.getSubFormByUid(subForm.getUid()))) || 
				 anyMatch(otherForm.getSubForms(), subForm -> getSubFormByUid(subForm.getUid()) == null));
	}
	
	public void clearSubForms() {
		if (subForms != null) {
			subForms.clear();
		}
	}
	
	public void clearRelationForms() {
		if (mapRelations != null) {
			mapRelations.clear();
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
		removeNewObjects(getFunctions());
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
		initUids(getFunctions());
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
