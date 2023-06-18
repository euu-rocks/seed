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
public class CreateReportTest extends AbstractReportTest {
	
	@Test
	@Order(1)
	void testCreateReport() {
		clickMenu("administration-reporte");
		assertEquals("Reporte", findTab("reporte").getText());
		WebElement tabpanel = findTabpanel("reporte");
		clickButton(tabpanel, "new");
		
		WebElement window = findWindow("new-report");
		assertEquals("Neuen Report erstellen", findWindowHeader(window).getText());
		findCombobox(window, "module").sendKeys("Testmodule");
		clickButton(window, "create");
		
		clickButton(tabpanel, "save");
		findValidationMessage();
		
		findTextbox(tabpanel, "name").sendKeys("Testreport");
		saveReport(tabpanel);
	}
	
	@Test
	@Order(2)
	void testAddDatasource() {
		WebElement tabpanel = showReport("testreport");
		assertEquals("Abfragen", findTab(tabpanel, "datasources").getText());
		WebElement tabpanelDataSources = findTabpanel(tabpanel, "datasources");
		clickButton(tabpanelDataSources, "new");
		
		findOptionCombobox(tabpanelDataSources, "datasource").sendKeys("Testquery");
		saveReport(tabpanel);
	}
	
	@Test
	@Order(3)
	void testAddPermission() {
		WebElement tabpanel = showReport("testreport");
		assertEquals("Berechtigungen", findTab(tabpanel, "permissions").getText());
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		dragAndDrop(tabpanelPermissions, "testrole", "selected");
		saveReport(tabpanel);
	}
	
}
