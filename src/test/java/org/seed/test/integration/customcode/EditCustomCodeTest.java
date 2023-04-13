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

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class EditCustomCodeTest extends AbstractCustomCodeTest {
	
	@Test
	@Order(1)
	void testEditCustomCode() {
		WebElement tabpanel = showCustomCode("test-testcustomcode");
		assertEquals("Quellcode: test.TestCustomCode", findTab("quellcode").getText());
		WebElement codeMirror = findCodeMirror(tabpanel, "content", 3);
		codeMirror.sendKeys(repeatKey(Keys.BACK_SPACE, 2));
		codeMirror.sendKeys("New {");
		findTab("quellcode").click(); // lose focus
		saveCustomCode(tabpanel);
	}
	
}
