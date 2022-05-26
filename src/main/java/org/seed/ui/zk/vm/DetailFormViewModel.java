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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.OptimisticLockException;

import org.seed.C;
import org.seed.core.data.FileObject;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.revision.Revision;
import org.seed.core.entity.value.revision.RevisionService;
import org.seed.core.form.Form;
import org.seed.core.form.FormAction;
import org.seed.core.form.FormActionType;
import org.seed.core.form.FormFieldExtra;
import org.seed.core.form.SubForm;
import org.seed.core.form.SubFormAction;
import org.seed.core.form.SubFormField;
import org.seed.core.util.Assert;
import org.seed.core.util.MultiKey;
import org.seed.ui.FormParameter;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModel;

public class DetailFormViewModel extends AbstractFormViewModel {
	
	private final Map<MultiKey, ListModel<ValueObject>> listModelMap = new ConcurrentHashMap<>();
	
	@WireVariable(value="revisionServiceImpl")
	private RevisionService revisionService;
	
	private List<FileObject> fileObjects; // initial file objects
	
	private List<Revision> revisions;
	
	private Revision revision;
	
	public Revision getRevision() {
		return revision;
	}

	public void setRevision(Revision revision) {
		this.revision = revision;
	}

	public List<Revision> getRevisions() {
		if (revisions == null && isAudited() && !getObject().isNew()) {
			revisions = revisionService.getRevisions(getForm().getEntity(), getObject().getId());
		}
		return revisions;
	}
	
	public boolean isAudited() {
		return getForm().getEntity().isAudited();
	}
	
	public boolean isActionDisabled(FormAction action) {
		return revision != null && 
			   action.getType() != FormActionType.OVERVIEW &&
			   action.getType() != FormActionType.NEWOBJECT &&
			   action.getType() != FormActionType.REFRESH;
	}
	
	public boolean isActionDisabled(SubFormAction action) {
		return revision != null;
	}
	
	public boolean isFieldVisible(String fieldUid) {
		final EntityField field = getEntityField(fieldUid);
		return getForm().isFieldVisible(field, getStatus(), getUser());
	}
	
	public boolean isFieldMandatory(String fieldUid) {
		final EntityField field = getEntityField(fieldUid);
		return getForm().isFieldMandatory(field);
	}
	
	public boolean isFieldReadonly(String fieldUid) {
		if (revision != null) {
			return true;
		}
		final EntityField field = getEntityField(fieldUid);
		return getForm().isFieldReadonly(field, getStatus(), getUser());
	}
	
	public boolean isReferenceEmpty(String referenceFieldUid) {
		Assert.notNull(referenceFieldUid, REFERENCE_FIELD_UID);
		final EntityField referenceField = getEntityField(referenceFieldUid);
		final SubFormField subFormField = getForm().getSubFormField(referenceField);
		ValueObject referenceObject;
		if (subFormField != null) {
			final NestedEntity nested = subFormField.getSubForm().getNestedEntity();
			referenceObject = getForm().getSubFormByNestedEntityId(nested.getId()).getSelectedObject();
			if (referenceObject == null) {
				return true;
			}
		}
		else {
			referenceObject = getObject();
		}
		return valueObjectService().isEmpty(referenceObject, referenceField);
	}
	
	public ListModel<ValueObject> getReferenceListModel(String referenceFieldUid) {
		Assert.notNull(referenceFieldUid, REFERENCE_FIELD_UID);
		
		final MultiKey key = MultiKey.valueOf(0L, referenceFieldUid);
		if (listModelMap.containsKey(key)) {
			return listModelMap.get(key);
		}
		final EntityField referenceField = getEntityField(referenceFieldUid);
		final FormFieldExtra fieldExtra = getForm().getFieldExtra(referenceField);
		final ListModel<ValueObject> model = createReferenceListModel(referenceField, fieldExtra != null ? fieldExtra.getFilter() : null);
		listModelMap.put(key, model);
		return model;
	}
	
	public ListModel<ValueObject> getNestedReferenceListModel(String nestedEntityUid, String referenceFieldUid) {
		Assert.notNull(nestedEntityUid, "nestedEntityUid");
		Assert.notNull(referenceFieldUid, REFERENCE_FIELD_UID);
		
		final MultiKey key = MultiKey.valueOf(nestedEntityUid, referenceFieldUid);
		if (listModelMap.containsKey(key)) {
			return listModelMap.get(key);
		}
		final SubForm subForm = getSubForm(nestedEntityUid);
		final EntityField referenceField = subForm.getNestedEntity().getNestedEntity().getFieldByUid(referenceFieldUid);
		checkReferenceField(referenceField, referenceFieldUid);
		
		final SubFormField subFormField = subForm.getFieldByEntityFieldUid(referenceField.getUid());
		final ListModel<ValueObject> model = createReferenceListModel(referenceField, subFormField.getFilter());
		listModelMap.put(key, model);
		return model;
	}
	
	public List<ValueObject> getReferenceValues(String referenceFieldUid) {
		Assert.notNull(referenceFieldUid, REFERENCE_FIELD_UID);
		
		final EntityField referenceField = getForm().getEntity().getFieldByUid(referenceFieldUid);
		checkReferenceField(referenceField, referenceFieldUid);
		
		if (isFieldReadonly(referenceFieldUid)) {
			return Collections.singletonList((ValueObject) valueObjectService().getValue(getObject(), referenceField));
		}
		final FormFieldExtra fieldExtra = getForm().getFieldExtra(referenceField);
		return getReferenceValues(referenceField, fieldExtra != null ? fieldExtra.getFilter() : null);
	}
	
	public List<ValueObject> getNestedReferenceValues(String nestedEntityUid, String referenceFieldUid) {
		Assert.notNull(nestedEntityUid, "nestedEntityUid");
		Assert.notNull(referenceFieldUid, REFERENCE_FIELD_UID); 
		
		final SubForm subForm = getSubForm(nestedEntityUid);
		final EntityField referenceField = subForm.getNestedEntity().getNestedEntity().getFieldByUid(referenceFieldUid);
		checkReferenceField(referenceField, referenceFieldUid);
		if (isFieldReadonly(referenceFieldUid)) {
			return Collections.singletonList((ValueObject) valueObjectService().getValue(getObject(), referenceField));
		}
		final SubFormField subFormField = subForm.getFieldByEntityFieldUid(referenceField.getUid());
		return getReferenceValues(referenceField, subFormField.getFilter());
	}
	
	@Init
	@Override
	public void init(@ExecutionArgParam(C.PARAM) FormParameter param) {
		super.init(param);
		
		if (param.object != null) {
			setObject(param.object);
		}
		else if (param.objectId != null) {
			setObject(valueObjectService().getObject(getForm().getEntity(), param.objectId));
		}
		else {
			newObject();
		}
		if (getForm().getEntity().hasStatus()) {
			setStatus(getObject().getEntityStatus());
		}
		initFileObjects();
		resetRelationForms();
	}
	
	@Command
	public void addRelation(@BindingParam("relationId") String relationUid) {
		final EntityRelation relation = getForm().getEntity().getRelationByUid(relationUid);
		showDialog("/form/selectrelation.zul", new SelectRelationParameter(this, relation));
	}
	
	@Command
	@NotifyChange("getRelationForm")
	public void removeRelation(@BindingParam("relationId") String relationUid) {
		final EntityRelation relation = getForm().getEntity().getRelationByUid(relationUid);
		removeRelationObject(relation);
	}
	
	@Command
	public void showDetail(@BindingParam("nestedId") String nestedUid) {
		final NestedEntity nested = getForm().getEntity().getNestedByUid(nestedUid);
		final SubForm subForm = getForm().getSubFormByNestedEntityId(nested.getId());
		final ValueObject valueObject = subForm.getSelectedObject();
		final List<Form> forms = formService().findForms(nested.getNestedEntity());
		if (!forms.isEmpty()) {
			openTab(forms.get(0), valueObject);
		}
	}
	
	@Command
	public void showReference(@BindingParam("fieldId") String fieldUid) {
		Form detailForm;
		ValueObject mainObject;
		final EntityField entityField = getEntityField(fieldUid);
		final SubFormField subFormField = getForm().getSubFormField(entityField);
		if (subFormField != null) {
			final NestedEntity nested = subFormField.getSubForm().getNestedEntity();
			detailForm = subFormField.getDetailForm();
			mainObject = getForm().getSubFormByNestedEntityId(nested.getId()).getSelectedObject();
		}
		else {
			final FormFieldExtra fieldExtra = getForm().getFieldExtra(entityField);
			Assert.stateAvailable(fieldExtra, "fieldExtra");
			detailForm = fieldExtra.getDetailForm();
			mainObject = getObject();
		}
		final ValueObject referenceObject = (ValueObject) valueObjectService().getValue(mainObject, entityField);
		openTab(detailForm, referenceObject);
	}
	
	@Command
	public void editImage(@BindingParam("fieldId") String fieldUid,
			  			  @BindingParam("nestedObject") ValueObject nestedObject) {
		if (!isFieldReadonly(fieldUid)) {
			final EntityField entityField = getEntityField(fieldUid);
			final ValueObject valueObject = nestedObject != null ? nestedObject : getObject();
			showDialog("/form/editimage.zul", new EditImageParameter(this, valueObject, entityField));
		}
	}
	
	@Command
	public void changeStatus(@BindingParam(C.ACTION) FormAction action,
							 @BindingParam(C.ELEM) Component component) {
		confirm("question.status", component, action, getStatus().getNumberAndName());
	}
	
	@Command
	@NotifyChange("isReferenceEmpty")
	public void objectChanged(@BindingParam("fieldId") String fieldUid,
							  @BindingParam(C.ELEM) Component component) {
		closeComponent(component);
		boolean notifyChange = false;
		final EntityField entityField = getEntityField(fieldUid);
		final FormFieldExtra fieldExtra = getForm().getFieldExtra(entityField);
		if (fieldExtra != null && fieldExtra.getTransformer() != null) {
			valueObjectService().transform(fieldExtra.getTransformer(),
										   getObject(),
										   entityField);
			notifyChange = true;
		}
		if (valueObjectService().notifyChange(getObject())) {
			notifyChange = true;
		}
		if (notifyChange) {
			notifyObjectChange("*");
		}
		flagDirty();
	}
	
	@Command
	@NotifyChange("isReferenceEmpty")
	public void nestedChanged(@BindingParam(C.NESTED) ValueObject valueObject,
							  @BindingParam("fieldId") String fieldUid,
							  @BindingParam(C.ELEM) Component component) {
		closeComponent(component);
		final SubFormField subFormField = getSubFormByEntityId(valueObject.getEntityId())
											.getFieldByEntityFieldUid(fieldUid);
		Assert.state(subFormField != null, "subFormField not available " + fieldUid);
		boolean notifyChange = false;
		if (subFormField.getTransformer() != null) {
			valueObjectService().transform(subFormField.getTransformer(), 
										   valueObject, 
										   subFormField.getEntityField());
			notifyChange = true;
		}
		if (valueObjectService().notifyChange(valueObject)) {
			notifyChange = true;
		}
		if (notifyChange) {
			notifyObjectChange(valueObject, "*");
		}
		flagDirty();
	}
	
	@Command
	public void callAction(@BindingParam(C.ACTION) FormAction action,
						   @BindingParam(C.ELEM) Component component) {
		
		switch (action.getType()) {
			case OVERVIEW:
				showListForm();
				break;
				
			case SEARCH:
				showSearchForm();
				break;
			
			case REFRESH:
				if (isDirty()) {
					confirm("question.dirty", component, action);
				}
				else {
					revision = null;
					refreshObject();
				}
				break;
				
			case NEWOBJECT:
				if (isDirty()) {
					confirm("question.dirty", component, action);
				}
				else {
					newObject();
				}
				break;
				
			case TRANSFORM:
				transformObject();
				break;
				
			case PRINT:
				printObject();
				break;
				
			case DELETE:
				confirm("question.delete", component, action);
				break;
				
			case SAVE:
				save(component);
				break;
				
			case CUSTOM:
				callCustomAction(component, action);
				break;
			
			default:
				throw new UnsupportedOperationException(action.getType().name());
		}
	}
	
	@Command
	@NotifyChange(C.OBJECT)
	public void callSubFormAction(@BindingParam("nestedId") String nestedUid,
								  @BindingParam(C.ACTION) SubFormAction action,
								  @BindingParam(C.ELEM) Component component) {
		callSubFormAction(component, getSubForm(nestedUid), action);
		flagDirty();
	}
	
	@Command
	public void selectRevision() {
		// current version
		if (revisions.indexOf(revision) == revisions.size() - 1) {
			setObject(valueObjectService().getObject(getForm().getEntity(), getObject().getId()));
			revision = null;
		}
		else {
			setObject(revisionService.getRevisionObject(getForm().getEntity(), getObject().getId(), revision));
		}
		if (hasStatus()) {
			setStatus(getObject().getEntityStatus());
		}
		initFileObjects();
		reset();
	}
	
	@Command
	@NotifyChange({"getSubForm", "isReferenceEmpty"})
	public void selectSubFormObject() {
		// do nothing, just notify
	}
	
	@Command
	@NotifyChange("getRelationForm")
	public void selectRelationFormObject() {
		// do nothing, just notify
	}
	
	@Override
	protected String getLayoutPath() {
		return "/detail";
	}
	
	@Override
	protected void confirmed(boolean confirmed, Component component, Object confirmParam) {
		final FormAction action = (FormAction) confirmParam;
		switch (action.getType()) {
			case DELETE:
				if (confirmed) {
					try {
						deleteObject();
						showListForm();
					}
					catch (ValidationException vex) {
						showValidationErrors(component, "form.action.deletefail", vex.getErrors());
					}
				}
				break;
			
			case STATUS:
				if (confirmed) {
					try {
						valueObjectService().changeStatus(getObject(), getStatus());
					}
					catch (ValidationException vex) {
						showValidationErrors(component, "form.action.statusfail", vex.getErrors());
					}
				}
				setStatus(getObject().getEntityStatus());
				revisions = null;
				notifyChange(C.STATUS, "availableStatusList", "transformers", "revisions",
							 "isFieldReadonly", "isFieldVisible", "getReferenceValues");
				break;
				
			default:
				resetDirty();
				callAction(action, component);
		}
	}
	
	void notifyPropertyChange(ValueObject valueObject, EntityField entityField) {
		notifyObjectChange(valueObject, entityField.getInternalName());
		flagDirty();
	}
	
	private EntityField getEntityField(String fieldUid) {
		final EntityField entityField = getForm().getEntity().findFieldByUid(fieldUid);
		Assert.state(entityField != null, "entityField not available: " + fieldUid);
		return entityField;
	}
	
	private void newObject() {
		setObject(valueObjectService().createInstance(getForm().getEntity()));
		initFileObjects();
		revision = null;
		reset();
		flagDirty(); // new object is always dirty
	}
	
	private void refreshObject() {
		valueObjectService().reloadObject(getObject());
		initFileObjects();
		revision = null;
		reset();
	}
	
	private void save(Component component) {
		try {
			final List<FileObject> deletedFileObjects = new ArrayList<>(fileObjects);
			// removes all file objects that still exist, only deleted ones remain
			deletedFileObjects.removeAll(valueObjectService().getFileObjects(getObject()));
			valueObjectService().saveObject(getObject(), deletedFileObjects);
			initFileObjects();
			revision = null;
			reset();
		}
		catch (ValidationException vex) {
			showValidationErrors(component, "form.action.savefail", vex.getErrors());
		}
		catch (OptimisticLockException olex) {
			showError(component, "form.action.stalefail");
		}
	}
	
	private void initFileObjects() {
		fileObjects = valueObjectService().getFileObjects(getObject());
		valueObjectService().preallocateFileObjects(getObject());
	}
	
	private void reset() {
		revisions = null;
		resetDirty();
		resetSubForms();
		resetRelationForms();
		notifyChangeAll();
	}
	
}
