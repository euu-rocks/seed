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
	void testEditView() {
		WebElement tabpanel = showDBObject("testview");
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestviewNew");
		
		WebElement codeMirror = findCodeMirror(tabpanel, "content", 1);
		codeMirror.sendKeys(repeatKey(Keys.BACK_SPACE, 23));
		codeMirror.sendKeys("name from transferabletest");
		saveDBObject(tabpanel);
	}
	
	@Test
	@Order(2)
	void testEditProcedure() {
		WebElement tabpanel = showDBObject("testprocedure");
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestprocedureNew");
		
		WebElement codeMirror = findCodeMirror(tabpanel, "content", 1);
		codeMirror.sendKeys(repeatKey(Keys.BACK_SPACE, 1));
		codeMirror.sendKeys("New(");
		saveDBObject(tabpanel);
	}
	
	@Test
	@Order(3)
	void testEditFunction() {
		WebElement tabpanel = showDBObject("testfunction");
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestfunctionNew");
		
		WebElement codeMirror = findCodeMirror(tabpanel, "content", 1);
		codeMirror.sendKeys(repeatKey(Keys.BACK_SPACE, 2));
		codeMirror.sendKeys("New()");
		saveDBObject(tabpanel);
	}
}
