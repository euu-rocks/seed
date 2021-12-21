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
package org.seed.core.config;

import org.seed.C;
import org.seed.core.util.Assert;

public final class DatabaseInfo {
	
	private final String name;
	
	private final String version;

	DatabaseInfo(String name, String version) {
		Assert.notNull(name, C.NAME);
		Assert.notNull(version, "version");
		
		this.name = name;
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}
	
	public boolean isPostgres() {
		return name.toLowerCase().startsWith("postgres");
	}
	
}
