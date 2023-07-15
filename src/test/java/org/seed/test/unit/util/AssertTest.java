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
package org.seed.test.unit.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.seed.core.util.Assert;

public class AssertTest {
	
	@Test
	void testNotNull() {
		var ex = assertThrows(IllegalArgumentException.class, () -> {
			Assert.notNull(null, null);
		});
		assertEquals("Object is null", ex.getMessage());
		
		ex = assertThrows(IllegalArgumentException.class, () -> {
			Assert.notNull(null, "test");
		});
		assertEquals("test is null", ex.getMessage());
		
		Assert.notNull(this, "test");
	}
	
	@Test
	void testHasText() {
		var ex = assertThrows(IllegalArgumentException.class, () -> {
			Assert.hasText(null, null);
		});
		assertEquals("Text has no content", ex.getMessage());
		
		ex = assertThrows(IllegalArgumentException.class, () -> {
			Assert.hasText(null, "test");
		});
		assertEquals("test has no content", ex.getMessage());
		
		ex = assertThrows(IllegalArgumentException.class, () -> {
			Assert.hasText("\t ", "test");
		});
		assertEquals("test has no content", ex.getMessage());
		
		Assert.hasText("test", "test");
	}
	
	@Test
	void testGreaterThanZero() {
		var ex = assertThrows(IllegalStateException.class, () -> {
			Assert.greaterThanZero(-1, null);
		});
		assertEquals("Illegal number -1", ex.getMessage());
		
		ex = assertThrows(IllegalStateException.class, () -> {
			Assert.greaterThanZero(0, "test");
		});
		assertEquals("Illegal test 0", ex.getMessage());
		
		Assert.greaterThanZero(1, "test");
	}
	
	@Test
	void testState() {
		var ex = assertThrows(IllegalStateException.class, () -> {
			Assert.state(false, null);
		});
		assertEquals("An invalid state has occurred", ex.getMessage());
		
		ex = assertThrows(IllegalStateException.class, () -> {
			Assert.state(false, "test");
		});
		assertEquals("test", ex.getMessage());
		
		Assert.state(true, "test");
	}
	
	@Test
	void testStateAvailable() {
		var ex = assertThrows(IllegalStateException.class, () -> {
			Assert.stateAvailable(null, null);
		});
		assertEquals("Object not available", ex.getMessage());
		
		ex = assertThrows(IllegalStateException.class, () -> {
			Assert.stateAvailable(null, "test");
		});
		assertEquals("test not available", ex.getMessage());
		
		Assert.stateAvailable(this, "test");
	}
	
	@Test
	void testStateIllegal() {
		var ex = assertThrows(IllegalStateException.class, () -> {
			Assert.stateIllegal(null);
		});
		assertEquals("An invalid state has occurred", ex.getMessage());
		
		ex = assertThrows(IllegalStateException.class, () -> {
			Assert.stateIllegal("test");
		});
		assertEquals("test", ex.getMessage());
	}
	
}
