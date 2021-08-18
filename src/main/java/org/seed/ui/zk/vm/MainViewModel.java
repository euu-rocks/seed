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

import org.seed.C;
import org.seed.Seed;
import org.seed.core.application.setting.Setting;
import org.seed.core.form.Form;
import org.seed.core.form.FormService;
import org.seed.core.task.TaskService;
import org.seed.core.util.Assert;
import org.seed.ui.FormParameter;
import org.seed.ui.MenuManager;
import org.seed.ui.Tab;
import org.seed.ui.TabParameterMap;
import org.seed.ui.TreeNode;
import org.seed.ui.ViewParameterMap;
import org.seed.ui.zk.TreeModel;

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
	
	@WireVariable(value="formServiceImpl")
	private FormService formService;
	
	@WireVariable(value="taskServiceImpl")
	private TaskService taskService;
	
	@WireVariable(value="menuManager")
	private MenuManager menuManager;
	
	@WireVariable(value="seed")
	private Seed seed;
	
	private final List<String> openNodes = new ArrayList<>();
	
	private final List<Tab> tabs = new ArrayList<>();
	
	private TreeNode selectedNode;
	
	private Tab selectedTab;
	
	private Tab popupTab;
	
	public String getApplicationName() {
		final String appName = getSettingOrNull(Setting.APPLICATION_NAME);
		if (appName != null) {
			return appName;
		}
		final String seedVersion = seed.getVersion();
		if (seedVersion != null) {
			return "seed " + seedVersion;
		}
		return "seed";
	}
	
	public boolean isMenuMode(String mode) {
		return getSetting(Setting.MENU_MODE).equals(mode);
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
		return menuManager.getMenuList(getUser(), 
									   !getUserReports().isEmpty(), 
									   !taskService.getTasks(getUser()).isEmpty(), 
									   isFullTextSearchAvailable());
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
			globalCommand("globalRefreshObject", selectedTab.getObjectId());
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
		if (!isDoubleClick("nodeClicked") && selectedNode != null) {
			if (selectedNode.isLink()) {
				redirect(selectedNode.viewName);
			}
			else if (MenuManager.REDIRECT_LOGOUT.equals(selectedNode.viewName)) {
				confirm("question.logout", null, MenuManager.REDIRECT_LOGOUT);
			}
			else if (selectedNode.viewName != null) {
				FormParameter param = null;
				if (selectedNode.formId != null) {
					final Form form = formService.getObject(selectedNode.formId);
					param = new FormParameter(form);
				}
				globalOpenTab(selectedNode.label, selectedNode.viewName, selectedNode.iconClass, param);
			}
		}
	}
	
	@Override
	protected void confirmed(boolean confirmed, Component elem, Object confirmParam) {
		if (confirmed && MenuManager.REDIRECT_LOGOUT.equals(confirmParam)) {
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
	public void globalRefreshMenu() {
		notifyChange("isMenuMode", "menuTree", "menuList");
	}
	
	@GlobalCommand
	public void globalOpenTab(@BindingParam(TabParameterMap.NAME) String name, 
							  @BindingParam(TabParameterMap.VIEW) String view, 
							  @BindingParam(TabParameterMap.ICON) String icon, 
							  @BindingParam(TabParameterMap.PARAMETER) FormParameter parameter) {
		Assert.notNull(name, C.NAME);
		Assert.notNull(view, "view");
		
		if (MenuManager.URL_FULLTEXTSEARCH.equals(view)) {
			selectedTab = new Tab(name, ZUL_PATH + view, icon);
			selectedTab.setParameter(selectedTab);
		}
		else {
			selectedTab = new Tab(name, ZUL_PATH + view, icon, parameter);
		}
		if (parameter != null) {
			parameter.setTab(selectedTab);
			selectedTab.setObjectId(parameter.getObjectId());
		}
		tabs.add(selectedTab);
		notifyChange("tabs", "selectedTab");
	}
	
	@GlobalCommand
	public void globalShowView(@BindingParam(ViewParameterMap.VIEW) String view,
							   @BindingParam(ViewParameterMap.PARAM) Object param) {
		Assert.notNull(view, "view");
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
