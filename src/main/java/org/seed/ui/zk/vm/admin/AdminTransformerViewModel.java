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

import org.seed.C;
import org.seed.core.application.ContentObject;
import org.seed.core.codegen.SourceCode;
import org.seed.core.data.SystemObject;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.transform.NestedTransformer;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerElement;
import org.seed.core.entity.transform.TransformerFunction;
import org.seed.core.entity.transform.TransformerPermission;
import org.seed.core.entity.transform.TransformerService;
import org.seed.core.entity.transform.TransformerStatus;
import org.seed.core.entity.transform.codegen.TransformerFunctionCodeProvider;
import org.seed.core.user.Authorisation;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.ui.ListFilter;

import org.springframework.util.ObjectUtils;
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

public class AdminTransformerViewModel extends AbstractAdminViewModel<Transformer> {
	
	private static final String FUNCTIONS = "functions";
	private static final String PERMISSIONS = "permissions";
	private static final String STATUS = "status";
	
	@Wire("#newTransformerWin")
	private Window window;
	
	@WireVariable(value="transformerServiceImpl")
	private TransformerService transformerService;
	
	@WireVariable(value="transformerFunctionCodeProvider")
	private TransformerFunctionCodeProvider transformerFunctionCodeProvider;
	
	@WireVariable(value="entityServiceImpl")
	private EntityService entityService;
	
	private List<TransformerElement> elements;
	
	private List<NestedTransformer> nesteds;
	
	private TransformerElement element;
	
	private TransformerFunction function;
	
	private NestedTransformer nested;
	
	private TransformerElement nestedElement;
	
	private TransformerPermission permission;
	
	public AdminTransformerViewModel() {
		super(Authorisation.ADMIN_ENTITY, C.TRANSFORMER,
			  "/admin/transform/transformerlist.zul", 
			  "/admin/transform/transformer.zul",
			  "/admin/transform/newtransformer.zul");
	}

	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam(C.PARAM) Object object) {
		super.init(object, view);
	}
	
	@Override
	protected void initObject(Transformer transformer) {
		super.initObject(transformer);
		elements = transformerService.getMainObjectElements(transformer);
		if (elements.isEmpty()) {
			elements = new ArrayList<>();
		}
		nesteds = transformerService.getNestedTransformers(transformer);
	}
	
	@Override
	protected void initFilters() {
		final ListFilter<Transformer> filterSourceEntity = getFilter(FILTERGROUP_LIST, "sourceentity");
		filterSourceEntity.setValueFunction(o -> o.getSourceEntity().getName());
		
		final ListFilter<Transformer> filterTargetEntity = getFilter(FILTERGROUP_LIST, "targetentity");
		filterTargetEntity.setValueFunction(o -> o.getTargetEntity().getName());
		
		for (Transformer transformer : getObjectList()) {
			filterSourceEntity.addValue(transformer.getSourceEntity().getName());
			filterTargetEntity.addValue(transformer.getTargetEntity().getName());
		}
	}
	
	public List<TransformerElement> getElements() {
		return elements;
	}

	public List<NestedTransformer> getNesteds() {
		return nesteds;
	}

	@Override
	protected TransformerService getObjectService() {
		return transformerService;
	}

	public TransformerElement getElement() {
		return element;
	}

	public void setElement(TransformerElement element) {
		this.element = element;
	}
	
	public TransformerFunction getFunction() {
		return function;
	}

	public void setFunction(TransformerFunction function) {
		this.function = function;
	}

	public NestedTransformer getNested() {
		return nested;
	}

	public void setNested(NestedTransformer nested) {
		this.nested = nested;
	}

	public TransformerElement getNestedElement() {
		return nestedElement;
	}

	public void setNestedElement(TransformerElement nestedElement) {
		this.nestedElement = nestedElement;
	}
	
	public TransformerPermission getPermission() {
		return permission;
	}

	public void setPermission(TransformerPermission permission) {
		this.permission = permission;
	}

	public List<Entity> getEntities() {
		return entityService.findNonGenericEntities(currentSession());
	}
	
	public List<EntityField> getAvailableTargetFields() {
		return getAvailableTargetFields(element, false);
	}
	
	public List<EntityField> getAvailableNestedTargetFields() {
		return getAvailableTargetFields(nestedElement, true);
	}
	
	private List<EntityField> getAvailableTargetFields(TransformerElement element, boolean isNested) {
		if (element == null || element.getSourceField() == null) {
			return Collections.emptyList();
		}
		// reset target field if type does not match
		if (element.getTargetField() != null &&
			element.getSourceField().getType() != element.getTargetField().getType()) {
				element.setTargetField(null);
				notifyObjectChange(element, "targetField");
		}
		final Entity targetEntity = isNested 
									? nested.getTargetNested().getNestedEntity() 
									: getObject().getTargetEntity();
		return targetEntity.getAllFieldsByType(element.getSourceField().getType());
	}
	
	@Command
	public void autoMatchFields() {
		if (transformerService.autoMatchFields(getObject(), elements)) {
			notifyChange("elements");
			flagDirty();
		}
	}
	
	@Command
	public void autoMatchNestedFields() {
		if (transformerService.autoMatchFields(nested)) {
			notifyObjectChange(nested, "elements");
			flagDirty();
		}
	}
	
	@Command
	@SmartNotifyChange(C.NESTED)
	public void selectNestedElements() {
		if (nested == null && !ObjectUtils.isEmpty(nesteds)) {
			nested = nesteds.get(0);
		}
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void newTransformer() {
		cmdNewObjectDialog();
	}
	
	@Command
	public void editTransformer() {
		cmdEditObject();
	}
	
	@Command
	public void editFunction() {
		if (function.getContent() == null) {
			function.setContent(transformerFunctionCodeProvider.getFunctionTemplate(function));
		}
		showCodeDialog(new CodeDialogParameter(this, function));
	}
	
	@Command
	public void createTransformer(@BindingParam(C.ELEM) Component elem) {
		cmdInitObject(elem, window);
	}
	
	@Command
	public void cancel() {
		window.detach();
	} 
	
	@Command
	@NotifyChange({C.ELEMENT, "elements"})
	public void newElement() {
		element = new TransformerElement();
		element.setTransformer(getObject());
		elements.add(element);
		flagDirty();
	}
	
	@Command
	@NotifyChange({C.ELEMENT, "elements"})
	public void removeElement() {
		elements.remove(element);
		element = null;
		flagDirty();
	}
	
	@Command
	@NotifyChange({C.NESTED, "nesteds"})
	public void newNested() {
		nested = new NestedTransformer();
		nesteds.add(nested);
		flagDirty();
	}
	
	@Command
	@NotifyChange({C.NESTED, "nesteds"})
	public void removeNested() {
		nesteds.remove(nested);
		nested = null;
		flagDirty();
	}
	
	@Command
	@NotifyChange({C.NESTED, "nestedElement"})
	public void newNestedElement() {
		nestedElement = new TransformerElement();
		nestedElement.setTransformer(getObject());
		nested.addElement(nestedElement);
		flagDirty();
	}
	
	@Command
	@NotifyChange({C.NESTED, "nestedElement"})
	public void removeNestedElement() {
		nested.removeElement(nestedElement);
		nestedElement = null;
		flagDirty();
	}
	
	@Command
	@NotifyChange(C.FUNCTION)
	public void newFunction() {
		function = transformerService.createFunction(getObject());
		notifyObjectChange(FUNCTIONS);
		flagDirty();
	}
	
	@Command
	@NotifyChange(C.FUNCTION)
	public void removeFunction(@BindingParam(C.ELEM) Component component) {
		getObject().removeFunction(function);
		function = null;
		notifyObjectChange(FUNCTIONS);
		flagDirty();
	}
	
	@Command
	public void refreshTransformer(@BindingParam(C.ELEM) Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteTransformer(@BindingParam(C.ELEM) Component component) {
		cmdDeleteObject(component);
	}
	
	@Override
	protected void afterObjectDeleted(Transformer transformer) {
		resetCurrentSession();
	}
	
	@Command
	public void saveTransformer(@BindingParam(C.ELEM) Component component) {
		adjustLists(getObject().getPermissions(), getListManagerList(PERMISSIONS, LIST_SELECTED));
		adjustLists(getObject().getStatus(), getListManagerList(STATUS, LIST_SELECTED));
		transformerService.adjustElements(getObject(), elements, nesteds);
		if (cmdSaveObject(component)) {
			resetCurrentSession();
			refreshObject();
		}
	}
	
	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, null, notifyObject);
	}
	
	@Command
	public void dropToPermissionList(@BindingParam(C.ITEM) TransformerPermission item,
									 @BindingParam(C.LIST) int listNum) {
		dropToList(PERMISSIONS, listNum, item);
	}
	
	@Command
	public void insertToPermissionList(@BindingParam(C.BASE) TransformerPermission base,
									   @BindingParam(C.ITEM) TransformerPermission item,
									   @BindingParam(C.LIST) int listNum) {
		insertToList(PERMISSIONS, listNum, base, item);
	}
	
	@Command
	public void dropToStatusList(@BindingParam(C.ITEM) TransformerStatus item,
								 @BindingParam(C.LIST) int listNum) {
		dropToList(STATUS, listNum, item);
	}
	
	@Command
	public void insertToStatusList(@BindingParam(C.BASE) TransformerStatus base,
								   @BindingParam(C.ITEM) TransformerStatus item,
								   @BindingParam(C.LIST) int listNum) {
		insertToList(STATUS, listNum, base, item);
	}
	
	@Command
	public void swapFunctions(@BindingParam(C.BASE) TransformerFunction base,
							  @BindingParam(C.ITEM) TransformerFunction item) {
		swapItems(FUNCTIONS, base, item);
	}
	
	@GlobalCommand
	public void globalRefreshObject(@BindingParam(C.PARAM) Long objectId) {
		refreshObject(objectId);
	}
	
	@Override
	protected List<SystemObject> getListSorterSource(String key) {
		if (FUNCTIONS.equals(key)) {
			return MiscUtils.castList(getObject().getFunctions());
		}
		else {
			throw new IllegalStateException("unknown list sorter key: " + key);
		}
	}
	
	@Override
	protected List<SystemObject> getListManagerSource(String key, int listNum) {
		switch (key) {
			case PERMISSIONS:
				return MiscUtils.castList(listNum == LIST_AVAILABLE
							? transformerService.getAvailablePermissions(getObject(), currentSession())
							: new ArrayList<>(getObject().getPermissions()));
						
			case STATUS:
				return MiscUtils.castList(listNum == LIST_AVAILABLE
							? transformerService.getAvailableStatus(getObject())
							: new ArrayList<>(getObject().getStatus()));
						
			default:
				throw new IllegalStateException("unknown list manager key: " + key);
		}
	}

	@Override
	protected void resetProperties() {
		element = null;
		elements = null;
		function = null;
		nested = null;
		nestedElement = null;
		permission = null;
	}

	@Override
	protected SourceCode getSourceCode(ContentObject contentObject) {
		Assert.notNull(contentObject, "contentObject");
		
		return transformerFunctionCodeProvider.getFunctionSource((TransformerFunction) contentObject);
	}
	
}
