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
import org.seed.core.user.UserGroupAuthorisation;
import org.seed.core.user.UserGroupMetadata;
import org.seed.core.user.UserGroupService;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;

public class AdminUserGroupViewModel extends AbstractAdminViewModel<UserGroup> {
	
	private static final String AUTHORISATIONS = "authorisations";
	private static final String USERS = "users";
	
	private final List<Long> originalUserIds = new ArrayList<>();
	
	public AdminUserGroupViewModel() {
		super(Authorisation.ADMIN_USER, "usergroup",
			  "/admin/user/usergrouplist.zul", 
			  "/admin/user/usergroup.zul");
	}
	
	@Init
	public void init(@ExecutionArgParam("param") Object object) {
		super.init(object, null);
	}
	
	protected void initObject(UserGroup userGroup) {
		if (!userGroup.isNew() && userGroup.hasUsers()) {
			for (User user : userGroup.getUsers()) {
				originalUserIds.add(user.getId());
			}
		}
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void newUserGroup() {
		cmdNewObject();
	}
	
	@Command
	public void editUserGroup() {
		cmdEditObject();
	}
	
	@Command
	public void refreshUserGroup(@BindingParam("elem") Component elem) {
		cmdRefresh();
	}
	
	@Command
	public void deleteUserGroup(@BindingParam("elem") Component elem) {
		cmdDeleteObject(elem);
	}
	
	@Command
	public void saveUserGroup(@BindingParam("elem") Component elem) {
		
		((UserGroupMetadata) getObject()).setOriginalUserIds(originalUserIds);
		adjustLists(getObject().getAuthorisations(), getListManagerList(AUTHORISATIONS, LIST_SELECTED));
		adjustLists(getObject().getUsers(), getListManagerList(USERS, LIST_SELECTED));
		
		cmdSaveObject(elem);
	}
	
	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, notifyObject);
	}
	
	@Command
	public void dropToAuthorisationList(@BindingParam("item") UserGroupAuthorisation item,
							   			@BindingParam("list") int listNum) {
		super.dropToList(AUTHORISATIONS, listNum, item);
	}
	
	@Command
	public void insertToAuthorisationList(@BindingParam("base") UserGroupAuthorisation base,
								 		  @BindingParam("item") UserGroupAuthorisation item,
								 		  @BindingParam("list") int listNum) {
		super.insertToList(AUTHORISATIONS, listNum, base, item);
	}
	
	@Command
	public void dropToUserList(@BindingParam("item") User item,
							   @BindingParam("list") int listNum) {
		super.dropToList(USERS, listNum, item);
	}
	
	@Command
	public void insertToUserList(@BindingParam("base") User base,
								 @BindingParam("item") User item,
								 @BindingParam("list") int listNum) {
		super.insertToList(USERS, listNum, base, item);
	}
	
	@GlobalCommand
	public void _refreshObject(@BindingParam("param") Long objectId) {
		refreshObject(objectId);
	}

	@Override
	protected List<? extends SystemObject> getListManagerSource(String key, int listNum) {
		switch (key) {
			case AUTHORISATIONS:
				return listNum == LIST_AVAILABLE
						? userGroupService().getAvailableAuthorisations(getObject())
						: getObject().getAuthorisations();
				
			case USERS:
				return listNum == LIST_AVAILABLE
						? userGroupService().getAvailableUsers(getObject())
						: new ArrayList<>(getObject().getUsers());
			
			default:
				throw new IllegalStateException("unknown list manager key: " + key);
		}
	}

	@Override
	protected void resetProperties() {
		originalUserIds.clear();
	}

	@Override
	protected UserGroupService getObjectService() {
		return userGroupService();
	}

}
