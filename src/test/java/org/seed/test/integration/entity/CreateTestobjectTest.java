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

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class CreateTestobjectTest extends AbstractEntityTest {
	
	@Test
	@Order(1)
	void testCreateTestEntity() {
		WebElement tabpanel = newEntity();
		assertEquals("Entitäten", findTab("entitaeten").getText());
		WebElement window = newEntityWindow();
		findCombobox(window, "module").sendKeys("Testmodule");
		findCombobox(window, "menu").sendKeys("Testmenu");
		clickButton(window, "create");
		
		findTextbox(tabpanel, "name").sendKeys("Testobject");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(2)
	void testAddFieldGroups() {
		WebElement tabpanel = showEntity("testobject");
		assertEquals("Feldgruppen", findTab(tabpanel, "fieldgroups").getText());
		clickTab(tabpanel, "fieldgroups");
		WebElement tabpanelGroups = findTabpanel(tabpanel, "fieldgroups");
		
		clickButton(tabpanelGroups, "new");
		findOptionTextbox(tabpanelGroups, "groupname").sendKeys("Group A");
		clickButton(tabpanelGroups, "new");
		findOptionTextbox(tabpanelGroups, "groupname").sendKeys("Group B");
		clickButton(tabpanelGroups, "new");
		findOptionTextbox(tabpanelGroups, "groupname").sendKeys("Group C");
		clickButton(tabpanelGroups, "new");
		findOptionTextbox(tabpanelGroups, "groupname").sendKeys("Group D");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(3)
	void testAddFields() {
		WebElement tabpanel = showEntity("testobject");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		
		clickButton(tabpanelFields, "new");
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Group A");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Automatische Nummer");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Autonum");
		clickTab(tabpanel, "fields"); // lose focus
		findOptionTextbox(tabpanelFields, "autonumpattern").sendKeys("Test-");
		
		clickButton(tabpanelFields, "new");
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Group A");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Text");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Text");
		
		clickButton(tabpanelFields, "new");
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Group A");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Text (lang)");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Description");
		
		clickButton(tabpanelFields, "new");
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Group B");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Ganzzahl");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Integer");
		
		clickButton(tabpanelFields, "new");
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Group B");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Ganzzahl (lang)");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Long int");
		
		clickButton(tabpanelFields, "new");
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Group B");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Dezimalzahl");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Decimal");
		
		clickButton(tabpanelFields, "new");
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Group B");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Gleitkommazahl");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Floating");
		
		clickButton(tabpanelFields, "new");
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Group C");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Datum");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Date");
		
		clickButton(tabpanelFields, "new");
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Group C");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Datum & Zeit");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Datetime");
		
		clickButton(tabpanelFields, "new");
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Group D");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Ja / Nein");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Bool");
		
		clickButton(tabpanelFields, "new");
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Group D");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Datei");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("File");
		
		clickButton(tabpanelFields, "new");
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("Group D");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Referenz");
		clickTab(tabpanel, "fields"); // lose focus
		findOptionCombobox(tabpanelFields, "referenceentity").sendKeys("TransferableTest");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("Reference");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(4)
	void testAddStatusmodel() {
		WebElement tabpanel = showEntity("testobject");
		assertEquals("Statusmodell", findTab(tabpanel, "statusmodel").getText());
		clickTab(tabpanel, "statusmodel");
		findTab(tabpanel, "status");
		WebElement tabpanelStatus = findTabpanel(tabpanel, "status");
		
		clickButton(tabpanelStatus, "new");
		findOptionTextbox(tabpanelStatus, "statusname").sendKeys("Status A");
		findOptionIntbox(tabpanelStatus, "statusnumber").sendKeys("10");
		
		clickButton(tabpanelStatus, "new");
		findOptionTextbox(tabpanelStatus, "statusname").sendKeys("Status B");
		findOptionIntbox(tabpanelStatus, "statusnumber").sendKeys("20");
		
		clickButton(tabpanelStatus, "new");
		findOptionTextbox(tabpanelStatus, "statusname").sendKeys("Status C");
		findOptionIntbox(tabpanelStatus, "statusnumber").sendKeys("30");
		
		clickTab(tabpanel, "transitions");
		WebElement tabpanelTransitions = findTabpanel(tabpanel, "transitions");
		
		clickButton(tabpanelTransitions, "new");
		findOptionCombobox(tabpanelTransitions, "sourcestatus").sendKeys("10 Status A");
		findOptionCombobox(tabpanelTransitions, "targetstatus").sendKeys("20 Status B");
		
		clickButton(tabpanelTransitions, "new");
		findOptionCombobox(tabpanelTransitions, "sourcestatus").sendKeys("20 Status B");
		findOptionCombobox(tabpanelTransitions, "targetstatus").sendKeys("30 Status C");
		saveEntity(tabpanel);
	}
	
}
