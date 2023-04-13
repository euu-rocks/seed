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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class EditDBObjectTest extends AbstractDBObjectTest {
	
	@Test
	@Order(1)
	void testEditProcedure() {
		WebElement tabpanel = showDBObject("testprocedure");
		assertEquals("Datenbankelemente: Testprocedure", findTab("datenbankelemente").getText());
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestprocedureNew");
		
		WebElement codeMirror = findCodeMirror(tabpanel, "content", 1);
		codeMirror.sendKeys(repeatKey(Keys.BACK_SPACE, 1));
		codeMirror.sendKeys("New(");
		saveDBObject(tabpanel);
	}
	
	@Test
	@Order(2)
	void testEditFunction() {
		WebElement tabpanel = showDBObject("testfunction");
		assertEquals("Datenbankelemente: Testfunction", findTab("datenbankelemente").getText());
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestfunctionNew");
		
		WebElement codeMirror = findCodeMirror(tabpanel, "content", 1);
		codeMirror.sendKeys(repeatKey(Keys.BACK_SPACE, 2));
		codeMirror.sendKeys("New()");
		saveDBObject(tabpanel);
	}
	
	@Test
	@Order(3)
	void testEditView() {
		WebElement tabpanel = showDBObject("testview");
		assertEquals("Datenbankelemente: Testview", findTab("datenbankelemente").getText());
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestviewNew");
		
		WebElement codeMirror = findCodeMirror(tabpanel, "content", 1);
		codeMirror.sendKeys(repeatKey(Keys.BACK_SPACE, 23));
		codeMirror.sendKeys("name,testfunctionnew() from transferabletestnew");
		saveDBObject(tabpanel);
	}
	
	@Test
	@Order(4)
	void testFailRenameViewFunction() {
		WebElement tabpanel = showDBObject("testfunctionnew");
		assertEquals("Datenbankelemente: TestfunctionNew", findTab("datenbankelemente").getText());
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("Testfunction");
		
		WebElement codeMirror = findCodeMirror(tabpanel, "content", 1);
		codeMirror.sendKeys(repeatKey(Keys.BACK_SPACE, 5));
		codeMirror.sendKeys("()");
		clickButton(tabpanel, "save");
		findValidationMessage();
	}
	
	@Test
	@Order(5)
	void testFailRenameTriggerFunction() {
		WebElement tabpanel = showDBObject("triggerfunction");
		assertEquals("Datenbankelemente: Triggerfunction", findTab("datenbankelemente").getText());
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TriggerfunctionNew");
		
		WebElement codeMirror = findCodeMirror(tabpanel, "content", 1);
		codeMirror.sendKeys(repeatKey(Keys.BACK_SPACE, 2));
		codeMirror.sendKeys("New()");
		clickButton(tabpanel, "save");
		findValidationMessage();
	}
	
}
