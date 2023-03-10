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
package org.seed.ui.zk.vm;

import java.util.Collections;
import java.util.List;

import org.seed.C;
import org.seed.core.api.ApplicationException;
import org.seed.core.data.QueryCursor;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectRepository;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.form.AbstractFormAction;
import org.seed.core.form.Form;
import org.seed.core.form.FormAction;
import org.seed.core.form.FormActionType;
import org.seed.core.form.FormFieldExtra;
import org.seed.core.form.FormMetadata;
import org.seed.core.form.FormPrintout;
import org.seed.core.form.FormService;
import org.seed.core.form.FormTransformer;
import org.seed.core.form.RelationForm;
import org.seed.core.form.SubForm;
import org.seed.core.form.SubFormAction;
import org.seed.core.form.printout.PrintoutService;
import org.seed.core.util.Assert;

import org.seed.ui.FormParameter;
import org.seed.ui.Tab;
import org.seed.ui.zk.LoadOnDemandListModel;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Bandpopup;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;

abstract class AbstractFormViewModel extends AbstractApplicationViewModel {
	
	protected static final String FIELD_UID           = "fieldUid";
	protected static final String REFERENCE_FIELD_UID = "referenceFieldUid";
	
	@WireVariable(value="entityServiceImpl")
	private EntityService entityService;
	
	@WireVariable(value="formServiceImpl")
	private FormService formService;
	
	@WireVariable(value="printoutServiceImpl")
	private PrintoutService printoutService;
	
	@WireVariable(value="valueObjectServiceImpl")
	private ValueObjectService valueObjectService;
	
	private FormParameter formParameter;
	
	private Form form;
	
	private Tab tab;
	
	private ValueObject object;
	
	private EntityStatus status;
	
	private FormTransformer transformer;

	public Form getForm() {
		return form;
	}

	public Tab getTab() {
		return tab;
	}

	public ValueObject getObject() {
		return object;
	}

	public void setObject(ValueObject object) {
		this.object = object;
	}
	
	public FormTransformer getTransformer() {
		return transformer;
	}

	public void setTransformer(FormTransformer transformer) {
		this.transformer = transformer;
	}

	public EntityStatus getStatus() {
		return status;
	}

	public void setStatus(EntityStatus status) {
		this.status = status;
	}
	
	public boolean hasStatus() {
		return form.getEntity().hasStatus();
	}
	
	public List<EntityStatus> getAvailableStatusList() {
		return object != null && object.getEntityStatus() != null
				? entityService.getAvailableStatusList(form.getEntity(), object.getEntityStatus(), getUser()) 
				: null;
	}
	
	public List<FormTransformer> getTransformers() {
		return object != null 
				? formService.getFormTransformers(form, getUser(), object.getEntityStatus())
				: null;
	}
	
	public List<FormAction> getListActions() {
		return formService.getListFormActions(form);
	}
	
	public List<FormAction> getDetailActions() {
		return formService.getDetailFormActions(form);
	}
	
	public boolean isActionEnabled(FormAction action) {
		return form.isActionEnabled(action, getUser());
	}
	
	public boolean isActionEnabled(SubFormAction action) {
		return form.isActionEnabled(action, getUser());
	}
	
	public boolean isSubFormVisible(String nestedEntityUid) {
		return form.isSubFormVisible(nestedEntityUid, getUser());
	}
	
	public boolean isRelationFormVisible(String relationEntityUid) {
		return form.isRelationFormVisible(relationEntityUid, getUser());
	}
	
	public List<SubFormAction> getSubFormActions(String nestedEntityUid) {
		return getSubForm(nestedEntityUid).getActions();
	}
	
	public SubForm getSubForm(String nestedEntityUid) {
		final SubForm subForm = form.getSubFormByNestedEntityUid(nestedEntityUid);
		Assert.stateAvailable(subForm, "sub form for nested " + nestedEntityUid);
		return subForm;
	}
	
	public RelationForm getRelationForm(String relationFormUid) {
		final RelationForm relationForm = form.getRelationFormByUid(relationFormUid);
		Assert.stateAvailable(relationForm, "relation form for relation " + relationFormUid);
		return relationForm;
	}
	
	public String getIdentifier(ValueObject object) {
		return object != null 
				? valueObjectService.getIdentifier(object, currentSession())
				: null;
	}
	
	public String getActionLabel(AbstractFormAction action) {
		String label = null;
		if (action != null) {
			if (action.getLabel() != null) {
				label = action.getLabel();
			}
			else if (action.isCustom()) {
				if (action.getEntityFunction() != null) {
					label = action.getEntityFunction().getName();
				}
			}
			else {
				label = getActionLabel(action.getType());
			}
		}
		return label;
	}
	
	public List<ValueObject> asList(ValueObject object) {
		return Collections.singletonList(object);
	}
	
	public String getLayoutInclude() {
		return "/generated" + getLayoutPath() + '/' + form.getId();
	}
	
	protected abstract String getLayoutPath();
	
	protected void init(FormParameter formParameter) {
		Assert.notNull(formParameter, "formParameter");
		this.formParameter = formParameter;
		form = formParameter.form;
		tab = formParameter.getTab();
		Assert.stateAvailable(form, C.FORM);
		Assert.stateAvailable(tab, C.TAB);
		form = formService.getObject(form.getId(), currentSession());
	}
	
	protected void showListForm() {
		showView("/form/listform.zul", new FormParameter(form, tab));
	}
	
	protected void showSearchForm() {
		showView("/form/searchform.zul", new FormParameter(form, tab));
	}
	
	protected void showDetailForm(Long objectId) {
		showView("/form/detailform.zul", new FormParameter(form, tab, objectId));
	}
	
	protected void showDetailForm(Form form, ValueObject object) {
		showView("/form/detailform.zul", new FormParameter(form, tab, object));
	}
	
	protected void showSelectFieldsDialog() {
		showDialog("/form/selectcolumns.zul", this);
	}
	
	protected void notifyObjectChange(String property) {
		Assert.notNull(property, C.PROPERTY);
		
		notifyObjectChange(object, property);
	}
	
	protected FormParameter getFormParameter() {
		return formParameter;
	}
	
	protected FormService formService() {
		return formService;
	}
 	
	protected ValueObjectService valueObjectService() {
		return valueObjectService;
	}
	
	protected void deleteObject() throws ValidationException {
		valueObjectService.deleteObject(object);
		object = null;
	}
	
	protected void printObject() {
		if (form.getPrintouts().size() == 1) {
			print(form.getPrintouts().get(0));
		}
		else {
			showDialog("/form/selectprintout.zul", this);
		}
	}
	
	protected void callCustomAction(Component component, FormAction action) {
		callEntityFunction(component, object, action.getEntityFunction());
	}
	
	protected void transformObject() {
		final ValueObject targetObject = valueObjectService.transform(transformer.getTransformer(), object, currentSession());
		showDetailForm(transformer.getTargetForm(), targetObject);
	}
	
	protected List<ValueObject> getReferenceValues(EntityField referenceField, Filter filter) {
		final List<ValueObject> valueObjectList = filter != null 
												? valueObjectService.find(currentSession(), referenceField.getReferenceEntity(), filter)
												: valueObjectService.getAllObjects(currentSession(), referenceField.getReferenceEntity());
		final FormFieldExtra fieldExtra = form.getFieldExtra(referenceField);
		if (fieldExtra == null || !fieldExtra.isUnsortedValues()) {
			valueObjectService.sortObjects(valueObjectList);
		}
		return valueObjectList;
	}
	
	protected boolean checkObjectExistence() {
		if (object == null || (!object.isNew() && valueObjectService.getObject(currentSession(), form.getEntity(), object.getId()) == null)) {
			showWarnMessage(getLabel("form.action.faildeleted"));
			return false;
		}
		return true;
	}
	
	protected boolean checkFormIntegrity() {
		final Form reloadedForm = formService().getObject(form.getId(), currentSession());
		if (reloadedForm == null) {
			showWarnMessage(getLabel("form.action.formdeleted"));
			return false;
		}
		else if (reloadedForm.getVersion() != form.getVersion()) {
			showWarnMessage(getLabel("form.action.formaltered"));
			return false;
		}
		if (form.getEntity().getVersion() != entityService.getObject(form.getEntity().getId(), currentSession()).getVersion()) {
			showWarnMessage(getLabel("form.action.entityaltered"));
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("serial")
	protected ListModel<ValueObject> createReferenceListModel(EntityField referenceField, Filter filter) {
		final QueryCursor<ValueObject> cursor = valueObjectService.createCursor(currentSession(), referenceField.getReferenceEntity(), 
																				filter, ValueObjectRepository.DEFAULT_CHUNK_SIZE);
		return new LoadOnDemandListModel<ValueObject>(cursor, true) {
			
			@Override
			protected List<ValueObject> loadChunk(QueryCursor<ValueObject> cursor) {
				return valueObjectService.loadChunk(currentSession(), cursor);
			}
		};
	}
	
	protected void callSubFormAction(Component component, SubForm subForm, SubFormAction action) {
		Assert.notNull(subForm, C.SUBFORM);
		Assert.notNull(action, C.ACTION);
		
		switch (action.getType()) {
			case NEWOBJECT:
				if (checkObjectExistence()) {
					final ValueObject nestedObject = valueObjectService.addNestedInstance(getObject(), subForm.getNestedEntity());
					valueObjectService.preallocateFileObjects(nestedObject, currentSession());
				}
				break;
			
			case DELETE:
				if (checkObjectExistence()) {
					valueObjectService.removeNestedObject(getObject(), subForm.getNestedEntity(), 
														  subForm.getSelectedObject());
					subForm.clearSelectedObject();
					notifyChange("getSubForm");
				}
				break;
				
			case CUSTOM:
				if (checkObjectExistence()) {
					callEntityFunction(component, subForm.getSelectedObject(), action.getEntityFunction());
				}
				break;
				
			default:
				throw new UnsupportedOperationException(action.getType().name());
		}
	}
	
	protected SubForm getSubFormByEntityId(Long entityId) {
		Assert.notNull(entityId, "entityId");
		
		final SubForm subForm = form.getSubFormByEntityId(entityId);
		Assert.state(subForm != null, "subform not found for entity " + entityId);
		return subForm;
	}
	
	protected void resetSubForms() {
		if (getForm().hasSubForms()) {
			getForm().getSubForms().forEach(SubForm::clearSelectedObject);
		}
	}
	
	protected void resetRelationForms() {
		((FormMetadata) getForm()).clearRelationForms();
	}
	
	protected void closeComponent(Component component) {
		if (component instanceof Listbox) {
			component = component.getParent();
			if (component instanceof Bandpopup) {
				component = component.getParent();
				if (component instanceof Bandbox) {
					((Bandbox) component).close();
				}
			}
		}
	}
	
	void addRelationObject(EntityRelation relation, ValueObject relatedObject) {
		valueObjectService.addRelation(getObject(), relation, relatedObject);
		notifyObjectChange(relation.getInternalName());
		flagDirty();
	}
	
	void removeRelationObject(EntityRelation relation) {
		final RelationForm relationForm = getRelationForm(relation.getUid());
		valueObjectService.removeRelation(getObject(), relation, relationForm.getSelectedObject());
		relationForm.clearSelectedObject();
		notifyObjectChange(relation.getInternalName());
		flagDirty();
	}
	
	void print(FormPrintout printout) {
		final byte[] printoutData = printoutService.print(printout, object);
		Filedownload.save(printoutData, printout.getContentType(), printout.getFileName());
	}
	
	private String getActionLabel(FormActionType actionType) {
		return getLabel("button." + actionType.name().toLowerCase());
	}
	
	private void callEntityFunction(Component component, ValueObject object, EntityFunction function) {
		try {
			final String successMessage = valueObjectService.callUserActionFunction(currentSession(), object, function);
			if (successMessage != null) {
				showNotification(component, false, "value.parameter", successMessage);
			}
			notifyChangeAll();
		}
		catch (ApplicationException applicationException) {
			showErrorMessage(applicationException.getMessage());
		}
	}
	
	protected static void checkReferenceField(EntityField referenceField, String referenceFieldUid) {
		Assert.stateAvailable(referenceField, "referenceField: " + referenceFieldUid);
	}
	
}
