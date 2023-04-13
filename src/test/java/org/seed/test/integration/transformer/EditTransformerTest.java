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
package org.seed.test.integration.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class EditTransformerTest extends AbstractTransformerTest {
	
	@Test
	@Order(1)
	void testRenameTransformer() {
		WebElement tabpanel = showTransformer("testtransformer");
		assertEquals("Transformationen: Testtransformer", findTab("transformationen").getText());
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TesttransformerNew");
		saveTransformer(tabpanel);
	}
	
	@Test
	@Order(2)
	void testEditElement() {
		WebElement tabpanel = showTransformer("testtransformernew");
		assertEquals("Elemente", findTab("elements").getText());
		WebElement tabpanelElements = findTabpanel(tabpanel, "elements");
		clickListItem(tabpanelElements, "textfieldnew-textfieldnew");
		
		clearOptionCombobox(tabpanelElements, "sourcefield");
		findOptionCombobox(tabpanelElements, "sourcefield").sendKeys("DerivedTest");
		clearOptionCombobox(tabpanelElements, "targetfield");
		findOptionCombobox(tabpanelElements, "targetfield").sendKeys("DerivedTest");
		saveTransformer(tabpanel);
	}
	
	@Test
	@Order(3)
	void testRemoveStatus() {
		WebElement tabpanel = showTransformer("testtransformernew");
		assertEquals("Status", findTab(tabpanel, "status").getText());
		clickTab(tabpanel, "status");
		WebElement tabpanelStatus = findTabpanel(tabpanel, "status");
		pause(100);
		dragAndDrop(tabpanelStatus, "one-new", "available");
		saveTransformer(tabpanel);
	}
	
	@Test
	@Order(4)
	void testEditFunction() {
		WebElement tabpanel = showTransformer("testtransformernew");
		assertEquals("Funktionen", findTab(tabpanel, "functions").getText());
		clickTab(tabpanel, "functions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "functions");
		clickListItem(tabpanelFunctions, "testfunction");
		clickButton(tabpanelFunctions, "editfunction");
		
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 9).sendKeys("// edited");
		clickButton(window, "apply");
		waitWindowDisappear("code-dialog");
		saveTransformer(tabpanel);
	}
	
	@Test
	@Order(5)
	void testRemovePermission() {
		WebElement tabpanel = showTransformer("testtransformernew");
		assertEquals("Berechtigungen", findTab(tabpanel, "permissions").getText());
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		dragAndDrop(tabpanelPermissions, "testrole", "available");
		saveTransformer(tabpanel);
	}
	
}
