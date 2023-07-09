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

import java.util.Arrays;

import org.seed.C;
import org.seed.core.form.navigation.Menu;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.ui.zk.Icons;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Window;

public class SelectIconViewModel extends AbstractApplicationViewModel {
	
	private static final int ICONS_PER_LINE = 15;
	
	@Wire("#selectIconWin")
	private Window window;
	
	private Menu menu;
	
	private AdminMenuViewModel parentVM;
	
	public Icons[] getIcons(int row) {
		final int start = row * ICONS_PER_LINE;
		final int leftover = Icons.values().length - start;
		
		return leftover > 1
				? Arrays.copyOfRange(Icons.values(), start, start + Math.min(leftover, ICONS_PER_LINE))
				: MiscUtils.toArray();
	}
	
	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam(C.PARAM) SelectIconParameter param) {
		Assert.notNull(param, C.PARAM);
		
		menu = param.menu;
		parentVM = param.parentVM;
		wireComponents(view);
	}
	
	@Command
	public void selectIcon(@BindingParam(C.ICON) Icons icon) {
		Assert.notNull(icon, C.ICON);
		
		if (icon == Icons.DUMMY_NO_ICON) {
			menu.setIcon(null);
		}
		else {
			menu.setIcon(icon.getIconClass());
		}
		parentVM.updateIcon(menu);
		window.detach();
	}
	
}
