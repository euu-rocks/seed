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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.ModuleMetadata;
import org.seed.core.application.module.ModuleParameter;
import org.seed.core.application.module.ModuleService;
import org.seed.core.customcode.CustomCodeService;
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
import org.seed.core.report.ReportService;
import org.seed.core.task.TaskService;
import org.seed.core.user.Authorisation;
import org.seed.core.user.UserGroupService;

import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.util.Assert;
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
	private static final String CUSTOMCODES = "customcodes";
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
	
	@WireVariable(value="reportServiceImpl")
	private ReportService reportService;
	
	@WireVariable(value="customCodeServiceImpl")
	private CustomCodeService customCodeService;
	
	@WireVariable(value="userGroupServiceImpl")
	private UserGroupService groupService;
	
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
	private boolean existCustomCodes;
	private boolean existUserGroups;
	
	public AdminModuleViewModel() {
		super(Authorisation.ADMIN_MODULE, "module",
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
	
	public boolean existCustomCodes() {
		return existCustomCodes;
	}
	
	public boolean existNonSystemUserGroups() {
		return existUserGroups;
	}
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam("param") Object object) {
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
		existCustomCodes = customCodeService.existObjects();
		existUserGroups = !groupService.findNonSystemGroups().isEmpty();
	}
	
	@Command
	@NotifyChange("parameter")
	public void newParameter() {
		parameter = moduleService.createParameter(getObject());
		notifyObjectChange(PARAMETERS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("parameter")
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
	public void deleteModule(@BindingParam("elem") Component component) {
		cmdDeleteObject(component);
	}
	
	@Command
	public void saveModule(@BindingParam("elem") Component component) {
		((ModuleMetadata)getObject()).setChangedObjects(getChangedObjects());
		cmdSaveObject(component);
		cmdRefresh();
	}
	
	@Command
	public void exportModule(@BindingParam("elem") Component elem) {
		Filedownload.save(moduleService.exportModule(getObject()),
						  "application/xml",
						  getObject().getName() + ".module");
	}
	
	@Command
	public void analyzeModule(@BindingParam("elem") Component elem,
			 				  @ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event) {
		try (InputStream inputStream = getMediaStream(event.getMedia(), Charset.forName("UTF-8"))) {
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
		Assert.notNull(module, "module is null");
		
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
		super.flagDirty(notify, notifyObject);
	}
	
	@Command
	public void dropToList(@BindingParam("key") String key, 
						   @BindingParam("list") int listNum, 
						   @BindingParam("item") SystemObject item) {
		super.dropToList(key, listNum, item);
	}
	
	@Command
	public void insertToList(@BindingParam("key") String key,
							 @BindingParam("list") int listNum,
							 @BindingParam("base") SystemObject base,
							 @BindingParam("item") SystemObject item) {
		super.insertToList(key, listNum, base, item);
	}
	
	@GlobalCommand
	public void _refreshObject(@BindingParam("param") Long objectId) {
		refreshObject(objectId);
	}
	
	@Override
	protected ModuleService getObjectService() {
		return moduleService;
	}

	@Override
	protected List<? extends SystemObject> getListManagerSource(String key, int listNum) {
		switch (key) {
			case DBOBJECTS:
				return listNum == LIST_SELECTED
						? getObject().getDBObjects()
						: dbObjectService.findObjectsWithoutModule();
			case DATASOURCES:
				return listNum == LIST_SELECTED
						? getObject().getDataSources()
						: dataSourceService.findObjectsWithoutModule();
			case ENTITIES:
				return listNum == LIST_SELECTED
						? getObject().getEntities()
						: entityService.findObjectsWithoutModule();
			case FILTERS:
				return listNum == LIST_SELECTED
						? getObject().getFilters()
						: filterService.findObjectsWithoutModule();
			case TRANSFORMERS:
				return listNum == LIST_SELECTED
						? getObject().getTransformers()
						: transformerService.findObjectsWithoutModule();
			case TRANSFERS:
				return listNum == LIST_SELECTED
						? getObject().getTransfers()
						: transferService.findObjectsWithoutModule();
			case FORMS:
				return listNum == LIST_SELECTED
						? getObject().getForms()
						: formService.findObjectsWithoutModule();
			case MENUS:
				return listNum == LIST_SELECTED
						? getObject().getMenus()
						: menuService.findObjectsWithoutModule();
			case TASKS:
				return listNum == LIST_SELECTED
						? getObject().getTasks()
						: taskService.findObjectsWithoutModule();
			case REPORTS:
				return listNum == LIST_SELECTED
						? getObject().getReports()
						: reportService.findObjectsWithoutModule();
			case CUSTOMCODES:
				return listNum == LIST_SELECTED
						? getObject().getCustomCodes()
						: customCodeService.findObjectsWithoutModule();
			case USERGROUPS:
				return listNum == LIST_SELECTED
						? getObject().getUserGroups()
						: groupService.findNonSystemGroupsWithoutModule();
			default:
				throw new IllegalStateException("unknown list manager key: " + key);
		}
	}

	@Override
	protected void resetProperties() {
		parameter = null;
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
		collectChangedObjects(changedObjects, CUSTOMCODES);
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
