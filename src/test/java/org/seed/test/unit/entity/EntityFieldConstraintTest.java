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
package org.seed.test.unit.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.seed.core.data.FieldAccess;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldConstraint;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityStatus;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupMetadata;

class EntityFieldConstraintTest {
	
	@Test
	void testGetFieldUid() {
		final EntityFieldConstraint constraint = new EntityFieldConstraint();
		final EntityField field = new EntityField();
		constraint.setFieldUid("test");
		assertEquals("test", constraint.getFieldUid());
		
		field.setUid("field");
		constraint.setField(field);
		assertEquals("field", constraint.getFieldUid());
	}
	
	@Test
	void testGetFieldGroupUid() {
		final EntityFieldConstraint constraint = new EntityFieldConstraint();
		final EntityFieldGroup group = new EntityFieldGroup();
		constraint.setFieldGroupUid("test");
		assertEquals("test", constraint.getFieldGroupUid());
		
		group.setUid("group");
		constraint.setFieldGroup(group);
		assertEquals("group", constraint.getFieldGroupUid());
	}
	
	@Test
	void testGetStatusUid() {
		final EntityFieldConstraint constraint = new EntityFieldConstraint();
		final EntityStatus status = new EntityStatus();
		constraint.setStatusUid("test");
		assertEquals("test", constraint.getStatusUid());
		
		status.setUid("status");
		constraint.setStatus(status);
		assertEquals("status", constraint.getStatusUid());
	}
	
	@Test
	void testGetUserGroupUid() {
		final EntityFieldConstraint constraint = new EntityFieldConstraint();
		final UserGroup userGroup = new UserGroupMetadata();
		constraint.setUserGroupUid("test");
		assertEquals("test", constraint.getUserGroupUid());
		
		userGroup.setUid("group");
		constraint.setUserGroup(userGroup);
		assertEquals("group", constraint.getUserGroupUid());
	}
	
	@Test
	void testIsEqual() {
		final EntityFieldConstraint constraint1 = new EntityFieldConstraint();
		final EntityFieldConstraint constraint2 = new EntityFieldConstraint();
		assertTrue(constraint1.isEqual(constraint2));
		
		constraint1.setFieldUid("fieldUid");
		constraint1.setFieldGroupUid("goupUid");
		constraint1.setStatusUid("statusUid");
		constraint1.setUserGroupUid("userGroupUid");
		constraint1.setAccess(FieldAccess.READ);
		constraint1.setMandatory(true);
		assertFalse(constraint1.isEqual(constraint2));
		
		constraint2.setFieldUid("fieldUid");
		constraint2.setFieldGroupUid("goupUid");
		constraint2.setStatusUid("statusUid");
		constraint2.setUserGroupUid("userGroupUid");
		constraint2.setAccess(FieldAccess.READ);
		constraint2.setMandatory(true);
		assertTrue(constraint1.isEqual(constraint2));
	}
}
