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
package org.seed.test.integration.entity;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class EditEntityTest extends AbstractEntityTest {
	
	@Test
	@Order(1)
	void testRenameEntity() {
		WebElement tabpanel = showEntity("integrationtest");
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("IntegrationTestNew");
		saveEntity(tabpanel);
	}
	
	/*@Test
	@Order(2)
	void testRenameField() {
		WebElement tabpanel = showEntity("integrationtestnew");
		findTab(tabpanel, "fields");
		WebElement tabpanelFields = findTabpanel(tabpanel, "fields");
		
	}*/
}
