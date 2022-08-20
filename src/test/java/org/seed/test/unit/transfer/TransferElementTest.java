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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.seed.core.entity.EntityField;
import org.seed.core.entity.transfer.TransferElement;

public class TransferElementTest {
	
	@Test
	public void testIsEqual() {
		final TransferElement element1 = new TransferElement();
		final TransferElement element2 = new TransferElement();
		final EntityField field = new EntityField();
		field.setUid("test");
		assertTrue(element1.isEqual(element2));
		
		element1.setFieldUid("test");
		element1.setIdentifier(true);
		assertFalse(element1.isEqual(element2));
		
		element2.setEntityField(field);
		element2.setIdentifier(true);
		assertTrue(element1.isEqual(element2));
	}
}
