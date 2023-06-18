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
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class CreateCustomCodeTest extends AbstractCustomCodeTest {
	
	@Test
	@Order(1)
	void testCreateCustomCode() {
		clickMenu("administration-quellcode");
		assertEquals("Quellcode", findTab("quellcode").getText());
		WebElement tabpanel = findTabpanel("quellcode");
		clickButton(tabpanel, "new");
		
		WebElement window = findWindow("new-customcode");
		assertEquals("Neuen Quellcode erstellen", findWindowHeader(window).getText());
		findCombobox(window, "module").sendKeys("Testmodule");
		clickButton(window, "create");
		
		clickButton(tabpanel, "save");
		findValidationMessage(); // code is empty
		
		findCodeMirror(tabpanel, "content", 1).sendKeys("package test;\n"
				+ "\n"
				+ "public class TestCustomCode {\n"
				+ "\n"
				+ "	public void dummy() {\n"
				+ "		System.out.println(\"TestCustomCode\");\n"
				+ "	}\n"
				+ "\n"
				+ "}");
		
		saveCustomCode(tabpanel);
	}
}
