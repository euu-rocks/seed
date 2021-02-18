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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

class DefaultDataSourceResult implements DataSourceResult {
	
	private final List<Object[]> resultList;
	
	private final List<ColumnMetadata> columns;
	
	DefaultDataSourceResult(List<Object[]> resultList, ResultSetMetaData metaData) throws SQLException {
		Assert.notNull(resultList, "resultList is null");
		
		if (metaData != null) {
			final List<ColumnMetadata> columns = new ArrayList<>(metaData.getColumnCount());
			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				columns.add(new ColumnMetadata(metaData.getColumnName(i),
											   metaData.getColumnType(i)));
			}
			this.columns = Collections.unmodifiableList(columns);
		}
		else {
			this.columns = null;
		}
		this.resultList = Collections.unmodifiableList(resultList);
	}
	
	@Override
	public List<Object[]> getResultList() {
		return resultList;
	}
	
	@Override
	public List<ColumnMetadata> getColumns() {
		return columns;
	}
	
}
