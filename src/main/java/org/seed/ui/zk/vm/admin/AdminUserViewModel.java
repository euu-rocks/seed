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
import java.util.List;

import org.seed.core.data.SystemObject;
import org.seed.core.user.Authorisation;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserMetadata;
import org.seed.core.user.UserService;
import org.seed.core.util.MiscUtils;
import org.seed.ui.zk.vm.PwdDialogParameter;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;

public class AdminUserViewModel extends AbstractAdminViewModel<User> {
	
	private static final String USERGROUPS = "usergroups";
	
	public AdminUserViewModel() {
		super(Authorisation.ADMIN_USER, "user",
			  "/admin/user/userlist.zul", 
			  "/admin/user/user.zul");
	}
	
	@Init
	public void init(@ExecutionArgParam("param") Object object) {
		super.init(object, null);
	}
	
	@Override
	protected UserService getObjectService() {
		return userService;
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void newUser() {
		cmdNewObject();
	}
	
	@Command
	public void editUser() {
		cmdEditObject();
	}
	
	@Command
	public void refreshUser(@BindingParam("elem") Component elem) {
		cmdRefresh();
	}
	
	@Command
	public void deleteUser(@BindingParam("elem") Component elem) {
		cmdDeleteObject(elem);
	}
	
	@Command
	public void setPwd(@BindingParam("elem") Component elem) {
		showDialog("/admin/user/pwddialog.zul", new PwdDialogParameter(this, getObject()));
	}
	
	@Command
	public void saveUser(@BindingParam("elem") Component elem) {
		
		adjustLists(getObject().getUserGroups(), getListManagerList(USERGROUPS, LIST_SELECTED));
		
		cmdSaveObject(elem);
	}
	
	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, null, notifyObject);
	}
	
	@Command
	public void dropToGroupList(@BindingParam("item") UserGroup item,
								@BindingParam("list") int listNum) {
		super.dropToList(USERGROUPS, listNum, item);
	}
	
	@Command
	public void insertToGroupList(@BindingParam("base") UserGroup base,
								  @BindingParam("item") UserGroup item,
								  @BindingParam("list") int listNum) {
		super.insertToList(USERGROUPS, listNum, base, item);
	}
	
	@GlobalCommand
	public void globalRefreshObject(@BindingParam("param") Long objectId) {
		refreshObject(objectId);
	}
	
	@Override
	protected void refreshObject() {
		((UserMetadata) getObject()).setPasswordChange(false);
		super.refreshObject();
	}

	@Override
	protected List<SystemObject> getListManagerSource(String key, int listNum) {
		if (USERGROUPS.equals(key)) {
			return MiscUtils.cast(listNum == LIST_AVAILABLE
					? userService.getAvailableUserGroups(getObject())
					: new ArrayList<>(getObject().getUserGroups()));
		}
		else {
			throw new IllegalStateException("unknown list manager key: " + key);
		}
	}

	@Override
	protected void resetProperties() {
		// no properties
	}
	
}
