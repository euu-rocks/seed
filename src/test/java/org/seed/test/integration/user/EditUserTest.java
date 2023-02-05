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
package org.seed.test.integration.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class EditUserTest extends AbstractUserTest {
	
	@Test
	@Order(1)
	void testRenameUser() {
		WebElement tabpanel = showUser("testuser");
		assertEquals("Benutzer", findTab("benutzer").getText());
		clearTextbox(tabpanel, "username");
		findTextbox(tabpanel, "username").sendKeys("TestuserNew");
		saveUser(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRemoveRole() {
		WebElement tabpanel = showUser("testusernew");
		assertEquals("Rollen", findTab(tabpanel, "usergroups").getText());
		WebElement tabpanelUsergroups = findTabpanel(tabpanel, "usergroups");
		pause(100);
		dragAndDrop(tabpanelUsergroups, "testrole", "available");
		saveUser(tabpanel);
	}
	
}
