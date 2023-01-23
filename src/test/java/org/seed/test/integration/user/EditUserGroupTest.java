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

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class EditUserGroupTest extends AbstractUserGroupTest {
	
	@Test
	@Order(1)
	void testRenameUserGroup() {
		WebElement tabpanel = showGroup("testrole");
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestroleNew");
		saveGroup(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRemoveUser() {
		WebElement tabpanel = showGroup("testrolenew");
		findTab(tabpanel, "users");
		WebElement tabpanelUsers = findTabpanel(tabpanel, "users");
		pause(100);
		dragAndDrop(tabpanelUsers, "seed", "available");
		saveGroup(tabpanel);
	}
	
	@Test
	@Order(3)
	void testRemovePermission() {
		WebElement tabpanel = showGroup("testrolenew");
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		pause(100);
		dragAndDrop(tabpanelPermissions, "admin-menu", "available");
		saveGroup(tabpanel);
	}
	
}
