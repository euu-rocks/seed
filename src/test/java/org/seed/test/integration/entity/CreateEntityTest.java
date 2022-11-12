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
package org.seed.test.integration.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.openqa.selenium.WebElement;

import org.seed.test.integration.AbstractIntegrationTest;

@TestMethodOrder(OrderAnnotation.class)
public class CreateEntityTest extends AbstractIntegrationTest {
	
	@Test
	@Order(1)
	void testCreateEntity() {
		clickMenu("administration-entitaeten");
		findTab("entitaeten");
		WebElement tabpanel = findTabpanel("entitaeten");
		clickButton(tabpanel, "new");
		
		WebElement window = findWindow("new-entity");
		assertEquals("Neue Entität erstellen", findWindowHeader(window).getText());
		clickButton(window, "create");
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("IntegrationTest");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(2)
	void testAddFieldGroup() {
		WebElement tabpanel = showEntity("integrationtest");
		clickTab(tabpanel, "fieldgroups");
		WebElement tabpanelGroups = findTabpanel(tabpanel, "fieldgroups");
		clickButton(tabpanelGroups, "new");
		
		findOptionTextbox(tabpanelGroups, "groupname").sendKeys("Testgroup");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(3)
	void testAddField() {
		WebElement tabpanel = showEntity("integrationtest");
		findTab(tabpanel, "fields");
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickButton(tabpanelFields, "new");
		
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Testgroup");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Text");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Textfield");
		
		pause(50);
		clickOptionCheckbox(tabpanelFields, "mandatory");
		clickOptionCheckbox(tabpanelFields, "unique");
		clickOptionCheckbox(tabpanelFields, "indexed");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(4)
	void testAddFunction() {
		WebElement tabpanel = showEntity("integrationtest");
		clickTab(tabpanel, "functions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "functions");
		clickButton(tabpanelFunctions, "new");
		
		findOptionTextbox(tabpanelFunctions, "functionname").sendKeys("Testfunction");
		pause(500);
		clickButton(tabpanelFunctions, "editfunction");
		
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 4).sendKeys("       System.out.println(\"Testfunction\");");
		clickButton(window, "apply");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(5)
	void testAddCallbackFunction() {
		WebElement tabpanel = showEntity("integrationtest");
		clickTab(tabpanel, "callbackfunctions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "callbackfunctions");
		clickButton(tabpanelFunctions, "new");
		
		findOptionTextbox(tabpanelFunctions, "functionname").sendKeys("Testcallback");
		pause(500);
		clickButton(tabpanelFunctions, "editfunction");
		
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 10).sendKeys("System.out.println(\"Testcallback\");");
		clickButton(window, "apply");
		
		clickOptionCheckbox(tabpanelFunctions, "activebeforeinsert");
		clickOptionCheckbox(tabpanelFunctions, "activebeforeupdate");
		clickOptionCheckbox(tabpanelFunctions, "activeontransition");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(6)
	void testAddStatusmodel() {
		WebElement tabpanel = showEntity("integrationtest");
		clickTab(tabpanel, "statusmodel");
		findTab(tabpanel, "status");
		WebElement tabpanelStatus = findTabpanel(tabpanel, "status");
		
		clickButton(tabpanelStatus, "new");
		findOptionTextbox(tabpanelStatus, "statusname").sendKeys("One");
		findOptionIntbox(tabpanelStatus, "statusnumber").sendKeys("1");
		
		clickButton(tabpanelStatus, "new");
		findOptionTextbox(tabpanelStatus, "statusname").sendKeys("Two");
		findOptionIntbox(tabpanelStatus, "statusnumber").sendKeys("2");
		
		clickButton(tabpanelStatus, "new");
		findOptionTextbox(tabpanelStatus, "statusname").sendKeys("Three");
		findOptionIntbox(tabpanelStatus, "statusnumber").sendKeys("3");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(7)
	void testAddStatusTransitions() {
		WebElement tabpanel = showEntity("integrationtest");
		clickTab(tabpanel, "statusmodel");
		clickTab(tabpanel, "transitions");
		WebElement tabpanelTransitions = findTabpanel(tabpanel, "transitions");
		
		clickButton(tabpanelTransitions, "new");
		findOptionCombobox(tabpanelTransitions, "sourcestatus").sendKeys("1 One");
		findOptionCombobox(tabpanelTransitions, "targetstatus").sendKeys("2 Two");
		
		clickButton(tabpanelTransitions, "new");
		findOptionCombobox(tabpanelTransitions, "sourcestatus").sendKeys("2 Two");
		findOptionCombobox(tabpanelTransitions, "targetstatus").sendKeys("3 Three");
		
		clickButton(tabpanelTransitions, "new");
		findOptionCombobox(tabpanelTransitions, "sourcestatus").sendKeys("3 Three");
		findOptionCombobox(tabpanelTransitions, "targetstatus").sendKeys("1 One");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(8)
	void testAddStatusPermission() {
		WebElement tabpanel = showEntity("integrationtest");
		clickTab(tabpanel, "statusmodel");
		clickTab(tabpanel, "transitionpermissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "transitionpermissions");
		dragAndDrop(tabpanelPermissions, "testrole", "selected");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(9)
	void testAddStatusFunction() {
		WebElement tabpanel = showEntity("integrationtest");
		clickTab(tabpanel, "statusmodel");
		clickTab(tabpanel, "transitionfunctions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "transitionfunctions");
		dragAndDrop(tabpanelFunctions, "testcallback", "selected");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(10)
	void testAddPermission() {
		WebElement tabpanel = showEntity("integrationtest");
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		dragAndDrop(tabpanelPermissions, "testrole", "selected");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(11)
	void testAddFieldConstraint() {
		WebElement tabpanel = showEntity("integrationtest");
		clickTab(tabpanel, "constraints");
		WebElement tabpanelConstraints = findTabpanel(tabpanel, "constraints");
		clickButton(tabpanelConstraints, "new");
		
		findOptionCombobox(tabpanelConstraints, "field").sendKeys("Textfield");
		findOptionCombobox(tabpanelConstraints, "status").sendKeys("1 One");
		findOptionCombobox(tabpanelConstraints, "usergroup").sendKeys("Testrole");
		findOptionCombobox(tabpanelConstraints, "access").sendKeys("Lesen");
		clickOptionCheckbox(tabpanelConstraints, "mandatory");
		saveEntity(tabpanel);
	}
	
	private WebElement showEntity(String name) {
		clickMenu("administration-entitaeten");
		findTab("entitaeten");
		final WebElement tabpanel = findTabpanel("entitaeten");
		clickListItem(tabpanel, name);
		clickButton(tabpanel, "edit");
		return tabpanel;
	}
	
	private void saveEntity(WebElement tabpanel) {
		clickButton(tabpanel, "save");
		pause(2000);
		findSuccessMessage();
	}
	
}
