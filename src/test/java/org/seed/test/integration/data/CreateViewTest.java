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
public class CreateViewTest extends AbstractIntegrationTest {
	
	@Test
	@Order(1)
	void testCreateView() {
		clickMenu("administration-datenbankelemente");
		findTab("datenbankelemente");
		WebElement tabpanel = findTabpanel("datenbankelemente");
		clickButton(tabpanel, "new");
		
		WebElement window = findWindow("new-dbobject");
		assertEquals("Neues Datenbankelement erstellen", findWindowHeader(window).getText());
		findCombobox(window, "type").sendKeys("View");
		findCombobox(window, "module").sendKeys("Testmodule");
		clickButton(window, "create");
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("Testview");
		findCodeMirror(tabpanel, "content", 1).sendKeys("select * from integrationtest");
		clickButton(tabpanel, "save");
		pause(2000);
		findSuccessMessage();
	}
	
}
