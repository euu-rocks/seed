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
}
