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
package org.seed.test.integration.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class DeleteDBObjectTest extends AbstractDBObjectTest {
	
	@Test
	@Order(1)
	void testFailDeleteView() {
		WebElement tabpanel = showDBObject("testviewnew");
		assertEquals("Datenbankelemente: TestviewNew", findTab("datenbankelemente").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
		findValidationMessage(); // still in use
	}
	
	@Test
	@Order(2)
	void testDeleteViewInView() {
		WebElement tabpanel = showDBObject("testviewinview");
		assertEquals("Datenbankelemente: TestviewInView", findTab("datenbankelemente").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
	}
	
	@Test
	@Order(3)
	void testDeleteView() {
		WebElement tabpanel = showDBObject("testviewnew");
		assertEquals("Datenbankelemente: TestviewNew", findTab("datenbankelemente").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
	}
	
	@Test
	@Order(4)
	void testDeleteSequence() {
		WebElement tabpanel = showDBObject("testsequence");
		assertEquals("Datenbankelemente: Testsequence", findTab("datenbankelemente").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
	}
	
	@Test
	@Order(5)
	void testDeleteProcedure() {
		WebElement tabpanel = showDBObject("testprocedurenew");
		assertEquals("Datenbankelemente: TestprocedureNew", findTab("datenbankelemente").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
	}
	
	@Test
	@Order(6)
	void testDeleteFunction() {
		WebElement tabpanel = showDBObject("testfunctionnew");
		assertEquals("Datenbankelemente: TestfunctionNew", findTab("datenbankelemente").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
	}
	
	@Test
	@Order(7)
	void testDeleteTrigger() {
		WebElement tabpanel = showDBObject("testtrigger");
		assertEquals("Datenbankelemente: Testtrigger", findTab("datenbankelemente").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
	}
	
	@Test
	@Order(8)
	void testDeleteTriggerFunction() {
		WebElement tabpanel = showDBObject("triggerfunction");
		assertEquals("Datenbankelemente: Triggerfunction", findTab("datenbankelemente").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
	}
	
}
