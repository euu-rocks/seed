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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.ModuleService;
import org.seed.core.data.DataException;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.SystemEntityService;
import org.seed.core.data.SystemObject;
import org.seed.core.data.ValidationException;
import org.seed.core.user.Authorisation;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupService;
import org.seed.ui.DragDropListManager;
import org.seed.ui.DragDropListSorter;
import org.seed.ui.ListFilter;
import org.seed.ui.ListFilterGroup;
import org.seed.ui.ListFilterListener;
import org.seed.ui.ViewMode;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.Assert;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;

public abstract class AbstractAdminViewModel<T extends SystemEntity> extends AbstractApplicationViewModel 
	implements ListFilterListener {
	
	static final int LIST_AVAILABLE = 0;
	static final int LIST_SELECTED  = 1;
	
	static final String KEY_SUCCESS = "success";
	static final String KEY_FAIL 	= "fail";
	
	static final String FILTERGROUP_LIST = "list";
	
	static final String CONFIRM_BACK 		= "back";
	static final String CONFIRM_DELETE 		= "delete";
	static final String CONFIRM_NEW 		= "new";
	static final String CONFIRM_NEW_DIALOG 	= "newdialog";
	static final String CONFIRM_RELOAD 		= "reload";
	
	private final Authorisation authorisation;
	
	private final String objectLabelKey;
	private final String listViewZul;
	private final String detailViewZul;
	private final String createDialogZul;
	
	@WireVariable(value="moduleServiceImpl")
	private ModuleService moduleService;
	
	@WireVariable(value="userGroupServiceImpl")
	private UserGroupService userGroupService;
	
	private DialogParameter dialogParameter;
	
	private ViewMode viewMode;
	
	private T object;
	
	private List<T> objectList;
	
	private Map<String, ListFilterGroup> filterGroupMap;
	private Map<String, DragDropListSorter> listSorterMap;
	private Map<String, DragDropListManager> listManagerMap;
	
	protected abstract SystemEntityService<T> getObjectService();
	protected abstract void resetProperties();
	
	protected AbstractAdminViewModel() {
		authorisation = null;
		objectLabelKey = null;
		listViewZul = null;
		detailViewZul = null;
		createDialogZul = null;
	}
	
	protected AbstractAdminViewModel(Authorisation authorisation,
									 String objectLabelKey, 
			 						 String listViewZul, 
			 						 String detailViewZul) {
		this (authorisation, objectLabelKey, listViewZul, detailViewZul, null);
	}
	
	protected AbstractAdminViewModel(Authorisation authorisation,
									 String objectLabelKey, 
									 String listViewZul, 
									 String detailViewZul,
									 String createDialogZul) {
		Assert.notNull(authorisation, "authorisation is null");
		Assert.notNull(objectLabelKey, "objectLabelKey is null");
		Assert.notNull(listViewZul, "listView is null");
		Assert.notNull(detailViewZul, "detailView is null");
		
		this.authorisation = authorisation;
		this.objectLabelKey = objectLabelKey;
		this.listViewZul = listViewZul;
		this.detailViewZul = detailViewZul;
		this.createDialogZul = createDialogZul;
	}

	@SuppressWarnings("unchecked")
	protected T init(Object object, Component view) {
		checkAuthorisation();
		// window
		if (view != null && view.getClass() == Window.class) {
			Assert.state(object instanceof DialogParameter, "object is not DialogParameter: " + object);
			
			this.viewMode = ViewMode.CREATE;
			this.dialogParameter = (DialogParameter) object;
			this.object = (T) dialogParameter.parameter;
			wireComponents(view);
		}
		// detail
		else if (object != null) {
			this.viewMode = ViewMode.DETAIL;
			if (object instanceof Long) {
				this.object = getObjectService().getObject((Long) object);
			}
			else {
				this.object = (T) object;
			}
			initObject(this.object);
			if (this.object.isNew()) {
				flagDirty();
			}
		}
		// list
		else {
			this.viewMode = ViewMode.LIST;
			initFilterGroup(FILTERGROUP_LIST, "objectList");
			if (getObjectService().isEntityType(ApplicationEntity.class)) {
				createModuleFilter();
			}
			initFilters();
		}
		return this.object;
	}
	
	protected void initObject(T object) {
		// do nothing by default
	}
	
	protected ViewMode getViewMode() {
		return viewMode;
	}
	
	protected AbstractAdminViewModel<?> getParentVM() {
		Assert.state(dialogParameter != null, "DialogParameter not available");
		
		return dialogParameter.parentViewModel;
	}
	
	@SuppressWarnings("unchecked")
	protected void internalRefresh(Object object) {
		Assert.notNull(object, "object is null");
		
		filterGroupMap = null;
		listSorterMap = null;
		listManagerMap = null;
		resetDirty();
		resetProperties();
		
		final T obj = (T) object;
		init(obj.isNew() ? obj : obj.getId(), null);
		notifyChangeAll();
	}
	
	protected void showListView() {
		showView(listViewZul, null);
	}
	
	protected void showDetailView(T object) {
		Assert.notNull(object, "object is null");
		
		showView(detailViewZul, object.isNew() ? object : object.getId());
	}
	
	protected void showCodeDialog(CodeDialogParameter parameter) {
		Assert.notNull(parameter, "parameter is null");
		
		showDialog("/admin/codedialog.zul", parameter);
	}
	
	protected void notifyObjectChange(String ...property) {
		Assert.notNull(property, "property is null");
		
		notifyObjectChange(object, property);
	}

	protected void flagDirty(String notify, String notifyObject) {
		flagDirty();
		if (notify != null) {
			notifyChange(notify);
		}
		if (notifyObject != null) {
			notifyObjectChange(notifyObject);
		}
	}
	
	public final T getObject() {
		return object;
	}
	
	public final void setObject(T object) {
		this.object = object;
	}
	
	public final List<T> getObjectList() {
		if (objectList == null) {
			objectList = loadObjectList();
		}
		if (getListViewFilterGroup().isVisible()) {
			return (List<T>) getListViewFilterGroup().filter(objectList);
		}
		return objectList;
	}
	
	protected List<T> loadObjectList() {
		return getObjectService().findAllObjects();
	}
	
	public final boolean existObjects() {
		return objectList != null 
				? !objectList.isEmpty() 
				: getObjectService().existObjects();
	}
	
	public final boolean existModules() {
		return moduleService.existObjects();
	}
	
	public final List<Module> getModules() {
		return moduleService.findAllObjects();
	}
	
	public final List<UserGroup> getUserGroups() {
		return userGroupService.findAllObjects();
	}
	
	public final boolean existUserGroups() {
		return userGroupService.existObjects();
	}
	
	protected UserGroupService userGroupService() {
		return userGroupService;
	}
	
	public String getTitle() {
		return object.isNew() 
				? getLabel("admin.create." + objectLabelKey) 
				: getLabel("label." + objectLabelKey) + ": " + object.getName();
	}
	
	// filter ----------------------------------------------
	
	@Override
	public final void filterChanged(ListFilterGroup filterGroup) {
		notifyChange(filterGroup.getNotifyChange());
	}
	
	public final ListFilter getFilter(String filterGroupName, String filterName) {
		Assert.notNull(filterName, "filterName is null");
		
		return getFilterGroup(filterGroupName).getFilter(filterName);
	}
	
	public final ListFilterGroup getListViewFilterGroup() {
		return getFilterGroup(FILTERGROUP_LIST);
	}
	
	protected void initFilters() {
		// do nothing by default
	}
	
	protected void initFilterGroup(String filterGroupName, String ...notifyChange) {
		Assert.notNull(filterGroupName, "filterGroupName is null");
		
		if (filterGroupMap == null) {
			filterGroupMap = new HashMap<>();
		}
		filterGroupMap.put(filterGroupName, new ListFilterGroup(this, notifyChange));
	}
	
	protected ListFilterGroup getFilterGroup(String filterGroupName) {
		Assert.notNull(filterGroupName, "filterGroupName is null");
		Assert.state(filterGroupMap != null, "filters not initialized");
		Assert.state(filterGroupMap.containsKey(filterGroupName), "filter group not exist: " + filterGroupName);
		
		return filterGroupMap.get(filterGroupName);
	}
	
	private void createModuleFilter() {
		final ListFilter filterModule = getFilter(FILTERGROUP_LIST, "module");
		filterModule.setValueFunction(o -> ((ApplicationEntity) o).getModule() != null 
											? ((ApplicationEntity) o).getModule().getName() 
											: null);
		for (T object : getObjectList()) {
			if (((ApplicationEntity) object).getModule() != null) {
				filterModule.addValue(((ApplicationEntity) object).getModule().getName());
			}
		}
	}
	
	// d&d list sorter ----------------------------------------
	
	protected List<? extends SystemObject> getListSorterSource(String key) {
		throw new UnsupportedOperationException();
	}

	protected void swapItems(String key, SystemObject base, SystemObject item) {
		Assert.notNull(key, "key is null");
		Assert.notNull(base, "base is null");
		Assert.notNull(item, "item is null");
		
		if (listSorterMap == null) {
			listSorterMap = new HashMap<>();
		}
		DragDropListSorter listSorter = listSorterMap.get(key);
		if (listSorter == null) {
			listSorter = new DragDropListSorter(getListSorterSource(key));
			listSorterMap.put(key, listSorter);
		}
		listSorter.swap(base, item);
		notifyObjectChange(key);
		flagDirty();
	}
	
	// d&d list manager -------------------------------------
	
	protected List<? extends SystemObject> getListManagerSource(String key, int listNum) {
		throw new UnsupportedOperationException();
	}
	
	public final List<SystemObject> getListManagerList(String key, int listNum) {
		return getListManager(key).getList(listNum);
	}
	
	protected void dropToList(String key, int listNum, SystemObject item) {
		Assert.notNull(item, "item is null");
		
		getListManager(key).drop(item, listNum);
		notifyChange("getListManagerList");
		flagDirty();
	}
	
	protected void insertToList(String key, int listNum, SystemObject base, SystemObject item) {
		Assert.notNull(base, "base is null");
		Assert.notNull(item, "item is null");
		
		getListManager(key).insert(base, item, listNum);
		notifyChange("getListManagerList");
		flagDirty();
	}
	
	protected void selectAll(String key) {
		Assert.notNull(key, "key is null");
		
		getListManager(key).selectAll();
		notifyChange("getListManagerList");
		flagDirty();
	}
	
	protected void removeListManager(String key) {
		Assert.notNull(key, "key is null");
		
		if (listManagerMap != null) {
			listManagerMap.remove(key);
		}
	}
	
	protected DragDropListManager getListManager(String key) {
		Assert.notNull(key, "key is null");
		
		if (listManagerMap == null) {
			listManagerMap = new HashMap<>();
		}
		DragDropListManager listManager = listManagerMap.get(key);
		if (listManager == null) {
			listManager = new DragDropListManager();
			for (int listNum = 0; listNum < 2; listNum++) {
				final List<? extends SystemObject> list = getListManagerSource(key, listNum);
				if (list != null) {
					listManager.getList(listNum).addAll(list);
				}
			}
			listManagerMap.put(key, listManager);
		}
		return listManager;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void adjustLists(Collection objectList, List viewList) {
		Assert.notNull(objectList, "objectList is null");
		Assert.notNull(viewList, "viewList is null");
		
		objectList.clear();
		objectList.addAll(viewList);
	}
	
	// commands ----------------------------------------
	
	protected void cmdBack() {
		if (isDirty()) {
			confirmDirty(CONFIRM_BACK);
		}
		else {
			showListView();
		}
	}
	
	protected void cmdNewObject() {
		final T newObject = createObject();
		switch(viewMode) {
			case LIST:
				showDetailView(newObject);
				break;
				
			case DETAIL:
				if (isDirty()) {
					confirmDirty(CONFIRM_NEW);
				}
				else {
					resetMainTabbox();
					internalRefresh(newObject);
				}
				break;
				
			default:
				throw new UnsupportedOperationException(viewMode.name());
		}
	}
	
	protected T createObject() {
		return getObjectService().createInstance(null);
	}
	
	protected void cmdNewObjectDialog() {
		if (isDirty()) {
			confirmDirty(CONFIRM_NEW_DIALOG);
		}
		else {
			Assert.state(createDialogZul != null, "create-dialog not defined");
			showDialog(createDialogZul, new DialogParameter(this, createObject())); 
		}
	}
	
	protected void cmdRefresh() {
		switch (viewMode) {
			case LIST:
				refreshList();
				break;
			
			case DETAIL:
				if (isDirty()) {
					confirmDirty(CONFIRM_RELOAD);
				}
				else {
					refreshObject();
				}
				break;
			
			default:
				throw new UnsupportedOperationException(viewMode.name());
		}
	}
	
	protected void refreshList() {
		objectList = null;
		notifyChange("existObjects", "objectList");
	}
	
	// called from tab.onselect
	protected void refreshObject(Long objectId) {
		Assert.notNull(objectId, "objectId is null");
		
		if (viewMode == ViewMode.DETAIL && objectId.equals(object.getId())) {
			internalRefresh(getObjectService().getObject(object.getId()));
		}
	}
	
	protected void refreshObject() {
		object.removeNewObjects();
		getObjectService().reloadObject(object);
		internalRefresh(object);
	}
	
	protected void cmdEditObject() {
		Assert.state(object != null, "object not available");
		
		showDetailView(object);
	}
	
	protected void cmdDeleteObject(Component component) {
		Assert.notNull(component, "component is null");
		Assert.state(object != null, "object not available");
		
		final String msgKey = "admin." + objectLabelKey + ".confirmdelete";
		confirm(msgKey, component, CONFIRM_DELETE, object.getName());
	}
	
	protected void deleteObject(Component component) {
		try {
			getObjectService().deleteObject(object);
			switch(viewMode) {
				case DETAIL:
					cmdBack();
					break;
				
				case LIST:
					object = null;
					objectList = null;
					notifyChange("object", "objectList");
					break;
					
				default:
					throw new UnsupportedOperationException(viewMode.name());
			}
		}
		catch (ValidationException vex) {
			final String msgKey = "admin." + objectLabelKey + ".deletefail";
			showValidationErrors(component, msgKey, vex.getErrors());
		}
	}
	
	protected void cmdInitObject(Component component, Window window) {
		Assert.notNull(component, "component is null");
		Assert.notNull(window, "window is null");
		
		try {
			getObjectService().initObject(object);
			window.detach();
			switch (getParentVM().getViewMode()) {
				case LIST:
					showDetailView(object);
					break;
					
				case DETAIL:
					getParentVM().resetMainTabbox();
					getParentVM().internalRefresh(object);
					break;
					
				default:
					throw new UnsupportedOperationException(getParentVM().getViewMode().name());
			}
		} 
		catch (ValidationException vex) {
			final String msgKey = "admin." + objectLabelKey + ".createfail";
			showValidationErrors(component, msgKey, vex.getErrors());
		}
	}
	
	protected boolean cmdSaveObject(Component component) {
		Assert.notNull(component, "component is null");
		
		final String msgKey = "admin." + objectLabelKey + ".save";
		try {
			getObjectService().saveObject(object);
			showNotification(component, false, msgKey + KEY_SUCCESS);
			notifyChange("object", "title");
			resetDirty();
			return true;
		}
		catch (ValidationException vex) {
			showValidationErrors(component, msgKey + KEY_FAIL, vex.getErrors());
			if (vex.getCause() instanceof DataException) {
				throw (DataException) vex.getCause();
			}
		}
		catch (OptimisticLockException olex) {
			final String errMsgKey = "admin." + objectLabelKey + ".failstale";
			showError(component, errMsgKey);
		}
		catch (PersistenceException persitenceException) {
			final Throwable cause =  persitenceException.getCause();
			if (cause instanceof org.hibernate.exception.ConstraintViolationException &&
				cause.getCause().getMessage().contains("(name)=")) {
				final String errMsgKey = "admin." + objectLabelKey + ".failunique";
				showError(component, errMsgKey, object.getName());
			}
			else
				throw persitenceException;
		}
		return false;
	}
	
	@Override
	protected void confirmed(boolean confirmed, Component component, Object confirmParam) {
		Assert.notNull(confirmParam, "confirmParam is null");
		
		if (confirmed) {
			switch (confirmParam.toString()) {
				case CONFIRM_BACK:
					resetDirty();
					cmdBack();
					break;
					
				case CONFIRM_NEW:
					resetDirty();
					cmdNewObject();
					break;
					
				case CONFIRM_NEW_DIALOG:
					resetDirty();
					cmdNewObjectDialog();
					break;
					
				case CONFIRM_RELOAD:
					resetDirty();
					cmdRefresh();
					break;
					
				case CONFIRM_DELETE:
					deleteObject(component);
					refreshMenu();
					break;
			}
		}
	}
	
	// misc ------------------------------------
	
	private void checkAuthorisation() {
		if (!getUser().isAuthorised(authorisation)) {
			throw new AccessDeniedException(getLabel("label.accessdenied"));
		}
	}
	
	private void resetMainTabbox() {
		final Tabbox tabbox = getComponentById("mainTabbox");
		if (tabbox != null) {
			tabbox.setSelectedIndex(0);
		}
	}
	
	private void confirmDirty(String action) {
		Assert.notNull(action, "action is null");
		
		confirm("question.dirty", null, action);
	}

}
