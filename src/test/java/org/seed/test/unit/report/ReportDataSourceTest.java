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

import org.junit.jupiter.api.Test;

import org.seed.core.report.ReportDataSource;

class ReportDataSourceTest {
	
	@Test
	void testIsEqual() {
		final ReportDataSource dataSource1 = new ReportDataSource();
		final ReportDataSource dataSource2 = new ReportDataSource();
		assertTrue(dataSource1.isEqual(dataSource2));
		
		dataSource1.setLabel("test");
		dataSource1.setDataSourceUid("dataSource");
		assertFalse(dataSource1.isEqual(dataSource2));
		
		dataSource2.setLabel("test");
		dataSource2.setDataSourceUid("dataSource");
		assertTrue(dataSource1.isEqual(dataSource2));
	}
}
