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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class CreateRestTest extends AbstractRestTest {
	
	@Test
	@Order(1)
	void testCreateRest() {
		clickMenu("administration-rest-services");
		assertEquals("REST-Services", findTab("rest-services").getText());
		WebElement tabpanel = findTabpanel("rest-services");
		clickButton(tabpanel, "new");
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("TestRest");
		saveRest(tabpanel);
	}
	
	@Test
	@Order(2)
	void testAddFunction() {
		WebElement tabpanel = showRest("testrest");
		assertEquals("Funktionen", findTab(tabpanel, "functions").getText());
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "functions");
		clickButton(tabpanelFunctions, "new");
		
		findOptionCombobox(tabpanelFunctions, "method").sendKeys("GET");
		findOptionTextbox(tabpanelFunctions, "functionname").sendKeys("test");
		pause(500);
		clickButton(tabpanelFunctions, "editfunction");
		
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 12).sendKeys("        System.out.println(\"Testrest\");");
		clickButton(window, "apply");
		waitWindowDisappear("code-dialog");
		saveRest(tabpanel);
	}
	
	@Test
	@Order(3)
	void testAddPermission() {
		WebElement tabpanel = showRest("testrest");
		assertEquals("Berechtigungen", findTab(tabpanel, "permissions").getText());
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		dragAndDrop(tabpanelPermissions, "testrole", "selected");
		saveRest(tabpanel);
	}
	
}
