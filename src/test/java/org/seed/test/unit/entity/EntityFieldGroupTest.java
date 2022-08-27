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

import org.seed.core.entity.EntityFieldGroup;

class EntityFieldGroupTest {

	@Test
	void testIsEqual() {
		final EntityFieldGroup group1 = new EntityFieldGroup();
		final EntityFieldGroup group2 = new EntityFieldGroup();
		assertTrue(group1.isEqual(group2));
		
		group1.setName("test");
		assertFalse(group1.isEqual(group2));
		
		group2.setName("test");
		assertTrue(group1.isEqual(group2));
	}
	
}
