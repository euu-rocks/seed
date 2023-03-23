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
package org.seed.test.integration.form;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

import org.seed.test.integration.AbstractIntegrationTest;

@TestMethodOrder(OrderAnnotation.class)
public class TestobjectFormTest extends AbstractIntegrationTest {
	
	@Test
	@Order(1)
	void testCreateTestobject() {
		openMenu("testmenu");
		clickMenu("testmenu-testobject");
		assertEquals("Testobject", findTab("testobject").getText());
		final WebElement tabpanel = findTabpanel("testobject");
		clickButton(tabpanel, "neu");
		
		findTextbox(tabpanel, "text").sendKeys("Testobject");
		findTextbox(tabpanel, "description").sendKeys("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");
		findIntbox(tabpanel, "integer").sendKeys("12345");
		findLongbox(tabpanel, "long-int").sendKeys("1234567890");
		findDecimalbox(tabpanel, "decimal").sendKeys("12,34");
		findDoublebox(tabpanel, "floating").sendKeys("3,14159265359");
		findDatebox(tabpanel, "date").sendKeys("14.03.2023");
		findDatebox(tabpanel, "datetime").sendKeys("14.03.2023, 22:02:44");
		clickCheckbox(tabpanel, "bool");
		clickButton(tabpanel, "speichern");
	}
	
	@Test
	@Order(2)
	void testAddNestedobject() {
		final WebElement tabpanel = showObject("test-1");
		assertEquals("TestobjectNested", findTab(tabpanel, "testobjectnested").getText());
		final WebElement tabpanelNested = findTabpanel(tabpanel, "testobjectnested");
		clickButton(tabpanelNested, "neu");
		clickTab(tabpanel, "testobjectnested");
		findNestedField(tabpanelNested, "nestedtext").sendKeys("Nested 1");
		findNestedField(tabpanelNested, "nesteddecimal").sendKeys("1,23");
		findNestedField(tabpanelNested, "nestedfloat").sendKeys("0,123456789");
		clickButton(tabpanel, "speichern");
	}
	
	@Test
	@Order(3)
	void testEditTestobject() {
		final WebElement tabpanel = showObject("test-1");
		assertEquals("Testobject", findTab("testobject").getText());
		findTextbox(tabpanel, "text").sendKeys(" renamed");
		clearTextbox(tabpanel, "description");
		findTextbox(tabpanel, "description").sendKeys("new description");
		clearIntbox(tabpanel, "integer");
		findIntbox(tabpanel, "integer").sendKeys("98765");
		clearLongbox(tabpanel, "long-int");
		findLongbox(tabpanel, "long-int").sendKeys("9876543210");
		clearDecimalbox(tabpanel, "decimal");
		findDecimalbox(tabpanel, "decimal").sendKeys("98,76");
		clearDoublebox(tabpanel, "floating");
		findDoublebox(tabpanel, "floating").sendKeys("2.71828182846 ");
		clearDatebox(tabpanel, "date");
		findDatebox(tabpanel, "date").sendKeys("19.03.2023");
		clearDatebox(tabpanel, "datetime");
		findDatebox(tabpanel, "datetime").sendKeys("19.03.2023, 22:58:03");
		clickCheckbox(tabpanel, "bool");
		clickButton(tabpanel, "speichern");
	}
	
	@Test
	@Order(4)
	void testEditNestedobject() {
		final WebElement tabpanel = showObject("test-1");
		assertEquals("TestobjectNested", findTab(tabpanel, "testobjectnested").getText());
		final WebElement tabpanelNested = findTabpanel(tabpanel, "testobjectnested");
		clickListItem(tabpanelNested, "nested-1");
		
		clearNestedField(tabpanelNested, "nestedtext");
		findNestedField(tabpanelNested, "nestedtext").sendKeys("Nested renamed");
		clearNestedField(tabpanelNested, "nesteddecimal");
		findNestedField(tabpanelNested, "nesteddecimal").sendKeys("9,87");
		clearNestedField(tabpanelNested, "nestedfloat");
		findNestedField(tabpanelNested, "nestedfloat").sendKeys("9,876543210");
		clickButton(tabpanel, "speichern");
	}
	
	@Test
	@Order(5)
	void testChangeTestobjectStatus() {
		final WebElement tabpanel = showObject("test-1");
		openToolbarCombobox(tabpanel, "status");
		clickToolbarComboItem("20");
		
		final WebElement confirmDialog = findConfirmDialog();
		confirm(confirmDialog);
		pause(100);
		assertEquals("20 Status B", getToolbarComboValue(tabpanel, "status"));
	}
	
	@Test
	@Order(6)
	void testRemoveNestedobject() {
		final WebElement tabpanel = showObject("test-1");
		assertEquals("TestobjectNested", findTab(tabpanel, "testobjectnested").getText());
		final WebElement tabpanelNested = findTabpanel(tabpanel, "testobjectnested");
		clickListItem(tabpanelNested, "nested-renamed");
		clickButton(tabpanelNested, "loeschen");
		clickButton(tabpanel, "speichern");
	}
	
	@Test
	@Order(7)
	void testDeleteTestobject() {
		final WebElement tabpanel = showObject("test-1");
		assertEquals("Testobject", findTab("testobject").getText());
		
		clickButton(tabpanel, "loeschen");
		final WebElement confirmDialog = findConfirmDialog();
		confirm(confirmDialog);
		waitConfirmDialogDisappear();
	}
	
	private WebElement showObject(String name) {
		openMenu("testmenu");
		clickMenu("testmenu-testobject");
		findTab("testobject");
		final WebElement tabpanel = findTabpanel("testobject");
		clickListItem(tabpanel, name);
		clickButton(tabpanel, "bearbeiten");
		return tabpanel;
	}
	
}
