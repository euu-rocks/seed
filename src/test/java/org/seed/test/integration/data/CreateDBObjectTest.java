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

import org.seed.test.integration.AbstractIntegrationTest;

@TestMethodOrder(OrderAnnotation.class)
public class CreateDBObjectTest extends AbstractIntegrationTest {
	
	@Test
	@Order(1)
	void testCreateView() {
		WebElement tabpanel = newDBObject();
		WebElement window = newDBObjectWindow();
		
		findCombobox(window, "type").sendKeys("View");
		findCombobox(window, "module").sendKeys("Testmodule");
		clickButton(window, "create");
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("Testview");
		findCodeMirror(tabpanel, "content", 1).sendKeys("select * from integrationtest");
		saveDBObject(tabpanel);
	}
	
	@Test
	@Order(2)
	void testCreateProcedure() {
		WebElement tabpanel = newDBObject();
		WebElement window = newDBObjectWindow();
		
		findCombobox(window, "type").sendKeys("Prozedur");
		findCombobox(window, "module").sendKeys("Testmodule");
		clickButton(window, "create");
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("Testprocedure");
		findCodeMirror(tabpanel, "content", 1).sendKeys("create or replace procedure Testprocedure()\r\n"
				+ "language plpgsql as\n"
				+ "$$\n"
				+ "begin\n"
				+ "	select * from sys_user;\n"
				+ "end\n"
				+ "$$");
		saveDBObject(tabpanel);
	}
	
	@Test
	@Order(3)
	void testCreateFunction() {
		WebElement tabpanel = newDBObject();
		WebElement window = newDBObjectWindow();
		
		findCombobox(window, "type").sendKeys("Funktion");
		findCombobox(window, "module").sendKeys("Testmodule");
		clickButton(window, "create");
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("Testfunction");
		findCodeMirror(tabpanel, "content", 1).sendKeys("create function Testfunction()\n"
				+ "returns int\n"
				+ "language plpgsql\n"
				+ "as\n"
				+ "$$\n"
				+ "declare\n"
				+ "	ret integer;\n"
				+ "\n"
				+ "begin\n"
				+ "	select count(*) into ret from sys_user;\n"
				+ "	return ret;\n"
				+ "end\n"
				+ "$$");
		saveDBObject(tabpanel);
	}
	
	private WebElement newDBObject() {
		clickMenu("administration-datenbankelemente");
		findTab("datenbankelemente");
		WebElement tabpanel = findTabpanel("datenbankelemente");
		clickButton(tabpanel, "new");
		return tabpanel;
	}
	
	private WebElement newDBObjectWindow() {
		final WebElement window = findWindow("new-dbobject");
		assertEquals("Neues Datenbankelement erstellen", findWindowHeader(window).getText());
		return window;
	}
	
	private void saveDBObject(WebElement tabpanel) {
		clickButton(tabpanel, "save");
		pause(2000);
		findSuccessMessage();
	}
	
}
