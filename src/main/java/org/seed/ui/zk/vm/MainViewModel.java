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

import org.seed.core.application.setting.ApplicationSettingService;
import org.seed.core.application.setting.Setting;
import org.seed.core.form.Form;
import org.seed.core.form.FormService;
import org.seed.core.form.navigation.Menu;
import org.seed.core.form.navigation.MenuService;
import org.seed.core.report.ReportService;
import org.seed.core.task.TaskService;
import org.seed.core.user.Authorisation;
import org.seed.ui.FormParameter;
import org.seed.ui.Tab;
import org.seed.ui.TabParameterMap;
import org.seed.ui.TreeNode;
import org.seed.ui.ViewParameterMap;
import org.seed.ui.zk.TreeModel;

import org.springframework.util.Assert;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;

public class MainViewModel extends AbstractApplicationViewModel {
	
	private static final String REDIRECT_LOGOUT = "/logout";
	
	@WireVariable(value="applicationSettingServiceImpl")
	private ApplicationSettingService settingService;
	
	@WireVariable(value="menuServiceImpl")
	private MenuService menuService;
	
	@WireVariable(value="formServiceImpl")
	private FormService formService;
	
	@WireVariable(value="taskServiceImpl")
	private TaskService taskService;
	
	@WireVariable(value="reportServiceImpl")
	private ReportService reportService;
	
	private final List<String> openNodes = new ArrayList<>();
	
	private final List<Tab> tabs = new ArrayList<>();
	
	private TreeNode selectedNode;
	
	private Tab selectedTab;
	
	private Tab popupTab;
	
	public String getApplicationName() {
		final String appName = settingService.getSettingOrNull(Setting.APPLICATION_NAME);
		return appName != null ? appName : "Seed";
	}
	
	public boolean isMenuMode(String mode) {
		return settingService.getSetting(Setting.MENU_MODE).equals(mode);
	}
	
	public List<Tab> getTabs() {
		return Collections.unmodifiableList(tabs);
	}
	
	public Tab getSelectedTab() {
		return selectedTab;
	}
	
	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}
	
	public boolean isNodeOpen(TreeNode node) {
		return openNodes.contains(node.label);
	}

	public List<TreeNode> getMenuList() {
		final List<TreeNode> result = new ArrayList<>();
		
		// admin
		if (getUser().hasAdminAuthorisations()) {
			final TreeNode nodeAdmin = new TreeNode(getLabel("label.administration"), null, null);
			nodeAdmin.setTop(true);
			result.add(nodeAdmin);
			
			if (getUser().isAuthorised(Authorisation.ADMIN_ENTITY)) {
				final TreeNode nodeEntities = nodeAdmin.addChild(new TreeNode(getLabel("label.entities"), 
						  												"/admin/entity/entitylist.zul", 
						  												"z-icon-table z-icon-fw"));
				nodeEntities.addChild(new TreeNode(getLabel("label.filter"), 
											"/admin/filter/filterlist.zul", 
											"z-icon-filter z-icon-fw"));
				nodeEntities.addChild(new TreeNode(getLabel("label.transfers"), 
											"/admin/transfer/transferlist.zul", 
											"z-icon-exchange z-icon-fw"));
				nodeEntities.addChild(new TreeNode(getLabel("label.transformers"), 
											"/admin/transform/transformerlist.zul", 
											"z-icon-random z-icon-fw"));
			}
			if (getUser().isAuthorised(Authorisation.ADMIN_FORM)) {
				nodeAdmin.addChild(new TreeNode(getLabel("label.forms"), 
											"/admin/form/formlist.zul", 
											"z-icon-list-alt z-icon-fw"));
			}
			if (getUser().isAuthorised(Authorisation.ADMIN_MENU)) {
				nodeAdmin.addChild(new TreeNode(getLabel("label.menus"), 
											"/admin/menu/menulist.zul", 
											"z-icon-navicon z-icon-fw"));
			}
			if (getUser().isAuthorised(Authorisation.ADMIN_JOB)) {
				nodeAdmin.addChild(new TreeNode(getLabel("label.jobs"), 
											"/admin/task/tasklist.zul", 
											"z-icon-cog z-icon-fw"));
			}
			if (getUser().isAuthorised(Authorisation.ADMIN_DBOBJECT)) {
				nodeAdmin.addChild(new TreeNode(getLabel("label.dbobjects"), 
											"/admin/dbobject/dbobjectlist.zul", 
											"z-icon-database z-icon-fw"));
			}
			if (getUser().isAuthorised(Authorisation.ADMIN_DATASOURCE)) {
				nodeAdmin.addChild(new TreeNode(getLabel("label.datasources"), 
											"/admin/datasource/datasourcelist.zul", 
											"z-icon-share-alt z-icon-fw"));
			}
			if (getUser().isAuthorised(Authorisation.ADMIN_REPORT)) {
				nodeAdmin.addChild(new TreeNode(getLabel("label.reports"), 
											"/admin/report/reportlist.zul", 
											"z-icon-book z-icon-fw"));
			}
			if (getUser().isAuthorised(Authorisation.ADMIN_SOURCECODE)) {
				nodeAdmin.addChild(new TreeNode(getLabel("label.customcode"), 
											"/admin/customcode/customcodelist.zul", 
											"z-icon-code z-icon-fw"));
			}
			if (getUser().isAuthorised(Authorisation.ADMIN_USER)) {
				final TreeNode nodeUsers = nodeAdmin.addChild(new TreeNode(getLabel("label.user"), 
																	"/admin/user/userlist.zul", 
																	"z-icon-user z-icon-fw"));
				nodeUsers.addChild(new TreeNode(getLabel("label.usergroups"), 
											"/admin/user/usergrouplist.zul", 
											"z-icon-users z-icon-fw"));
			}
			if (getUser().isAuthorised(Authorisation.ADMIN_MODULE)) {
				nodeAdmin.addChild(new TreeNode(getLabel("label.modules"), 
											"/admin/module/modulelist.zul", 
											"z-icon-cube z-icon-fw"));
			}
			if (getUser().isAuthorised(Authorisation.ADMIN_SETTINGS)) {
				nodeAdmin.addChild(new TreeNode(getLabel("label.systemsettings"), 
											"/admin/setting/settings.zul", 
											"z-icon-wrench z-icon-fw"));
			}
		}
		
		// user menus
		for (Menu menu : menuService.getMenus(getUser())) {
			final TreeNode menuNode = menu.getForm() != null 
										? new TreeNode(menu.getName(), 
													   "/form/listform.zul", 
													   menu.getIcon(),
													   menu.getForm().getId())
										: new TreeNode(menu.getName(), null, 
													   menu.getIcon());
			menuNode.setTop(true);
			result.add(menuNode);
			if (menu.hasSubMenus()) {
				for (Menu subMenu : menu.getSubMenus()) {
					menuNode.addChild(new TreeNode(subMenu.getName(), 
												   "/form/listform.zul", 
												   subMenu.getIcon(), 
												   subMenu.getForm().getId()));
				}
			}
		}
		
		// jobs
		if (getUser().isAuthorised(Authorisation.RUN_JOBS) && 
			!taskService.getTasks(getUser()).isEmpty()) {
			result.add(new TreeNode(getLabel("label.runjobs"), 
									"/task/tasklist.zul", 
									"z-icon-cogs z-icon-fw"));
		}
		
		// reports
		if (getUser().isAuthorised(Authorisation.PRINT_REPORTS) &&
			!reportService.getReports(getUser()).isEmpty()) {
			result.add(new TreeNode(getLabel("label.reports"), 
									"/report/reportlist.zul", 
									"z-icon-book z-icon-fw"));
		}
		
		// account
		final TreeNode nodeAccount = new TreeNode(getUserName(), 
				 								 null, 
				 								 "z-icon-user z-icon-fw");
		nodeAccount.addChild(new TreeNode(getLabel("label.logout"), 
										  REDIRECT_LOGOUT, 
				 						  "z-icon-sign-out z-icon-fw"));
		result.add(nodeAccount);
		return result;
	}
	
	public TreeModel getMenuTree() {
		final TreeNode root = new TreeNode("root", null, null);
		for (TreeNode menuNode : getMenuList()) {
			root.addChild(menuNode);
		}
		return new TreeModel(root);
	}
	
	@Init
    public void init() {
		Clients.confirmClose(getLabel("question.quit"));
		openNodes.add(getLabel("label.administration"));
		openNodes.add(getLabel("label.defaultmenu"));
		openNodes.add(getUserName());
	}
	
	@Command // called on open and close
	public void openMenu(@BindingParam("node") TreeNode node) {
		if (!openNodes.remove(node.label)) {
			openNodes.add(node.label);
		}
	}
	
	@Command
	@NotifyChange("selectedTab")
	public void selectTab(@BindingParam("tab") Tab tab) {
		selectedTab = tab;
		if (selectedTab.getObjectId() != null) {
			globalCommand("_refreshObject", selectedTab.getObjectId());
		}
	}
	
	@Command
	@NotifyChange({"tabs", "selectedTab"})
	public void dropTab(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent event) {
		Collections.swap(tabs, getChildIndex(event.getDragged()), 
							   getChildIndex(event.getTarget()));
	}
	
	@Command
	public void closeTab(@BindingParam("tab") Tab tab) {
		removeTab(tab);
	}
	
	@Command
	@NotifyChange({"tabs", "selectedTab"})
	public void closePopupTab() {
		removeTab(popupTab);
	}
	
	@Command
	@NotifyChange({"tabs", "selectedTab"})
	public void closeAllTabs() {
		tabs.clear();
		selectedTab = null;
	}
	
	@Command
	@NotifyChange({"tabs", "selectedTab"})
	public void closeOtherTabs() {
		tabs.removeIf(t -> !t.equals(popupTab));
		selectedTab = popupTab;
	}
	
	@Command
	public void openTabPopup(@ContextParam(ContextType.TRIGGER_EVENT) OpenEvent event) {
		popupTab = event.isOpen() ? tabs.get(getChildIndex(event.getReference())) : null;
	}
	
	@Command
	public void menuClicked(@BindingParam("node") TreeNode node) {
		selectedNode = node;
		nodeClicked();
	}
	
	@Command
	public void nodeClicked() {
		if (selectedNode != null) {
			if (selectedNode.isLink()) {
				redirect(selectedNode.viewName);
			}
			else if (REDIRECT_LOGOUT.equals(selectedNode.viewName)) {
				confirm("question.logout", null, REDIRECT_LOGOUT);
			}
			else if (selectedNode.viewName != null) {
				FormParameter param = null;
				if (selectedNode.formId != null) {
					final Form form = formService.getObject(selectedNode.formId);
					param = new FormParameter(form);
				}
				_openTab(selectedNode.label, selectedNode.viewName, selectedNode.iconClass, param);
			}
		}
	}
	
	@Override
	protected void confirmed(boolean confirmed, Component elem, Object confirmParam) {
		if (confirmed && REDIRECT_LOGOUT.equals(confirmParam)) {
			Clients.confirmClose(null);
			logout();
		}
	}
	
	private void removeTab(Tab tab) {
		tabs.remove(tab);
		if (tabs.isEmpty()) {
			selectedTab = null;
		}
	}
	
	// ------ global commands ---------------------------------------
	
	@GlobalCommand
	public void _refreshMenu() {
		notifyChange("isMenuMode", "menuTree", "menuList");
	}
	
	@GlobalCommand
	public void _openTab(@BindingParam(TabParameterMap.NAME) String name, 
						 @BindingParam(TabParameterMap.VIEW) String view, 
						 @BindingParam(TabParameterMap.ICON) String icon, 
						 @BindingParam(TabParameterMap.PARAMETER) FormParameter parameter) {
		Assert.notNull(name, "name is null");
		Assert.notNull(view, "view is null");
		
		selectedTab = new Tab(name, ZUL_PATH + view, icon, parameter);
		selectedTab.setObjectId(parameter != null ? parameter.getObjectId() : null);
		tabs.add(selectedTab);
		notifyChange("tabs", "selectedTab");
	}
	
	@GlobalCommand
	public void _showView(@BindingParam(ViewParameterMap.VIEW) String view,
						  @BindingParam(ViewParameterMap.PARAM) Object param) {
		Assert.notNull(view, "view is null");
		Assert.state(selectedTab != null, "no tab selected");
		
		if (param instanceof Long) {
			selectedTab.setObjectId((Long) param);
		}
		else {
			selectedTab.resetName();
		}
		
		selectedTab.setPath(ZUL_PATH + view);
		selectedTab.setParameter(param);
		notifyObjectChange(selectedTab, "name", "parameter", "path");
	}
	
}
