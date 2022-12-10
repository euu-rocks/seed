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
package org.seed.test.integration.rest;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

import org.seed.test.integration.AbstractIntegrationTest;

@TestMethodOrder(OrderAnnotation.class)
public class CreateRestTest extends AbstractIntegrationTest {
	
	@Test
	@Order(1)
	void testCreateRest() {
		clickMenu("administration-rest-services");
		findTab("rest-services");
		WebElement tabpanel = findTabpanel("rest-services");
		clickButton(tabpanel, "new");
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("TestRest");
		clickButton(tabpanel, "save");
		findSuccessMessage();
	}
	
	@Test
	@Order(2)
	void testAddFunction() {
		WebElement tabpanel = showRest("testrest");
		findTab(tabpanel, "functions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "functions");
		clickButton(tabpanelFunctions, "new");
		
		findOptionCombobox(tabpanelFunctions, "method").sendKeys("GET");
		findOptionTextbox(tabpanelFunctions, "functionname").sendKeys("test");
		pause(500);
		clickButton(tabpanelFunctions, "editfunction");
		
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 10).sendKeys("return \"TestRestfunction\";");
		clickButton(window, "apply");
		clickButton(tabpanel, "save");
		findSuccessMessage();
	}
	
	@Test
	@Order(3)
	void testAddPermission() {
		WebElement tabpanel = showRest("testrest");
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		dragAndDrop(tabpanelPermissions, "testrole", "selected");
		clickButton(tabpanel, "save");
		findSuccessMessage();
	}
	
	private WebElement showRest(String name) {
		clickMenu("administration-rest-services");
		findTab("rest-services");
		final WebElement tabpanel = findTabpanel("rest-services");
		clickListItem(tabpanel, name);
		clickButton(tabpanel, "edit");
		return tabpanel;
	}
}
