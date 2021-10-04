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
import org.seed.core.data.SystemObject;
import org.seed.core.data.datasource.IDataSource;
import org.seed.core.data.datasource.DataSourceService;
import org.seed.core.report.Report;
import org.seed.core.report.ReportDataSource;
import org.seed.core.report.ReportPermission;
import org.seed.core.report.ReportService;
import org.seed.core.user.Authorisation;
import org.seed.core.util.MiscUtils;
import org.seed.ui.zk.vm.SelectReportFormatParameter;

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

public class AdminReportViewModel extends AbstractAdminViewModel<Report> {
	
	private static final String DATASOURCES = "dataSources";
	private static final String PERMISSIONS = "permissions";
	
	@Wire("#newReportWin")
	private Window window;
	
	@WireVariable(value="dataSourceServiceImpl")
	private DataSourceService dataSourceService;
	
	private ReportDataSource dataSource;
	
	private ReportPermission permission;
	
	public AdminReportViewModel() {
		super(Authorisation.ADMIN_REPORT, C.REPORT,
			  "/admin/report/reportlist.zul", 
			  "/admin/report/report.zul",
			  "/admin/report/newreport.zul");
	}
	
	public List<IDataSource> getDataSources() {
		return dataSourceService.getObjects();
	}
	
	public ReportDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(ReportDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public ReportPermission getPermission() {
		return permission;
	}

	public void setPermission(ReportPermission permission) {
		this.permission = permission;
	}

	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam(C.PARAM) Object object) {
		super.init(object, view);
	}
	
	@Override
	protected ReportService getObjectService() {
		return reportService;
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
	public void newReport() {
		cmdNewObject();
	}
	
	@Command
	public void editReport() {
		cmdEditObject();
	}
	
	@Command
	public void createReport(@BindingParam(C.ELEM) Component elem) {
		cmdInitObject(elem, window);
	}
	
	@Command
	public void refreshReport(@BindingParam(C.ELEM) Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteReport(@BindingParam(C.ELEM) Component component) {
		cmdDeleteObject(component);
	}
	
	@Command
	@NotifyChange(C.DATASOURCE)
	public void newDataSource() {
		dataSource = reportService.createDataSource(getObject());
		notifyObjectChange(DATASOURCES);
		flagDirty();
	}
	
	@Command
	@NotifyChange(C.DATASOURCE)
	public void removeDataSource() {
		getObject().removeDataSource(dataSource);
		dataSource = null;
		notifyObjectChange(DATASOURCES);
		flagDirty();
	}
	
	@Command
	public void generateReport(@BindingParam(C.ELEM) Component elem) {
		showDialog("/form/selectreportformat.zul", new SelectReportFormatParameter(this, getObject()));
	}
	
	@Override
	protected List<SystemObject> getListManagerSource(String key, int listNum) {
		if (PERMISSIONS.equals(key)) {
			return MiscUtils.cast(listNum == LIST_AVAILABLE 
					? reportService.getAvailablePermissions(getObject()) 
					: getObject().getPermissions());
		}
		else {
			throw new IllegalStateException("unknown list manager key: " + key);
		}
	}
	
	@Override
	protected List<SystemObject> getListSorterSource(String key) {
		if (DATASOURCES.equals(key)) {
			return MiscUtils.cast(getObject().getDataSources());
		}
		else {
			throw new IllegalStateException("unknown list sorter key: " + key);
		}
	}
	
	@Command
	@SmartNotifyChange(C.PERMISSION)
	public void insertToPermissionList(@BindingParam(C.BASE) ReportPermission base,
									   @BindingParam(C.ITEM) ReportPermission item,
									   @BindingParam(C.LIST) int listNum) {
		insertToList(PERMISSIONS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == permission) {
			this.permission = null;
		}
	}
	
	@Command
	@SmartNotifyChange(C.PERMISSION)
	public void dropToPermissionList(@BindingParam(C.ITEM) ReportPermission item,
									 @BindingParam(C.LIST) int listNum) {
		dropToList(PERMISSIONS, listNum, item);
		if (listNum == LIST_AVAILABLE && item == permission) {
			this.permission = null;
		}
	}
	
	@Command
	public void swapDataSources(@BindingParam(C.BASE) ReportDataSource base, 
							    @BindingParam(C.ITEM) ReportDataSource item) {
		swapItems(DATASOURCES, base, item);
	}
	
	@Command
	public void saveReport(@BindingParam(C.ELEM) Component component) {
		adjustLists(getObject().getPermissions(), getListManagerList(PERMISSIONS, LIST_SELECTED));
		cmdSaveObject(component);
	}
	
	@Override
	protected void resetProperties() {
		dataSource = null;
		permission = null;
	}
	
}
