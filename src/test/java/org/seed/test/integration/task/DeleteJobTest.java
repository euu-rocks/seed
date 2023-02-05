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
package org.seed.test.integration.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class DeleteJobTest extends AbstractJobTest {
	
	@Test
	@Order(1)
	void testRemoveNotification() {
		WebElement tabpanel = showJob("testjobnew");
		assertEquals("Benachrichtigungen", findTab(tabpanel, "notifications").getText());
		clickTab(tabpanel, "notifications");
		WebElement tabpanelNotifications = findTabpanel(tabpanel, "notifications");
		clickListItem(tabpanelNotifications, "testusernew");
		clickButton(tabpanelNotifications, "remove");
		saveJob(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRemoveParameter() {
		WebElement tabpanel = showJob("testjobnew");
		assertEquals("Parameter", findTab(tabpanel, "parameters").getText());
		clickTab(tabpanel, "parameters");
		WebElement tabpanelParameters = findTabpanel(tabpanel, "parameters");
		clickListItem(tabpanelParameters, "testparameter");
		clickButton(tabpanelParameters, "remove");
		saveJob(tabpanel);
	}
	
	@Test
	@Order(3)
	void testDeleteJob() {
		WebElement tabpanel = showJob("testjobnew");
		assertEquals("Jobs", findTab("jobs").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
		waitTabDisappear("jobruns");
	}
	
}
