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

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

public class CreateUserTest extends AbstractUserTest {
	
	@Test
	void testCreateUser() {
		clickMenu("administration-benutzer");
		assertEquals("Benutzer", findTab("benutzer").getText());
		WebElement tabpanel = findTabpanel("benutzer");
		clickButton(tabpanel, "new");
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "username").sendKeys("Testuser");
		findTextbox(tabpanel, "email").sendKeys("testuser@euu.rocks");
		
		WebElement tabpanelGroups = findTabpanel(tabpanel, "usergroups");
		dragAndDrop(tabpanelGroups, "testrole", "selected");
		dragAndDrop(tabpanelGroups, "administration", "selected");
		saveUser(tabpanel);
	}
	
}
