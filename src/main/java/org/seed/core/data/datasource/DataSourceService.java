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

import java.util.Map;

import org.hibernate.Session;

import org.seed.core.application.ApplicationEntityService;

public interface DataSourceService extends ApplicationEntityService<IDataSource> {
	
	DataSourceParameter createParameter(IDataSource dataSource);
	
	DataSourceResult query(IDataSource dataSource, Map<String, Object> parameters, Session session);
	
	DataSourceResult query(IDataSource dataSource, Map<String, Object> parameters);
	
}
