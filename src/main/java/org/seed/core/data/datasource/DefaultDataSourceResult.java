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
import java.util.List;

import org.seed.core.util.Assert;

class DefaultDataSourceResult implements DataSourceResult {
	
	private final List<Object[]> resultList;
	
	private final List<ColumnMetadata> columns;
	
	DefaultDataSourceResult(List<Object[]> resultList, ResultSetMetaData metaData) throws SQLException {
		Assert.notNull(resultList, "result list");
		
		if (metaData != null) {
			final List<ColumnMetadata> columnList = new ArrayList<>(metaData.getColumnCount());
			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				columnList.add(new ColumnMetadata(metaData.getColumnName(i),
											      metaData.getColumnType(i)));
			}
			this.columns = columnList;
		}
		else {
			this.columns = null;
		}
		this.resultList = new ArrayList<>(resultList.size());
		// ensure that each list entry is an array
		for (Object object : resultList) {
			if (object.getClass().isArray()) {
				this.resultList.add((Object[]) object);
			}
			else {
				this.resultList.add(new Object[] {object});
			}
		}
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
