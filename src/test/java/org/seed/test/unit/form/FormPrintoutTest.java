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
package org.seed.test.unit.form;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.seed.core.form.FormPrintout;

public class FormPrintoutTest {
	
	@Test
	public void testIsEqual() {
		final FormPrintout printout1 = new FormPrintout();
		final FormPrintout printout2 = new FormPrintout();
		final byte[] content = new byte[0];
		assertTrue(printout1.isEqual(printout2));
		
		printout1.setName("test");
		printout1.setFileName("fieldName");
		printout1.setContentType("contentType");
		printout1.setContent(content);
		assertFalse(printout1.isEqual(printout2));
		
		printout2.setName("test");
		printout2.setFileName("fieldName");
		printout2.setContentType("contentType");
		printout2.setContent(content);
		assertTrue(printout1.isEqual(printout2));
	}
}
