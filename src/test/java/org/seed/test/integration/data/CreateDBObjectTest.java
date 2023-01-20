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
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class CreateDBObjectTest extends AbstractDBObjectTest {
	
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
		findCodeMirror(tabpanel, "content", 1).sendKeys("* from transferabletest");
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
		findCodeMirror(tabpanel, "content", 1).sendKeys("Testprocedure(\n"
				+ "	text1 in varchar(100),\n"
				+ "	text2 in varchar(100),\n"
				+ "	result inout varchar(200)\n"
				+ ")\n"
				+ "language plpgsql as\n"
				+ "$$\n"
				+ "begin\n"
				+ "	select text1 || ' ' || text2 into result;\n"
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
		findCodeMirror(tabpanel, "content", 1).sendKeys("Testfunction()\n"
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
	
	@Test
	@Order(4)
	void testCreateTriggerFunction() {
		WebElement tabpanel = newDBObject();
		WebElement window = newDBObjectWindow();
		
		findCombobox(window, "type").sendKeys("Funktion");
		findCombobox(window, "module").sendKeys("Testmodule");
		clickButton(window, "create");
		
		findTextbox(tabpanel, "name").sendKeys("Triggerfunction");
		findCodeMirror(tabpanel, "content", 1).sendKeys("Triggerfunction()\n"
				+ "returns trigger\n"
				+ "language plpgsql\n"
				+ "as\n"
				+ "$$\n"
				+ "begin\n"
				+ "	return NEW;\n"
				+ "end\n"
				+ "$$");
		saveDBObject(tabpanel);
	}
	
	@Test
	@Order(5)
	void testCreateTrigger() {
		WebElement tabpanel = newDBObject();
		WebElement window = newDBObjectWindow();
		
		findCombobox(window, "type").sendKeys("Trigger");
		findCombobox(window, "module").sendKeys("Testmodule");
		clickButton(window, "create");
	
		findTextbox(tabpanel, "name").sendKeys("Testtrigger");
		findCodeMirror(tabpanel, "content", 1).sendKeys("Testtrigger\n"
				+ "  before update\n"
				+ "  on sys_user\n"
				+ "  for each row\n"
				+ "  execute procedure triggerfunction()");
		saveDBObject(tabpanel);
	}
	
	@Test
	@Order(6)
	void testCreateSequence() {
		WebElement tabpanel = newDBObject();
		WebElement window = newDBObjectWindow();
		
		findCombobox(window, "type").sendKeys("Sequenz");
		findCombobox(window, "module").sendKeys("Testmodule");
		clickButton(window, "create");
		
		findTextbox(tabpanel, "name").sendKeys("Testsequence");
		findCodeMirror(tabpanel, "content", 1).sendKeys("Testsequence START 100");
		saveDBObject(tabpanel);
	}
	
}
