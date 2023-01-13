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

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

import org.seed.test.integration.AbstractIntegrationTest;

@TestMethodOrder(OrderAnnotation.class)
public class CreateDataSourceTest extends AbstractIntegrationTest {
	
	@Test
	@Order(1)
	void testCreateDataSource() {
		clickMenu("administration-abfragen");
		assertEquals("Abfragen", findTab("abfragen").getText());
		
		WebElement tabpanel = findTabpanel("abfragen");
		clickButton(tabpanel, "new");
		
		clickTab(tabpanel, "parameters");
		WebElement tabpanelParameters = findTabpanel(tabpanel, "parameters");
		clickButton(tabpanelParameters, "new");
		
		findOptionTextbox(tabpanelParameters, "name").sendKeys("id");
		findOptionCombobox(tabpanelParameters, "type").sendKeys("Ganzzahl");
		
		clickTab(tabpanel, "query");
		WebElement tabpanelQuery = findTabpanel(tabpanel, "query");
		findCodeMirror(tabpanelQuery, "content", 1).sendKeys("select * from integrationtest where id={id}");
		clickButton(tabpanel, "save");
		findValidationMessage(); // name is empty
		
		findTextbox(tabpanel, "name").sendKeys("Testquery");
		clickButton(tabpanel, "save");
		findSuccessMessage();
	}
	
}
