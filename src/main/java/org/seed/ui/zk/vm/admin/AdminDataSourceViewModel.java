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

import org.seed.core.application.ContentObject;
import org.seed.core.codegen.SourceCode;
import org.seed.core.data.DataException;
import org.seed.core.data.datasource.IDataSource;
import org.seed.core.data.datasource.DataSourceParameter;
import org.seed.core.data.datasource.DataSourceParameterType;
import org.seed.core.data.datasource.DataSourceService;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityService;
import org.seed.core.user.Authorisation;
import org.seed.core.util.ExceptionUtils;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class AdminDataSourceViewModel extends AbstractAdminViewModel<IDataSource> {
	
	private static final String PARAMETERS = "parameters";
	
	@WireVariable(value="dataSourceServiceImpl")
	private DataSourceService dataSourceService;
	
	@WireVariable(value="entityServiceImpl")
	private EntityService entityService;
	
	private DataSourceParameter parameter;
	
	private String errorMessage;
	
	public AdminDataSourceViewModel() {
		super(Authorisation.ADMIN_DATASOURCE, "datasource",
			  "/admin/datasource/datasourcelist.zul", 
			  "/admin/datasource/datasource.zul");
	}
	
	public DataSourceParameterType[] getParameterTypes() {
		return DataSourceParameterType.values();
	}
	
	public List<Entity> getEntities() {
		return entityService.findNonGenericEntities();
	}
	
	public DataSourceParameter getParameter() {
		return parameter;
	}

	public void setParameter(DataSourceParameter parameter) {
		this.parameter = parameter;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam("param") Object object) {
		super.init(object, view);
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
	public void newDataSource() {
		cmdNewObject();
	}
	
	@Command
	public void editDataSource() {
		cmdEditObject();
	}
	
	@Command
	public void refreshDataSource(@BindingParam("elem") Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteDataSource(@BindingParam("elem") Component component) {
		cmdDeleteObject(component);
	}
	
	@Command
	@NotifyChange("parameter")
	public void newParameter() {
		parameter = dataSourceService.createParameter(getObject());
		notifyObjectChange(PARAMETERS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("parameter")
	public void removeParameter() {
		getObject().removeParameter(parameter);
		parameter = null;
		notifyObjectChange(PARAMETERS);
		flagDirty();
	}
	
	@Command
	@SmartNotifyChange("errorMessage")
	public void saveDataSource(@BindingParam("elem") Component component) {
		try {
			cmdSaveObject(component);
			errorMessage = null;
		}
		catch (DataException dex) {
			errorMessage = ExceptionUtils.getOriginalCauseMessage(dex.getCause());
		}
	}

	@Override
	protected DataSourceService getObjectService() {
		return dataSourceService;
	}

	@Override
	protected void resetProperties() {
		parameter = null;
		errorMessage = null;
	}
	
	@Override
	protected SourceCode getSourceCode(ContentObject contentObject) {
		throw new UnsupportedOperationException();
	}
	
}
