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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntity;
import org.seed.core.data.ValidationException;
import org.seed.core.data.datasource.DataSourceParameter;
import org.seed.core.data.datasource.DataSourceResult;
import org.seed.core.data.datasource.DataSourceService;
import org.seed.core.data.datasource.IDataSource;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.util.Assert;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class QueryDialogViewModel extends AbstractApplicationViewModel {
	
	@WireVariable(value="dataSourceServiceImpl")
	private DataSourceService dataSourceService;
	
	@WireVariable(value="valueObjectServiceImpl")
	private ValueObjectService valueObjectService;
	
	private IDataSource dataSource;
	
	private DataSourceResult result;
	
	public IDataSource getDataSource() {
		return dataSource;
	}

	public DataSourceResult getResult() {
		return result;
	}
	
	public List<ValueObject> getReferenceObjects(DataSourceParameter parameter) {
		return valueObjectService.getAllObjects(currentSession(), parameter.getReferenceEntity());
	}
	
	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam(C.PARAM) IDataSource param) {
		Assert.notNull(param, C.PARAM);
		
		dataSource = param;
		if (!dataSource.hasParameters()) {
			query(null);
		}
	}
	
	@Command
	@NotifyChange(C.RESULT)
	public void query(@BindingParam(C.ELEM) Component component) {
		try {
			result = dataSourceService.query(dataSource, getParameterMap(), currentSession());
		} 
		catch (ValidationException vex) {
			showValidationErrors(component, "admin.datasource.queryfail", vex.getErrors());
		}
	}
	
	private Map<String, Object> getParameterMap() {
		if (!dataSource.hasParameters()) {
			return Collections.emptyMap();
		}
		return dataSource.getParameters().stream()
				  		 .filter(param -> param.getValue() != null)
				  		 .collect(Collectors.toMap(AbstractSystemEntity::getName, 
				  				 				   DataSourceParameter::getValue));
	}
	
}
