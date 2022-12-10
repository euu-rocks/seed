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
package org.seed.test.integration.module;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

import org.seed.test.integration.AbstractIntegrationTest;

@TestMethodOrder(OrderAnnotation.class)
public class CreateModuleTest extends AbstractIntegrationTest {
	
	@Test
	@Order(1)
	void testCreateModule() {
		clickMenu("administration-module");
		findTab("module");
		WebElement tabpanel = findTabpanel("module");
		clickButton(tabpanel, "new");
		
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("Testmodule");
		clickButton(tabpanel, "save");
		findSuccessMessage();
	}
	
	@Test
	@Order(2)
	void testAddParameter() {
		WebElement tabpanel = showModule("testmodule");
		findTab(tabpanel, "parameters");
		WebElement tabpanelParameters = findTabpanel(tabpanel, "parameters");
		clickButton(tabpanelParameters, "new");
		
		findOptionTextbox(tabpanelParameters, "parametername").sendKeys("Testparameter");
		findOptionTextbox(tabpanelParameters, "parametervalue").sendKeys("test");
		clickButton(tabpanel, "save");
		findSuccessMessage();
	}
	
	@Test
	@Order(3)
	void testAddUsergroup() {
		WebElement tabpanel = showModule("testmodule");
		clickTab(tabpanel, "usergroups");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "usergroups");
		dragAndDrop(tabpanelPermissions, "testrole", "selected");
		clickButton(tabpanel, "save");
		findSuccessMessage();
	}
	
	protected WebElement showModule(String name) {
		clickMenu("administration-module");
		findTab("module");
		final WebElement tabpanel = findTabpanel("module");
		clickListItem(tabpanel, name);
		clickButton(tabpanel, "edit");
		return tabpanel;
	}
	
}
