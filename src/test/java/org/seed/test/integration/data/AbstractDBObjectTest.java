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
package org.seed.test.integration.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.openqa.selenium.WebElement;

import org.seed.test.integration.AbstractIntegrationTest;

abstract class AbstractDBObjectTest extends AbstractIntegrationTest {
	
	protected WebElement newDBObject() {
		clickMenu("administration-datenbankelemente");
		findTab("datenbankelemente");
		WebElement tabpanel = findTabpanel("datenbankelemente");
		clickButton(tabpanel, "new");
		return tabpanel;
	}
	
	protected WebElement newDBObjectWindow() {
		final WebElement window = findWindow("new-dbobject");
		assertEquals("Neues Datenbankelement erstellen", findWindowHeader(window).getText());
		return window;
	}
	
	protected WebElement showDBObject(String name) {
		clickMenu("administration-datenbankelemente");
		findTab("datenbankelemente");
		WebElement tabpanel = findTabpanel("datenbankelemente");
		clickListItem(tabpanel, name);
		clickButton(tabpanel, "edit");
		return tabpanel;
	}
	
	protected void saveDBObject(WebElement tabpanel) {
		clickButton(tabpanel, "save");
		findSuccessMessage();
	}
	
}
