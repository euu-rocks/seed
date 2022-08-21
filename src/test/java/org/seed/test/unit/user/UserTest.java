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
package org.seed.test.unit.user;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.seed.core.user.Authorisation;
import org.seed.core.user.User;
import org.seed.core.user.UserGroupAuthorisation;
import org.seed.core.user.UserGroupMetadata;
import org.seed.core.user.UserMetadata;

class UserTest {
	
	@Test
	void testBelongsTo() {
		final User user = new UserMetadata();
		final UserGroupMetadata group = new UserGroupMetadata();
		final Set<UserGroupMetadata> groups = new HashSet<>();
		((UserMetadata) user).setUserGroups(groups);
		
		assertFalse(user.belongsTo(group));
		
		groups.add(group);
		
		assertTrue(user.belongsTo(group));
	}
	
	@Test
	void testbelongsToOneOf() {
		final User user = new UserMetadata();
		final UserGroupMetadata group = new UserGroupMetadata();
		final Set<UserGroupMetadata> groups = new HashSet<>();
		((UserMetadata) user).setUserGroups(groups);
		groups.add(group);
		
		assertFalse(user.belongsToOneOf(Collections.emptyList()));
		assertTrue(user.belongsToOneOf(Collections.singletonList(group)));
	}
	
	@Test
	void testHasAdminAuthorisations() {
		final User user = new UserMetadata();
		final UserGroupAuthorisation groupAuthorisation = new UserGroupAuthorisation();
		final UserGroupMetadata group = new UserGroupMetadata();
		final List<UserGroupAuthorisation> authorisations = new ArrayList<>();
		final Authorisation auth = Authorisation.ADMIN_MODULE;
		final Set<UserGroupMetadata> groups = new HashSet<>();
		((UserMetadata) user).setUserGroups(groups);
		((UserMetadata) user).setEnabled(true);
		groups.add(group);
		groupAuthorisation.setAuthorisation(auth);
		groupAuthorisation.setUserGroup(group);
		group.setAuthorisations(authorisations);
		
		assertFalse(user.hasAdminAuthorisations());
		
		authorisations.add(groupAuthorisation);
		
		assertTrue(user.hasAdminAuthorisations());
		
		((UserMetadata) user).setEnabled(false);
		
		assertFalse(user.hasAdminAuthorisations());
	}
	
	@Test
	void testIsAuthorised() {
		final User user = new UserMetadata();
		final UserGroupAuthorisation groupAuthorisation = new UserGroupAuthorisation();
		final UserGroupMetadata group = new UserGroupMetadata();
		final List<UserGroupAuthorisation> authorisations = new ArrayList<>();
		final Authorisation auth = Authorisation.ADMIN_MODULE;
		final Set<UserGroupMetadata> groups = new HashSet<>();
		((UserMetadata) user).setUserGroups(groups);
		((UserMetadata) user).setEnabled(true);
		groups.add(group);
		groupAuthorisation.setAuthorisation(auth);
		groupAuthorisation.setUserGroup(group);
		group.setAuthorisations(authorisations);
		
		assertFalse(user.isAuthorised(auth));
		
		authorisations.add(groupAuthorisation);
		
		assertTrue(user.isAuthorised(auth));
		
		((UserMetadata) user).setEnabled(false);
		
		assertFalse(user.isAuthorised(auth));
	}
	
}
