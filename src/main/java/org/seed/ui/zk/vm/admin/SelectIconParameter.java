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
package org.seed.ui.zk.vm.admin;

import org.seed.C;
import org.seed.core.form.navigation.Menu;
import org.seed.core.util.Assert;

public class SelectIconParameter {
	
	final AdminMenuViewModel parentVM;
	
	final Menu menu;
	
	SelectIconParameter(AdminMenuViewModel parentVM, Menu menu) {
		Assert.notNull(parentVM, "parentViewModel");
		Assert.notNull(menu, C.MENU);
		
		this.parentVM = parentVM;
		this.menu = menu;
	}
	
}
