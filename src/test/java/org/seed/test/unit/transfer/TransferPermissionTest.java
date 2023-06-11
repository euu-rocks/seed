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
package org.seed.test.unit.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.seed.core.entity.transfer.TransferAccess;
import org.seed.core.entity.transfer.TransferPermission;
import org.seed.core.user.UserGroupMetadata;

class TransferPermissionTest {
	
	@Test
	void testGetUserGroupUid() {
		final TransferPermission permission = new TransferPermission();
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
		final TransferPermission permission1 = new TransferPermission();
		final TransferPermission permission2 = new TransferPermission();
		assertTrue(permission1.isEqual(permission2));
		
		permission1.setUserGroupUid("test");
		assertFalse(permission1.isEqual(permission2));
		
		permission2.setUserGroupUid("test");
		assertTrue(permission1.isEqual(permission2));
		
		permission2.setAccess(TransferAccess.IMPORT);
		assertFalse(permission1.isEqual(permission2));
	}
	
}
