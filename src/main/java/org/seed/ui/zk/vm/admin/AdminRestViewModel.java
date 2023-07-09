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

import org.seed.C;
import org.seed.core.api.RestFunction.MethodType;
import org.seed.core.application.ContentObject;
import org.seed.core.codegen.SourceCode;
import org.seed.core.data.SystemObject;
import org.seed.core.rest.Rest;
import org.seed.core.rest.RestFunction;
import org.seed.core.rest.RestPermission;
import org.seed.core.rest.RestService;
import org.seed.core.rest.codegen.RestCodeProvider;
import org.seed.core.user.Authorisation;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

public class AdminRestViewModel extends AbstractAdminViewModel<Rest> {
	
	private static final String FUNCTIONS = "functions";
	private static final String PERMISSIONS = "permissions";
	
	@Wire("#newRestWin")
	private Window window;
	
	@WireVariable(value="restServiceImpl")
	private RestService restService;
	
	@WireVariable(value="restCodeProvider")
	private RestCodeProvider restCodeProvider;
	
	private RestFunction function;
	
	private RestPermission permission;
	
	public AdminRestViewModel() {
		super(Authorisation.ADMIN_REST, C.REST,
			  "/admin/rest/restlist.zul", 
			  "/admin/rest/rest.zul",
			  "/admin/rest/newrest.zul");
	}
	
	public RestFunction getFunction() {
		return function;
	}

	public void setFunction(RestFunction function) {
		this.function = function;
	}
	
	public RestPermission getPermission() {
		return permission;
	}

	public void setPermission(RestPermission permission) {
		this.permission = permission;
	}

	public MethodType[] getMethodTypes() {
		return MethodType.values();
	}

	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam(C.PARAM) Object object) {
		super.init(object, view);
	}
	
	@Command
	@Override
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam(C.OBJECT) Object object, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, object, notifyObject);
	}
	
	@Override
	protected List<SystemObject> getListManagerSource(String key, int listNum) {
		if (PERMISSIONS.equals(key)) {
			return MiscUtils.castList(listNum == LIST_AVAILABLE 
					? restService.getAvailablePermissions(getObject(), currentSession()) 
					: getObject().getPermissions());
		}
		else {
			throw new UnsupportedOperationException(key);
		}
	}
	
	@Command
	@SmartNotifyChange(C.PERMISSION)
	public void insertToPermissionList(@BindingParam(C.BASE) RestPermission base,
									   @BindingParam(C.ITEM) RestPermission item,
									   @BindingParam(C.LIST) int listNum) {
		insertToList(PERMISSIONS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == permission) {
			this.permission = null;
		}
	}
	
	@Command
	@SmartNotifyChange(C.PERMISSION)
	public void dropToPermissionList(@BindingParam(C.ITEM) RestPermission item,
									 @BindingParam(C.LIST) int listNum) {
		dropToList(PERMISSIONS, listNum, item);
		if (listNum == LIST_AVAILABLE && item == permission) {
			this.permission = null;
		}
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void newRest() {
		if (existModules()) {
			cmdNewObjectDialog();
		}
		else {
			cmdNewObject();
		}
	}
	
	@Command
	public void createRest(@BindingParam(C.ELEM) Component elem) {
		cmdInitObject(elem, window);
	}
	
	@Override
	@Command
	public void showSwagger() {
		super.showSwagger();
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
	@Command
	public void editRest() {
		cmdEditObject();
	}
	
	@Command
	public void refreshRest(@BindingParam(C.ELEM) Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteRest(@BindingParam(C.ELEM) Component component) {
		cmdDeleteObject(component);
	}
	
	@Override
	protected void afterObjectDeleted(Rest rest) {
		resetCurrentSession();
	}
	
	@Command
	@NotifyChange(C.FUNCTION)
	public void newFunction() {
		function = restService.createFunction(getObject());
		notifyObjectChange(FUNCTIONS);
		flagDirty();
	}
	
	@Command
	@NotifyChange(C.FUNCTION)
	public void removeFunction(@BindingParam(C.ELEM) Component component) {
		restService.removeFunction(getObject(), function);
		notifyObjectChange(FUNCTIONS);
		flagDirty();
	}
	
	@Command
	public void editFunctionSource() {
		if (function.getContent() == null) {
			function.setContent(restCodeProvider.getFunctionTemplate(function));
		}
		showCodeDialog(new CodeDialogParameter(this, function));
	}

	@Override
	protected RestService getObjectService() {
		return restService;
	}

	@Override
	protected void resetProperties() {
		function = null;
		permission = null;
	}

	@Override
	protected SourceCode getSourceCode(ContentObject contentObject) {
		Assert.notNull(contentObject, "contentObject");
		final RestFunction restFunction = (RestFunction) contentObject;
		
		return restCodeProvider.getRestSource(restFunction);
	}
	
	@Command
	public void saveRest(@BindingParam(C.ELEM) Component elem) {
		adjustLists(getObject().getPermissions(), getListManagerList(PERMISSIONS, LIST_SELECTED));
		if (cmdSaveObject(elem)) {
			resetCurrentSession();
		}
	}
	
}
