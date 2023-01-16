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

import org.seed.C;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ContentObject;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.ModuleService;
import org.seed.core.codegen.SourceCode;
import org.seed.core.data.DataException;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.SystemEntityService;
import org.seed.core.data.SystemObject;
import org.seed.core.data.ValidationException;
import org.seed.core.user.Authorisation;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupService;
import org.seed.core.util.Assert;
import org.seed.core.util.ExceptionUtils;
import org.seed.ui.DragDropListManager;
import org.seed.ui.DragDropListSorter;
import org.seed.ui.ListFilter;
import org.seed.ui.ListFilterGroup;
import org.seed.ui.ListFilterListener;
import org.seed.ui.ViewMode;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.springframework.security.access.AccessDeniedException;
import org.zkoss.bind.annotation.BindingParam;
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
	
	static final String CONFIRM_BACK 		= "back";
	static final String CONFIRM_DELETE 		= "delete";
	static final String CONFIRM_NEW 		= "new";
	static final String CONFIRM_NEW_DIALOG 	= "newdialog";
	static final String CONFIRM_RELOAD 		= "reload";
	
	protected static final String FILTERGROUP_LIST = "list";
	protected static final String LISTMANAGER_LIST = "getListManagerList";
	protected static final String OBJECT_LIST      = "objectList";
	protected static final String PRE_ADMIN 	   = "admin.";
	
	private final Authorisation authorisation;
	
	private final String objectLabelKey;
	private final String listViewZul;
	private final String detailViewZul;
	private final String createDialogZul;
	
	@WireVariable(value="moduleServiceImpl")
	private ModuleService moduleService;
	
	@WireVariable(value="userGroupServiceImpl")
	protected UserGroupService userGroupService;
	
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
		Assert.notNull(authorisation, "authorisation");
		Assert.notNull(objectLabelKey, "objectLabelKey");
		Assert.notNull(listViewZul, "listView");
		Assert.notNull(detailViewZul, "detailView");
		
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
				this.object = getObjectService().getObject((Long) object, currentSession());
			}
			else {
				this.object = (T) object;
			}
			Assert.stateAvailable(this.object, C.OBJECT);
			initObject(this.object);
			if (this.object.isNew()) {
				flagDirty();
			}
		}
		// list
		else {
			this.viewMode = ViewMode.LIST;
			initFilterGroup(FILTERGROUP_LIST, OBJECT_LIST);
			if (getObjectService().isEntityType(ApplicationEntity.class)) {
				createModuleFilter();
			}
			initFilters();
		}
		return this.object;
	}
	
	protected void initObject(T object) {
		if (!object.isNew()) {
			object.removeNewObjects();
		}
	}
	
	protected ViewMode getViewMode() {
		return viewMode;
	}
	
	@SuppressWarnings("unchecked")
	protected <E extends SystemEntity> AbstractAdminViewModel<E> getParentVM() {
		Assert.stateAvailable(dialogParameter, "dialog parameter");
		
		return (AbstractAdminViewModel<E>) dialogParameter.parentViewModel;
	}
	
	@SuppressWarnings("unchecked")
	protected void internalRefresh(Object object) {
		Assert.notNull(object, C.OBJECT);
		
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
		Assert.notNull(object, C.OBJECT);
		
		showView(detailViewZul, object.isNew() ? object : object.getId());
	}
	
	protected void showCodeDialog(CodeDialogParameter parameter) {
		Assert.notNull(parameter, C.PARAMETER);
		
		showDialog("/admin/codedialog.zul", parameter);
	}
	
	protected void notifyObjectChange(String ...property) {
		Assert.notNull(property, C.PROPERTY);
		
		notifyObjectChange(object, property);
	}

	protected void flagDirty(String notify, Object object, String notifyObject) {
		flagDirty();
		if (notify != null) {
			notifyChange(notify);
		}
		if (notifyObject != null) {
			if (object != null) {
				notifyObjectChange(object, notifyObject);
			}
			else {
				notifyObjectChange(notifyObject);
			}
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
			return getListViewFilterGroup().filter(objectList);
		}
		return objectList;
	}
	
	public String getListItemTestClass(Object object) {
		return getTestClass(object, "-listitem");
	}
	
	public String getItemTestClass(Object object) {
		return getTestClass(object, "-item");
	}
	
	protected List<T> loadObjectList() {
		return getObjectService().getObjects(currentSession());
	}
	
	public final boolean existObjects() {
		return objectList != null 
				? !objectList.isEmpty() 
				: getObjectService().existObjects(currentSession());
	}
	
	public final boolean existModules() {
		return moduleService.existObjects(currentSession());
	}
	
	public final List<Module> getModules() {
		return moduleService.getObjects(currentSession());
	}
	
	public final List<UserGroup> getUserGroups() {
		return userGroupService.findNonSystemGroups(currentSession());
	}
	
	public final boolean existUserGroups() {
		return !getUserGroups().isEmpty();
	}
	
	protected UserGroupService userGroupService() {
		return userGroupService;
	}
	
	protected SourceCode getSourceCode(ContentObject contentObject) {
		throw new UnsupportedOperationException();
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
	
	public final ListFilter<T> getFilter(String filterGroupName, String filterName) {
		Assert.notNull(filterName, "filterName");
		
		return getFilterGroup(filterGroupName).getFilter(filterName);
	}
	
	public final ListFilterGroup getListViewFilterGroup() {
		return getFilterGroup(FILTERGROUP_LIST);
	}
	
	protected void initFilters() {
		// do nothing by default
	}
	
	protected void initFilterGroup(String filterGroupName, String ...notifyChange) {
		Assert.notNull(filterGroupName, "filterGroupName");
		
		if (filterGroupMap == null) {
			filterGroupMap = new HashMap<>();
		}
		filterGroupMap.put(filterGroupName, new ListFilterGroup(this, notifyChange));
	}
	
	protected ListFilterGroup getFilterGroup(String filterGroupName) {
		Assert.notNull(filterGroupName, "filterGroupName");
		Assert.state(filterGroupMap != null, "filters not initialized");
		Assert.state(filterGroupMap.containsKey(filterGroupName), "filter group not exist: " + filterGroupName);
		
		return filterGroupMap.get(filterGroupName);
	}
	
	@SuppressWarnings("unchecked")
	private void createModuleFilter() {
		final ListFilter<? extends ApplicationEntity> filterModule = 
			(ListFilter<ApplicationEntity>) getFilter(FILTERGROUP_LIST, C.MODULE);
		filterModule.setValueFunction(o -> o.getModule() != null 
											? o.getModule().getName() 
											: null);
		for (T obj : getObjectList()) {
			if (((ApplicationEntity) obj).getModule() != null) {
				filterModule.addValue(((ApplicationEntity) obj).getModule().getName());
			}
		}
	}
	
	// d&d list sorter ----------------------------------------
	
	protected List<SystemObject> getListSorterSource(String key) {
		throw new UnsupportedOperationException();
	}

	protected void swapItems(String key, SystemObject base, SystemObject item) {
		Assert.notNull(key, C.KEY);
		Assert.notNull(base, C.BASE);
		Assert.notNull(item, C.ITEM);
		
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
	
	protected List<SystemObject> getListManagerSource(String key, int listNum) {
		throw new UnsupportedOperationException();
	}
	
	public final List<SystemObject> getListManagerList(String key, int listNum) {
		return getListManager(key).getList(listNum);
	}
	
	protected void dropToList(String key, int listNum, SystemObject item) {
		Assert.notNull(item, C.ITEM);
		
		getListManager(key).drop(item, listNum);
		notifyChange(LISTMANAGER_LIST);
		flagDirty();
	}
	
	protected void insertToList(String key, int listNum, SystemObject base, SystemObject item) {
		Assert.notNull(base, C.BASE);
		Assert.notNull(item, C.ITEM);
		
		getListManager(key).insert(base, item, listNum);
		notifyChange(LISTMANAGER_LIST);
		flagDirty();
	}
	
	protected void selectAll(String key) {
		Assert.notNull(key, C.KEY);
		
		getListManager(key).selectAll();
		notifyChange(LISTMANAGER_LIST);
		flagDirty();
	}
	
	protected void removeListManager(String key) {
		Assert.notNull(key, C.KEY);
		
		if (listManagerMap != null) {
			listManagerMap.remove(key);
		}
	}
	
	protected DragDropListManager getListManager(String key) {
		Assert.notNull(key, C.KEY);
		
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
	
	@SuppressWarnings({ "unchecked" })
	protected void adjustLists(Collection<?> objectList, List<?> viewList) {
		Assert.notNull(objectList, OBJECT_LIST);
		Assert.notNull(viewList, "viewList");
		
		final Collection<Object> dest = (Collection<Object>) objectList;
		dest.clear();
		dest.addAll(viewList);
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
		notifyChange("existObjects", OBJECT_LIST);
	}
	
	// called from tab.onselect
	protected void refreshObject(Long objectId) {
		Assert.notNull(objectId, "objectId");
		
		if (viewMode == ViewMode.DETAIL && objectId.equals(object.getId())) {
			final T reloadedObject = getObjectService().getObject(object.getId(), currentSession());
			if (reloadedObject != null) {
				internalRefresh(reloadedObject);
			}
		}
	}
	
	protected void refreshObject() {
		object.removeNewObjects();
		getObjectService().reloadObject(object, currentSession());
		internalRefresh(object);
	}
	
	protected void cmdEditObject() {
		Assert.stateAvailable(object, C.OBJECT);
		
		showDetailView(object);
	}
	
	protected void cmdDeleteObject(Component component) {
		Assert.notNull(component, C.COMPONENT);
		Assert.stateAvailable(object, C.OBJECT);
		
		final String msgKey = PRE_ADMIN + objectLabelKey + ".confirmdelete";
		confirm(msgKey, component, CONFIRM_DELETE, object.getName());
	}
	
	protected void deleteObject(Component component) {
		try {
			getObjectService().deleteObject(object);
			afterObjectDeleted(object);
			switch(viewMode) {
				case DETAIL:
					cmdBack();
					break;
				
				case LIST:
					object = null;
					objectList = null;
					notifyChange(C.OBJECT, OBJECT_LIST);
					break;
					
				default:
					throw new UnsupportedOperationException(viewMode.name());
			}
		}
		catch (ValidationException vex) {
			final String msgKey = PRE_ADMIN + objectLabelKey + ".deletefail";
			showValidationErrors(component, msgKey, vex.getErrors());
		}
	}
	
	protected void afterObjectDeleted(T object) {
		// do nothing by default
	}
	
	protected void cmdInitObject(Component component, Window window) {
		Assert.notNull(component, C.COMPONENT);
		Assert.notNull(window, "window");
		
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
			final String msgKey = PRE_ADMIN + objectLabelKey + ".createfail";
			showValidationErrors(component, msgKey, vex.getErrors());
		}
	}
	
	protected boolean cmdSaveObject(Component component) {
		Assert.notNull(component, C.COMPONENT);
		
		final String msgKey = PRE_ADMIN + objectLabelKey + ".save";
		try {
			getObjectService().saveObject(object);
			showNotification(component, false, msgKey + KEY_SUCCESS);
			notifyChange(C.OBJECT, "title");
			resetDirty();
			return true;
		}
		catch (ValidationException vex) {
			handleValidationException(vex, component, msgKey);
			if (vex.getCause() instanceof DataException) {
				throw (DataException) vex.getCause();
			}
		}
		catch (OptimisticLockException olex) {
			final String errMsgKey = PRE_ADMIN + objectLabelKey + ".failstale";
			showError(component, errMsgKey);
		}
		catch (PersistenceException persitenceException) {
			if (ExceptionUtils.isUniqueConstraintViolation(persitenceException)) {
				final String errMsgKey = PRE_ADMIN + objectLabelKey + ".failunique";
				showError(component, errMsgKey, object.getName());
			}
			else {
				throw persitenceException;
			}
		}
		return false;
	}
	
	public void setObjectContent(@BindingParam(C.CONTENT) String content) {
		((ContentObject) object).setContent(content);
		flagDirty();
	}
	
	protected void handleValidationException(ValidationException vex, Component component, String msgKey) {
		showValidationErrors(component, msgKey + KEY_FAIL, vex.getErrors());
	}
	
	@Override
	protected void confirmed(boolean confirmed, Component component, Object confirmParam) {
		Assert.notNull(confirmParam, "confirmParam");
		
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
					
				default:
					throw new UnsupportedOperationException(confirmParam.toString());
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
		Assert.notNull(action, C.ACTION);
		
		confirm("question.dirty", null, action);
	}
	
}
