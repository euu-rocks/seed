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
package org.seed.test.integration.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class CreateMenuTest extends AbstractMenuTest {
	
	@Test
	@Order(1)
	void testCreateMenu() {
		clickMenu("administration-menues");
		assertEquals("Menüs", findTab("menues").getText());
		WebElement tabpanel = findTabpanel("menues");
		clickButton(tabpanel, "new");
		
		WebElement window = findWindow("new-menu");
		assertEquals("Neues Menü erstellen", findWindowHeader(window).getText());
		findCombobox(window, "module").sendKeys("Testmodule");
		clickButton(window, "create");
		
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("Testmenu");
		saveMenu(tabpanel);
	}
}
