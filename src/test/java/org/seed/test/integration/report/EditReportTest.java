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
package org.seed.test.integration.report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class EditReportTest extends AbstractReportTest {
	
	@Test
	@Order(1)
	void testRenameReport() {
		WebElement tabpanel = showReport("testreport");
		assertEquals("Reporte", findTab("reporte").getText());
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestreportNew");
		saveReport(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRenameDatasource() {
		WebElement tabpanel = showReport("testreportnew");
		findTab(tabpanel, "datasources");
		WebElement tabpanelDataSources = findTabpanel(tabpanel, "datasources");
		clickListItem(tabpanelDataSources, "testquery");
		findOptionTextbox(tabpanelDataSources, "label").sendKeys("TestqueryNew");
		clickTab(tabpanel, "datasources"); // lose focus
		saveReport(tabpanel);
	}
	
	@Test
	@Order(3)
	void testRemovePermission() {
		WebElement tabpanel = showReport("testreportnew");
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		dragAndDrop(tabpanelPermissions, "testrole", "available");
		saveReport(tabpanel);
	}
	
}
