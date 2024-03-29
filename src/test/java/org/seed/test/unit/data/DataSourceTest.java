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

import org.seed.core.data.datasource.DataSourceMetadata;
import org.seed.core.data.datasource.DataSourceParameter;
import org.seed.core.data.datasource.DataSourceType;
import org.seed.core.data.datasource.IDataSource;

class DataSourceTest {
	
	@Test
	void testAddParameter() {
		final IDataSource dataSource = new DataSourceMetadata();
		final DataSourceParameter parameter = new DataSourceParameter();
		assertFalse(dataSource.hasParameters());
		
		dataSource.addParameter(parameter);
		
		assertEquals(dataSource, parameter.getDataSource());
		
		assertTrue(dataSource.hasParameters());
		assertSame(1, dataSource.getParameters().size());
		assertSame(parameter, dataSource.getParameters().get(0));
	}
	
	@Test
	void testGetParameterByName() {
		final IDataSource dataSource = new DataSourceMetadata();
		final DataSourceParameter parameter = new DataSourceParameter();
		dataSource.addParameter(parameter);
		parameter.setName("other");
		
		assertNull(dataSource.getParameterByName("test"));
		
		parameter.setName("test");
		
		assertSame(parameter, dataSource.getParameterByName("test"));
	}
	
	@Test
	void testGetParameterByUid() {
		final IDataSource dataSource = new DataSourceMetadata();
		final DataSourceParameter parameter = new DataSourceParameter();
		dataSource.addParameter(parameter);
		parameter.setUid("other");
		
		assertNull(dataSource.getParameterByUid("test"));
		
		parameter.setUid("test");
		
		assertSame(parameter, dataSource.getParameterByUid("test"));
	}
	
	@Test
	void testGetContentParameterNames() {
		final IDataSource dataSource = new DataSourceMetadata();
		((DataSourceMetadata) dataSource).setContent("no param");
		((DataSourceMetadata) dataSource).setType(DataSourceType.SQL);
		assertTrue(dataSource.getContentParameterNames().isEmpty());
		
		((DataSourceMetadata) dataSource).setContent("Lorem ipsum{test}dolor sit amet");
		assertFalse(dataSource.getContentParameterNames().isEmpty());
		assertEquals("test", dataSource.getContentParameterNames().iterator().next());
		
		((DataSourceMetadata) dataSource).setType(DataSourceType.HQL);
		((DataSourceMetadata) dataSource).setContent("sed diam nonumy:test eirmod tempor");
		assertFalse(dataSource.getContentParameterNames().isEmpty());
		assertEquals("test", dataSource.getContentParameterNames().iterator().next());
	}
	
	@Test
	void testIsEqual() {
		final IDataSource dataSource1 = new DataSourceMetadata();
		final IDataSource dataSource2 = new DataSourceMetadata();
		assertTrue(dataSource1.isEqual(dataSource2));
		
		dataSource1.setName("test");
		((DataSourceMetadata) dataSource1).setContent("content");
		((DataSourceMetadata) dataSource1).setType(DataSourceType.SQL);
		assertFalse(dataSource1.isEqual(dataSource2));
		
		dataSource2.setName("test");
		((DataSourceMetadata) dataSource2).setContent("content");
		((DataSourceMetadata) dataSource2).setType(DataSourceType.SQL);
		assertTrue(dataSource1.isEqual(dataSource2));
	}
	
	@Test
	void testIsEqualParameters() {
		final IDataSource dataSource1 = new DataSourceMetadata();
		final IDataSource dataSource2 = new DataSourceMetadata();
		final DataSourceParameter parameter1 = new DataSourceParameter();
		final DataSourceParameter parameter2 = new DataSourceParameter();
		parameter1.setUid("test");
		parameter2.setUid("test");
		dataSource1.addParameter(parameter1);
		assertFalse(dataSource1.isEqual(dataSource2));
		
		dataSource2.addParameter(parameter2);
		assertTrue(dataSource1.isEqual(dataSource2));
		
		parameter2.setUid("other");
		assertFalse(dataSource1.isEqual(dataSource2));
	}
	
	@Test
	void testRemoveParameter() {
		final IDataSource dataSource = new DataSourceMetadata();
		final DataSourceParameter parameter = new DataSourceParameter();
		dataSource.addParameter(parameter);
		assertSame(1, dataSource.getParameters().size());
		
		dataSource.removeParameter(parameter);
		
		assertFalse(dataSource.hasParameters());
		assertSame(0, dataSource.getParameters().size());
	}
	
}
