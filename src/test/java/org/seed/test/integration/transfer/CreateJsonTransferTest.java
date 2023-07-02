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
package org.seed.test.integration.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class CreateJsonTransferTest extends AbstractTransferTest {
	
	@Test
	@Order(1)
	void testCreateTransfer() {
		openMenu("administration-entitaeten");
		clickMenu("administration-entitaeten-import--export");
		findTab("import--export");
		WebElement tabpanel = findTabpanel("import--export");
		clickButton(tabpanel, "new");
		
		WebElement window = findWindow("new-transfer");
		assertEquals("Neuen Import / Export erstellen", findWindowHeader(window).getText());
		findCombobox(window, "entity").sendKeys("IntegrationTest");
		findCombobox(window, "format").sendKeys("Json");
		findCombobox(window, "module").sendKeys("Testmodule");
		clickButton(window, "create");
		
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		findTextbox(tabpanel, "name").sendKeys("Jsontransfer");
		saveTransfer(tabpanel);
	}
	
	@Test
	@Order(2)
	void testAddElement() {
		WebElement tabpanel = showTransfer("jsontransfer");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		dragAndDrop(tabpanelFields, "textfield", "selected");
		saveTransfer(tabpanel);
	}
	
	@Test
	@Order(3)
	void testAddNestedElement() {
		WebElement tabpanel = showTransfer("jsontransfer");
		assertEquals("Unterobjekte", findTab(tabpanel, "nesteds").getText());
		clickTab(tabpanel, "nesteds");
		
		WebElement tabpanelNesteds = findTabpanel(tabpanel, "nesteds");
		dragAndDrop(tabpanelNesteds, "nestedtest", "selected");
		clickTab(tabpanelNesteds, "nestedelements");
		
		WebElement tabpanelNestedFields = findTabpanel(tabpanelNesteds, "nestedelements");
		dragAndDrop(tabpanelNestedFields, "name", "selected");
		saveTransfer(tabpanel);
	}
	
}
