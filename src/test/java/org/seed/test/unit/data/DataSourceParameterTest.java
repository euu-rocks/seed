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

import org.seed.core.data.datasource.DataSourceParameter;
import org.seed.core.data.datasource.DataSourceParameterType;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityMetadata;

class DataSourceParameterTest {
	
	@Test
	void testGetReferenceEntityUid() {
		final DataSourceParameter parameter = new DataSourceParameter();
		final Entity referenceEntity = new EntityMetadata();
		referenceEntity.setUid("ref");
		assertNull(parameter.getReferenceEntityUid());
		
		parameter.setReferenceEntityUid("test");
		assertEquals("test", parameter.getReferenceEntityUid());
		
		parameter.setReferenceEntity(referenceEntity);
		assertEquals("ref", parameter.getReferenceEntityUid());
	}
	
	@Test
	void testIsEqual() {
		final DataSourceParameter parameter1 = new DataSourceParameter();
		final DataSourceParameter parameter2 = new DataSourceParameter();
		final Entity referenceEntity = new EntityMetadata();
		assertTrue(parameter1.isEqual(parameter2));
		
		parameter1.setName("test");
		parameter1.setReferenceEntity(referenceEntity);
		parameter1.setType(DataSourceParameterType.TEXT);
		assertFalse(parameter1.isEqual(parameter2));
		
		parameter2.setName("test");
		parameter2.setReferenceEntity(referenceEntity);
		parameter2.setType(DataSourceParameterType.TEXT);
		assertTrue(parameter1.isEqual(parameter2));
	}
}
