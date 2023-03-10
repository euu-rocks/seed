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
package org.seed.test.integration.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class DeleteTransformerTest extends AbstractTransformerTest {
	
	@Test
	@Order(1)
	void testRemoveFunction() {
		WebElement tabpanel = showTransformer("testtransformernew");
		assertEquals("Funktionen", findTab(tabpanel, "functions").getText());
		clickTab(tabpanel, "functions");
		WebElement tabpanelFunctions = findTabpanel(tabpanel, "functions");
		clickListItem(tabpanelFunctions, "testfunction");
		clickButton(tabpanelFunctions, "remove");
		saveTransformer(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRemoveNested() {
		WebElement tabpanel = showTransformer("testtransformernew");
		assertEquals("Unterobjekte", findTab(tabpanel, "nesteds").getText());
		clickTab(tabpanel, "nesteds");
		WebElement tabpanelNesteds = findTabpanel(tabpanel, "nesteds");
		clickListItem(tabpanelNesteds, "nestedtestnew");
		clickButton(tabpanelNesteds, "remove");
		saveTransformer(tabpanel);
	}
	
	@Test
	@Order(3)
	void testRemoveElement() {
		WebElement tabpanel = showTransformer("testtransformernew");
		assertEquals("Elemente", findTab("elements").getText());
		WebElement tabpanelElements = findTabpanel(tabpanel, "elements");
		clickListItem(tabpanelElements, "derivedtest-derivedtest");
		clickButton(tabpanelElements, "remove");
		saveTransformer(tabpanel);
	}
	
	@Test
	@Order(4)
	void testDeleteTransformer() {
		WebElement tabpanel = showTransformer("testtransformernew");
		assertEquals("Transformationen", findTab("transformationen").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
		waitTabDisappear("elements");
	}
	
}
