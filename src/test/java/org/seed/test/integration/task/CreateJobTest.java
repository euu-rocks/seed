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
public class CreateJobTest extends AbstractJobTest {
	
	@Test
	@Order(1)
	void testCreateJob() {
		clickMenu("administration-jobs");
		assertEquals("Jobs", findTab("jobs").getText());
		WebElement tabpanel = findTabpanel("jobs");
		clickButton(tabpanel, "new");
		
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("Testjob");
		pause(500);
		clickButton(tabpanel, "editcode");
		
		WebElement window = findWindow("code-dialog");
		findCodeMirror(window, "content", 11).sendKeys("StoredProcedureCall call = context.getStoredProcedureProvider().createCall(\"testprocedure\");\n"
				+ "		call.setParameter(\"text1\",\"Hello\");\n"
				+ "      	call.setParameter(\"text2\",\"World\");\n"
				+ "      	call.awaitOutput(\"result\",String.class);\n"
				+ "        String result = call.getOutput(\"result\");\n"
				+ "      	context.logInfo(result);");
		clickButton(window, "apply");
		waitWindowDisappear("code-dialog");
		saveJob(tabpanel);
	}
	
	@Test
	@Order(2)
	void testAddParameter() {
		WebElement tabpanel = showJob("testjob");
		clickTab(tabpanel, "parameters");
		WebElement tabpanelParameters = findTabpanel(tabpanel, "parameters");
		clickButton(tabpanelParameters, "new");

		findOptionTextbox(tabpanelParameters, "name").sendKeys("Testparameter");
		findOptionTextbox(tabpanelParameters, "value").sendKeys("Test value");
		saveJob(tabpanel);
	}
	
	@Test
	@Order(3)
	void testAddNotification() {
		WebElement tabpanel = showJob("testjob");
		clickTab(tabpanel, "notifications");
		WebElement tabpanelNotifications = findTabpanel(tabpanel, "notifications");
		clickButton(tabpanelNotifications, "new");
		
		findOptionCombobox(tabpanelNotifications, "user").sendKeys("Testuser");
		saveJob(tabpanel);
	}
	
	@Test
	@Order(4)
	void testAddPermission() {
		WebElement tabpanel = showJob("testjob");
		clickTab(tabpanel, "permissions");
		
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		dragAndDrop(tabpanelPermissions, "testrole", "selected");
		saveJob(tabpanel);
	}
	
}
