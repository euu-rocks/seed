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

import org.seed.core.entity.EntityStatus;

public class EntityStatusTest {
	
	@Test
	public void testGetNumberAndName() {
		final EntityStatus status = new EntityStatus();
		assertEquals("", status.getNumberAndName());
		
		status.setName("Name");
		assertEquals("Name", status.getNumberAndName());
		
		status.setStatusNumber(123);
		assertEquals("123 Name", status.getNumberAndName());
	}
	
	@Test
	public void testIsEqual() {
		final EntityStatus status1 = new EntityStatus();
		final EntityStatus status2 = new EntityStatus();
		assertTrue(status1.isEqual(status2));
		
		status1.setName("name");
		status1.setStatusNumber(123);
		status1.setDescription("desc");
		status1.setInitial(true);
		assertFalse(status1.isEqual(status2));
		
		status2.setName("name");
		status2.setStatusNumber(123);
		status2.setDescription("desc");
		status2.setInitial(true);
		assertTrue(status1.isEqual(status2));
	}
	
}
