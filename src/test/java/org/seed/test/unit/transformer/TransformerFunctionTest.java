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
package org.seed.test.unit.transformer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerFunction;
import org.seed.core.entity.transform.TransformerMetadata;

class TransformerFunctionTest {
	
	@Test
	void testGetGeneratedPackage() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerFunction function = new TransformerFunction();
		transformer.addFunction(function);
		transformer.setName("TÄST");
		
		assertEquals("org.seed.generated.transform.taest", function.getGeneratedPackage());
	}
	
	@Test
	void testGetGeneratedClass() {
		final EntityFunction function = new EntityFunction();
		function.setName("tästSomething");
		
		assertEquals("TaestSomething", function.getGeneratedClass());
	}
	
	@Test
	void testIsEqual() {
		final TransformerFunction function1 = new TransformerFunction();
		final TransformerFunction function2 = new TransformerFunction();
		assertTrue(function1.isEqual(function2));
		
		function1.setName("test");
		function1.setContent("content");
		function1.setActiveBeforeTransformation(true);
		function1.setActiveAfterTransformation(true);
		assertFalse(function1.isEqual(function2));
		
		function2.setName("test");
		function2.setContent("content");
		function2.setActiveBeforeTransformation(true);
		function2.setActiveAfterTransformation(true);
		assertTrue(function1.isEqual(function2));
		
	}
}
