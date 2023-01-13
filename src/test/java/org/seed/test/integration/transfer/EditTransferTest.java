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
public class EditTransferTest extends AbstractTransferTest {
	
	@Test
	@Order(1)
	void testRenameTransfer() {
		WebElement tabpanel = showTransfer("testtransfer");
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TesttransferNew");
		saveTransfer(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRemoveElement() {
		WebElement tabpanel = showTransfer("testtransfernew");
		assertEquals("Felder", findTab(tabpanel, "fields").getText());
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		dragAndDrop(tabpanelFields, "textfieldnew", "available");
		saveTransfer(tabpanel);
	}
	
}
