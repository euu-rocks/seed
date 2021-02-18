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

import org.seed.core.data.SystemObject;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.transform.NestedTransformer;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerElement;
import org.seed.core.entity.transform.TransformerFunction;
import org.seed.core.entity.transform.TransformerService;
import org.seed.core.user.Authorisation;
import org.seed.core.user.UserGroup;
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
	private static final String USERGROUPS = "usergroups";
	private static final String STATUS = "status";
	
	@Wire("#newTransformerWin")
	private Window window;
	
	@WireVariable(value="transformerServiceImpl")
	private TransformerService transformerService;
	
	@WireVariable(value="entityServiceImpl")
	private EntityService entityService;
	
	private List<TransformerElement> elements;
	
	private List<NestedTransformer> nesteds;
	
	private TransformerElement element;
	
	private TransformerFunction function;
	
	private NestedTransformer nested;
	
	private TransformerElement nestedElement;
	
	public AdminTransformerViewModel() {
		super(Authorisation.ADMIN_ENTITY, "transformer",
			  "/admin/transform/transformerlist.zul", 
			  "/admin/transform/transformer.zul",
			  "/admin/transform/newtransformer.zul");
	}

	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam("param") Object object) {
		super.init(object, view);
	}
	
	@Override
	protected void initObject(Transformer transformer) {
		elements = transformerService.getMainObjectElements(transformer);
		nesteds = transformerService.getNestedTransformers(transformer);
	}
	
	@Override
	protected void initFilters() {
		final ListFilter filterSourceEntity = getFilter(FILTERGROUP_LIST, "sourceentity");
		filterSourceEntity.setValueFunction(o -> ((Transformer) o).getSourceEntity().getName());
		
		final ListFilter filterTargetEntity = getFilter(FILTERGROUP_LIST, "targetentity");
		filterTargetEntity.setValueFunction(o -> ((Transformer) o).getTargetEntity().getName());
		
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
	
	public List<Entity> getEntities() {
		return entityService.findNonGenericEntities();
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
	@SmartNotifyChange("nested")
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
		showCodeDialog(new CodeDialogParameter(this, function));
	}
	
	@Command
	public void createTransformer(@BindingParam("elem") Component elem) {
		cmdInitObject(elem, window);
	}
	
	@Command
	public void cancel() {
		window.detach();
	} 
	
	@Command
	@NotifyChange({"element", "elements"})
	public void newElement() {
		element = new TransformerElement();
		element.setTransformer(getObject());
		elements.add(element);
		flagDirty();
	}
	
	@Command
	@NotifyChange({"element", "elements"})
	public void removeElement() {
		elements.remove(element);
		element = null;
		flagDirty();
	}
	
	@Command
	@NotifyChange({"nested", "nesteds"})
	public void newNested() {
		nested = new NestedTransformer();
		nesteds.add(nested);
		flagDirty();
	}
	
	@Command
	@NotifyChange({"nested", "nesteds"})
	public void removeNested() {
		nesteds.remove(nested);
		nested = null;
		flagDirty();
	}
	
	@Command
	@NotifyChange({"nested", "nestedElement"})
	public void newNestedElement() {
		nestedElement = new TransformerElement();
		nestedElement.setTransformer(getObject());
		nested.addElement(nestedElement);
		flagDirty();
	}
	
	@Command
	@NotifyChange({"nested", "nestedElement"})
	public void removeNestedElement() {
		nested.removeElement(nestedElement);
		nestedElement = null;
		flagDirty();
	}
	
	@Command
	@NotifyChange("function")
	public void newFunction() {
		function = transformerService.createFunction(getObject());
		notifyObjectChange(FUNCTIONS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("function")
	public void removeFunction(@BindingParam("elem") Component component) {
		getObject().removeFunction(function);
		function = null;
		notifyObjectChange(FUNCTIONS);
		flagDirty();
	}
	
	@Command
	public void refreshTransformer(@BindingParam("elem") Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteTransformer(@BindingParam("elem") Component component) {
		cmdDeleteObject(component);
	}
	
	@Command
	public void saveTransformer(@BindingParam("elem") Component component) {
		adjustLists(getObject().getUserGroups(), getListManagerList(USERGROUPS, LIST_SELECTED));
		adjustLists(getObject().getStatus(), getListManagerList(STATUS, LIST_SELECTED));
		transformerService.adjustElements(getObject(), elements, nesteds);
		
		cmdSaveObject(component);
		refreshObject();
	}
	
	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, notifyObject);
	}
	
	@Command
	public void dropToGroupList(@BindingParam("item") UserGroup item,
								@BindingParam("list") int listNum) {
		dropToList(USERGROUPS, listNum, item);
	}
	
	@Command
	public void insertToGroupList(@BindingParam("base") UserGroup base,
								  @BindingParam("item") UserGroup item,
								  @BindingParam("list") int listNum) {
		insertToList(USERGROUPS, listNum, base, item);
	}
	
	@Command
	public void dropToStatusList(@BindingParam("item") EntityStatus item,
								 @BindingParam("list") int listNum) {
		dropToList(STATUS, listNum, item);
	}
	
	@Command
	public void insertToStatusList(@BindingParam("base") EntityStatus base,
								   @BindingParam("item") EntityStatus item,
								   @BindingParam("list") int listNum) {
		insertToList(STATUS, listNum, base, item);
	}
	
	@Command
	public void swapFunctions(@BindingParam("base") TransformerFunction base,
							  @BindingParam("item") TransformerFunction item) {
		swapItems(FUNCTIONS, base, item);
	}
	
	@GlobalCommand
	public void _refreshObject(@BindingParam("param") Long objectId) {
		refreshObject(objectId);
	}
	
	@Override
	protected List<? extends SystemObject> getListSorterSource(String key) {
		switch (key) {
			case FUNCTIONS:
				return getObject().getFunctions();
			default:
				throw new IllegalStateException("unknown list sorter key: " + key);
		}
	}
	
	@Override
	protected List<? extends SystemObject> getListManagerSource(String key, int listNum) {
		switch (key) {
			case USERGROUPS:
				return listNum == LIST_AVAILABLE
						? transformerService.getAvailableUserGroups(getObject())
						: new ArrayList<>(getObject().getUserGroups());
						
			case STATUS:
				return listNum == LIST_AVAILABLE
						? transformerService.getAvailableStatus(getObject())
						: new ArrayList<>(getObject().getStatus());
						
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
	}
	
}
