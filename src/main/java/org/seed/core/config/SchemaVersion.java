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

/**
 * For schema update, a new version must be added 
 * and a new file named system-update-{@literal <}Version{@literal >}.json
 * must be created in the folder resources/liquibase
 * that contains a changeset 
 * (e.g. V_1_0_1 -> system-update-V_1_0_1.json)
 */
public enum SchemaVersion {
	
	V_0_9,
	V_0_9_19,
	V_0_9_20,
	V_0_9_21,
	V_0_9_22,
	V_0_9_23,
	V_0_9_29,
	V_0_9_31,
	V_0_9_32,
	V_0_9_33,
	V_0_9_34,
	V_0_9_39,
	V_0_9_40; // add new versions below
	
	public static SchemaVersion currentVersion() {
		return lastVersion();
	}
	
	public static SchemaVersion getVersion(int idx) {
		return values()[idx];
	}
	
	static boolean existUpdates() {
		return values().length > 1;
	}
	
	static SchemaVersion firstVersion() {
		return getVersion(0);
	}
	
	static SchemaVersion lastVersion() {
		return getVersion(values().length - 1);
	}
	
}
