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
package org.seed.test.unit.menu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.seed.core.form.Form;
import org.seed.core.form.FormMetadata;
import org.seed.core.form.navigation.Menu;
import org.seed.core.form.navigation.MenuMetadata;

class MenuTest {
	
	@Test
	void testAddSubMenu() {
		final Menu menu = new MenuMetadata();
		final Menu subMenu = new MenuMetadata();
		assertFalse(menu.hasSubMenus());
		
		menu.addSubMenu(subMenu);
		assertSame(menu, subMenu.getParent());
		assertTrue(menu.hasSubMenus());
		assertSame(subMenu, menu.getSubMenus().get(0));
	}
	
	@Test
	void testGetChildByUid() {
		final Menu menu = new MenuMetadata();
		final Menu subMenu = new MenuMetadata();
		subMenu.setUid("sub");
		menu.addSubMenu(subMenu);
		assertNull(menu.getChildByUid("test"));
		
		subMenu.setUid("test");
		assertSame(subMenu, menu.getChildByUid("test"));
	}
	
	@Test
	void testGetFormUid() {
		final Menu menu = new MenuMetadata();
		final Form form = new FormMetadata();
		form.setUid("form");
		assertNull(menu.getFormUid());
		
		((MenuMetadata) menu).setFormUid("test");
		assertEquals("test", menu.getFormUid());
		
		((MenuMetadata) menu).setForm(form);
		assertEquals("form", menu.getFormUid());
	}
	
	@Test
	void testIsEqual() {
		final Menu menu1 = new MenuMetadata();
		final Menu menu2 = new MenuMetadata();
		assertTrue(menu1.isEqual(menu2));
		
		((MenuMetadata) menu1).setFormUid("form");
		((MenuMetadata) menu1).setIcon("icon");
		assertFalse(menu1.isEqual(menu2));
		
		((MenuMetadata) menu2).setFormUid("form");
		((MenuMetadata) menu2).setIcon("icon");
		assertTrue(menu1.isEqual(menu2));
	}
	
	@Test
	void testIsEqualSubMenus() {
		final Menu menu1 = new MenuMetadata();
		final Menu menu2 = new MenuMetadata();
		final Menu subMenu1 = new MenuMetadata();
		final Menu subMenu2 = new MenuMetadata();
		subMenu1.setUid("test");
		subMenu2.setUid("test");
		menu1.addSubMenu(subMenu1);
		assertFalse(menu1.isEqual(menu2));
		
		menu2.addSubMenu(subMenu2);
		assertTrue(menu1.isEqual(menu2));
		
		subMenu2.setUid("other");
		assertFalse(menu1.isEqual(menu2));
	}
	
	@Test
	void testRemoveSubMenu() {
		final Menu menu = new MenuMetadata();
		final Menu subMenu = new MenuMetadata();
		menu.addSubMenu(subMenu);
		assertSame(1, menu.getSubMenus().size());
		
		menu.removeSubMenu(subMenu);
		assertFalse(menu.hasSubMenus());
	}
}
