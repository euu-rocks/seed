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
import java.util.Set;

import org.seed.core.application.ApplicationEntity;

public interface IDataSource extends ApplicationEntity, org.seed.core.api.DataSource {
	
	String getContent();
	
	boolean hasParameters();
	
	DataSourceParameter getParameterByUid(String uid);
	
	DataSourceParameter getParameterByName(String name);
	
	List<DataSourceParameter> getParameters();
	
	void addParameter(DataSourceParameter parameter);
	
	void removeParameter(DataSourceParameter parameter);
	
	Set<String> getContentParameterSet();
	
}