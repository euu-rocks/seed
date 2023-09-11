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

class DBObjectTest {
	
	@Test
	void testContains() {
		final DBObject dbObject = new DBObjectMetadata();
		assertFalse(dbObject.contains("test"));
		
		((DBObjectMetadata) dbObject).setContent("This is a test");
		assertTrue(dbObject.contains("test"));
		assertFalse(dbObject.contains("other"));
	}
	
	@Test
	void testContainsDBObject() {
		final DBObject dbObject1 = new DBObjectMetadata();
		final DBObject dbObject2 = new DBObjectMetadata();
		dbObject2.setName("Test");
		assertFalse(dbObject1.contains(dbObject2));
		
		((DBObjectMetadata) dbObject1).setContent("select * from test");
		assertTrue(dbObject1.contains(dbObject2));
		
		((DBObjectMetadata) dbObject1).setContent("select * from testtable");
		assertFalse(dbObject1.contains(dbObject2));
		((DBObjectMetadata) dbObject1).setContent("select * from table.test");
		assertFalse(dbObject1.contains(dbObject2));
		((DBObjectMetadata) dbObject1).setContent("select * from test-table");
		assertFalse(dbObject1.contains(dbObject2));
	}
	
	@Test
	void testIsEqual() {
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
	
	@Test
	void testIsEnabled() {
		final DBObject dbObject = new DBObjectMetadata();
		assertFalse(dbObject.isEnabled());
		((DBObjectMetadata) dbObject).setOrder(1);
		assertTrue(dbObject.isEnabled());
		((DBObjectMetadata) dbObject).setOrder(0);
		assertFalse(dbObject.isEnabled());
		((DBObjectMetadata) dbObject).setOrder(-1);
		assertFalse(dbObject.isEnabled());
	}
	
	@Test
	void testIsOrderHigherThan() {
		final DBObject dbObject1 = new DBObjectMetadata();
		final DBObject dbObject2 = new DBObjectMetadata();
		assertFalse(dbObject1.isOrderHigherThan(dbObject2));
		assertFalse(dbObject2.isOrderHigherThan(dbObject1));
		
		((DBObjectMetadata) dbObject1).setOrder(1);
		assertTrue(dbObject1.isOrderHigherThan(dbObject2));
		((DBObjectMetadata) dbObject2).setOrder(1);
		assertFalse(dbObject1.isOrderHigherThan(dbObject2));
		
		((DBObjectMetadata) dbObject1).setOrder(2);
		assertTrue(dbObject1.isOrderHigherThan(dbObject2));
		assertFalse(dbObject2.isOrderHigherThan(dbObject1));
	}
	
}
