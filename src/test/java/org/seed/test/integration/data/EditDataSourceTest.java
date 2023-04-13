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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@TestMethodOrder(OrderAnnotation.class)
public class EditDataSourceTest extends AbstractDataSourceTest {
	
	@Test
	@Order(1)
	void testRenameDataSource() {
		WebElement tabpanel = showDataSource("testquery");
		assertEquals("Abfragen: Testquery", findTab("abfragen").getText());
		
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestqueryNew");
		saveDataSource(tabpanel);
	}
	
	@Test
	@Order(2)
	void testEditDataSource() {
		WebElement tabpanel = showDataSource("testquerynew");
		assertEquals("SQL-Ausdruck", findTab("query").getText());
		WebElement tabpanelQuery = findTabpanel(tabpanel, "query");
		WebElement codeMirror = findCodeMirror(tabpanelQuery, "content", 1);
		codeMirror.sendKeys(repeatKey(Keys.BACK_SPACE, 36));
		codeMirror.sendKeys("textfieldnew from integrationtest where id={id}");
		findTab("query").click(); // lose focus
		saveDataSource(tabpanel);
	}
	
	@Test
	@Order(3)
	void testChangeParameter() {
		WebElement tabpanel = showDataSource("testquerynew");
		clickTab(tabpanel, "parameters");
		assertEquals("Parameter", findTab("parameters").getText());
		WebElement tabpanelParameters = findTabpanel("parameters");
		
		clickListItem(tabpanelParameters, "id");
		clearOptionCombobox(tabpanelParameters, "type");
		findOptionCombobox(tabpanelParameters, "type").sendKeys("Ganzzahl (lang)");
		saveDataSource(tabpanel);
	}
	
}
