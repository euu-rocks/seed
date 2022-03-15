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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.seed.C;
import org.seed.core.form.layout.LayoutService;
import org.seed.core.util.Assert;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

public class LayoutSourceViewModel extends AbstractApplicationViewModel {
	
	@Wire("#layoutDialogWin")
	private Window window;
	
	@WireVariable(value="layoutServiceImpl")
	private LayoutService layoutService;
	
	private AdminFormViewModel formViewModel;
	
	private String content;
	
	private String errorMessage;
	
	public String getContent() {
		return content;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam(C.PARAM) AdminFormViewModel param) {
		Assert.notNull(param, C.PARAM);
		wireComponents(view);
		
		formViewModel = param;
		reset();
	}
	
	@Command
	public void cancel() {
		window.detach();
	}

	@Command
	@NotifyChange({C.CONTENT, "errorMessage"})
	public void reset() {
		errorMessage = null;
		content = formViewModel.getLayoutContent();
	}
	
	@Command
	@SmartNotifyChange("errorMessage")
	public void validateLayout(@BindingParam(C.LAYOUT) String layout,
							   @BindingParam(C.ELEM) Component component) {
		try {
			layoutService.parseLayout(layout);
			errorMessage = null;
			showNotification(component, false, "admin.validate.success");
		} 
		catch (SAXException | IOException | ParserConfigurationException ex) {
			errorMessage = formatErrorMessage(ex);
		}
	}
	
	@Command
	@SmartNotifyChange("errorMessage")
	public void applyLayout(@BindingParam(C.LAYOUT) String layout,
							@BindingParam(C.ELEM) Component component) {
		try {
			formViewModel.setLayout(layoutService.parseLayout(layout));
			window.detach();
		} 
		catch (SAXException | IOException | ParserConfigurationException ex) {
			errorMessage = formatErrorMessage(ex);
		}
	}
	
	private String formatErrorMessage(Exception ex) {
		String error = ex.getMessage();
		if (ex instanceof SAXParseException) {
			final SAXParseException pex = (SAXParseException) ex;
			error += '\n' + getLabel("label.row") + ": " + pex.getLineNumber() + 
					 "  " + getLabel("label.column")  + ": " + pex.getColumnNumber();
		}
		return error;
	}
	
}
