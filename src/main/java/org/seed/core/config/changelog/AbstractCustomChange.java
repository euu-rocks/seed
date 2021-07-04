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
package org.seed.core.config.changelog;

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;

abstract class AbstractCustomChange implements CustomSqlChange {

	protected abstract String getSql();
	
	protected abstract String getParameterName();
	
	protected abstract String getParameterValue();
	
	@Override
	public void setUp() throws SetupException {
		// do nothing
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// do nothing
	}

	@Override
	public final SqlStatement[] generateStatements(Database database) throws CustomChangeException {
		return new SqlStatement[] { new RawSqlStatement(getSql()) };
	}

}
