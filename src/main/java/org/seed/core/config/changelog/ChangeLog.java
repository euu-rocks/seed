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

import javax.persistence.Entity;
import javax.persistence.Table;

import org.seed.core.data.AbstractSystemObject;

@Entity
@Table(name = "sys_changelog")
public class ChangeLog extends AbstractSystemObject {
	
	private String changeSet;

	public String getChangeSet() {
		return changeSet;
	}

	public void setChangeSet(String changeSet) {
		this.changeSet = changeSet;
	}
	
}
