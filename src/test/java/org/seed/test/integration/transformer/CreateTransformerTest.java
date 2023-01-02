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
public class CreateTransformerTest extends AbstractTransformerTest {
	
	@Test
	@Order(1)
	void testCreateTransformer() {
		openMenu("administration-entitaeten");
		clickMenu("administration-entitaeten-transformationen");
		findTab("transformationen");
		WebElement tabpanel = findTabpanel("transformationen");
		clickButton(tabpanel, "new");
		
		WebElement window = findWindow("new-transformer");
		assertEquals("Neue Transformation erstellen", findWindowHeader(window).getText());
		findCombobox(window, "sourceentity").sendKeys("IntegrationTest");
		findCombobox(window, "targetentity").sendKeys("IntegrationTest");
		findCombobox(window, "module").sendKeys("Testmodule");
		clickButton(window, "create");
		
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		findTextbox(tabpanel, "name").sendKeys("Testtransformer");
		saveTransformer(tabpanel);
	}
	
	@Test
	@Order(2)
	void testAddElement() {
		WebElement tabpanel = showTransformer("testtransformer");
		findTab(tabpanel, "elements");
		WebElement tabpanelElements = findTabpanel(tabpanel, "elements");
		clickButton(tabpanelElements, "new");
		
		findOptionCombobox(tabpanelElements, "sourcefield").sendKeys("Textfield");
		findOptionCombobox(tabpanelElements, "targetfield").sendKeys("Textfield");
		saveTransformer(tabpanel);
	}
	
	@Test
	@Order(3)
	void testAddNested() {
		WebElement tabpanel = showTransformer("testtransformer");
		clickTab(tabpanel, "nesteds");
		WebElement tabpanelNesteds = findTabpanel(tabpanel, "nesteds");
		
		findTab(tabpanelNesteds, "nestedtransformers");
		WebElement tabpanelTransformers = findTabpanel(tabpanel, "nestedtransformers");
		clickButton(tabpanelTransformers, "new");
		findOptionCombobox(tabpanelTransformers, "sourcenested").sendKeys("NestedTest");
		findOptionCombobox(tabpanelTransformers, "targetnested").sendKeys("NestedTest");
		
		clickTab(tabpanelNesteds, "nestedelements");
		WebElement tabpanelElements = findTabpanel(tabpanel, "nestedelements");
		clickButton(tabpanelElements, "new");
		findOptionCombobox(tabpanelElements, "sourcefield").sendKeys("Name");
		findOptionCombobox(tabpanelElements, "targetfield").sendKeys("Name");
		saveTransformer(tabpanel);
	}
	
	@Test
	@Order(4)
	void testAddStatus() {
		WebElement tabpanel = showTransformer("testtransformer");
		clickTab(tabpanel, "status");
		WebElement tabpanelElements = findTabpanel(tabpanel, "status");
		dragAndDrop(tabpanelElements, "one", "selected");
		saveTransformer(tabpanel);
	}
	
	@Test
	@Order(5)
	void testAddPermission() {
		WebElement tabpanel = showTransformer("testtransformer");
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		dragAndDrop(tabpanelPermissions, "testrole", "selected");
		saveTransformer(tabpanel);
	}
	
	@Test
	@Order(6)
	void testAddFunction() {
		WebElement tabpanel = showTransformer("testtransformer");
		clickTab(tabpanel, "functions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "functions");
		clickButton(tabpanelFunctions, "new");
		
		findOptionTextbox(tabpanelFunctions, "functionname").sendKeys("Testfunction");
		pause(500);
		clickButton(tabpanelFunctions, "editfunction");
		
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 9).sendKeys("System.out.println(\"Testtransformation\");");
		clickButton(window, "apply");
		waitWindowDisapear("code-dialog");
		
		clickCheckbox(tabpanelFunctions, "activebeforetransformation");
		clickCheckbox(tabpanelFunctions, "activeaftertransformation");
		saveTransformer(tabpanel);
	}
	
}
