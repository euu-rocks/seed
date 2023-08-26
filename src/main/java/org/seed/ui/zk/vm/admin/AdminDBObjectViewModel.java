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

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.seed.C;
import org.seed.core.data.DataException;
import org.seed.core.data.dbobject.DBObject;
import org.seed.core.data.dbobject.DBObjectService;
import org.seed.core.data.dbobject.DBObjectType;
import org.seed.core.user.Authorisation;
import org.seed.ui.ListFilter;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

public class AdminDBObjectViewModel extends AbstractAdminViewModel<DBObject> {
	
	@Wire("#newDBObjectWin")
	private Window window;
	
	@WireVariable(value="DBObjectServiceImpl")
	private DBObjectService objectService; 
	
	private String errorMessage;
	
	public AdminDBObjectViewModel() {
		super(Authorisation.ADMIN_DBOBJECT, "dbobject",
			  "/admin/dbobject/dbobjectlist.zul", 
			  "/admin/dbobject/dbobject.zul",
			  "/admin/dbobject/newdbobject.zul");
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public DBObjectType[] getObjectTypes() {
		return DBObjectType.values();
	}
	
	public boolean isReadonly(DBObject dbObject) {
		return dbObject != null && !dbObject.isNew() && 
				!dbObject.getType().isEditable();
	}
	
	public String getCodeMirrorClass(DBObject dbObject) {
		return isReadonly(dbObject) ? "pointer-events:none" : null;
	}
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam(C.PARAM) Object object) {
		super.init(object, view);
	}
	
	@Override
	protected void initFilters() {
		final ListFilter<DBObject> filterType = getFilter(FILTERGROUP_LIST, C.TYPE);
		filterType.setValueFunction(o -> getEnumLabel(o.getType()));
		for (DBObject dbObject : getObjectList()) {
			filterType.addValue(getEnumLabel(dbObject.getType()));
		}
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, null, notifyObject);
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void createDBObject(@BindingParam(C.ELEM) Component elem) {
		cmdInitObject(elem, window);
	}
	
	@Command
	public void newDBObject() {
		cmdNewObjectDialog();
	}
	
	@Command
	public void editDBObject() {
		cmdEditObject();
	}
	
	@Command
	public void refreshDBObject(@BindingParam(C.ELEM) Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteDBObject(@BindingParam(C.ELEM) Component component) {
		cmdDeleteObject(component);
	}
	
	@Override
	protected void afterObjectDeleted(DBObject dbObject) {
		resetCurrentSession();
	}
	
	@Command
	@SmartNotifyChange("errorMessage")
	public void saveDBObject(@BindingParam(C.ELEM) Component component) {
		try {
			if (cmdSaveObject(component)) {
				resetCurrentSession();
			}
			errorMessage = null;
		}
		catch (DataException dboex) {
			errorMessage = ExceptionUtils.getRootCauseMessage(dboex.getCause());
		}
	}
	
	@Override
	protected DBObjectService getObjectService() {
		return objectService;
	}

	@Override
	protected void resetProperties() {
		errorMessage = null;
	}
	
}
