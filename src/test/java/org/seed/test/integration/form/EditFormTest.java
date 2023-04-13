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

@TestMethodOrder(OrderAnnotation.class)
public class EditFormTest extends AbstractFormTest {
	
	@Test
	@Order(1)
	void testRenameForm() {
		WebElement tabpanel = showForm("testform");
		assertEquals("Formulare: Testform", findTab("formulare").getText());
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestformNew");
		saveForm(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRebuildLayout() {
		WebElement tabpanel = showForm("testformnew");
		assertEquals("Layout", findTab(tabpanel, "layout").getText());
		
		WebElement tabpanelLayout = findTabpanel(tabpanel, "layout");
		clickButton(tabpanelLayout, "autolayout");
		saveForm(tabpanel);
	}
	
	@Test
	@Order(3)
	void testRenameField() {
		WebElement tabpanel = showForm("testformnew");
		assertEquals("Liste", findTab(tabpanel, "fields").getText());
		clickTab(tabpanel, "fields");
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		clickItem(tabpanelFields, "status");
		findOptionTextbox(tabpanelFields, "fieldlabel").sendKeys("StatusNew");
		clickTab(tabpanel, "fields"); // lose focus
		saveForm(tabpanel);
	}
	
	@Test
	@Order(4)
	void testRenameAction() {
		WebElement tabpanel = showForm("testformnew");
		assertEquals("Aktionen", findTab(tabpanel, "actions").getText());
		clickTab(tabpanel, "actions");
		WebElement tabpanelActions = findTabpanel(tabpanel, "actions");
		clickItem(tabpanelActions, "save");
		findOptionTextbox(tabpanelActions, "actionlabel").sendKeys("SpeichernNew");
		clickTab(tabpanel, "actions"); // lose focus
		saveForm(tabpanel);
	}
	
	@Test
	@Order(5)
	void testRenameTransformer() {
		WebElement tabpanel = showForm("testformnew");
		assertEquals("Transformationen", findTab(tabpanel, "transformers").getText());
		clickTab(tabpanel, "transformers");
		WebElement tabpanelTransformers = findTabpanel(tabpanel, "transformers");
		clickItem(tabpanelTransformers, "testtransformernew");
		findOptionTextbox(tabpanelTransformers, "transformerlabel").sendKeys("TesttransformerRenamed");
		clickTab(tabpanel, "transformers"); // lose focus
		saveForm(tabpanel);
	}
	
}
