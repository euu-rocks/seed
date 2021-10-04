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
import org.seed.core.form.Form;
import org.seed.core.form.layout.LayoutElement;
import org.seed.core.util.Assert;

class LayoutDialogParameter {
	
	final AdminFormViewModel parentViewModel;
	
	final String command;
	
	final Form form;
	
	final LayoutElement layoutRoot;
	
	final String contextId;
	
	LayoutDialogParameter(AdminFormViewModel parentViewModel, 
						  Form form, LayoutElement layoutRoot, 
						  String command, String contextId) {
		
		Assert.notNull(parentViewModel, "parentViewModel");
		Assert.notNull(form, C.FORM);
		Assert.notNull(command, C.COMMAND);
		
		this.parentViewModel = parentViewModel;
		this.form = form;
		this.layoutRoot = layoutRoot;
		this.command = command;
		this.contextId = contextId;
	}
	
}
