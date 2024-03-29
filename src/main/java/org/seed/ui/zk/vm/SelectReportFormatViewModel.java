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
package org.seed.ui.zk.vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.seed.C;
import org.seed.core.data.ValidationException;
import org.seed.core.data.datasource.DataSourceParameter;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.report.Report;
import org.seed.core.report.ReportFormat;
import org.seed.core.util.Assert;
import org.seed.ui.Tab;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

public class SelectReportFormatViewModel extends AbstractApplicationViewModel  {
	
	@Wire("#selectFormatWin")
	private Window window;
	
	@WireVariable(value="valueObjectServiceImpl")
	private ValueObjectService valueObjectService;
	
	private Report report;
	
	private ReportFormat format;
	
	private List<Tab> parameterTabs;
	
	private AbstractApplicationViewModel parentVM;
	
	public List<ValueObject> getReferenceObjects(DataSourceParameter parameter) {
		return valueObjectService.getAllObjects(currentSession(), parameter.getReferenceEntity());
	}
	
	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam(C.PARAM) SelectReportFormatParameter param) {
		Assert.notNull(param, C.PARAM);
		
		report = param.report;
		parentVM = param.parentVM;
		wireComponents(view);
	}

	public ReportFormat getFormat() {
		return format;
	}

	public void setFormat(ReportFormat format) {
		this.format = format;
	}
	
	public ReportFormat[] getFormats() {
		return ReportFormat.values();
	}
	
	public List<Tab> getParameterTabs() {
		if (parameterTabs == null) {
			parameterTabs = new ArrayList<>();
			report.getDataSourceParameterMap().entrySet()
				  .forEach(entry -> parameterTabs.add(new Tab(entry.getKey(), 
						  									  entry.getValue())));
		}
		return parameterTabs;
	}
	
	public boolean showParameters() {
		return getParameterTabs().size() == 1;
	}
	
	public boolean showParameterTabs() {
		return getParameterTabs().size() > 1;
	}
	
	@SuppressWarnings("unchecked")
	public List<DataSourceParameter> getParameters() {
		if (!getParameterTabs().isEmpty()) {
			return (List<DataSourceParameter>) getParameterTabs().get(0).getParameter();
		}
		return Collections.emptyList();
	}
	
	@Command
	public void selectFormat(@BindingParam(C.ELEM) Component elem) {
		try {
			parentVM.downloadReport(report, format);
			window.detach();
		} 
		catch (ValidationException vex) {
			showValidationErrors(elem, "admin.report.generatefail", vex.getErrors());
		}
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
}
