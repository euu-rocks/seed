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
import java.util.List;

import org.junit.jupiter.api.Test;

import org.seed.core.user.Authorisation;
import org.seed.core.user.UserGroupAuthorisation;
import org.seed.core.user.UserGroupMetadata;

class UserGroupTest {
	
	@Test
	void testGetAuthorisationByUid() {
		final UserGroupMetadata group = new UserGroupMetadata();
		final UserGroupAuthorisation groupAuthorisation = new UserGroupAuthorisation();
		final List<UserGroupAuthorisation> authorisations = new ArrayList<>();
		group.setAuthorisations(authorisations);
		authorisations.add(groupAuthorisation);
		groupAuthorisation.setUid("other");
		
		assertNull(group.getAuthorisationByUid("test"));
		
		groupAuthorisation.setUid("test");
		
		assertSame(groupAuthorisation, group.getAuthorisationByUid("test"));
	}
	
	@Test
	void testIsAuthorised() {
		final UserGroupMetadata group = new UserGroupMetadata();
		final UserGroupAuthorisation groupAuthorisation = new UserGroupAuthorisation();
		final List<UserGroupAuthorisation> authorisations = new ArrayList<>();
		final Authorisation auth = Authorisation.ADMIN_MODULE;
		group.setAuthorisations(authorisations);
		authorisations.add(groupAuthorisation);
		
		assertFalse(group.isAuthorised(auth));
		
		groupAuthorisation.setAuthorisation(auth);
		
		assertTrue(group.isAuthorised(auth));
	}
	
	@Test
	void testIsEqual() {
		final UserGroupMetadata group1 = new UserGroupMetadata();
		final UserGroupMetadata group2 = new UserGroupMetadata();
		assertTrue(group1.isEqual(group2));
		
		group1.setName("test");
		group1.setSystemGroup(true);
		assertFalse(group1.isEqual(group2));
		
		group2.setName("test");
		group2.setSystemGroup(true);
		assertTrue(group1.isEqual(group2));
	}
	
	@Test
	void testIsEqualAuthorisations() {
		final UserGroupMetadata group1 = new UserGroupMetadata();
		final UserGroupMetadata group2 = new UserGroupMetadata();
		final UserGroupAuthorisation groupAuthorisation1 = new UserGroupAuthorisation();
		final UserGroupAuthorisation groupAuthorisation2 = new UserGroupAuthorisation();
		final List<UserGroupAuthorisation> authorisations1 = new ArrayList<>();
		final List<UserGroupAuthorisation> authorisations2 = new ArrayList<>();
		authorisations1.add(groupAuthorisation1);
		authorisations2.add(groupAuthorisation2);
		group1.setAuthorisations(authorisations1);
		groupAuthorisation1.setUid("test");
		groupAuthorisation2.setUid("test");
		assertFalse(group1.isEqual(group2));
		
		group2.setAuthorisations(authorisations2);
		assertTrue(group1.isEqual(group2));
		
		groupAuthorisation2.setUid("other");
		assertFalse(group1.isEqual(group2));
	}
}
