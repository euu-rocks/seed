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

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.seed.core.data.SystemEntity;

public interface User extends SystemEntity {
	
	String getPassword();
	
	String getEmail();
	
	String getFirstname();
	
	String getLastname();
	
	Date getLastLogin();
	
	boolean isEnabled();
	
	boolean hasUserGroups();
	
	boolean belongsToSystemGroup();
	
	boolean belongsTo(UserGroup userGroup);
	
	boolean belongsToOneOf(Collection<? extends UserGroup> userGroups);
	
	Set<UserGroup> getUserGroups();
	
	boolean isAuthorised(Authorisation authorisation);
	
	boolean hasAdminAuthorisations();
	
}
