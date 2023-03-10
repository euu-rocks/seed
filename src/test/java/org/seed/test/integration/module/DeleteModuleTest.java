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
package org.seed.test.integration.module;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class DeleteModuleTest extends AbstractModuleTest {
	
	@Test
	@Order(1)
	void testRemoveParameter() {
		WebElement tabpanel = showModule("testmodulenew");
		assertEquals("Parameter", findTab(tabpanel, "parameters").getText());
		WebElement tabpanelParameters = findTabpanel(tabpanel, "parameters");
		clickListItem(tabpanelParameters, "testparameter");
		clickButton(tabpanelParameters, "remove");
		saveModule(tabpanel);
	}
	
	@Test
	@Order(2)
	void testDeleteModule() {
		WebElement tabpanel = showModule("testmodulenew");
		assertEquals("Module", findTab("module").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
	}
	
}
