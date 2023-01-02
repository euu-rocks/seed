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

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class EditFilterTest extends AbstractFilterTest {
	
	@Test
	@Order(1)
	void testRenameFilter() {
		WebElement tabpanel = showFilter("testfilter");
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestfilterNew");
		saveFilter(tabpanel);
	}
	
	@Test
	@Order(2)
	void testEditCriteria() {
		WebElement tabpanel = showFilter("testfilternew");
		findTab(tabpanel, "criteria");
		WebElement tabpanelCriteria = findTabpanel(tabpanel, "criteria");
		clickListItem(tabpanelCriteria, "textfieldnew");
		
		findOptionCombobox(tabpanelCriteria, "nested").sendKeys("NestedTestNew");
		clickTab(tabpanel, "criteria"); // lose focus
		findOptionCombobox(tabpanelCriteria, "field").sendKeys("Name");
		clickTab(tabpanel, "criteria"); // lose focus
		findOptionCombobox(tabpanelCriteria, "operator").sendKeys("ungleich");
		saveFilter(tabpanel);
	}
	
	@Test
	@Order(3)
	void testEditHQLFilter() {
		WebElement tabpanel = showFilter("test-hql-filter");
		findTab(tabpanel, "hql");
		WebElement tabpanelHQL = findTabpanel(tabpanel, "hql");
		findCodeMirror(tabpanelHQL, "hql", 1).sendKeys("   --edited");
		clickTab(tabpanel, "hql"); // lose focus
		saveFilter(tabpanel);
	}
	
	@Test
	@Order(4)
	void testRemovePermission() {
		WebElement tabpanel = showFilter("testfilternew");
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		dragAndDrop(tabpanelPermissions, "testrole", "available");
		saveFilter(tabpanel);
	}
	
}
