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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class EditRestTest extends AbstractRestTest {
	
	@Test
	@Order(1)
	void testRenameRest() {
		WebElement tabpanel = showRest("testrest");
		assertEquals("REST-Services: TestRest", findTab("rest-services").getText());
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestRestNew");
		saveRest(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRenameFunction() {
		WebElement tabpanel = showRest("testrestnew");
		assertEquals("Funktionen", findTab(tabpanel, "functions").getText());
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "functions");
		clickListItem(tabpanelFunctions, "test");
		
		clearOptionTextbox(tabpanelFunctions, "functionname");
		findOptionTextbox(tabpanelFunctions, "functionname").sendKeys("testnew");
		clickTab(tabpanel, "functions"); // lose focus
		clickButton(tabpanel, "save");
		findValidationMessage(); // syntax error
		
		clickButton(tabpanelFunctions, "editfunction");
		WebElement window = findWindow("code-dialog");
		WebElement codeMirror = findCodeMirror(window, "content", 6);
		codeMirror.sendKeys(repeatKey(Keys.BACK_SPACE, 26));
		codeMirror.sendKeys("new implements RestFunction {");
		clickButton(window, "apply");
		waitWindowDisappear("code-dialog");
		saveRest(tabpanel);
	}
	
	@Test
	@Order(3)
	void testRemovePermission() {
		WebElement tabpanel = showRest("testrestnew");
		assertEquals("Berechtigungen", findTab(tabpanel, "permissions").getText());
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		dragAndDrop(tabpanelPermissions, "testrole", "available");
		saveRest(tabpanel);
	}
	
}
