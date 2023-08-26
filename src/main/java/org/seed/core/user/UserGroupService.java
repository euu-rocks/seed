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

import java.util.List;

import org.hibernate.Session;

import org.seed.core.application.ApplicationEntityService;

public interface UserGroupService extends ApplicationEntityService<UserGroup> {
	
	List<User> getAvailableUsers(UserGroup userGroup, Session session);
	
	List<UserGroup> findNonSystemGroups();
	
	List<UserGroup> findNonSystemGroupsWithoutModule(Session session);
	
	List<UserGroup> findNonSystemGroups(Session session);
	
	List<UserGroupAuthorisation> getAvailableAuthorisations(UserGroup userGroup);
	
}