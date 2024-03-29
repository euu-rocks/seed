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

import org.seed.C;
import org.seed.core.data.SystemObject;
import org.seed.core.user.Authorisation;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupAuthorisation;
import org.seed.core.user.UserGroupMetadata;
import org.seed.core.user.UserGroupService;
import org.seed.core.util.MiscUtils;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Window;

public class AdminUserGroupViewModel extends AbstractAdminViewModel<UserGroup> {
	
	private static final String AUTHORISATIONS = "authorisations";
	private static final String USERS = "users";
	
	@Wire("#newUserGroupWin")
	private Window window;
	
	private final List<Long> originalUserIds = new ArrayList<>();
	
	public AdminUserGroupViewModel() {
		super(Authorisation.ADMIN_USER, "usergroup",
			  "/admin/user/usergrouplist.zul", 
			  "/admin/user/usergroup.zul",
			  "/admin/user/newusergroup.zul");
	}
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam(C.PARAM) Object object) {
		super.init(object, view);
	}
	
	@Override
	protected void initObject(UserGroup userGroup) {
		super.initObject(userGroup);
		if (!userGroup.isNew() && userGroup.hasUsers()) {
			for (User user : userGroup.getUsers()) {
				originalUserIds.add(user.getId());
			}
		}
	}
	
	@Command
	public void createUserGroup(@BindingParam(C.ELEM) Component elem) {
		cmdInitObject(elem, window);
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void newUserGroup() {
		if (existModules()) {
			cmdNewObjectDialog();
		}
		else {
			cmdNewObject();
		}
	}
	
	@Command
	public void editUserGroup() {
		cmdEditObject();
	}
	
	@Command
	public void refreshUserGroup(@BindingParam(C.ELEM) Component elem) {
		cmdRefresh();
	}
	
	@Command
	public void deleteUserGroup(@BindingParam(C.ELEM) Component elem) {
		cmdDeleteObject(elem);
	}
	
	@Command
	public void saveUserGroup(@BindingParam(C.ELEM) Component elem) {
		
		((UserGroupMetadata) getObject()).setOriginalUserIds(originalUserIds);
		adjustLists(getObject().getAuthorisations(), getListManagerList(AUTHORISATIONS, LIST_SELECTED));
		adjustLists(getObject().getUsers(), getListManagerList(USERS, LIST_SELECTED));
		
		cmdSaveObject(elem);
	}
	
	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		flagDirty(notify, null, notifyObject);
	}
	
	@Command
	public void dropToAuthorisationList(@BindingParam(C.ITEM) UserGroupAuthorisation item,
							   			@BindingParam(C.LIST) int listNum) {
		dropToList(AUTHORISATIONS, listNum, item);
	}
	
	@Command
	public void insertToAuthorisationList(@BindingParam(C.BASE) UserGroupAuthorisation base,
								 		  @BindingParam(C.ITEM) UserGroupAuthorisation item,
								 		  @BindingParam(C.LIST) int listNum) {
		insertToList(AUTHORISATIONS, listNum, base, item);
	}
	
	@Command
	public void dropToUserList(@BindingParam(C.ITEM) User item,
							   @BindingParam(C.LIST) int listNum) {
		dropToList(USERS, listNum, item);
	}
	
	@Command
	public void insertToUserList(@BindingParam(C.BASE) User base,
								 @BindingParam(C.ITEM) User item,
								 @BindingParam(C.LIST) int listNum) {
		insertToList(USERS, listNum, base, item);
	}
	
	@GlobalCommand
	public void globalRefreshObject(@BindingParam(C.PARAM) Long objectId) {
		refreshObject(objectId);
	}

	@Override
	protected List<SystemObject> getListManagerSource(String key, int listNum) {
		switch (key) {
			case AUTHORISATIONS:
				return MiscUtils.castList(listNum == LIST_AVAILABLE
						? sortAuthorisations(userGroupService().getAvailableAuthorisations(getObject()))
						: sortAuthorisations(getObject().getAuthorisations()));
				
			case USERS:
				return MiscUtils.castList(listNum == LIST_AVAILABLE
						? userGroupService().getAvailableUsers(getObject(), currentSession())
						: new ArrayList<>(getObject().getUsers()));
			
			default:
				throw new IllegalStateException("unknown list manager key: " + key);
		}
	}
	
	private List<UserGroupAuthorisation> sortAuthorisations(List<UserGroupAuthorisation> authorisations) {
		authorisations.sort((a1, a2) -> 
			getEnumLabel(a1.getAuthorisation()).compareTo(getEnumLabel(a2.getAuthorisation())));
		return authorisations;
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
