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
package org.seed.ui.zk.vm.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.seed.core.data.FieldAccess;
import org.seed.core.data.FieldType;
import org.seed.core.data.SystemObject;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityAccess;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityPermission;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.EntityStatusTransition;
import org.seed.core.entity.EntityStatusTransitionFunction;
import org.seed.core.entity.EntityStatusTransitionPermission;
import org.seed.core.entity.EntityFieldConstraint;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.form.FormOptions;
import org.seed.core.form.navigation.Menu;
import org.seed.core.form.navigation.MenuService;
import org.seed.core.user.Authorisation;
import org.seed.core.util.MiscUtils;
import org.seed.ui.ListFilter;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

public class AdminEntityViewModel extends AbstractAdminViewModel<Entity> {
	
	private static final String FIELDS = "fields";
	private static final String FIELDGROUPS = "fieldGroups";
	private static final String FIELDCONSTRAINTS = "fieldConstraints";
	private static final String FUNCTIONS = "memberFunctions";
	private static final String CALLBACKS = "callbackFunctions";
	private static final String NESTEDS = "nesteds";
	private static final String STATUS = "status";
	private static final String STATUSTRANSITIONS = "statusTransition";
	private static final String TRANSITIONFUNCTIONS = "transitionFunctions";
	private static final String TRANSITIONPERMISSIONS = "transitionPermissions";
	private static final String PERMISSIONS = "permissions";
	
	private final List<Long> mandatoryFieldIds = new ArrayList<>();
	
	@Wire("#newEntityWin")
	private Window window;
	
	@WireVariable(value="entityServiceImpl")
	private EntityService entityService;
	
	@WireVariable(value="menuServiceImpl")
	private MenuService menuService;
	
	@WireVariable(value="valueObjectServiceImpl")
	private ValueObjectService valueObjectService;
	
	private EntityField field;
	
	private EntityFieldGroup fieldGroup;
	
	private EntityFunction function;
	
	private EntityFunction callbackFunction;
	
	private EntityStatus entityStatus;
	
	private EntityStatusTransition statusTransition;
	
	private NestedEntity nested;
	
	private EntityPermission permission;
	
	private EntityFieldConstraint fieldConstraint;
	
	private NestedEntity constraintNested;
	
	private EntityStatusTransitionFunction transitionFunction;
	
	private String originalName;
	
	// see selectFieldType()
	private boolean resetUnique;
	private boolean resetMandatory;
	
	public AdminEntityViewModel() {
		super(Authorisation.ADMIN_ENTITY, "entity",
			  "/admin/entity/entitylist.zul", 
			  "/admin/entity/entity.zul",
			  "/admin/entity/newentity.zul");
	}

	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam("param") Object object) {
		super.init(object, view);
	}
	
	@Override
	protected void initObject(Entity entity) {
		originalName = entity.getInternalName();
		if (!entity.isNew() && entity.hasFields()) {
			for (EntityField entityField : entity.getFields()) {
				if (entityField.isMandatory()) {
					mandatoryFieldIds.add(entityField.getId());
				}
			}
		}
	}
	
	@Override
	protected void initFilters() {
		final ListFilter<Entity> filterGenericName = getFilter(FILTERGROUP_LIST, "genericname");
		filterGenericName.setValueFunction(o -> o.getGenericEntity() != null 
												? o.getGenericEntity().getName() 
												: null);
		for (Entity entity : getObjectList()) {
			if (entity.getGenericEntity() != null) {
				filterGenericName.addValue(entity.getGenericEntity().getName());
			}
		}
		getFilter(FILTERGROUP_LIST, "generic").setBooleanFilter(true);
	}
	
	public boolean existGenericEntities() {
		return entityService.existGenericEntities();
	}
	
	public boolean existValueObjects() {
		return !getObject().isNew() && !getObject().isGeneric() &&
				valueObjectService.existObjects(getObject());
	}
	
	public List<Menu> getMenus() {
		return menuService.getTopLevelMenus();
	}
	
	public Object getReferenceValue(ValueObject valueObject) {
		return valueObject != null 
				? valueObjectService.getIdentifier(valueObject) 
				: null;
	}
	
	public List<ValueObject> getReferenceValues(EntityField referenceField) {
		return referenceField != null && referenceField.getReferenceEntity() != null
				? valueObjectService.getAllObjects(referenceField.getReferenceEntity())
				: null;
	}
	
	public List<EntityField> getConstraintFields(NestedEntity nested) {
		return nested != null 
				? nested.getFields(true) 
				: getObject().getFields();
	}
	
	public boolean isAlreadyMandatory(EntityField field) {
		return field != null && mandatoryFieldIds.contains(field.getId());
	}
	
	public String getMaxFieldLength() {
		return String.valueOf(getLimit("entity.stringfield.length"));
	}
	
	@Override
	protected EntityService getObjectService() {
		return entityService;
	}
	
	@Override
	protected void resetProperties() {
		field = null;
		fieldGroup = null;
		entityStatus = null;
		nested = null;
		function = null;
		callbackFunction = null;
		permission = null;
		fieldConstraint = null;
		constraintNested = null;
		statusTransition = null;
		transitionFunction = null;
		originalName = null;
		resetUnique = false;
		resetMandatory = false;
		mandatoryFieldIds.clear();
	}
	
	public EntityField getField() {
		return field;
	}

	public void setField(EntityField field) {
		this.field = field;
	}

	public EntityFieldGroup getFieldGroup() {
		return fieldGroup;
	}

	public void setFieldGroup(EntityFieldGroup fieldGroup) {
		this.fieldGroup = fieldGroup;
	}

	public EntityFunction getFunction() {
		return function;
	}

	public void setFunction(EntityFunction function) {
		this.function = function;
	}

	public EntityFunction getCallbackFunction() {
		return callbackFunction;
	}

	public void setCallbackFunction(EntityFunction callbackFunction) {
		this.callbackFunction = callbackFunction;
	}

	public EntityStatus getStatus() {
		return entityStatus;
	}

	public void setStatus(EntityStatus status) {
		this.entityStatus = status;
	}

	public EntityStatusTransition getStatusTransition() {
		return statusTransition;
	}

	public void setStatusTransition(EntityStatusTransition statusTransition) {
		this.statusTransition = statusTransition;
	}

	public NestedEntity getNested() {
		return nested;
	}

	public void setNested(NestedEntity nested) {
		this.nested = nested;
	}

	public EntityPermission getPermission() {
		return permission;
	}

	public void setPermission(EntityPermission permission) {
		this.permission = permission;
	}

	public EntityFieldConstraint getFieldConstraint() {
		return fieldConstraint;
	}

	public void setFieldConstraint(EntityFieldConstraint fieldConstraint) {
		this.fieldConstraint = fieldConstraint;
	}

	public NestedEntity getConstraintNested() {
		return constraintNested;
	}

	public void setConstraintNested(NestedEntity constraintNested) {
		this.constraintNested = constraintNested;
	}

	public EntityStatusTransitionFunction getTransitionFunction() {
		return transitionFunction;
	}

	public void setTransitionFunction(EntityStatusTransitionFunction transitionFunction) {
		this.transitionFunction = transitionFunction;
	}
	
	public String getFieldName(EntityField field) {
		if (field != null) {
			if (field.getEntity().equals(getObject())) {
				return field.getName();
			}
			return field.getName() + " (" + field.getEntity().getName() + ')';
		}
		return null;
	}

	public FieldType[] getFieldTypes() {
		return entityService.getAvailableFieldTypes(getObject(), field, existValueObjects());
	}
	
	public FieldAccess[] getFieldAccesses() {
		return FieldAccess.values();
	}
	
	public EntityAccess[] getEntityAccesses() {
		return EntityAccess.values();
	}
	
	public List<Entity> getGenericEntities() {
		return entityService.findGenericEntities();
	}
	
	// reference entity can only be selected if field id new
	public List<Entity> getReferenceEntities() {
		if (field != null) {
			return field.isNew() 
					? entityService.findNonGenericEntities()
					: Collections.singletonList(field.getReferenceEntity());
		}
		return Collections.emptyList();
	}
	
	public List<Entity> getAvailableNesteds() {
		return entityService.getAvailableNestedEntities(getObject());
	}
	
	@Override
	public Entity createObject() {
		return entityService.createInstance(new FormOptions());
	}
	
	@Command
	public void createEntity(@BindingParam("elem") Component elem) {
		// set module in options
		final FormOptions formOptions = getObject().getOptions();
		if (formOptions != null) {
			formOptions.setModule(getObject().getModule());
		}
		cmdInitObject(elem, window);
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void newEntity() {
		cmdNewObjectDialog();
	}
	
	@Command
	public void editEntity() {
		cmdEditObject();
	}
	
	@Command
	public void refreshEntity(@BindingParam("elem") Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteEntity(@BindingParam("elem") Component component) {
		cmdDeleteObject(component);
	}
	
	@Command
	public void saveEntity(@BindingParam("elem") Component component) {
		try {
			final boolean isCreate = getObject().isNew();
			final String currentName = getObject().getName();
			// name has changed but validateMandatoryDefaultValues()
			// expects originalName to work properly
			if (!isCreate && currentName != null && 
				!currentName.equals(originalName)) {
				getObject().setName(originalName);
			}
			if (currentName != null) {
				validateMandatoryDefaultValues();
			}
			// set current name again
			getObject().setName(currentName);
			
			adjustLists(getObject().getPermissions(), getListManagerList(PERMISSIONS, LIST_SELECTED));
			
			if (cmdSaveObject(component)) {
				// old session is invalid after configuration update
				refreshObject();
				if (isCreate) {
					refreshMenu();
				}
			}
			
		} catch (ValidationException vex) {
			showValidationErrors(component, "admin.entity.savefail", vex.getErrors());
		}
	}
	
	@Command
	@NotifyChange({"field", "referenceEntities", "fieldTypes"})
	public void newField() {
		field = entityService.createField(getObject());
		resetUnique = false;
		resetMandatory = false;
		notifyObjectChange(FIELDS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("field")
	public void removeField(@BindingParam("elem") Component component) {
		try {
			entityService.removeField(getObject(), field);
			field = null;
			resetUnique = false;
			resetMandatory = false;
			notifyObjectChange(FIELDS);
			flagDirty();
		} 
		catch (ValidationException vex) {
			showValidationErrors(component, "admin.entity.removefieldfail", vex.getErrors());
		}
	}
	
	@Command
	@NotifyChange("fieldGroup")
	public void newFieldGroup() {
		fieldGroup = entityService.createFieldGroup(getObject());
		notifyObjectChange(FIELDGROUPS);
		flagDirty();
	}
	
	@Command
	@NotifyChange({"field", "fieldGroup"})
	public void removeFieldGroup(@BindingParam("elem") Component component) {
		try {
			entityService.removeFieldGroup(getObject(), fieldGroup);
			fieldGroup = null;
			notifyObjectChange(FIELDS, FIELDGROUPS);
			flagDirty();
		}
		catch (ValidationException vex) {
			showValidationErrors(component, "admin.entity.removefieldgroupfail", vex.getErrors());
		}
	}
	
	@Command
	@NotifyChange({"fieldConstraint", "constraintNested"})
	public void newConstraint() {
		fieldConstraint = entityService.createFieldConstraint(getObject());
		constraintNested = null;
		notifyObjectChange(FIELDCONSTRAINTS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("fieldConstraint")
	public void removeConstraint(@BindingParam("elem") Component component) {
		getObject().removeFieldConstraint(fieldConstraint);
		fieldConstraint = null;
		notifyObjectChange(FIELDCONSTRAINTS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("function")
	public void newFunction() {
		function = entityService.createFunction(getObject(), false);
		notifyObjectChange(FUNCTIONS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("callbackFunction")
	public void newCallbackFunction() {
		callbackFunction = entityService.createFunction(getObject(), true);
		notifyObjectChange(CALLBACKS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("function")
	public void removeFunction(@BindingParam("elem") Component component) {
		try {
			entityService.removeFunction(getObject(), function);
			function = null;
			notifyObjectChange(FUNCTIONS);
			flagDirty();
		}
		catch (ValidationException vex) {
			showValidationErrors(component, "admin.entity.removefunctionfail", vex.getErrors());
		}
	}
	
	@Command
	@NotifyChange("callbackFunction")
	public void removeCallbackFunction(@BindingParam("elem") Component component) {
		try {
			entityService.removeFunction(getObject(), callbackFunction);
			callbackFunction = null;
			notifyObjectChange(CALLBACKS);
			flagDirty();
		}
		catch (ValidationException vex) {
			showValidationErrors(component, "admin.entity.removefunctionfail", vex.getErrors());
		}
	}
	
	@Command
	@NotifyChange({"nested", "availableNesteds"})
	public void newNested() {
		nested = entityService.createNested(getObject());
		notifyObjectChange(NESTEDS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("nested")
	public void removeNested(@BindingParam("elem") Component component) {
		try {
			entityService.removeNested(getObject(), nested);
			nested = null;
			notifyObjectChange(NESTEDS);
			flagDirty();
		}
		catch (ValidationException vex) {
			showValidationErrors(component, "admin.entity.removenestedfail", vex.getErrors());
		}
	}
	
	@Command
	@NotifyChange("status")
	public void newStatus() {
		entityStatus = entityService.createStatus(getObject());
		notifyObjectChange("statusList");
		flagDirty();
	}
	
	@Command
	@NotifyChange("status")
	public void removeStatus(@BindingParam("elem") Component component) {
		try {
			entityService.removeStatus(getObject(), entityStatus);
			entityStatus = null;
			notifyObjectChange("statusList");
			flagDirty();
		}
		catch (ValidationException vex) {
			showValidationErrors(component, "admin.entity.removestatusfail", vex.getErrors());
		}
	}
	
	@Command
	@NotifyChange("statusTransition")
	public void newStatusTransition() {
		statusTransition = entityService.createStatusTransition(getObject());
		notifyObjectChange("statusTransitions");
		flagDirty();
	}
	
	@Command
	@NotifyChange("statusTransition")
	public void removeStatusTransition(@BindingParam("elem") Component component) {
		try {
			entityService.removeStatusTransition(getObject(), statusTransition);
			statusTransition = null;
			notifyObjectChange("statusTransitions");
			flagDirty();
		}
		catch (ValidationException vex) {
			showValidationErrors(component, "admin.entity.removestatustransitionfail", vex.getErrors());
		}
	}
	
	@Command
	public void editFunction() {
		showCodeDialog(new CodeDialogParameter(this, function));
	}
	
	@Command
	public void editCallbackFunction() {
		showCodeDialog(new CodeDialogParameter(this, callbackFunction));
	}
	
	@Command
	public void selectFieldTab() {
		notifyObjectChange("allFieldGroups");
	}
	
	@Command
	@NotifyChange("object")
	public void selectConstraintTab() {
		// do nothing, just notify
	}
	
	@Command
	@NotifyChange({"referenceEntities", "fieldTypes"})
	public void selectField() {
		resetUnique = false;
		resetMandatory = false;
	}
	
	@Command
	public void selectConstraintField() {
		if (fieldConstraint.getFieldGroup() != null) {
			fieldConstraint.setFieldGroup(null);
			notifyObjectChange(fieldConstraint, "fieldGroup");
		}
		flagDirty();
	}
	
	@Command
	public void selectConstraintFieldGroup() {
		if (fieldConstraint.getField() != null) {
			fieldConstraint.setField(null);
			notifyObjectChange(fieldConstraint, "field");
		}
		flagDirty();
	}
	
	@Command
	@NotifyChange("field")
	public void selectFieldType() {
		if (field.getType() != null && 
			field.getType().isAutonum()) {
			if (!field.isUnique()) {
				resetUnique = true;
				field.setUnique(true);
			}
			if (!field.isMandatory()) {
				resetMandatory = true;
				field.setMandatory(true);
			}
		}
		else {
			if (resetUnique) {
				field.setUnique(false);
				resetUnique = false;
			}
			if (resetMandatory) {
				field.setMandatory(false);
				resetMandatory = false;
			}
		}
		flagDirty();
	}
	
	@Command
	@NotifyChange(LISTMANAGER_LIST)
	public void selectStatusTransition() {
		removeListManager(TRANSITIONFUNCTIONS);
		removeListManager(TRANSITIONPERMISSIONS);
	}
	
	@Command
	@SmartNotifyChange("statusTransition")
	public void selectTransitionDependent() {
		if (statusTransition == null) {
			setDefaultTransition();
		}
	}
	
	@Command
	@NotifyChange("field")
	public void selectReferencedEntity() {
		if (field.getName() == null) {
			field.setName(field.getReferenceEntity().getName());
		}
		flagDirty();
	}
	
	@Command
	@NotifyChange("nested")
	public void selectNestedEntity() {
		if (nested.getName() == null) {
			nested.setName(nested.getNestedEntity().getName());
		}
		if (nested.getReferenceField() == null && 
			nested.getNestedEntity().getReferenceFields(getObject()).size() == 1) {
			nested.setReferenceField(nested.getNestedEntity().getReferenceFields(getObject()).get(0));
		}
		else {
			nested.setReferenceField(null);
		}
		flagDirty();
	}
	
	@Command
	@NotifyChange("constraintNested")
	public void selectFieldConstraint() {
		if (fieldConstraint.getField() != null && getObject().hasNesteds()) {
			for (NestedEntity nestedEntity : getObject().getNesteds()) {
				if (nestedEntity.getNestedEntity().containsField(fieldConstraint.getField())) {
					constraintNested = nestedEntity;
					return;
				}
			}
		}
		constraintNested = null;
	}
	
	@Command
	@NotifyChange("fieldConstraint")
	public void selectConstraintNested() {
		fieldConstraint.setField(null);
		flagDirty();
	}
	
	@Command
	public void swapFields(@BindingParam("base") EntityField base, 
						   @BindingParam("item") EntityField item) {
		swapItems(FIELDS, base, item);
	}
	
	@Command
	public void swapFieldGroups(@BindingParam("base") EntityFieldGroup base, 
								@BindingParam("item") EntityFieldGroup item) {
		swapItems(FIELDGROUPS, base, item);
	}
	
	@Command
	public void swapCallbackFunctions(@BindingParam("base") EntityFunction base,
							  		  @BindingParam("item") EntityFunction item) {
		swapItems(CALLBACKS, base, item);
	}
	
	@Command
	public void swapStatus(@BindingParam("base") EntityStatus base, 
						   @BindingParam("item") EntityStatus item) {
		swapItems(STATUS, base, item);
	}
	
	@Command
	public void swapStatusTransition(@BindingParam("base") EntityStatusTransition base, 
						   			 @BindingParam("item") EntityStatusTransition item) {
		swapItems(STATUSTRANSITIONS, base, item);
	}
	
	@Command
	public void swapConstraints(@BindingParam("base") EntityFieldConstraint base, 
			   					@BindingParam("item") EntityFieldConstraint item) {
		swapItems(FIELDCONSTRAINTS, base, item);
	}
	
	@Command
	public void swapNesteds(@BindingParam("base") NestedEntity base, 
						    @BindingParam("item") NestedEntity item) {
		swapItems(NESTEDS, base, item);
	}
	
	@Command
	@Override
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, notifyObject);
	}
	
	@Command
	@SmartNotifyChange("permission")
	public void dropToPermissionList(@BindingParam("item") EntityPermission item,
									 @BindingParam("list") int listNum) {
		dropToList(PERMISSIONS, listNum, item);
		if (listNum == LIST_AVAILABLE && item == permission) {
			this.permission = null;
		}
	}
	
	@Command
	@SmartNotifyChange("transitionFunction")
	public void dropToStatusFunctionList(@BindingParam("item") EntityStatusTransitionFunction item,
										 @BindingParam("list") int listNum) {
		dropToList(TRANSITIONFUNCTIONS, listNum, item);
		adjustLists(statusTransition.getFunctions(), getListManagerList(TRANSITIONFUNCTIONS, LIST_SELECTED));
		if (listNum == LIST_AVAILABLE && item == transitionFunction) {
			this.transitionFunction = null;
		}
	}
	
	@Command
	public void dropToStatusPermissionList(@BindingParam("item") EntityStatusTransitionPermission item,
									  	   @BindingParam("list") int listNum) {
		dropToList(TRANSITIONPERMISSIONS, listNum, item);
		adjustLists(statusTransition.getPermissions(), getListManagerList(TRANSITIONPERMISSIONS, LIST_SELECTED));
	}
	
	@Command
	@SmartNotifyChange("permission")
	public void insertToPermissionList(@BindingParam("base") EntityPermission base,
									   @BindingParam("item") EntityPermission item,
									   @BindingParam("list") int listNum) {
		insertToList(PERMISSIONS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == permission) {
			this.permission = null;
		}
	}
	
	@Command
	@SmartNotifyChange("transitionFunction")
	public void insertToStatusFunctionList(@BindingParam("base") EntityStatusTransitionFunction base,
			   							   @BindingParam("item") EntityStatusTransitionFunction item,
			   							   @BindingParam("list") int listNum) {
		insertToList(TRANSITIONFUNCTIONS, listNum, base, item);
		adjustLists(statusTransition.getFunctions(), getListManagerList(TRANSITIONFUNCTIONS, LIST_SELECTED));
		if (listNum == LIST_AVAILABLE && item == transitionFunction) {
			this.transitionFunction = null;
		}
	}
	
	@Command
	public void insertToStatusPermissionList(@BindingParam("base") EntityStatusTransitionPermission base,
			   								 @BindingParam("item") EntityStatusTransitionPermission item,
			   								 @BindingParam("list") int listNum) {
		insertToList(TRANSITIONPERMISSIONS, listNum, base, item);
		adjustLists(statusTransition.getPermissions(), getListManagerList(TRANSITIONPERMISSIONS, LIST_SELECTED));
	}
	
	@GlobalCommand
	public void globalRefreshObject(@BindingParam("param") Long objectId) {
		refreshObject(objectId);
	}
	
	@Override
	protected List<SystemObject> getListSorterSource(String key) {
		switch (key) {
			case FIELDS:
				return MiscUtils.cast(getObject().getFields());
			case FIELDGROUPS:
				return MiscUtils.cast(getObject().getFieldGroups());
			case FIELDCONSTRAINTS:
				return MiscUtils.cast(getObject().getFieldConstraints());
			case STATUS:
				return MiscUtils.cast(getObject().getStatusList());
			case STATUSTRANSITIONS:
				return MiscUtils.cast(getObject().getStatusTransitions());
			case NESTEDS:
				return MiscUtils.cast(getObject().getNesteds());
			case CALLBACKS:
				return MiscUtils.cast(getObject().getCallbackFunctions());
			default:
				throw new IllegalStateException("unknown list sorter key: " + key);
		}
	}
	
	@Override
	protected List<SystemObject> getListManagerSource(String key, int listNum) {
		switch (key) {
			case PERMISSIONS:
				return MiscUtils.cast(listNum == LIST_AVAILABLE 
						? entityService.getAvailablePermissions(getObject()) 
						: getObject().getPermissions());
			
			case TRANSITIONFUNCTIONS:
				if (statusTransition != null) {
					return MiscUtils.cast(listNum == LIST_AVAILABLE
							  ? entityService.getAvailableStatusTransitionFunctions(getObject(), statusTransition)
							  : statusTransition.getFunctions());
				}
				break;
			
			case TRANSITIONPERMISSIONS:
				if (statusTransition != null) {
					return MiscUtils.cast(listNum == LIST_AVAILABLE
							  ? entityService.getAvailableStatusTransitionPermissions(statusTransition)
							  : statusTransition.getPermissions());
				}
				break;
						  
			default:
				throw new IllegalStateException("unknown list manager key: " + key);
		}
		return Collections.emptyList();
	}
	
	private void setDefaultTransition() {
		if (getObject().hasStatusTransitions()) {
			statusTransition = getObject().getStatusTransitions().get(0);
		}
	}
	
	private void validateMandatoryDefaultValues() throws ValidationException {
		if (getObject().hasFields() && existValueObjects()) {
			final ValidationErrors errors = new ValidationErrors();
			for (EntityField entityField : getObject().getFields()) {
				if (entityField.isMandatory() && !isAlreadyMandatory(entityField) &&
					!entityField.getType().isAutonum() && !entityField.getType().isBinary() &&
					!entityField.getType().isBoolean() && !entityField.getType().isFile() &&
					!entityField.hasDefaultValue()) {
					errors.addError("val.empty.default", entityField.getName());
				}
			}
			if (!errors.isEmpty()) {
				throw new ValidationException(errors);
			}
		}
	}

}
