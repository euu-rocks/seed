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
package org.seed.test.unit.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.seed.core.data.dbobject.DBObject;
import org.seed.core.data.dbobject.DBObjectMetadata;
import org.seed.core.data.dbobject.DBObjectType;

public class DBObjectTest {
	
	@Test
	public void testContains() {
		final DBObject dbObject = new DBObjectMetadata();
		assertFalse(dbObject.contains("test"));
		
		((DBObjectMetadata) dbObject).setContent("Das ist ein Test");
		assertTrue(dbObject.contains("test"));
		assertFalse(dbObject.contains("other"));
	}
	
	@Test
	public void testIsEqual() {
		final DBObject dbObject1 = new DBObjectMetadata();
		final DBObject dbObject2 = new DBObjectMetadata();
		assertTrue(dbObject1.isEqual(dbObject2));
		
		dbObject1.setName("test");
		((DBObjectMetadata) dbObject1).setType(DBObjectType.VIEW);
		((DBObjectMetadata) dbObject1).setOrder(123);
		((DBObjectMetadata) dbObject1).setContent("content");
		assertFalse(dbObject1.isEqual(dbObject2));
		
		dbObject2.setName("test");
		((DBObjectMetadata) dbObject2).setType(DBObjectType.VIEW);
		((DBObjectMetadata) dbObject2).setOrder(123);
		((DBObjectMetadata) dbObject2).setContent("content");
		assertTrue(dbObject1.isEqual(dbObject2));
	}
	
}
