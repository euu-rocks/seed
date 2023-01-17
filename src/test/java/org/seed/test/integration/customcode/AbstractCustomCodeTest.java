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
package org.seed.test.integration.customcode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.openqa.selenium.WebElement;

import org.seed.test.integration.AbstractIntegrationTest;

abstract class AbstractCustomCodeTest extends AbstractIntegrationTest {
	
	protected void saveCustomCode(WebElement tabpanel) {
		clickButton(tabpanel, "save");
		findSuccessMessage();
	}
	
	protected WebElement showCustomCode(String name) {
		clickMenu("administration-quellcode");
		assertEquals("Quellcode", findTab("quellcode").getText());
		WebElement tabpanel = findTabpanel("quellcode");
		clickListItem(tabpanel, name);
		clickButton(tabpanel, "edit");
		return tabpanel;
	}
	
}
