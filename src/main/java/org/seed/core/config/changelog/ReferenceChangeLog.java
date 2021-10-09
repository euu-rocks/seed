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

import org.seed.core.util.Assert;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;

public class ReferenceChangeLog {
	
	private ChangeSet changeSet;
	
	public void addChange(Change change) {
		Assert.notNull(change, "change");
		
		if (changeSet == null) {
			changeSet = AbstractChangeLogBuilder.createChangeSet();
		}
		changeSet.addChange(change);
	}
	
	public boolean isEmpty() {
		return changeSet == null;
	}
	
	public ChangeLog build() {
		return isEmpty() 
				? null 
				: AbstractChangeLogBuilder.build(changeSet);
	}
	
}
