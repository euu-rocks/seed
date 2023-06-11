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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.seed.core.entity.EntityField;
import org.seed.core.entity.transfer.CharEncoding;
import org.seed.core.entity.transfer.Newline;
import org.seed.core.entity.transfer.Transfer;
import org.seed.core.entity.transfer.TransferElement;
import org.seed.core.entity.transfer.TransferFormat;
import org.seed.core.entity.transfer.TransferMetadata;
import org.seed.core.entity.transfer.TransferPermission;

class TransferTest {
	
	@Test
	void testContainsField() {
		final Transfer transfer = new TransferMetadata();
		final TransferElement element = new TransferElement();
		final List<TransferElement> elements = new ArrayList<>();
		final EntityField entityField = new EntityField();
		elements.add(element);
		element.setEntityField(entityField);
		assertFalse(transfer.containsField(entityField));
		
		((TransferMetadata) transfer).setElements(elements);
		assertTrue(transfer.containsField(entityField));
		
		assertFalse(transfer.containsField(new EntityField()));
	}
	
	@Test
	void testGetElementByUid() {
		final Transfer transfer = new TransferMetadata();
		final TransferElement element = new TransferElement();
		final List<TransferElement> elements = new ArrayList<>();
		elements.add(element);
		element.setUid("other");
		((TransferMetadata) transfer).setElements(elements);
		
		assertNull(transfer.getElementByUid("test"));
		
		element.setUid("test");
		
		assertSame(element, transfer.getElementByUid("test"));
	}
	
	@Test
	void testGetPermissionByUid( ) {
		final Transfer transfer = new TransferMetadata();
		final TransferPermission permission = new TransferPermission();
		final List<TransferPermission> permissions = new ArrayList<>();
		permission.setUid("other");
		permissions.add(permission);
		((TransferMetadata) transfer).setPermissions(permissions);
		
		assertNull(transfer.getPermissionByUid("test"));
		
		permission.setUid("test");
		
		assertSame(permission, transfer.getPermissionByUid("test"));
	}
	
	@Test
	void testGetElementFields() {
		final Transfer transfer = new TransferMetadata();
		final EntityField entityField = new EntityField();
		final TransferElement element1 = new TransferElement();
		final TransferElement element2 = new TransferElement();
		final List<TransferElement> elements = new ArrayList<>();
		elements.add(element1);
		elements.add(element2);
		element1.setEntityField(entityField);
		((TransferMetadata) transfer).setElements(elements);
		
		assertSame(1, transfer.getElementFields().size());
		assertSame(entityField, transfer.getElementFields().get(0));
	}
	
	@Test
	void testGetIdentifierField() {
		final Transfer transfer = new TransferMetadata();
		final EntityField entityField = new EntityField();
		final TransferElement element1 = new TransferElement();
		final TransferElement element2 = new TransferElement();
		final List<TransferElement> elements = new ArrayList<>();
		elements.add(element1);
		elements.add(element2);
		((TransferMetadata) transfer).setElements(elements);
		assertNull(transfer.getIdentifierField());
		
		element2.setEntityField(entityField);
		element2.setIdentifier(true);
		assertSame(entityField, transfer.getIdentifierField());
	}
	
	@Test
	void testIsEqual() {
		final Transfer transfer1 = new TransferMetadata();
		final Transfer transfer2 = new TransferMetadata();
		assertTrue(transfer1.isEqual(transfer2));
		
		((TransferMetadata) transfer1).setFormat(TransferFormat.CSV);
		((TransferMetadata) transfer1).setSeparatorChar("separator");
		((TransferMetadata) transfer1).setQuoteChar("quote");
		((TransferMetadata) transfer1).setEscapeChar("escape");
		((TransferMetadata) transfer1).setEncoding(CharEncoding.UTF8);
		((TransferMetadata) transfer1).setNewline(Newline.LF);
		((TransferMetadata) transfer1).setQuoteAll(true);
		((TransferMetadata) transfer1).setHeader(true);
		assertFalse(transfer1.isEqual(transfer2));
		
		((TransferMetadata) transfer2).setFormat(TransferFormat.CSV);
		((TransferMetadata) transfer2).setSeparatorChar("separator");
		((TransferMetadata) transfer2).setQuoteChar("quote");
		((TransferMetadata) transfer2).setEscapeChar("escape");
		((TransferMetadata) transfer2).setEncoding(CharEncoding.UTF8);
		((TransferMetadata) transfer2).setNewline(Newline.LF);
		((TransferMetadata) transfer2).setQuoteAll(true);
		((TransferMetadata) transfer2).setHeader(true);
		assertTrue(transfer1.isEqual(transfer2));
	}
	
	@Test
	void testIsEqualElements() {
		final Transfer transfer1 = new TransferMetadata();
		final Transfer transfer2 = new TransferMetadata();
		final TransferElement element1 = new TransferElement();
		final TransferElement element2 = new TransferElement();
		final List<TransferElement> elements1 = new ArrayList<>();
		final List<TransferElement> elements2 = new ArrayList<>();
		((TransferMetadata) transfer1).setElements(elements1);
		elements1.add(element1);
		elements2.add(element2);
		element1.setUid("test");
		element2.setUid("test");
		assertFalse(transfer1.isEqual(transfer2));
		
		((TransferMetadata) transfer2).setElements(elements2);
		assertTrue(transfer1.isEqual(transfer2));
		
		element1.setUid("other");
		assertFalse(transfer1.isEqual(transfer2));
	}
	
	@Test
	void testIsEqualPermissions() {
		final Transfer transfer1 = new TransferMetadata();
		final Transfer transfer2 = new TransferMetadata();
		final TransferPermission permission1 = new TransferPermission();
		final TransferPermission permission2 = new TransferPermission();
		final List<TransferPermission> permissions1 = new ArrayList<>();
		final List<TransferPermission> permissions2 = new ArrayList<>();
		permissions1.add(permission1);
		permissions2.add(permission2);
		permission1.setUid("test");
		permission2.setUid("test");
		((TransferMetadata) transfer1).setPermissions(permissions1);
		assertFalse(transfer1.isEqual(transfer2));
		
		((TransferMetadata) transfer2).setPermissions(permissions2);
		assertTrue(transfer1.isEqual(transfer2));
		
		permission2.setUid("other");
		assertFalse(transfer1.isEqual(transfer2));
	}
	
}
