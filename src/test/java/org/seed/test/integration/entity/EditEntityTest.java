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
public class EditEntityTest extends AbstractEntityTest {
	
	@Test
	@Order(1)
	void testRenameEntity() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Entitäten: IntegrationTest", findTab("entitaeten").getText());
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("IntegrationTestNew");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(2)
	void testSetMinMaxValues() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickListItem(tabpanelFields, "numberfield");
		findOptionIntbox(tabpanelFields, "minvalue").sendKeys("1");
		findOptionIntbox(tabpanelFields, "maxvalue").sendKeys("100");
		clickTab(tabpanel, "fields"); // lose focus
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(3)
	void testDisableMandatory() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickListItem(tabpanelFields, "textfield");
		clickCheckbox(tabpanelFields, "mandatory");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(4)
	void testDisableUnique() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickListItem(tabpanelFields, "textfield");
		clickCheckbox(tabpanelFields, "unique");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(5)
	void testDisableIndexed() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickListItem(tabpanelFields, "textfield");
		clickCheckbox(tabpanelFields, "indexed");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(6)
	void testEnableConstraints() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickListItem(tabpanelFields, "textfield");
		clickCheckbox(tabpanelFields, "mandatory");
		clickCheckbox(tabpanelFields, "unique");
		clickCheckbox(tabpanelFields, "indexed");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(7)
	void testChangeColumnLength() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickListItem(tabpanelFields, "textfield");
		findOptionIntbox(tabpanelFields, "length").sendKeys("512");
		clickTab(tabpanel, "fields");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(8)
	void testRenameField() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickListItem(tabpanelFields, "textfield");
		
		clearOptionTextbox(tabpanelFields, "fieldname");
		findOptionTextbox(tabpanelFields, "fieldname").sendKeys("TextfieldNew");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(9)
	void testChangeDataType() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickListItem(tabpanelFields, "textfieldnew");
		
		clearOptionCombobox(tabpanelFields, "datatype");
		findOptionCombobox(tabpanelFields, "datatype").sendKeys("Text (lang)");
		clickTab(tabpanel, "fields"); // lose focus
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(10)
	void testRenameFieldGroup() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Feldgruppen", findTab(tabpanel, "fieldgroups").getText());
		clickTab(tabpanel, "fieldgroups");
		WebElement tabpanelGroups = findTabpanel(tabpanel, "fieldgroups");
		clickListItem(tabpanelGroups, "testgroup");
		
		clearOptionTextbox(tabpanelGroups, "groupname");
		findOptionTextbox(tabpanelGroups, "groupname").sendKeys("TestgroupNew");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(11)
	void testEditFunction() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Funktionen", findTab(tabpanel, "functions").getText());
		clickTab(tabpanel, "functions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "functions");
		clickListItem(tabpanelFunctions, "testfunction");
		clickButton(tabpanelFunctions, "editfunction");
		
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 4).sendKeys("//test edit");
		clickButton(window, "apply");
		waitWindowDisappear("code-dialog");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(12)
	void testRenameNested() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Unterobjekte", findTab(tabpanel, "nesteds").getText());
		clickTab(tabpanel, "nesteds");
		WebElement tabpanelNesteds = findTabpanel(tabpanel, "nesteds");
		clickListItem(tabpanelNesteds, "nestedtest");
		
		clearOptionTextbox(tabpanelNesteds, "nestedname");
		findOptionTextbox(tabpanelNesteds, "nestedname").sendKeys("NestedTestNew");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(13)
	void testRenameRelation() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Beziehungen", findTab(tabpanel, "relations").getText());
		clickTab(tabpanel, "relations");
		WebElement tabpanelRelations = findTabpanel(tabpanel, "relations");
		clickListItem(tabpanelRelations, "transferabletest");
		
		clearOptionTextbox(tabpanelRelations, "relationname");
		findOptionTextbox(tabpanelRelations, "relationname").sendKeys("TransferableTestNew");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(14)
	void testEditFieldConstraint() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Feldeinschränkungen", findTab(tabpanel, "constraints").getText());
		clickTab(tabpanel, "constraints");
		WebElement tabpanelConstraints = findTabpanel(tabpanel, "constraints");
		clickListItem(tabpanelConstraints, "textfieldnew");
		
		findOptionCombobox(tabpanelConstraints, "nested").sendKeys("NestedTestNew");
		clickTab(tabpanel, "constraints"); // lose focus
		findOptionCombobox(tabpanelConstraints, "field").sendKeys("Name");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(15)
	void testRenameStatus() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Statusmodell", findTab(tabpanel, "statusmodel").getText());
		clickTab(tabpanel, "statusmodel");
		findTab(tabpanel, "status");
		WebElement tabpanelStatus = findTabpanel(tabpanel, "status");
		clickListItem(tabpanelStatus, "one");
		
		clearOptionTextbox(tabpanelStatus, "statusname");
		findOptionTextbox(tabpanelStatus, "statusname").sendKeys("One new");
		findOptionTextbox(tabpanelStatus, "description").sendKeys("Initial status");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(16)
	void testRenameRelatedEntity() {
		WebElement tabpanel = showEntity("transferabletest");
		assertEquals("Entitäten: TransferableTest", findTab("entitaeten").getText());
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TransferableTestNew");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(17)
	void testRenameEntityBack() {
		WebElement tabpanel = showEntity("integrationtestnew");
		assertEquals("Entitäten: IntegrationTestNew", findTab("entitaeten").getText());
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("IntegrationTest");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(18)
	void testEditCallbackFunction() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Callback-Funktionen", findTab(tabpanel, "callbackfunctions").getText());
		clickTab(tabpanel, "callbackfunctions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "callbackfunctions");
		clickListItem(tabpanelFunctions, "testcallback");
		clickButton(tabpanelFunctions, "editfunction");
		
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 10).sendKeys("//test edit");
		clickButton(window, "apply");
		waitWindowDisappear("code-dialog");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(19)
	void testDisableVersioning() {
		WebElement tabpanel = showEntity("derivedtest");
		assertEquals("Entitäten: DerivedTest", findTab("entitaeten").getText());
		findCheckbox(tabpanel, "audited").click();
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(20)
	void testEnableVersioning() {
		WebElement tabpanel = showEntity("derivedtest");
		assertEquals("Entitäten: DerivedTest", findTab("entitaeten").getText());
		findCheckbox(tabpanel, "audited").click();
		saveEntity(tabpanel);
	}
	
}
