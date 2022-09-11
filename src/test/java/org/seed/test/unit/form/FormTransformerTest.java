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

import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerMetadata;
import org.seed.core.form.FormMetadata;
import org.seed.core.form.FormTransformer;

class FormTransformerTest {
	
	@Test
	void testGetName() {
		final FormTransformer formTransformer = new FormTransformer();
		final Transformer transformer = new TransformerMetadata();
		formTransformer.setTransformer(transformer);
		assertNull(formTransformer.getName());
		
		transformer.setName("test");
		assertEquals("test", formTransformer.getName());
		
		formTransformer.setLabel("label");
		assertEquals("label", formTransformer.getName());
	}
	
	@Test
	void testGetTransformerUid() {
		final FormTransformer formTransformer = new FormTransformer();
		final Transformer transformer = new TransformerMetadata();
		assertNull(formTransformer.getTransformerUid());
		
		formTransformer.setTransformerUid("test");
		assertEquals("test", formTransformer.getTransformerUid());
		
		formTransformer.setTransformer(transformer);
		transformer.setUid("transform");
		assertEquals("transform", formTransformer.getTransformerUid());
	}
	
	@Test
	void testGetTargetFormUid() {
		final FormTransformer transformer = new FormTransformer();
		final FormMetadata targetForm = new FormMetadata();
		assertNull(transformer.getTargetFormUid());
		
		transformer.setTargetFormUid("test");
		assertEquals("test", transformer.getTargetFormUid());	
		
		transformer.setTargetForm(targetForm);
		targetForm.setUid("target");
		assertEquals("target", transformer.getTargetFormUid());	
	}
	
	@Test
	void testIsEqual() {
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
