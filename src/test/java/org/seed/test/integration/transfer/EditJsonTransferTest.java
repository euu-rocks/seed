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
public class EditJsonTransferTest extends AbstractTransferTest {
	
	@Test
	@Order(1)
	void testChangeElement() {
		WebElement tabpanel = showTransfer("jsontransfer");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		
		clickTab(tabpanel, "fields");
		dragAndDrop(tabpanelFields, "textfieldnew", "available");
		dragAndDrop(tabpanelFields, "numberfield", "selected");
		saveTransfer(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRemoveNestedElement() {
		WebElement tabpanel = showTransfer("jsontransfer");
		assertEquals("Unterobjekte", findTab(tabpanel, "nesteds").getText());
		clickTab(tabpanel, "nesteds");
		WebElement tabpanelNesteds = findTabpanel(tabpanel, "nesteds");
		clickTab(tabpanelNesteds, "nestedelements");
		
		WebElement tabpanelNestedFields = findTabpanel(tabpanelNesteds, "nestedelements");
		dragAndDrop(tabpanelNestedFields, "name", "available");
		saveTransfer(tabpanel);
	}
	
}
