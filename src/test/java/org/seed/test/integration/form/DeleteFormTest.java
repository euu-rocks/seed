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
import org.openqa.selenium.WebElement;

public class DeleteFormTest extends AbstractFormTest {
	
	@Test
	@Order(1)
	void testRemoveTranformer() {
		WebElement tabpanel = showForm("testformnew");
		assertEquals("Transformationen", findTab(tabpanel, "transformers").getText());
		clickTab(tabpanel, "transformers");
		WebElement tabpanelTransformers = findTabpanel(tabpanel, "transformers");
		dragAndDrop(tabpanelTransformers, "testtransformernew", "available");
		saveForm(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRemoveAction() {
		WebElement tabpanel = showForm("testformnew");
		assertEquals("Aktionen", findTab(tabpanel, "actions").getText());
		clickTab(tabpanel, "actions");
		WebElement tabpanelActions = findTabpanel(tabpanel, "actions");
		dragAndDrop(tabpanelActions, "exportlist", "available");
		saveForm(tabpanel);
	}
	
	@Test
	@Order(3)
	void testRemoveField() {
		WebElement tabpanel = showForm("testformnew");
		assertEquals("Liste", findTab(tabpanel, "fields").getText());
		clickTab(tabpanel, "fields");
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		pause(100);
		dragAndDrop(tabpanelFields, "statusnew", "available");
		saveForm(tabpanel);
	}
	
	@Test
	@Order(4)
	void testDeleteForm() {
		WebElement tabpanel = showForm("testformnew");
		assertEquals("Formulare: TestformNew", findTab("formulare").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
		waitTabDisappear("layout");
	}
	
}
