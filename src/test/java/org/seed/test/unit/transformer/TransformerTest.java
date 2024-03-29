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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityMetadata;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerElement;
import org.seed.core.entity.transform.TransformerFunction;
import org.seed.core.entity.transform.TransformerMetadata;
import org.seed.core.entity.transform.TransformerPermission;
import org.seed.core.entity.transform.TransformerStatus;

class TransformerTest {
	
	@Test
	void testAddElement() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerElement element = new TransformerElement();
		assertFalse(transformer.hasElements());
		
		transformer.addElement(element);
		
		assertSame(transformer, element.getTransformer());
		assertTrue(transformer.hasElements());
		assertEquals(1, transformer.getElements().size());
		assertEquals(element, transformer.getElements().get(0));
	}
	
	@Test
	void testAddFunction() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerFunction function = new TransformerFunction();
		assertFalse(transformer.hasFunctions());
		
		transformer.addFunction(function);
		
		assertSame(transformer, function.getTransformer());
		assertTrue(transformer.hasFunctions());
		assertEquals(1, transformer.getFunctions().size());
		assertEquals(function, transformer.getFunctions().get(0));
	}
	
	@Test
	void testContainsElement() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerElement element = new TransformerElement();
		assertFalse(transformer.containsElement(element));
		
		transformer.addElement(element);
		
		assertTrue(transformer.containsElement(element));
	}
	
	@Test
	void testContainsStatus() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerStatus status = new TransformerStatus();
		final EntityStatus entityStatus = new EntityStatus();
		final List<TransformerStatus> statusList = new ArrayList<>();
		((TransformerMetadata)transformer).setStatus(statusList);
		status.setStatus(entityStatus);
		assertFalse(transformer.containsStatus(entityStatus));
		
		statusList.add(status);
		
		assertTrue(transformer.containsStatus(entityStatus));
	}
	
	@Test
	void testGetElementByUid() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerElement element = new TransformerElement();
		element.setUid("other");
		transformer.addElement(element);
		
		assertNull(transformer.getElementByUid("test"));
		
		element.setUid("test");
		
		assertSame(element, transformer.getElementByUid("test"));
	}
	
	@Test
	void testGetFunctionByUid() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerFunction function = new TransformerFunction();
		function.setUid("other");
		transformer.addFunction(function);
		
		assertNull(transformer.getFunctionByUid("test"));
		
		function.setUid("test");
		
		assertSame(function, transformer.getFunctionByUid("test"));
	}
	
	@Test
	void testGetPermissionByUid( ) {
		final Transformer transformer = new TransformerMetadata();
		final TransformerPermission permission = new TransformerPermission();
		final List<TransformerPermission> permissions = new ArrayList<>();
		permission.setUid("other");
		permissions.add(permission);
		((TransformerMetadata) transformer).setPermissions(permissions);
		
		assertNull(transformer.getPermissionByUid("test"));
		
		permission.setUid("test");
		
		assertSame(permission, transformer.getPermissionByUid("test"));
	}
	
	@Test
	void testGetStatusByUid() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerStatus status = new TransformerStatus();
		final List<TransformerStatus> statusList = new ArrayList<>();
		((TransformerMetadata) transformer).setStatus(statusList);
		status.setUid("other");
		statusList.add(status);
		
		assertNull(transformer.getStatusByUid("test"));
		
		status.setUid("test");
		
		assertSame(status, transformer.getStatusByUid("test"));
	}
	
	@Test
	void testGetSourceEntityUid() {
		final Transformer transformer = new TransformerMetadata();
		final Entity sourceEntity = new EntityMetadata();
		assertNull(transformer.getSourceEntityUid());
		
		((TransformerMetadata) transformer).setSourceEntityUid("test");
		assertEquals("test", transformer.getSourceEntityUid());
		
		sourceEntity.setUid("source");
		((TransformerMetadata) transformer).setSourceEntity(sourceEntity);
		assertEquals("source", transformer.getSourceEntityUid());
	}
	
	@Test
	void testGetTargetEntityUid() {
		final Transformer transformer = new TransformerMetadata();
		final Entity targetEntity = new EntityMetadata();
		assertNull(transformer.getTargetEntityUid());
		
		((TransformerMetadata) transformer).setTargetEntityUid("test");
		assertEquals("test", transformer.getTargetEntityUid());
		
		targetEntity.setUid("target");
		((TransformerMetadata) transformer).setTargetEntity(targetEntity);
		assertEquals("target", transformer.getTargetEntityUid());
	}
	
	@Test
	void testIsEqual() {
		final Transformer transformer1 = new TransformerMetadata();
		final Transformer transformer2 = new TransformerMetadata();
		assertTrue(transformer1.isEqual(transformer2));
		
		((TransformerMetadata) transformer1).setSourceEntityUid("source");
		((TransformerMetadata) transformer1).setTargetEntityUid("target");
		assertFalse(transformer1.isEqual(transformer2));
		
		((TransformerMetadata) transformer2).setSourceEntityUid("source");
		((TransformerMetadata) transformer2).setTargetEntityUid("target");
		assertTrue(transformer1.isEqual(transformer2));
	}
	
	@Test
	void testIsEqualElements() {
		final Transformer transformer1 = new TransformerMetadata();
		final Transformer transformer2 = new TransformerMetadata();
		final TransformerElement element1 = new TransformerElement();
		final TransformerElement element2 = new TransformerElement();
		element1.setUid("test");
		element2.setUid("test");
		transformer1.addElement(element1);
		assertFalse(transformer1.isEqual(transformer2));
		
		transformer2.addElement(element2);
		assertTrue(transformer1.isEqual(transformer2));
		
		element2.setUid("other");
		assertFalse(transformer1.isEqual(transformer2));
	}
	
	@Test
	void testIsEqualFunctions() {
		final Transformer transformer1 = new TransformerMetadata();
		final Transformer transformer2 = new TransformerMetadata();
		final TransformerFunction function1 = new TransformerFunction();
		final TransformerFunction function2 = new TransformerFunction();
		function1.setUid("test");
		function2.setUid("test");
		transformer1.addFunction(function1);
		assertFalse(transformer1.isEqual(transformer2));
		
		transformer2.addFunction(function2);
		assertTrue(transformer1.isEqual(transformer2));
		
		function2.setUid("other");
		assertFalse(transformer1.isEqual(transformer2));
	}
	
	@Test
	void testIsEqualPermissions() {
		final Transformer transformer1 = new TransformerMetadata();
		final Transformer transformer2 = new TransformerMetadata();
		final TransformerPermission permission1 = new TransformerPermission();
		final TransformerPermission permission2 = new TransformerPermission();
		final List<TransformerPermission> permissions1 = new ArrayList<>();
		final List<TransformerPermission> permissions2 = new ArrayList<>();
		permissions1.add(permission1);
		permissions2.add(permission2);
		permission1.setUid("test");
		permission2.setUid("test");
		((TransformerMetadata) transformer1).setPermissions(permissions1);
		assertFalse(transformer1.isEqual(transformer2));
		
		((TransformerMetadata) transformer2).setPermissions(permissions2);
		assertTrue(transformer1.isEqual(transformer2));
		
		permission2.setUid("other");
		assertFalse(transformer1.isEqual(transformer2));
	}
	
	@Test
	void testIsEqualStatus() {
		final Transformer transformer1 = new TransformerMetadata();
		final Transformer transformer2 = new TransformerMetadata();
		final TransformerStatus status1 = new TransformerStatus();
		final TransformerStatus status2 = new TransformerStatus();
		final List<TransformerStatus> statusList1 = new ArrayList<>();
		final List<TransformerStatus> statusList2 = new ArrayList<>();
		statusList1.add(status1);
		statusList2.add(status2);
		status1.setUid("test");
		status2.setUid("test");
		((TransformerMetadata) transformer1).setStatus(statusList1);
		assertFalse(transformer1.isEqual(transformer2));
		
		((TransformerMetadata) transformer2).setStatus(statusList2);
		assertTrue(transformer1.isEqual(transformer2));
		
		status2.setUid("other");
		assertFalse(transformer1.isEqual(transformer2));
	}
	
	@Test
	void testRemoveElement() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerElement element = new TransformerElement();
		transformer.addElement(element);
		assertEquals(1, transformer.getElements().size());
		
		transformer.removeElement(element);
		
		assertFalse(transformer.hasElements());
		assertEquals(0, transformer.getElements().size());
	}
	
	@Test
	void testRemoveFunction() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerFunction function = new TransformerFunction();
		transformer.addFunction(function);
		assertEquals(1, transformer.getFunctions().size());
		
		transformer.removeFunction(function);
		
		assertFalse(transformer.hasFunctions());
		assertEquals(0, transformer.getFunctions().size());
	}
	
}
