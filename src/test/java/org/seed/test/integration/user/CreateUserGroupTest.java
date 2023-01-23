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
public class CreateUserGroupTest extends AbstractUserGroupTest {
	
	@Test
	@Order(1)
	void testCreateUserGroup() {
		openMenu("administration-benutzer");
		clickMenu("administration-benutzer-rollen");
		assertEquals("Rollen", findTab("rollen").getText());
		WebElement tabpanel = findTabpanel("rollen");
		clickButton(tabpanel, "new");
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("Testrole");
		saveGroup(tabpanel);
	}
	
	@Test
	@Order(2)
	void testAssignUser() {
		WebElement tabpanel = showGroup("testrole");
		findTab(tabpanel, "users");
		WebElement tabpanelUsers = findTabpanel(tabpanel, "users");
		dragAndDrop(tabpanelUsers, "testuser", "selected");
		dragAndDrop(tabpanelUsers, "seed", "selected");
		saveGroup(tabpanel);
	}
	
	@Test
	@Order(3)
	void testAssignPermissions() {
		WebElement tabpanel = showGroup("testrole");
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		dragAndDrop(tabpanelPermissions, "admin-entity", "selected");
		dragAndDrop(tabpanelPermissions, "admin-form", "selected");
		dragAndDrop(tabpanelPermissions, "admin-menu", "selected");
		saveGroup(tabpanel);
	}
	
}
