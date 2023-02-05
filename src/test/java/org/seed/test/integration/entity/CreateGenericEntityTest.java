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
public class CreateGenericEntityTest extends AbstractEntityTest {
	
	@Test
	@Order(1)
	void testCreateGenericEntity() {
		WebElement tabpanel = newEntity();
		assertEquals("Entitäten", findTab("entitaeten").getText());
		WebElement window = newEntityWindow();
		
		clickCheckbox(window, "generic");
		findCombobox(window, "module").sendKeys("Testmodule");
		clickButton(window, "create");
		
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("GenericTest");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(2)
	void testAddFieldGroup() {
		WebElement tabpanel = showEntity("generictest");
		assertEquals("Feldgruppen", findTab(tabpanel, "fieldgroups").getText());
		clickTab(tabpanel, "fieldgroups");
		WebElement tabpanelGroups = findTabpanel(tabpanel, "fieldgroups");
		clickButton(tabpanelGroups, "new");
		
		findOptionTextbox(tabpanelGroups, "groupname").sendKeys("generic");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(3)
	void testAddField() {
		WebElement tabpanel = showEntity("generictest");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickButton(tabpanelFields, "new");
		
		findOptionCombobox(tabpanelFields, "fieldgroup").sendKeys("generic");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Text");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("generictext");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(4)
	void testAddFunction() {
		WebElement tabpanel = showEntity("generictest");
		assertEquals("Funktionen", findTab(tabpanel, "functions").getText());
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "functions");
		clickTab(tabpanel, "functions");
		clickButton(tabpanelFunctions, "new");
		
		findOptionTextbox(tabpanelFunctions, "functionname").sendKeys("Genericfunction");
		pause(500);
		clickButton(tabpanelFunctions, "editfunction");
		
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 4).sendKeys("       System.out.println(\"Genericfunction\");");
		clickButton(window, "apply");
		waitWindowDisappear("code-dialog");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(5)
	void testAddFieldConstraint() {
		WebElement tabpanel = showEntity("generictest");
		assertEquals("Feldeinschränkungen", findTab(tabpanel, "constraints").getText());
		clickTab(tabpanel, "constraints");
		WebElement tabpanelConstraints = findTabpanel(tabpanel, "constraints");
		clickButton(tabpanelConstraints, "new");
		
		findOptionCombobox(tabpanelConstraints, "fieldgroup").sendKeys("generic");
		findOptionCombobox(tabpanelConstraints, "usergroup").sendKeys("Testrole");
		findOptionCombobox(tabpanelConstraints, "access").sendKeys("Schreiben");
		clickCheckbox(tabpanelConstraints, "mandatory");
		saveEntity(tabpanel);
	}
	
}
