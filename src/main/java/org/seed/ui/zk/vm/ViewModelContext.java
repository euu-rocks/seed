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
package org.seed.ui.zk.vm;

import org.seed.C;
import org.seed.core.util.Assert;
import org.seed.ui.ViewModelProperty;

public class ViewModelContext {
	
	private final AbstractFormViewModel vm;
	
	ViewModelContext(AbstractFormViewModel vm) {
		this.vm = vm;
	}
	
	public AbstractFormViewModel getViewModel() {
		return vm;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String name) {
		Assert.notNull(name, C.NAME);
		
		return (T) property(name).getValue();
	}
	
	public void setProperty(String name, Object value) {
		Assert.notNull(name, C.NAME);
		
		property(name).setValue(value);
	}
	
	private ViewModelProperty property(String name) {
		return new ViewModelProperty(vm.getTab(), name);
	}
	
}
