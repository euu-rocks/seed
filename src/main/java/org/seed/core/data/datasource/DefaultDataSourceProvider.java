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
package org.seed.core.data.datasource;

import java.util.List;
import java.util.Map;

import org.seed.C;
import org.seed.InternalException;
import org.seed.Seed;
import org.seed.core.api.AbstractFunctionContext;
import org.seed.core.api.DataSource;
import org.seed.core.api.DataSourceProvider;
import org.seed.core.config.SystemLog;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

public class DefaultDataSourceProvider implements DataSourceProvider {
	
	private final AbstractFunctionContext functionContext;
	
	private final DataSourceService dataSourceService;
	
	public DefaultDataSourceProvider(AbstractFunctionContext functionContext) {
		Assert.notNull(functionContext, C.CONTEXT);
		
		this.functionContext = functionContext;
		dataSourceService = Seed.getBean(DataSourceService.class);
	}
	
	@Override
	public DataSource getDataSource(String dataSourceName) {
		Assert.notNull(dataSourceName, "dataSourceName");
		
		return dataSourceService.findByName(dataSourceName, functionContext.getSession());
	}

	@Override
	public List<Object[]> query(DataSource dataSource, Map<String, Object> parameters) {
		Assert.notNull(dataSource, C.DATASOURCE);
		Assert.notNull(parameters, "parameters");
		try {
			return dataSourceService.query((IDataSource) dataSource, parameters, functionContext.getSession())
									.getResultList();
		} 
		catch (ValidationException vex) {
			SystemLog.logError(vex);
			throw new InternalException(vex);
		}
	}

}
