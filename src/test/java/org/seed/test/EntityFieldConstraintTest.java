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
package org.seed.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.seed.core.data.FieldAccess;
import org.seed.core.entity.EntityFieldConstraint;

public class EntityFieldConstraintTest {
	
	@Test
	public void testIsEqual() {
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
