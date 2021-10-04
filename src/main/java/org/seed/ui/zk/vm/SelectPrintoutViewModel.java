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

import java.util.List;

import org.seed.C;
import org.seed.core.form.FormPrintout;
import org.seed.core.util.Assert;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Window;

public class SelectPrintoutViewModel extends AbstractViewModel {
	
	@Wire("#selectPrintoutWin")
	private Window window;
	
	private FormPrintout printout;
	
	private AbstractFormViewModel parentVM;
	
	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam(C.PARAM) AbstractFormViewModel param) {
		Assert.notNull(param, C.PARAM);
		
		parentVM = param;
		wireComponents(view);
	}
	
	public FormPrintout getPrintout() {
		return printout;
	}

	public void setPrintout(FormPrintout printout) {
		this.printout = printout;
	}

	public List<FormPrintout> getPrintouts() {
		return parentVM.getForm().getPrintouts();
	}
	
	@Command
	public void print() {
		window.detach();
		parentVM.print(printout);
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
}
