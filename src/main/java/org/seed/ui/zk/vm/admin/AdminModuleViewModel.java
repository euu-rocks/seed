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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.seed.C;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.ModuleMetadata;
import org.seed.core.application.module.ModuleParameter;
import org.seed.core.application.module.ModuleService;
import org.seed.core.customcode.CustomCodeService;
import org.seed.core.customcode.CustomLibService;
import org.seed.core.data.SystemObject;
import org.seed.core.data.ValidationException;
import org.seed.core.data.datasource.DataSourceService;
import org.seed.core.data.dbobject.DBObjectService;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.filter.FilterService;
import org.seed.core.entity.transfer.TransferService;
import org.seed.core.entity.transform.TransformerService;
import org.seed.core.form.FormService;
import org.seed.core.form.navigation.MenuService;
import org.seed.core.rest.RestService;
import org.seed.core.task.TaskService;
import org.seed.core.user.Authorisation;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.springframework.oxm.UnmarshallingFailureException;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;

public class AdminModuleViewModel extends AbstractAdminViewModel<Module> {
	
	private static final String DBOBJECTS = "dbobjects";
	private static final String DATASOURCES = "datasources";
	private static final String ENTITIES = "entities";
	private static final String FILTERS = "filters";
	private static final String TRANSFORMERS = "transformers";
	private static final String TRANSFERS = "transfers";
	private static final String FORMS = "forms";
	private static final String MENUS = "menus";
	private static final String PARAMETERS = "parameters";
	private static final String TASKS = "tasks";
	private static final String REPORTS = "reports";
	private static final String RESTS = "rests";
	private static final String CUSTOMCODES = "customcodes";
	private static final String CUSTOMLIBS = "customlibs";
	private static final String USERGROUPS = "usergroups";
	
	@WireVariable(value="moduleServiceImpl")
	private ModuleService moduleService;
	
	@WireVariable(value="DBObjectServiceImpl")
	private DBObjectService dbObjectService;
	
	@WireVariable(value="dataSourceServiceImpl")
	private DataSourceService dataSourceService;
	
	@WireVariable(value="entityServiceImpl")
	private EntityService entityService;
	
	@WireVariable(value="filterServiceImpl")
	private FilterService filterService;
	
	@WireVariable(value="transformerServiceImpl")
	private TransformerService transformerService;
	
	@WireVariable(value="transferServiceImpl")
	private TransferService transferService;
	
	@WireVariable(value="formServiceImpl")
	private FormService formService;
	
	@WireVariable(value="menuServiceImpl")
	private MenuService menuService;
	
	@WireVariable(value="taskServiceImpl")
	private TaskService taskService;
	
	@WireVariable(value="restServiceImpl")
	private RestService restService;
	
	@WireVariable(value="customCodeServiceImpl")
	private CustomCodeService customCodeService;
	
	@WireVariable(value="customLibServiceImpl")
	private CustomLibService customLibService;
	
	private ModuleParameter parameter;
	
	private boolean existDBObjects;
	private boolean existDataSources;
	private boolean existEntities;
	private boolean existFilters;
	private boolean existTransformers;
	private boolean existTransfers;
	private boolean existForms;
	private boolean existMenus;
	private boolean existTasks;
	private boolean existReports;
	private boolean existRests;
	private boolean existCustomCodes;
	private boolean existCustomLibs;
	private boolean existUserGroups;
	
	public AdminModuleViewModel() {
		super(Authorisation.ADMIN_MODULE, C.MODULE,
			  "/admin/module/modulelist.zul", 
			  "/admin/module/module.zul");
	}
	
	public ModuleParameter getParameter() {
		return parameter;
	}

	public void setParameter(ModuleParameter parameter) {
		this.parameter = parameter;
	}
	
	public boolean existDBObjects() {
		return existDBObjects;
	}
	
	public boolean existDataSources() {
		return existDataSources;
	}

	public boolean existEntities() {
		return existEntities;
	}
	
	public boolean existFilters() {
		return existFilters;
	}
	
	public boolean existTransformers() {
		return existTransformers;
	}
	
	public boolean existTransfers() {
		return existTransfers;
	}
	
	public boolean existForms() {
		return existForms;
	}
	
	public boolean existMenus() {
		return existMenus;
	}
	
	public boolean existTasks() {
		return existTasks;
	}
	
	public boolean existReports() {
		return existReports;
	}
	
	public boolean existRests() {
		return existRests;
	}
	
	public boolean existCustomCodes() {
		return existCustomCodes;
	}
	
	public boolean existCustomLibs() {
		return existCustomLibs;
	}
	
	public boolean existNonSystemUserGroups() {
		return existUserGroups;
	}
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam(C.PARAM) Object object) {
		super.init(object, view);
	}
	
	@Override
	public void initObject(Module module) {
		existDBObjects = dbObjectService.existObjects();
		existDataSources = dataSourceService.existObjects();
		existEntities = entityService.existObjects();
		existFilters = filterService.existObjects();
		existTransformers = transformerService.existObjects();
		existTransfers = transferService.existObjects();
		existForms = formService.existObjects();
		existMenus = menuService.existObjects();
		existTasks = taskService.existObjects();
		existReports = reportService.existObjects();
		existRests = restService.existObjects();
		existCustomCodes = customCodeService.existObjects();
		existCustomLibs = customLibService.existObjects();
		existUserGroups = !userGroupService.findNonSystemGroups().isEmpty();
	}
	
	@Command
	@NotifyChange(C.PARAMETER)
	public void newParameter() {
		parameter = moduleService.createParameter(getObject());
		notifyObjectChange(PARAMETERS);
		flagDirty();
	}
	
	@Command
	@NotifyChange(C.PARAMETER)
	public void removeParameter() {
		getObject().removeParameter(parameter);
		parameter = null;
		notifyObjectChange(PARAMETERS);
		flagDirty();
	}
 	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void newModule() {
		cmdNewObject();
	}
	
	@Command
	public void editModule() {
		cmdEditObject();
	}
	
	@Command
	public void refreshModule() {
		cmdRefresh();
	}
	
	@Command
	public void deleteModule(@BindingParam(C.ELEM) Component component) {
		cmdDeleteObject(component);
	}
	
	@Command
	public void saveModule(@BindingParam(C.ELEM) Component component) {
		((ModuleMetadata)getObject()).setChangedObjects(getChangedObjects());
		cmdSaveObject(component);
		cmdRefresh();
	}
	
	@Command
	public void exportModule(@BindingParam(C.ELEM) Component component) {
		Filedownload.save(moduleService.exportModule(getObject()),
						  "application/zip",
						  getObject().getFileName());
	}
	
	@Command
	public void analyzeModule(@BindingParam(C.ELEM) Component elem,
			 				  @ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event) {
		try (InputStream inputStream = getMediaStream(event.getMedia())) {
			final Module module = moduleService.readModule(inputStream);
			final ImportAnalysis analysis = moduleService.analyzeModule(module);
			showDialog("/admin/module/importanalysis.zul", new TransferDialogParameter(this, analysis));
		}
		catch (UnmarshallingFailureException ufex) {
			showError(elem, "admin.transfer.illegalformat");
		}
		catch (ValidationException vex) {
			showValidationErrors(elem, "admin.transfer.importfail", vex.getErrors());
		}
		catch (Exception ex) {
			showError(elem, ex);
		}
	}
	
	void importModule(Module module) {
		Assert.notNull(module, C.MODULE);
		
		moduleService.importModule(module);
		switch (getViewMode()) {
			case DETAIL:
				internalRefresh(module);
				break;
			
			case LIST:
				refreshList();
				break;
				
			default:
				throw new UnsupportedOperationException(getViewMode().name());
		}
		refreshMenu();
	}
	
	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, null, notifyObject);
	}
	
	@Command
	@Override
	public void dropToList(@BindingParam(C.KEY) String key, 
						   @BindingParam(C.LIST) int listNum, 
						   @BindingParam(C.ITEM) SystemObject item) {
		super.dropToList(key, listNum, item);
	}
	
	@Command
	@Override
	public void insertToList(@BindingParam(C.KEY) String key,
							 @BindingParam(C.LIST) int listNum,
							 @BindingParam(C.BASE) SystemObject base,
							 @BindingParam(C.ITEM) SystemObject item) {
		super.insertToList(key, listNum, base, item);
	}
	
	@GlobalCommand
	public void globalRefreshObject(@BindingParam(C.PARAM) Long objectId) {
		refreshObject(objectId);
	}
	
	@Override
	protected ModuleService getObjectService() {
		return moduleService;
	}
	
	@Override
	protected List<SystemObject> getListManagerSource(String key, int listNum) {
		switch (key) {
			case DBOBJECTS:
				return getListManagerSourceDBObject(listNum);
				
			case DATASOURCES:
				return getListManagerSourceDataSource(listNum);
				
			case ENTITIES:
				return getListManagerSourceEntity(listNum);
				
			case FILTERS:
				return getListManagerSourceFilter(listNum);
				
			case TRANSFORMERS:
				return getListManagerSourceTransformer(listNum);
				
			case TRANSFERS:
				return getListManagerSourceTransfer(listNum);
				
			case FORMS:
				return getListManagerSourceForm(listNum);
				
			case MENUS:
				return getListManagerSourceMenu(listNum);
				
			case TASKS:
				return getListManagerSourceTask(listNum);
				
			case REPORTS:
				return getListManagerSourceReport(listNum);
				
			case RESTS:
				return getListManagerSourceRest(listNum);
				
			case CUSTOMCODES:
				return getListManagerSourceCustomCode(listNum);
				
			case CUSTOMLIBS:
				return getListManagerSourceCustomLib(listNum);
				
			case USERGROUPS:
				return getListManagerSourceUserGroup(listNum);
				
			default:
				throw new IllegalStateException("unknown list manager key: " + key);
		}
	}

	@Override
	protected void resetProperties() {
		parameter = null;
	}
	
	private List<SystemObject> getListManagerSourceDBObject(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getDBObjects()
				: dbObjectService.findObjectsWithoutModule());
	}
	
	private List<SystemObject> getListManagerSourceDataSource(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getDataSources()
				: dataSourceService.findObjectsWithoutModule());
	}
	
	private List<SystemObject> getListManagerSourceEntity(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getEntities()
				: entityService.findObjectsWithoutModule());
	}
	
	private List<SystemObject> getListManagerSourceFilter(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getFilters()
				: filterService.findObjectsWithoutModule());
	}
	
	private List<SystemObject> getListManagerSourceTransformer(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getTransformers()
				: transformerService.findObjectsWithoutModule());
	}
	
	private List<SystemObject> getListManagerSourceTransfer(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getTransfers()
				: transferService.findObjectsWithoutModule());
	}
	
	private List<SystemObject> getListManagerSourceForm(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getForms()
				: formService.findObjectsWithoutModule());
	}
	
	private List<SystemObject> getListManagerSourceMenu(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getMenus()
				: menuService.findObjectsWithoutModule());
	}
	
	private List<SystemObject> getListManagerSourceTask(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getTasks()
				: taskService.findObjectsWithoutModule());
	}
	
	private List<SystemObject> getListManagerSourceReport(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getReports()
				: reportService.findObjectsWithoutModule());
	}
	
	private List<SystemObject> getListManagerSourceRest(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getRests()
				: restService.findObjectsWithoutModule());
	}
	
	private List<SystemObject> getListManagerSourceCustomCode(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getCustomCodes()
				: customCodeService.findObjectsWithoutModule());
	}
	
	private List<SystemObject> getListManagerSourceCustomLib(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getCustomLibs()
				: customLibService.findObjectsWithoutModule());
	}
	
	private List<SystemObject> getListManagerSourceUserGroup(int listNum) {
		return MiscUtils.castList(listNum == LIST_SELECTED
				? getObject().getUserGroups()
				: userGroupService.findNonSystemGroupsWithoutModule());
	}
	
	private List<ApplicationEntity> getChangedObjects() {
		final List<ApplicationEntity> changedObjects = new ArrayList<>();
		collectChangedObjects(changedObjects, DBOBJECTS);
		collectChangedObjects(changedObjects, DATASOURCES);
		collectChangedObjects(changedObjects, ENTITIES);
		collectChangedObjects(changedObjects, FILTERS);
		collectChangedObjects(changedObjects, TRANSFORMERS);
		collectChangedObjects(changedObjects, TRANSFERS);
		collectChangedObjects(changedObjects, FORMS);
		collectChangedObjects(changedObjects, MENUS);
		collectChangedObjects(changedObjects, TASKS);
		collectChangedObjects(changedObjects, REPORTS);
		collectChangedObjects(changedObjects, RESTS);
		collectChangedObjects(changedObjects, CUSTOMCODES);
		collectChangedObjects(changedObjects, CUSTOMLIBS);
		collectChangedObjects(changedObjects, USERGROUPS);
		return changedObjects;
	}
	
	private void collectChangedObjects(List<ApplicationEntity> changedObjects, String key) {
		ApplicationEntity entity;
		for (SystemObject object : getListManagerList(key, LIST_AVAILABLE)) {
			entity = (ApplicationEntity) object;
			if (entity.getModule() != null) {
				changedObjects.add(entity);
			}
		}
		for (SystemObject object : getListManagerList(key, LIST_SELECTED)) {
			entity = (ApplicationEntity) object;
			if (entity.getModule() == null) {
				changedObjects.add(entity);
			}
		}
	}

}
