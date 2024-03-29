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

import org.seed.C;
import org.seed.core.util.Assert;

public final class ColumnMetadata {
	
	public final String name;
	
	public final int type; // SQL type from java.sql.Types

	ColumnMetadata(String name, int type) {
		Assert.notNull(name, C.NAME);
		
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}
	
}
