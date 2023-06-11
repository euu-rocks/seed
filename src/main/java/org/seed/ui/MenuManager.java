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

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import org.seed.C;
import org.seed.LabelProvider;
import org.seed.core.form.navigation.Menu;
import org.seed.core.form.navigation.MenuService;
import org.seed.core.user.Authorisation;
import org.seed.core.user.User;
import org.seed.core.util.Assert;
import org.seed.core.util.NameUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

@Component
public class MenuManager {
	
	public static final String REDIRECT_LOGOUT		= "/logout";
	
	public static final String URL_FULLTEXTSEARCH 	= "/form/fulltextsearch.zul";
	public static final String URL_LISTFORM 		= "/form/listform.zul";
	
	public static final String NODE_LOGOUT			= "logout";
	public static final String NODE_SYSTEMINFO		= "systeminfo";
	
	@Autowired
	private LabelProvider labelProvider;
	
	@Autowired
	private MenuService menuService;
	
	public String getDefaultMenuName() {
		return menuService.getDefaultMenuName();
	}
	
	@Secured("ROLE_LOGIN")
	public List<TreeNode> getMenuList(User user, Session session,
									  boolean reportsExist, boolean tasksExist, 
									  boolean transfersExist, boolean fullTextSearchAvailable) {
		Assert.notNull(user, C.USER);
		Assert.notNull(session, C.SESSION);
		final List<TreeNode> menuList = new ArrayList<>();
		
		// admin
		if (user.hasAdminAuthorisations()) {
			menuList.add(createAdminMenu(user));
		}
		
		// jobs
		if (user.isAuthorised(Authorisation.RUN_JOBS) && tasksExist) {
			menuList.add(createNode("label.runjobs", 
									"/task/tasklist.zul", 
									"z-icon-cog"));
		}
		
		// reports
		if (user.isAuthorised(Authorisation.PRINT_REPORTS) && reportsExist) {
			menuList.add(createNode("label.runreport", 
									"/report/reportlist.zul", 
									"z-icon-book"));
		}
		
		// transfers
		if (user.isAuthorised(Authorisation.RUN_IMPORT_EXPORT) && transfersExist) {
			menuList.add(createNode("label.runtransfer", 
									"/transfer/transferlist.zul", 
									"z-icon-exchange"));
		}
		
		// user menus
		menuList.addAll(getMenusForUser(user, session));
		
		// full-text search
		if (fullTextSearchAvailable &&
			user.isAuthorised(Authorisation.SEARCH_FULLTEXT)) {
			menuList.add(createNode("label.fulltextsearch", 
									URL_FULLTEXTSEARCH, 
									"z-icon-search"));
		}
		
		// account
		menuList.add(createAccountMenu(user));
		return menuList;
	}
	
	private List<TreeNode> getMenusForUser(User user, Session session) {
		final List<TreeNode> menuList = new ArrayList<>();
		for (Menu menu : menuService.getMenus(user, session)) {
			final TreeNode menuRoot = createMenuTree(menu, menuList, user);
			if (findNode(menuList, menuRoot.getLabel()) == null) {
				menuRoot.setTop(true);
				menuList.add(menuRoot);
			}
		}
		return menuList;
	}
	
	private TreeNode createAdminMenu(User user) {
		final TreeNode nodeAdmin = new TreeNode(getLabel("label.administration"));
		nodeAdmin.setTop(true);
		
		if (user.isAuthorised(Authorisation.ADMIN_ENTITY)) {
			createEntityMenu(nodeAdmin);
		}
		if (user.isAuthorised(Authorisation.ADMIN_FORM)) {
			nodeAdmin.addChild(createNode("label.forms", 
										  "/admin/form/formlist.zul", 
										  "z-icon-list-alt"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_MENU)) {
			nodeAdmin.addChild(createNode("label.menus", 
										  "/admin/menu/menulist.zul", 
										  "z-icon-navicon"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_JOB)) {
			nodeAdmin.addChild(createNode("label.jobs", 
										  "/admin/task/tasklist.zul", 
										  "z-icon-cogs"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_DBOBJECT)) {
			nodeAdmin.addChild(createNode("label.dbobjects", 
										  "/admin/dbobject/dbobjectlist.zul", 
										  "z-icon-database"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_DATASOURCE)) {
			nodeAdmin.addChild(createNode("label.datasources", 
										  "/admin/datasource/datasourcelist.zul", 
										  "z-icon-share-alt"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_REPORT)) {
			nodeAdmin.addChild(createNode("label.reports", 
										  "/admin/report/reportlist.zul", 
										  "z-icon-book"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_SOURCECODE)) {
			createCustomCodeMenu(nodeAdmin);
		}
		if (user.isAuthorised(Authorisation.ADMIN_REST)) {
			nodeAdmin.addChild(createNode("label.restservice", 
										  "/admin/rest/restlist.zul", 
										  "z-icon-server"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_MODULE)) {
			nodeAdmin.addChild(createNode("label.modules", 
										  "/admin/module/modulelist.zul", 
										  "z-icon-cubes"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_SETTINGS)) {
			nodeAdmin.addChild(createNode("label.settings", 
										  "/admin/setting/settings.zul", 
										  "z-icon-wrench"));
		}
		if (user.isAuthorised(Authorisation.SYSTEMINFO)) {
			final TreeNode nodeInfo = createNode("user.authorisation.systeminfo", 
					  					  "/admin/systeminfo/systeminfo.zul", 
										  "z-icon-info");
			nodeInfo.setPurpose(NODE_SYSTEMINFO);
			nodeAdmin.addChild(nodeInfo);
		}
		if (user.isAuthorised(Authorisation.SYSTEMTASK)) {
			nodeAdmin.addChild(createNode("label.systemtasks",
										  "/admin/systemtask/systemtasklist.zul", 
										  "z-icon-gavel"));
		}
		if (user.isAuthorised(Authorisation.ADMIN_USER)) {
			createUserMenu(nodeAdmin);
		}
		return nodeAdmin;
	}
	
	private void createEntityMenu(TreeNode nodeAdmin) {
		final TreeNode nodeEntities = nodeAdmin.addChild(createNode("label.entities", 
										 "/admin/entity/entitylist.zul", 
										 "z-icon-table"));
		nodeEntities.addChild(createNode("label.filters", 
										 "/admin/filter/filterlist.zul", 
										 "z-icon-filter"));
		nodeEntities.addChild(createNode("label.transfer", 
										 "/admin/transfer/transferlist.zul", 
										 "z-icon-exchange"));
		nodeEntities.addChild(createNode("label.transformers", 
										 "/admin/transform/transformerlist.zul", 
										 "z-icon-random"));
	}
	
	private void createCustomCodeMenu(TreeNode nodeAdmin) {
		final TreeNode nodeCustomCode = nodeAdmin.addChild(createNode("label.customcode", 
										 "/admin/customcode/customcodelist.zul", 
										 "z-icon-code"));
		nodeCustomCode.addChild(createNode("label.customlibs", 
										   "/admin/customcode/customliblist.zul", 
										   "z-icon-file-archive-o"));
	}
	
	private void createUserMenu(TreeNode nodeAdmin) {
		final TreeNode nodeUsers = nodeAdmin.addChild(createNode("label.users", 
										 "/admin/user/userlist.zul", 
										 "z-icon-user"));
		nodeUsers.addChild(createNode("label.usergroups", 
									  "/admin/user/usergrouplist.zul", 
									  "z-icon-users"));
	}
	
	private TreeNode createAccountMenu(User user) {
		final TreeNode nodeAccount = new TreeNode(user.getName(), null, "z-icon-user");
		final TreeNode nodeLogout = createNode("label.logout", REDIRECT_LOGOUT, "z-icon-sign-out"); 
		nodeLogout.setPurpose(NODE_LOGOUT);
		nodeAccount.addChild(nodeLogout);
		return nodeAccount;
	}
	
	private TreeNode createNode(String labelKey, String viewName, String icon) {
		return new TreeNode(getLabel(labelKey), viewName, icon != null 
															? icon.concat(" z-icon-fw alpha-icon-lg") 
															: null);
	}
	
	private String getLabel(String key, String ...params) {
		return labelProvider.getLabel(key, params);
	}
	
	private static TreeNode createMenuTree(Menu menu, List<TreeNode> nodeList, User user) {
		TreeNode rootNode = null;
		final String[] nameParts = NameUtils.splitAndTrim(menu.getName(), "/");
		if (nameParts.length > 1) {
			// first part
			rootNode = findNode(nodeList, nameParts[0]);
			if (rootNode == null) {
				rootNode = new TreeNode(nameParts[0]);
			}
			
			// all parts between first and last part
			TreeNode parentNode = rootNode;
			for (int i = 1; i < nameParts.length - 1; i++) {
				TreeNode node = parentNode.findChild(nameParts[i]);
				if (node == null) {
					node = new TreeNode(nameParts[i]);
					parentNode.addChild(node);
				}
				parentNode = node;
			}
			
			// last part
			parentNode.addChild(createMenuNode(menu, nameParts[nameParts.length - 1], user));
		}
		else {
			rootNode = createMenuNode(menu, menu.getName(), user);
		}
		return rootNode;
	}
	
	private static TreeNode createMenuNode(Menu menu, String label, User user) {
		final TreeNode menuNode = menu.getForm() != null 
				? new TreeNode(label, URL_LISTFORM, menu.getIcon(), menu.getForm().getId())
				: new TreeNode(label, null, menu.getIcon());
		filterAndForEach(menu.getSubMenus(), 
						 subMenu -> subMenu.getForm().getEntity().checkPermissions(user), 
						 subMenu -> menuNode.addChild(
									new TreeNode(subMenu.getName(), URL_LISTFORM, 
												 subMenu.getIcon(), subMenu.getForm().getId())));	
		return menuNode;
	}
	
	private static TreeNode findNode(List<TreeNode> nodeList, String name) {
		return firstMatch(nodeList, node -> name.equalsIgnoreCase(node.getLabel()));
	}

}
