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
public class EditExpertFormTest extends AbstractFormTest {
	
	@Test
	@Order(1)
	void testEditLayout() {
		WebElement tabpanel = showForm("expertform");
		assertEquals("Formulare: Expertform", findTab("formulare").getText());
		WebElement tabpanelLayout = findTabpanel(tabpanel, "layout");
		clickButton(tabpanelLayout, "editsource");
		
		WebElement window = findWindow("layout-source");
		findCodeMirror(window, "content", 1).sendKeys("<!-- test edit -->");
		clickButton(window, "apply");
		waitWindowDisappear("code-dialog");
		saveForm(tabpanel);
	}
	
	@Test
	@Order(2)
	void testEditFunction() {
		WebElement tabpanel = showForm("expertform");
		assertEquals("Formulare: Expertform", findTab("formulare").getText());
		clickTab(tabpanel, "functions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "functions");
		clickListItem(tabpanelFunctions, "testfunction");
		clickButton(tabpanelFunctions, "editfunction");
		
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 12).sendKeys("//test edit");
		clickButton(window, "apply");
		waitWindowDisappear("code-dialog");
		saveForm(tabpanel);
	}
	
}
