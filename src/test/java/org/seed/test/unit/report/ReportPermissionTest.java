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
package org.seed.test.unit.report;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.seed.core.report.ReportPermission;
import org.seed.core.user.UserGroupMetadata;

class ReportPermissionTest {
	
	@Test
	void testGetUserGroupUid() {
		final ReportPermission permission = new ReportPermission();
		final UserGroupMetadata group = new UserGroupMetadata();
		assertNull(permission.getUserGroupUid());
		
		permission.setUserGroupUid("test");
		assertEquals("test", permission.getUserGroupUid());
		
		permission.setUserGroup(group);
		group.setUid("group");
		assertEquals("group", permission.getUserGroupUid());
	}
	
	@Test
	void testIsEqual() {
		final ReportPermission permission1 = new ReportPermission();
		final ReportPermission permission2 = new ReportPermission();
		assertTrue(permission1.isEqual(permission2));
		
		permission1.setUserGroupUid("test");
		assertFalse(permission1.isEqual(permission2));
		
		permission2.setUserGroupUid("test");
		assertTrue(permission1.isEqual(permission2));
		
		permission2.setUserGroupUid("other");
		assertFalse(permission1.isEqual(permission2));
	}
}
