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
package org.seed.test.unit.report;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.seed.core.data.datasource.DataSourceMetadata;
import org.seed.core.data.datasource.DataSourceParameter;
import org.seed.core.data.datasource.IDataSource;
import org.seed.core.report.Report;
import org.seed.core.report.ReportDataSource;
import org.seed.core.report.ReportMetadata;
import org.seed.core.report.ReportPermission;

public class ReportTest {
	
	@Test
	public void testAddDataSource() {
		final Report report = new ReportMetadata();
		final ReportDataSource dataSource = new ReportDataSource();
		assertFalse(report.hasDataSources());
		
		report.addDataSource(dataSource);
		
		assertSame(dataSource.getReport(), report);
		assertTrue(report.hasDataSources());
		assertSame(report.getDataSources().size(), 1);
		assertSame(report.getDataSources().get(0), dataSource);
	}
	
	@Test
	public void testGetDataSourceByUid() {
		final Report report = new ReportMetadata();
		final ReportDataSource dataSource = new ReportDataSource();
		report.addDataSource(dataSource);
		dataSource.setUid("other");
		
		assertNull(report.getDataSourceByUid("test"));
		
		dataSource.setUid("test");
		
		assertSame(report.getDataSourceByUid("test"), dataSource);
	}
	
	@Test
	public void testGetPermissionByUid() {
		final Report report = new ReportMetadata();
		final ReportPermission permission = new ReportPermission();
		final List<ReportPermission> permissions = new ArrayList<>();
		permissions.add(permission);
		((ReportMetadata) report).setPermissions(permissions);
		permission.setUid("other");
		
		assertNull(report.getPermissionByUid("test"));
		
		permission.setUid("test");
		
		assertSame(report.getPermissionByUid("test"), permission);
	}
	
	@Test
	public void testGetDataSourceParameterMap() {
		final Report report = new ReportMetadata();
		final ReportDataSource reportDataSource = new ReportDataSource();
		final List<ReportDataSource> dataSources = new ArrayList<>();
		final IDataSource dataSource = new DataSourceMetadata();
		final DataSourceParameter parameter = new DataSourceParameter();
		parameter.setName("testparam");
		parameter.setValue("testvalue");
		dataSource.setName("testdatasource");
		dataSource.addParameter(parameter);
		dataSources.add(reportDataSource);
		reportDataSource.setDataSource(dataSource);
		((ReportMetadata) report).setDataSources(dataSources);
		final Map<String, List<DataSourceParameter>> map =
			report.getDataSourceParameterMap();
		
		assertFalse(map.isEmpty());
		assertSame(map.size(), 1);
		assertEquals(map.keySet().iterator().next(), "testdatasource");
		
		final List<DataSourceParameter> params = map.values().iterator().next();
		
		assertSame(params.size(), 1);
		assertSame(params.get(0), parameter);
		assertEquals(params.get(0).getName(), "testparam");
		assertEquals(params.get(0).getValue(), "testvalue");
	}
	
	@Test
	public void testIsEqual() {
		final Report report1 = new ReportMetadata();
		final Report report2 = new ReportMetadata();
		assertTrue(report1.isEqual(report2));
		
		report1.setName("test");
		assertFalse(report1.isEqual(report2));
		
		report2.setName("test");
		assertTrue(report1.isEqual(report2));
		
		report2.setName("other");
		assertFalse(report1.isEqual(report2));
	}
	
	@Test
	public void testIsEqualDataSources() {
		final Report report1 = new ReportMetadata();
		final Report report2 = new ReportMetadata();
		final ReportDataSource reportDataSource1 = new ReportDataSource();
		final ReportDataSource reportDataSource2 = new ReportDataSource();
		reportDataSource1.setUid("test");
		reportDataSource2.setUid("test");
		report1.addDataSource(reportDataSource1);
		assertFalse(report1.isEqual(report2));
		
		report2.addDataSource(reportDataSource2);
		assertTrue(report1.isEqual(report2));
		
		reportDataSource2.setUid("other");
		assertFalse(report1.isEqual(report2));
	}
	
	@Test
	public void testIsEqualPermissions() {
		final Report report1 = new ReportMetadata();
		final Report report2 = new ReportMetadata();
		final ReportPermission permission1 = new ReportPermission();
		final ReportPermission permission2 = new ReportPermission();
		final List<ReportPermission> permissions1 = new ArrayList<>();
		final List<ReportPermission> permissions2 = new ArrayList<>();
		permissions1.add(permission1);
		permissions2.add(permission2);
		permission1.setUid("test");
		permission2.setUid("test");
		((ReportMetadata) report1).setPermissions(permissions1);
		assertFalse(report1.isEqual(report2));
		
		((ReportMetadata) report2).setPermissions(permissions2);
		assertTrue(report1.isEqual(report2));
		
		permission2.setUid("other");
		assertFalse(report1.isEqual(report2));
	}
	
	@Test
	public void testRemoveDataSource() {
		final Report report = new ReportMetadata();
		final ReportDataSource dataSource = new ReportDataSource();
		report.addDataSource(dataSource);
		assertSame(report.getDataSources().size(), 1);
		
		report.removeDataSource(dataSource);
		
		assertFalse(report.hasDataSources());
		assertSame(report.getDataSources().size(), 0);
	}
	
}
