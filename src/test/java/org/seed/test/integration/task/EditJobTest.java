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

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class EditJobTest extends AbstractJobTest {
	
	@Test
	@Order(1)
	void testRenameJob() {
		WebElement tabpanel = showJob("testjob");
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestjobNew");
		saveJob(tabpanel);
	}
	
	@Test
	@Order(2)
	void testEditCode() {
		WebElement tabpanel = showJob("testjobnew");
		clickButton(tabpanel, "editcode");
		WebElement window = findWindow("code-dialog");
		
		for (int i=0;i<22;i++) {
			findCodeMirror(window, "content", 7).sendKeys(Keys.BACK_SPACE);
		}
		findCodeMirror(window, "content", 7).sendKeys("New extends AbstractJob {");
		clickButton(window, "apply");
		waitWindowDisapear("code-dialog");
		saveJob(tabpanel);
	}
	
	@Test
	@Order(3)
	void testChangeParameterValue() {
		WebElement tabpanel = showJob("testjobnew");
		clickTab(tabpanel, "parameters");
		WebElement tabpanelParameters = findTabpanel(tabpanel, "parameters");
		clickListItem(tabpanelParameters, "testparameter");
		
		clearOptionTextbox(tabpanelParameters, "value");
		findOptionTextbox(tabpanelParameters, "value").sendKeys("Test value new");
		saveJob(tabpanel);
	}
	
	@Test
	@Order(4)
	void testChangeNotification() {
		WebElement tabpanel = showJob("testjobnew");
		clickTab(tabpanel, "notifications");
		WebElement tabpanelNotifications = findTabpanel(tabpanel, "notifications");
		clickListItem(tabpanelNotifications, "testuser");
		
		clearOptionCombobox(tabpanelNotifications, "result");
		findOptionCombobox(tabpanelNotifications, "result").sendKeys("Fehler");
		saveJob(tabpanel);
	}
	
	@Test
	@Order(5)
	void testRemovePermission() {
		WebElement tabpanel = showJob("testjobnew");
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		dragAndDrop(tabpanelPermissions, "testrole", "available");
		saveJob(tabpanel);
	}
	
}
