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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class CreateEntityTest extends AbstractEntityTest {
	
	@Test
	@Order(1)
	void testCreateEntity() {
		WebElement tabpanel = newEntity();
		assertEquals("Entitäten", findTab("entitaeten").getText());
		WebElement window = newEntityWindow();
		                                      
		clickCheckbox(window, "audited");
		findCombobox(window, "module").sendKeys("Testmodule");
		findCombobox(window, "menu").sendKeys("Testmenu");
		clickButton(window, "create");
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("String");
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is illegal
		
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("IntegrationTest");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(2)
	void testAddFieldGroup() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Feldgruppen", findTab(tabpanel, "fieldgroups").getText());
		clickTab(tabpanel, "fieldgroups");
		
		WebElement tabpanelGroups = findTabpanel(tabpanel, "fieldgroups");
		clickButton(tabpanelGroups, "new");
		
		findOptionTextbox(tabpanelGroups, "groupname").sendKeys("Testgroup");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(3)
	void testAddFields() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickButton(tabpanelFields, "new");
		
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Testgroup");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Text");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Textfield");
		clickTab(tabpanel, "fields"); // lose focus
		
		clickCheckbox(tabpanelFields, "mandatory");
		clickCheckbox(tabpanelFields, "unique");
		clickCheckbox(tabpanelFields, "indexed");
		saveEntity(tabpanel);
		
		clickButton(tabpanelFields, "new");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Ganzzahl");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Numberfield");
		clickTab(tabpanel, "fields"); // lose focus
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(4)
	void testAddReferenceField() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickButton(tabpanelFields, "new");
		
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Testgroup");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Referenz");
		clickTab(tabpanel, "fields"); // lose focus
		findOptionCombobox(tabpanelFields, "referenceentity").sendKeys("DerivedTest");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(5)
	void testAddFunction() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Funktionen", findTab(tabpanel, "functions").getText());
		clickTab(tabpanel, "functions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "functions");
		clickButton(tabpanelFunctions, "new");
		
		findOptionTextbox(tabpanelFunctions, "functionname").sendKeys("Testfunction");
		pause(500);
		clickButton(tabpanelFunctions, "editfunction");
		
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 4).sendKeys("       System.out.println(\"Testfunction\");");
		clickButton(window, "apply");
		waitWindowDisappear("code-dialog");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(6)
	void testAddCallbackFunction() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Callback-Funktionen", findTab(tabpanel, "callbackfunctions").getText());
		clickTab(tabpanel, "callbackfunctions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "callbackfunctions");
		clickButton(tabpanelFunctions, "new");
		
		findOptionTextbox(tabpanelFunctions, "functionname").sendKeys("Testcallback");
		pause(500);
		clickButton(tabpanelFunctions, "editfunction");
		
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 10).sendKeys("System.out.println(\"Testcallback\");");
		clickButton(window, "apply");
		waitWindowDisappear("code-dialog");
		
		clickCheckbox(tabpanelFunctions, "activebeforeinsert");
		clickCheckbox(tabpanelFunctions, "activebeforeupdate");
		clickCheckbox(tabpanelFunctions, "activeontransition");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(7)
	void testAddStatusmodel() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Statusmodell", findTab(tabpanel, "statusmodel").getText());
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
	@Order(8)
	void testAddStatusTransitions() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Statusmodell", findTab(tabpanel, "statusmodel").getText());
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
	@Order(9)
	void testAddStatusPermission() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Statusmodell", findTab(tabpanel, "statusmodel").getText());
		clickTab(tabpanel, "statusmodel");
		clickTab(tabpanel, "transitionpermissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "transitionpermissions");
		dragAndDrop(tabpanelPermissions, "testrole", "selected");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(10)
	void testAddStatusFunction() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Statusmodell", findTab(tabpanel, "statusmodel").getText());
		clickTab(tabpanel, "statusmodel");
		clickTab(tabpanel, "transitionfunctions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "transitionfunctions");
		dragAndDrop(tabpanelFunctions, "testcallback", "selected");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(11)
	void testAddPermission() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Berechtigungen", findTab(tabpanel, "permissions").getText());
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		dragAndDrop(tabpanelPermissions, "testrole", "selected");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(12)
	void testAddFieldConstraint() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Feldeinschränkungen", findTab(tabpanel, "constraints").getText());
		clickTab(tabpanel, "constraints");
		WebElement tabpanelConstraints = findTabpanel(tabpanel, "constraints");
		clickButton(tabpanelConstraints, "new");
		
		findOptionCombobox(tabpanelConstraints, "field").sendKeys("Textfield");
		findOptionCombobox(tabpanelConstraints, "status").sendKeys("1 One");
		findOptionCombobox(tabpanelConstraints, "usergroup").sendKeys("Testrole");
		findOptionCombobox(tabpanelConstraints, "access").sendKeys("Schreiben");
		clickCheckbox(tabpanelConstraints, "mandatory");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(13)
	void testAddRelation() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Beziehungen", findTab(tabpanel, "relations").getText());
		clickTab(tabpanel, "relations");
		WebElement tabpanelRelations = findTabpanel(tabpanel, "relations");
		clickButton(tabpanelRelations, "new");
		
		findOptionCombobox(tabpanelRelations, "relationentity").sendKeys("TransferableTest");
		saveEntity(tabpanel);
	}
	
}
