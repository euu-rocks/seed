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

import org.seed.core.form.FormTransformer;

public class FormTransformerTest {
	
	@Test
	public void testIsEqual() {
		final FormTransformer transformer1 = new FormTransformer();
		final FormTransformer transformer2 = new FormTransformer();
		assertTrue(transformer1.isEqual(transformer2));
		
		transformer1.setTransformerUid("transformer");
		transformer1.setTargetFormUid("targetForm");
		transformer1.setLabel("label");
		assertFalse(transformer1.isEqual(transformer2));
		
		transformer2.setTransformerUid("transformer");
		transformer2.setTargetFormUid("targetForm");
		transformer2.setLabel("label");
		assertTrue(transformer1.isEqual(transformer2));
	}
}
