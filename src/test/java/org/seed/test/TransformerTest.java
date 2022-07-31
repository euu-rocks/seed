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
package org.seed.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerElement;
import org.seed.core.entity.transform.TransformerFunction;
import org.seed.core.entity.transform.TransformerMetadata;
import org.seed.core.entity.transform.TransformerPermission;
import org.seed.core.entity.transform.TransformerStatus;

public class TransformerTest {
	
	@Test
	public void testAddElement() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerElement element = new TransformerElement();
		assertFalse(transformer.hasElements());
		
		transformer.addElement(element);
		
		assertSame(element.getTransformer(), transformer);
		assertTrue(transformer.hasElements());
		assertEquals(transformer.getElements().size(), 1);
		assertEquals(transformer.getElements().get(0), element);
	}
	
	@Test
	public void testAddFunction() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerFunction function = new TransformerFunction();
		assertFalse(transformer.hasFunctions());
		
		transformer.addFunction(function);
		
		assertSame(function.getTransformer(), transformer);
		assertTrue(transformer.hasFunctions());
		assertEquals(transformer.getFunctions().size(), 1);
		assertEquals(transformer.getFunctions().get(0), function);
	}
	
	@Test
	public void testContainsElement() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerElement element = new TransformerElement();
		assertFalse(transformer.containsElement(element));
		
		transformer.addElement(element);
		
		assertTrue(transformer.containsElement(element));
	}
	
	@Test
	public void testContainsStatus() {
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
	public void testGetElementByUid() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerElement element = new TransformerElement();
		element.setUid("other");
		transformer.addElement(element);
		
		assertNull(transformer.getElementByUid("test"));
		
		element.setUid("test");
		
		assertSame(transformer.getElementByUid("test"), element);
	}
	
	@Test
	public void testGetFunctionByUid() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerFunction function = new TransformerFunction();
		function.setUid("other");
		transformer.addFunction(function);
		
		assertNull(transformer.getFunctionByUid("test"));
		
		function.setUid("test");
		
		assertSame(transformer.getFunctionByUid("test"), function);
	}
	
	@Test
	public void testGetPermissionByUid( ) {
		final Transformer transformer = new TransformerMetadata();
		final TransformerPermission permission = new TransformerPermission();
		final List<TransformerPermission> permissions = new ArrayList<>();
		permission.setUid("other");
		permissions.add(permission);
		((TransformerMetadata) transformer).setPermissions(permissions);
		
		assertNull(transformer.getPermissionByUid("test"));
		
		permission.setUid("test");
		
		assertSame(transformer.getPermissionByUid("test"), permission);
	}
	
	@Test
	public void testGetStatusByUid() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerStatus status = new TransformerStatus();
		final List<TransformerStatus> statusList = new ArrayList<>();
		((TransformerMetadata)transformer).setStatus(statusList);
		status.setUid("other");
		statusList.add(status);
		
		assertNull(transformer.getStatusByUid("test"));
		
		status.setUid("test");
		
		assertSame(transformer.getStatusByUid("test"), status);
	}
	
	@Test
	public void testRemoveElement() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerElement element = new TransformerElement();
		transformer.addElement(element);
		assertEquals(transformer.getElements().size(), 1);
		
		transformer.removeElement(element);
		
		assertFalse(transformer.hasElements());
		assertEquals(transformer.getElements().size(), 0);
	}
	
	@Test
	public void testRemoveFunction() {
		final Transformer transformer = new TransformerMetadata();
		final TransformerFunction function = new TransformerFunction();
		transformer.addFunction(function);
		assertEquals(transformer.getFunctions().size(), 1);
		
		transformer.removeFunction(function);
		
		assertFalse(transformer.hasFunctions());
		assertEquals(transformer.getFunctions().size(), 0);
	}
	
}
