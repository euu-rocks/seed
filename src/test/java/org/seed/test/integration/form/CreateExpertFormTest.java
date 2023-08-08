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
package org.seed.test.integration.form;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class CreateExpertFormTest extends AbstractFormTest {
	
	@Test
	@Order(1)
	void testCreateForm() {
		clickMenu("administration-formulare");
		findTab("formulare");
		WebElement tabpanel = findTabpanel("formulare");
		clickButton(tabpanel, "new");
		
		WebElement window = findWindow("new-form");
		assertEquals("Neues Formular erstellen", findWindowHeader(window).getText());
		findCombobox(window, "entity").sendKeys("IntegrationTest");
		findCombobox(window, "module").sendKeys("Testmodule");
		findCombobox(window, "menu").sendKeys("Testmenu");
		clickCheckbox(window, "expertmode");
		clickButton(window, "create");
		
		findTextbox(tabpanel, "name").sendKeys("Expertform");
		saveForm(tabpanel);
	}
	
	@Test
	@Order(2)
	void testAddFunction() {
		WebElement tabpanel = showForm("expertform");
		assertEquals("Formulare: Expertform", findTab("formulare").getText());
		clickTab(tabpanel, "functions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "functions");
		clickButton(tabpanelFunctions, "new");
		findOptionTextbox(tabpanelFunctions, "functionname").sendKeys("Testfunction");
		pause(500);
		
		clickButton(tabpanelFunctions, "editfunction");
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 12).sendKeys("System.out.println(\"Testfunction\");");
		clickButton(window, "apply");
		waitWindowDisappear("code-dialog");
		saveForm(tabpanel);
	}
	
	@Test
	@Order(3)
	void testAddLayout() {
		WebElement tabpanel = showForm("expertform");
		assertEquals("Formulare: Expertform", findTab("formulare").getText());
		findTab(tabpanel, "layout");
		WebElement tabpanelLayout = findTabpanel(tabpanel, "layout");
		clickButton(tabpanelLayout, "editsource");
		
		WebElement window = findWindow("layout-source");
		findCodeMirror(window, "content", 1).sendKeys("<zk>\n"
				+ "    <borderlayout>\n"
				+ "        <north border=\"0\" class=\"alpha-noborder\">\n"
				+ "          	<div style=\"text-align:center;padding:50px\">\n"
				+ "            	<label value=\"@init(vm.object.frage.frage)\"\n"
				+ "                       style=\"font-weight:bold;font-size:16pt\"/>\n"
				+ "            </div>\n"
				+ "        </north>\n"
				+ "    </borderlayout>\n"
				+ "</zk>");
		clickButton(window, "apply");
		waitWindowDisappear("code-dialog");
		saveForm(tabpanel);
	}
	
}
