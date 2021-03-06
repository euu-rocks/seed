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

import java.util.List;

import org.seed.core.data.SystemObject;
import org.seed.core.form.Form;
import org.seed.core.form.FormService;
import org.seed.core.form.navigation.Menu;
import org.seed.core.form.navigation.MenuMetadata;
import org.seed.core.form.navigation.MenuService;
import org.seed.core.user.Authorisation;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class AdminMenuViewModel extends AbstractAdminViewModel<Menu> {
	
	private static final String ENTRIES = "entries";
	private static final String SUBMENUS = "subMenus";
	
	@WireVariable(value="menuServiceImpl")
	private MenuService menuService;
	
	@WireVariable(value="formServiceImpl")
	private FormService formService;
	
	private Menu subMenu;
	
	public AdminMenuViewModel() {
		super(Authorisation.ADMIN_MENU, "menu",
			  "/admin/menu/menulist.zul", 
			  "/admin/menu/menu.zul");
	}
	
	@Init
	public void init(@ExecutionArgParam("param") Object object) {
		super.init(object, null);
	}
	
	@Override
	protected MenuService getObjectService() {
		return menuService;
	}
	
	@Override
	protected List<Menu> loadObjectList() {
		return menuService.getTopLevelMenus();
	}
	
	public Menu getSubMenu() {
		return subMenu;
	}

	public void setSubMenu(Menu subMenu) {
		this.subMenu = subMenu;
	}
	
	public List<Form> getForms() {
		return formService.findAllObjects();
	}

	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void newMenu() {
		cmdNewObject();
	}
	
	@Command
	public void editMenu() {
		cmdEditObject();
	}
	
	@Command
	public void refreshMenu(@BindingParam("elem") Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteMenu(@BindingParam("elem") Component component) {
		cmdDeleteObject(component);
	}
	
	@Command
	public void saveMenu(@BindingParam("elem") Component elem) {
		cmdSaveObject(elem);
		refreshMenu();
	}
	
	@Command
	@NotifyChange("subMenu")
	public void newEntry() {
		subMenu = new MenuMetadata();
		getObject().addSubMenu(subMenu);
		flagDirty();
		notifyObjectChange(SUBMENUS);
	}
	
	@Command
	@NotifyChange("subMenu")
	public void removeEntry(@BindingParam("elem") Component component) {
		getObject().removeSubMenu(subMenu);
		subMenu = null;
		flagDirty();
		notifyObjectChange(SUBMENUS);
	}
	
	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, notifyObject);
	}
	
	@Command
	public void swapEntries(@BindingParam("base") Menu base, 
							@BindingParam("item") Menu item) {
		swapItems(ENTRIES, base, item);
	}
	
	@GlobalCommand
	public void _refreshObject(@BindingParam("param") Long objectId) {
		refreshObject(objectId);
	}

	@Override
	protected List<? extends SystemObject> getListSorterSource(String key) {
		switch (key) {
			case ENTRIES:
				return getObject().getSubMenus();
			default:
				throw new IllegalStateException("unknown list sorter key: " + key);
		}
	}

	@Override
	protected void resetProperties() {
		subMenu = null;
	}
}
