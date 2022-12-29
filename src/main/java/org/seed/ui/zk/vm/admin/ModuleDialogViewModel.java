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

import java.util.Collections;

import org.seed.C;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.ModuleService;
import org.seed.core.data.ValidationError;
import org.seed.core.util.Assert;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

public class ModuleDialogViewModel extends AbstractApplicationViewModel {
	
	@Wire("#moduleDialogWin")
	private Window window;
	
	@WireVariable(value="moduleServiceImpl")
	private ModuleService moduleService;
	
	private AdminModuleViewModel parentVM;
	
	private String moduleName;

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam(C.PARAM) AdminModuleViewModel param) {
		Assert.notNull(param, C.PARAM);
		wireComponents(view);
		
		parentVM = param;
	}
	
	@Command
	public void importModuleFromDir(@BindingParam(C.ELEM) Component elem) {
		final Module module = moduleService.readModuleFromDir(moduleName);
		if (module != null) {
			window.detach();
			parentVM.analyzeModule(module, elem);
		}
		else {
			showValidationErrors(elem, "admin.transfer.importfail", 
					 			 Collections.singleton(new ValidationError(null, "admin.module.notfound")));
		}
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
}
