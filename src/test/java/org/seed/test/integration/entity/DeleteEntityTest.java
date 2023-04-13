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
public class DeleteEntityTest extends AbstractEntityTest {
	
	@Test
	@Order(1)
	void testRemoveReferenceField() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Entitäten: IntegrationTest", findTab("entitaeten").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickListItem(tabpanelFields, "derivedtest");
		clickButton(tabpanelFields, "remove");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRemoveFieldConstraint() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Feldeinschränkungen", findTab(tabpanel, "constraints").getText());
		clickTab(tabpanel, "constraints");
		WebElement tabpanelConstraints = findTabpanel(tabpanel, "constraints");
		clickListItem(tabpanelConstraints, "name");
		clickButton(tabpanelConstraints, "remove");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(3)
	void testRemoveStatusFunction() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Statusmodell", findTab(tabpanel, "statusmodel").getText());
		clickTab(tabpanel, "statusmodel");
		clickTab(tabpanel, "transitionfunctions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "transitionfunctions");
		dragAndDrop(tabpanelFunctions, "testcallback", "available");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(4)
	void testRemoveStatusTransitions() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Statusmodell", findTab(tabpanel, "statusmodel").getText());
		clickTab(tabpanel, "statusmodel");
		clickTab(tabpanel, "transitions");
		WebElement tabpanelTransitions = findTabpanel(tabpanel, "transitions");
		clickListItem(tabpanelTransitions, "three-one-new");
		clickButton(tabpanelTransitions, "remove");
		clickListItem(tabpanelTransitions, "two-three");
		clickButton(tabpanelTransitions, "remove");
		clickListItem(tabpanelTransitions, "one-new-two");
		clickButton(tabpanelTransitions, "remove");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(5)
	void testRemoveStatus() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Statusmodell", findTab(tabpanel, "statusmodel").getText());
		clickTab(tabpanel, "statusmodel");
		findTab(tabpanel, "status");
		WebElement tabpanelStatus = findTabpanel(tabpanel, "status");
		clickListItem(tabpanelStatus, "three");
		clickButton(tabpanelStatus, "remove");
		saveEntity(tabpanel);
	}
	
	
	@Test
	@Order(6)
	void testRemoveCallbackFunction() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Callback-Funktionen", findTab(tabpanel, "callbackfunctions").getText());
		clickTab(tabpanel, "callbackfunctions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "callbackfunctions");
		clickListItem(tabpanelFunctions, "testcallback");
		clickButton(tabpanelFunctions, "remove");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(7)
	void testRemoveNested() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Entitäten: IntegrationTest", findTab("entitaeten").getText());
		clickTab(tabpanel, "nesteds");
		WebElement tabpanelNesteds = findTabpanel(tabpanel, "nesteds");
		clickListItem(tabpanelNesteds, "nestedtestnew");
		clickButton(tabpanelNesteds, "remove");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(8)
	void testRemoveRelation() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Beziehungen", findTab(tabpanel, "relations").getText());
		clickTab(tabpanel, "relations");
		WebElement tabpanelRelations = findTabpanel(tabpanel, "relations");
		clickListItem(tabpanelRelations, "transferabletestnew");
		clickButton(tabpanelRelations, "remove");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(9)
	void testRemoveFunction() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Funktionen", findTab(tabpanel, "functions").getText());
		clickTab(tabpanel, "functions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "functions");
		clickListItem(tabpanelFunctions, "testfunction");
		clickButton(tabpanelFunctions, "remove");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(10)
	void testRemoveField() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickListItem(tabpanelFields, "textfieldnew");
		clickButton(tabpanelFields, "remove");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(11)
	void testRemoveFieldGroup() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Feldgruppen", findTab(tabpanel, "fieldgroups").getText());
		clickTab(tabpanel, "fieldgroups");
		WebElement tabpanelGroups = findTabpanel(tabpanel, "fieldgroups");
		clickListItem(tabpanelGroups, "testgroupnew");
		clickButton(tabpanelGroups, "remove");
		saveEntity(tabpanel);
	}
	
	@Test
	@Order(12)
	void testDeleteDerivedEntity() {
		WebElement tabpanel = showEntity("derivedtest");
		assertEquals("Entitäten: DerivedTest", findTab("entitaeten").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
	}
	
	@Test
	@Order(13)
	void testDeleteGenericEntity() {
		WebElement tabpanel = showEntity("generictest");
		assertEquals("Entitäten: GenericTest", findTab("entitaeten").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
	}
	
	@Test
	@Order(14)
	void testDeleteTransferableEntity() {
		WebElement tabpanel = showEntity("transferabletestnew");
		assertEquals("Entitäten: TransferableTestNew", findTab("entitaeten").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
	}
	
	@Test
	@Order(15)
	void testDeleteNestedEntity() {
		WebElement tabpanel = showEntity("nestedtest");
		assertEquals("Entitäten: NestedTest", findTab("entitaeten").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
	}
	
	@Test
	@Order(16)
	void testDeleteEntity() {
		WebElement tabpanel = showEntity("integrationtest");
		assertEquals("Entitäten: IntegrationTest", findTab("entitaeten").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
	}
	
}
