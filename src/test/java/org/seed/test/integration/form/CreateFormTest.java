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
public class CreateFormTest extends AbstractFormTest {
	
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
		clickButton(window, "create");
		
		findTextbox(tabpanel, "name").sendKeys("Testform");
		saveForm(tabpanel);
	}
	
	@Test
	@Order(2)
	void testAssignFilter() {
		WebElement tabpanel = showForm("testform");
		assertEquals("Formulare: Testform", findTab("formulare").getText());
		findCombobox(tabpanel, "filter").sendKeys("Testfilter");
		clickTab(tabpanel, "fields"); // lose focus
		saveForm(tabpanel);
	}
	
	@Test
	@Order(3)
	void testAddField() {
		WebElement tabpanel = showForm("testform");
		assertEquals("Liste", findTab(tabpanel, "fields").getText());
		clickTab(tabpanel, "fields");
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		pause(100);
		dragAndDrop(tabpanelFields, "status", "selected");
		saveForm(tabpanel);
	}
	
	@Test
	@Order(4)
	void testAddTransformer() {
		WebElement tabpanel = showForm("testform");
		assertEquals("Transformationen", findTab(tabpanel, "transformers").getText());
		clickTab(tabpanel, "transformers");
		WebElement tabpanelTransformers = findTabpanel(tabpanel, "transformers");
		
		dragAndDrop(tabpanelTransformers, "testtransformer", "selected");
		findOptionCombobox(tabpanelTransformers, "targetform").sendKeys("IntegrationTest");
		saveForm(tabpanel);
	}
	
}
