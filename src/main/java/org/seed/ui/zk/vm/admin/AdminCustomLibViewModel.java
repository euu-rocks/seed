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

import org.seed.C;
import org.seed.core.customcode.CustomLib;
import org.seed.core.customcode.CustomLibMetadata;
import org.seed.core.customcode.CustomLibService;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.user.Authorisation;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class AdminCustomLibViewModel extends AbstractAdminViewModel<CustomLib> {
	
	@WireVariable(value="customLibServiceImpl")
	private CustomLibService customLibService;
	
	public AdminCustomLibViewModel() {
		super(Authorisation.ADMIN_SOURCECODE, "customlib",
			  "/admin/customcode/customliblist.zul", 
			  "/admin/customcode/customlib.zul");
	}
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam(C.PARAM) Object object) {
		super.init(object, view);
	}
	
	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, null, notifyObject);
	}
	
	@Command
	public void selectFile() {
		final CustomLibMetadata customLib = (CustomLibMetadata) getObject();
		if (customLib.getName() == null) {
			customLib.setName(customLib.getFilename().substring(0, customLib.getFilename().lastIndexOf('.')));
			notifyObjectChange(C.NAME);
		}
		else if(customLib.getFilename() == null) {
			customLib.setName(null);
			customLib.setError(null);
			notifyObjectChange(C.NAME, C.ERROR);
		}
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void newLib() {
		cmdNewObject();
	}
	
	@Command
	public void editLib() {
		cmdEditObject();
	}
	
	@Command
	public void refreshLib(@BindingParam(C.ELEM) Component component) {
		cmdRefresh();
	}
	
	@Command
	public void saveLib(@BindingParam(C.ELEM) Component component) {
		cmdSaveObject(component);
		notifyObjectChange(C.ERROR);
	}
	
	@Command
	public void deleteLib(@BindingParam(C.ELEM) Component component) {
		cmdDeleteObject(component);
	}
	
	@Override
	protected void handleValidationException(ValidationException vex, Component component, String msgKey) {
		for (ValidationError error : vex.getErrors()) {
			if ("val.illegal.jar".equals(error.getError())) {
				((CustomLibMetadata) getObject()).setError(error.getParameters()[0]);
				break;
			}
		}
		super.handleValidationException(vex, component, msgKey);
	}

	@Override
	protected CustomLibService getObjectService() {
		return customLibService;
	}

	@Override
	protected void resetProperties() {
		// do nothing
	}
	
}
