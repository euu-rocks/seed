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
package org.seed.core.user;

public enum Authorisation {
	
	ADMIN_ENTITY,
	ADMIN_FORM,
	ADMIN_MENU,
	ADMIN_JOB,
	ADMIN_USER,
	ADMIN_DBOBJECT,
	ADMIN_DATASOURCE,
	ADMIN_REPORT,
	ADMIN_SOURCECODE,
	ADMIN_REST,
	ADMIN_MODULE,
	ADMIN_SETTINGS,
	
	RUN_JOBS,
	PRINT_REPORTS,
	SEARCH_FULLTEXT,
	CALL_REST,
	ENDPOINTS,
	SYSTEMINFO,
	SYSTEMTASK,
	
	ADMIN_PERMISSIONS;
	
	private static final String ROLE_PREFIX = "ROLE_";
	
	String roleName() {
		return ROLE_PREFIX.concat(name());
	}
	
}
