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
package org.seed.test.integration.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class DeleteFilterTest extends AbstractFilterTest {
	
	@Test
	@Order(1)
	void testRemoveCriteria() {
		WebElement tabpanel = showFilter("testfilternew");
		assertEquals("Filterkriterien", findTab(tabpanel, "criteria").getText());
		WebElement tabpanelCriteria = findTabpanel(tabpanel, "criteria");
		clickListItem(tabpanelCriteria, "name");
		clickButton(tabpanelCriteria, "remove");
		saveFilter(tabpanel);
	}
	
	@Test
	@Order(2)
	void testDeleteFilter() {
		WebElement tabpanel = showFilter("testfilternew");
		assertEquals("Filterkriterien", findTab(tabpanel, "criteria").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
		waitTabDisappear("criteria");
	}
	
	@Test
	@Order(3)
	void testDeleteHQLFilter() {
		WebElement tabpanel = showFilter("test-hql-filter");
		assertEquals("HQL-Direkteingabe", findTab(tabpanel, "hql").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
		waitTabDisappear("hql");
	}
}
