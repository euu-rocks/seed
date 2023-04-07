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
package org.seed.core.api;

import java.util.List;
import java.util.Map;

/**
 * <code>DataSourceProvider</code> provides access to {@link DataSource} objects.
 * 
 * @author seed-master
 *
 */
public interface DataSourceProvider {
	
	/**
	 * Returns the <code>DataSource</code> with the given name.
	 * @param dataSourceName the name of the data source
	 * @return the <code>DataSource</code> with the given name or null if it doesn't exist
	 */
	DataSource getDataSource(String dataSourceName);

	/**
	 * Executes the data source query and returns the list of results.
	 * @param dataSource the <code>DataSource</code> to use
	 * @param parameters a map of named parameter objects
	 * @return the result list as list of arrays
	 * 		   Each array element represents a column value
	 */
	List<Object[]> query(DataSource dataSource, Map<String, Object> parameters);
	
}
