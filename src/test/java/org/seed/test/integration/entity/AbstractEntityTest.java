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
package org.seed.test.integration.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.openqa.selenium.WebElement;

import org.seed.test.integration.AbstractIntegrationTest;

abstract class AbstractEntityTest extends AbstractIntegrationTest {
	
	protected AbstractEntityTest() {}
	
	protected WebElement newEntity() {
		clickMenu("administration-entitaeten");
		findTab("entitaeten");
		final WebElement tabpanel = findTabpanel("entitaeten");
		clickButton(tabpanel, "new");
		return tabpanel;
	}
	
	protected WebElement newEntityWindow() {
		final WebElement window = findWindow("new-entity");
		assertEquals("Neue Entität erstellen", findWindowHeader(window).getText());
		return window;
	}
	
	protected WebElement showEntity(String name) {
		clickMenu("administration-entitaeten");
		findTab("entitaeten");
		final WebElement tabpanel = findTabpanel("entitaeten");
		clickListItem(tabpanel, name);
		clickButton(tabpanel, "edit");
		return tabpanel;
	}
	
	protected void saveEntity(WebElement tabpanel) {
		clickButton(tabpanel, "save");
		pause(2000);
		findSuccessMessage();
	}
	
}
