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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.seed.C;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.filter.CriterionOperator;
import org.seed.core.entity.value.AbstractValueObject;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.form.FormActionType;
import org.seed.core.form.SubForm;
import org.seed.core.form.SubFormAction;
import org.seed.core.util.Assert;
import org.seed.core.util.MultiKey;
import org.seed.ui.FormParameter;
import org.seed.ui.SearchParameter;

import org.springframework.util.StringUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Menuitem;

public class SearchFormViewModel extends AbstractFormViewModel {
	
	private final Map<MultiKey, ListModel<ValueObject>> listModelMap = new ConcurrentHashMap<>();
	
	private Map<Long, Map<String, CriterionOperator>> fieldOperatorsMap;
	
	@Init
	@Override
	public void init(@ExecutionArgParam(C.PARAM) FormParameter param) {
		super.init(param);
		
		final SearchParameter searchParam = getTab().getSearchParameter();
		if (searchParam != null) {
			setObject(searchParam.searchObject);
			fieldOperatorsMap = searchParam.mapOperators;
		}
		else {
			initNewSearch();
		}
	}
	
	public boolean isCriterionChecked(String type, String fieldUid, String nestedEntityUid) {
		Assert.notNull(type, C.TYPE);
		Assert.notNull(fieldUid, FIELD_UID);
		
		final ValueObject object = StringUtils.hasText(nestedEntityUid)
									? getSubForm(nestedEntityUid).getSelectedObject()
									: getObject();
		if (object == null) {
			return false;
		}
		CriterionOperator operator = getObjectCriteriaMap(object).get(fieldUid);
		if (operator == null) {
			operator = CriterionOperator.EQUAL;
		}
		return operator == CriterionOperator.valueOf(type);							
	}
	
	public boolean isFieldVisible(String fieldUid) {
		Assert.notNull(fieldUid, FIELD_UID);
		
		final EntityField field = getForm().getEntity().getFieldByUid(fieldUid);
		return getForm().isFieldVisible(field, null, getUser());
	}
	
	public ListModel<ValueObject> getReferenceListModel(String referenceFieldUid) {
		Assert.notNull(referenceFieldUid, REFERENCE_FIELD_UID);
		
		final MultiKey key = MultiKey.valueOf(0L, referenceFieldUid);
		if (listModelMap.containsKey(key)) {
			return listModelMap.get(key);
		}
		final EntityField referenceField = getForm().getEntity().getFieldByUid(referenceFieldUid);
		checkReferenceField(referenceField, referenceFieldUid);
		
		final ListModel<ValueObject> model = createReferenceListModel(referenceField, null);
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
		
		final ListModel<ValueObject> model = createReferenceListModel(referenceField, null);
		listModelMap.put(key, model);
		return model;
	}
	
	public List<ValueObject> getReferenceValues(String referenceFieldUid) {
		Assert.notNull(referenceFieldUid, REFERENCE_FIELD_UID);
		
		final EntityField referenceField = getForm().getEntity().getFieldByUid(referenceFieldUid);
		checkReferenceField(referenceField, referenceFieldUid);
		return getReferenceValues(referenceField, null);
	}
	
	public List<ValueObject> getNestedReferenceValues(String nestedEntityUid, String referenceFieldUid) {
		Assert.notNull(nestedEntityUid, "nestedEntityUid");
		Assert.notNull(referenceFieldUid, REFERENCE_FIELD_UID);
		
		final SubForm subForm = getSubForm(nestedEntityUid);
		final EntityField referenceField = subForm.getNestedEntity().getNestedEntity().getFieldByUid(referenceFieldUid);
		return getReferenceValues(referenceField, null);
	}
	
	@Override
	public List<SubFormAction> getSubFormActions(String nestedEntityUid) {
		final List<SubFormAction> actions = new ArrayList<>();
		// filter custom actions
		for (SubFormAction action : super.getSubFormActions(nestedEntityUid)) {
			if (!action.isCustom()) {
				actions.add(action);
			}
		}
		return actions;
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
	@NotifyChange({"getSubForm", "isCriterionChecked"})
	public void selectSubFormObject() {
		// do nothing, just notify
	}
	
	@Command
	@NotifyChange("getRelationForm")
	public void selectRelationFormObject() {
		// do nothing, just notify
	}
	
	@Command
	public void objectChanged(@BindingParam("fieldId") String fieldUid,
							  @BindingParam(C.ELEM) Component component) {
		closeComponent(component);
	}
	
	@Command
	public void nestedChanged(@BindingParam(C.NESTED) ValueObject valueObject,
							  @BindingParam("fieldId") String fieldUid,
							  @BindingParam(C.ELEM) Component component) {
		closeComponent(component);
	}
	
	@Command
	@NotifyChange("isCriterionChecked")
	public void checkCriterion(@BindingParam(C.ELEM) Menuitem elem,
							   @BindingParam(C.TYPE) String type,
							   @BindingParam("fieldId") String fieldUid,
							   @BindingParam("nestedEntityId") String nestedEntityUid) {
		Assert.notNull(elem, C.ELEM);
		Assert.notNull(type, C.TYPE);
		Assert.notNull(fieldUid, FIELD_UID);
		
		final ValueObject object = nestedEntityUid != null
									? getSubForm(nestedEntityUid).getSelectedObject()
									: getObject();
		Assert.stateAvailable(object, C.OBJECT);
		
		if (elem.isChecked()) {
			getObjectCriteriaMap(object).put(fieldUid, CriterionOperator.valueOf(type));
		}
		else {
			getObjectCriteriaMap(object).remove(fieldUid);
		}
	}
	
	@Command
	@NotifyChange(C.OBJECT)
	public void callSubFormAction(@BindingParam("nestedId") String nestedUid,
								  @BindingParam(C.ACTION) SubFormAction action,
								  @BindingParam(C.ELEM) Component component) {
		
		final SubForm subForm = getSubForm(nestedUid);
		if (action.getType() == FormActionType.DELETE) {
			fieldOperatorsMap.remove(((AbstractValueObject) subForm.getSelectedObject()).getTmpId());
		}
		callSubFormAction(component, subForm, action);
	}
	
	@Command
	@NotifyChange("*")
	public void clearSearch() {
		getTab().clearSearch();
		initNewSearch();
	}
	
	@Command
	public void search() {
		getTab().setSearchParameter(new SearchParameter(getObject(), fieldOperatorsMap));
		showListForm();
	}
	
	@Override
	protected String getLayoutPath() {
		return "/search";
	}
	
	private void initNewSearch() {
		setObject(valueObjectService().createInstance(getForm().getEntity()));
		fieldOperatorsMap = new ConcurrentHashMap<>();
		resetRelationForms();
	}
	
	private Map<String, CriterionOperator> getObjectCriteriaMap(ValueObject object) {
		Long objectId = ((AbstractValueObject) object).getTmpId();
		if (objectId == null) {
			objectId = 0L; // main object has no tmpId and gets id 0
		}
		fieldOperatorsMap.computeIfAbsent(objectId, o -> new ConcurrentHashMap<>());
		return fieldOperatorsMap.get(objectId);
	}
	
}
