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
package org.seed.test.unit.task;

import org.junit.jupiter.api.Test;

import org.seed.core.task.TaskParameter;

import static org.junit.jupiter.api.Assertions.*;

class TaskParameterTest {
	
	@Test
	void testIsEqual() {
		final TaskParameter parameter1 = new TaskParameter();
		final TaskParameter parameter2 = new TaskParameter();
		assertTrue(parameter1.isEqual(parameter2));
		
		parameter1.setName("test");
		parameter1.setValue("value");
		assertFalse(parameter1.isEqual(parameter2));
		
		parameter2.setName("test");
		parameter2.setValue("value");
		assertTrue(parameter1.isEqual(parameter2));
	}
	
}
