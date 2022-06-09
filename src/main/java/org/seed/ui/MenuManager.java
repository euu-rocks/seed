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
package org.seed.ui;

import java.util.ArrayList;
import java.util.List;

import org.seed.C;
import org.seed.LabelProvider;
import org.seed.core.form.navigation.Menu;
import org.seed.core.form.navigation.MenuService;
import org.seed.core.user.Authorisation;
import org.seed.core.user.User;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MenuManager {
	
	public static final String REDIRECT_LOGOUT = "/logout";
	
	public static final String URL_FULLTEXTSEARCH = "/form/fulltextsearch.zul";
	
	@Autowired
	private LabelProvider labelProvider;
	
	@Autowired
	private MenuService menuService;
	
	public String getDefaultMenuName() {
		return menuService.getDefaultMenuName();
	}
	
	public List<TreeNode> getMenuList(User user, boolean reportsExist, boolean tasksExist, 
									  boolean fullTextSearchAvailable) {
		Assert.notNull(user, C.USER);
		final List<TreeNode> menuList = new ArrayList<>();
		
		// admin
		if (user.hasAdminAuthorisations()) {
			menuList.add(createAdminMenu(user));
		}
		
		// jobs
		if (user.isAuthorised(Authorisation.RUN_JOBS) && tasksExist) {
			menuList.add(new TreeNode(getLabel("label.runjobs"), 
									"/task/tasklist.zul", 
									"z-icon-cogs z-icon-fw alpha-icon-lg"));
		}
		
		// reports
		if (user.isAuthorised(Authorisation.PRINT_REPORTS) && reportsExist) {
			menuList.add(new TreeNode(getLabel("label.reports"), 
									"/report/reportlist.zul", 
									"z-icon-book z-icon-fw alpha-icon-lg"));
		}
		
		// user menus
		menuList.addAll(getMenusForUser(user));
		
		// full-text search
		if (fullTextSearchAvailable &&
			user.isAuthorised(Authorisation.SEARCH_FULLTEXT)) {
			menuList.add(new TreeNode(getLabel("label.fulltextsearch"), 
									URL_FULLTEXTSEARCH, 
									"z-icon-search z-icon-fw alpha-icon-lg"));
		}
		
		// account
		menuList.add(createAccountMenu(user));
		return menuList;
	}
	
	private TreeNode createAdminMenu(User user) {
		final TreeNode nodeAdmin = new TreeNode(labelProvider.getLabel("label.administration"), null, null);
		nodeAdmin.setTop(true);
		
		if (user.isAuthorised(Authorisation.ADMIN_ENTITY)) {
			createEntityMenu(nodeAdmin);
		}
		if (user.isAuthorised(Authorisation.ADMIN_FORM)) {
			nodeAdmin.addChild(new TreeNode(getLabel("label.forms"), 
										"/admin/form/formlist.zul", 
										"z-icon-list-alt z-icon-fw alpha-icon-lg"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_MENU)) {
			nodeAdmin.addChild(new TreeNode(getLabel("label.menus"), 
										"/admin/menu/menulist.zul", 
										"z-icon-navicon z-icon-fw alpha-icon-lg"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_JOB)) {
			nodeAdmin.addChild(new TreeNode(getLabel("label.jobs"), 
										"/admin/task/tasklist.zul", 
										"z-icon-cog z-icon-fw alpha-icon-lg"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_DBOBJECT)) {
			nodeAdmin.addChild(new TreeNode(getLabel("label.dbobjects"), 
										"/admin/dbobject/dbobjectlist.zul", 
										"z-icon-database z-icon-fw alpha-icon-lg"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_DATASOURCE)) {
			nodeAdmin.addChild(new TreeNode(getLabel("label.datasources"), 
										"/admin/datasource/datasourcelist.zul", 
										"z-icon-share-alt z-icon-fw alpha-icon-lg"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_REPORT)) {
			nodeAdmin.addChild(new TreeNode(getLabel("label.reports"), 
										"/admin/report/reportlist.zul", 
										"z-icon-book z-icon-fw alpha-icon-lg"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_SOURCECODE)) {
			createCustomCodeMenu(nodeAdmin);
		}
		if (user.isAuthorised(Authorisation.ADMIN_REST)) {
			nodeAdmin.addChild(new TreeNode(getLabel("label.restservice"), 
										"/admin/rest/restlist.zul", 
										"z-icon-server z-icon-fw alpha-icon-lg"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_MODULE)) {
			nodeAdmin.addChild(new TreeNode(getLabel("label.modules"), 
										"/admin/module/modulelist.zul", 
										"z-icon-cube z-icon-fw alpha-icon-lg"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_SETTINGS)) {
			nodeAdmin.addChild(new TreeNode(getLabel("label.settings"), 
										"/admin/setting/settings.zul", 
										"z-icon-wrench z-icon-fw alpha-icon-lg"));
		}
		if (user.isAuthorised(Authorisation.SYSTEMINFO)) {
			nodeAdmin.addChild(new TreeNode(getLabel("user.authorisation.systeminfo"), 
										"/admin/systeminfo/systeminfo.zul", 
										"z-icon-info z-icon-fw alpha-icon-lg"));
		}
		if (user.isAuthorised(Authorisation.SYSTEMTASK)) {
			nodeAdmin.addChild(new TreeNode(getLabel("label.systemtasks"),
										"/admin/systemtask/systemtasklist.zul", 
										"z-icon-gavel z-icon-fw alpha-icon-lg"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_USER)) {
			createUserMenu(nodeAdmin);
		}
		return nodeAdmin;
	}
	
	private List<TreeNode> getMenusForUser(User user) {
		final List<TreeNode> menuList = new ArrayList<>();
		for (Menu menu : menuService.getMenus(user)) {
			final TreeNode menuNode = menu.getForm() != null 
										? new TreeNode(menu.getName(), 
													   "/form/listform.zul", 
													   menu.getIcon(),
													   menu.getForm().getId())
										: new TreeNode(menu.getName(), null, 
													   menu.getIcon());
			menuNode.setTop(true);
			menuList.add(menuNode);
			if (menu.hasSubMenus()) {
				for (Menu subMenu : menu.getSubMenus()) {
					menuNode.addChild(new TreeNode(subMenu.getName(), 
												   "/form/listform.zul", 
												   subMenu.getIcon(), 
												   subMenu.getForm().getId()));
				}
			}
		}
		return menuList;
	}
	
	private void createEntityMenu(TreeNode nodeAdmin) {
		final TreeNode nodeEntities = nodeAdmin.addChild(new TreeNode(getLabel("label.entities"), 
										 "/admin/entity/entitylist.zul", 
										 "z-icon-table z-icon-fw alpha-icon-lg"));
		nodeEntities.addChild(new TreeNode(getLabel("label.filters"), 
										 "/admin/filter/filterlist.zul", 
										 "z-icon-filter z-icon-fw alpha-icon-lg"));
		nodeEntities.addChild(new TreeNode(getLabel("label.transfer"), 
										 "/admin/transfer/transferlist.zul", 
										 "z-icon-exchange z-icon-fw alpha-icon-lg"));
		nodeEntities.addChild(new TreeNode(getLabel("label.transformers"), 
										 "/admin/transform/transformerlist.zul", 
										 "z-icon-random z-icon-fw alpha-icon-lg"));
	}
	
	private void createCustomCodeMenu(TreeNode nodeAdmin) {
		final TreeNode nodeCustomCode = nodeAdmin.addChild(new TreeNode(getLabel("label.customcode"), 
										"/admin/customcode/customcodelist.zul", 
										"z-icon-code z-icon-fw alpha-icon-lg"));
		nodeCustomCode.addChild(new TreeNode(getLabel("label.customlibs"), 
										"/admin/customcode/customliblist.zul", 
										"z-icon-file-archive-o z-icon-fw alpha-icon-lg"));
	}
	
	private void createUserMenu(TreeNode nodeAdmin) {
		final TreeNode nodeUsers = nodeAdmin.addChild(new TreeNode(getLabel("label.users"), 
										"/admin/user/userlist.zul", 
										"z-icon-user z-icon-fw alpha-icon-lg"));
		nodeUsers.addChild(new TreeNode(getLabel("label.usergroups"), 
										"/admin/user/usergrouplist.zul", 
										"z-icon-users z-icon-fw alpha-icon-lg"));
	}
	
	private TreeNode createAccountMenu(User user) {
		final TreeNode nodeAccount = new TreeNode(user.getName(), null, 
				 								  "z-icon-user z-icon-fw alpha-icon-lg");
		nodeAccount.addChild(new TreeNode(getLabel("label.logout"), 
										  REDIRECT_LOGOUT, 
										  "z-icon-sign-out z-icon-fw alpha-icon-lg"));
		return nodeAccount;
	}
	
	private String getLabel(String key, String ...params) {
		return labelProvider.getLabel(key, params);
	}

}
