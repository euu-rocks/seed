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

import org.junit.jupiter.api.Test;

import org.seed.core.data.datasource.DataSourceMetadata;
import org.seed.core.data.datasource.DataSourceParameter;
import org.seed.core.data.datasource.IDataSource;

public class DataSourceTest {
	
	@Test
	public void testAddParameter() {
		final IDataSource dataSource = new DataSourceMetadata();
		final DataSourceParameter parameter = new DataSourceParameter();
		assertFalse(dataSource.hasParameters());
		
		dataSource.addParameter(parameter);
		
		assertEquals(parameter.getDataSource(), dataSource);
		
		assertTrue(dataSource.hasParameters());
		assertSame(dataSource.getParameters().size(), 1);
		assertSame(dataSource.getParameters().get(0), parameter);
	}
	
	@Test
	public void testGetParameterByName() {
		final IDataSource dataSource = new DataSourceMetadata();
		final DataSourceParameter parameter = new DataSourceParameter();
		dataSource.addParameter(parameter);
		parameter.setName("other");
		
		assertNull(dataSource.getParameterByName("test"));
		
		parameter.setName("test");
		
		assertSame(dataSource.getParameterByName("test"), parameter);
	}
	
	@Test
	public void testGetParameterByUid() {
		final IDataSource dataSource = new DataSourceMetadata();
		final DataSourceParameter parameter = new DataSourceParameter();
		dataSource.addParameter(parameter);
		parameter.setUid("other");
		
		assertNull(dataSource.getParameterByUid("test"));
		
		parameter.setUid("test");
		
		assertSame(dataSource.getParameterByUid("test"), parameter);
	}
	
	@Test
	public void testGetContentParameterSet() {
		final IDataSource dataSource = new DataSourceMetadata();
		((DataSourceMetadata) dataSource).setContent("no param");
		
		assertTrue(dataSource.getContentParameterSet().isEmpty());
		
		((DataSourceMetadata) dataSource).setContent("{test}");
		
		assertFalse(dataSource.getContentParameterSet().isEmpty());
		assertEquals(dataSource.getContentParameterSet().iterator().next(), "test");
	}
	
	@Test
	public void testRemoveParameter() {
		final IDataSource dataSource = new DataSourceMetadata();
		final DataSourceParameter parameter = new DataSourceParameter();
		dataSource.addParameter(parameter);
		assertSame(dataSource.getParameters().size(), 1);
		
		dataSource.removeParameter(parameter);
		
		assertFalse(dataSource.hasParameters());
		assertSame(dataSource.getParameters().size(), 0);
	}
	
}
