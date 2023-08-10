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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class CreateModuleTest extends AbstractModuleTest {
	
	@Test
	@Order(1)
	void testCreateModule() {
		clickMenu("administration-module");
		assertEquals("Module", findTab("module").getText());
		WebElement tabpanel = findTabpanel("module");
		clickButton(tabpanel, "new");
		
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("Testmodule");
		saveModule(tabpanel);
	}
	
	@Test
	@Order(2)
	void testAddParameter() {
		WebElement tabpanel = showModule("testmodule");
		assertEquals("Parameter", findTab(tabpanel, "parameters").getText());
		WebElement tabpanelParameters = findTabpanel(tabpanel, "parameters");
		clickButton(tabpanelParameters, "new");
		
		findOptionTextbox(tabpanelParameters, "parametername").sendKeys("Testparameter");
		findOptionTextbox(tabpanelParameters, "parametervalue").sendKeys("test");
		saveModule(tabpanel);
	}
	
	@Test
	@Order(3)
	void testAddUsergroup() {
		WebElement tabpanel = showModule("testmodule");
		assertEquals("Rollen", findTab(tabpanel, "usergroups").getText());
		clickTab(tabpanel, "usergroups");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "usergroups");
		dragAndDrop(tabpanelPermissions, "testrole", "selected");
		saveModule(tabpanel);
	}
	
	@Test
	@Order(4)
	void testCreateSubModule() {
		clickMenu("administration-module");
		assertEquals("Module", findTab("module").getText());
		WebElement tabpanel = findTabpanel("module");
		clickButton(tabpanel, "new");
		
		findTextbox(tabpanel, "name").sendKeys("Submodule");
		saveModule(tabpanel);
	}
	
}
