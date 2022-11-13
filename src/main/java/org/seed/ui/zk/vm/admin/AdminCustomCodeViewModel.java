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
import org.seed.core.codegen.CodeManager;
import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.compile.CompilerException;
import org.seed.core.customcode.CustomCode;
import org.seed.core.customcode.CustomCodeService;
import org.seed.core.customcode.codegen.CustomCodeProvider;
import org.seed.core.user.Authorisation;

import org.springframework.util.StringUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class AdminCustomCodeViewModel extends AbstractAdminViewModel<CustomCode> {
	
	@WireVariable(value="customCodeServiceImpl")
	private CustomCodeService customCodeService;
	
	@WireVariable(value="customCodeProvider")
	private CustomCodeProvider customCodeProvider;
	
	@WireVariable(value="codeManagerImpl")
	private CodeManager codeManager;
	
	private String errorMessage;
	
	public AdminCustomCodeViewModel() {
		super(Authorisation.ADMIN_SOURCECODE, "customcode",
			  "/admin/customcode/customcodelist.zul", 
			  "/admin/customcode/customcode.zul");
	}
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam(C.PARAM) Object object) {
		super.init(object, view);
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, null, notifyObject);
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void newCode() {
		cmdNewObject();
	}
	
	@Command
	public void editCode() {
		cmdEditObject();
	}
	
	@Command
	public void refreshCode(@BindingParam(C.ELEM) Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteCode(@BindingParam(C.ELEM) Component component) {
		cmdDeleteObject(component);
	}
	
	@Command
	@NotifyChange("errorMessage")
	public void compile(@BindingParam(C.CODE) String code,
						@BindingParam(C.ELEM) Component component) {
		if (compileCode(code, component)) {
			showNotification(component, false, "admin.compile.success");
		}
	}
	
	@Command
	@NotifyChange("errorMessage")
	public void saveCode(@BindingParam(C.CODE) String code,
						 @BindingParam(C.ELEM) Component component) {
		if (compileCode(code, component) && 
			cmdSaveObject(component)) {
			resetCurrentSession();
		}
	}
	
	@Override
	protected CustomCodeService getObjectService() {
		return customCodeService;
	}

	@Override
	protected void resetProperties() {
		errorMessage = null;
	}
	
	private boolean compileCode(String code, Component component) {
		if (!StringUtils.hasText(code)) {
			showNotification(component, true, "admin.compile.nocode");
			return false;
		}
		try {
			getObject().setContent(code);
			final SourceCode sourceCode = customCodeProvider.getCustomCodeSource(getObject());
			codeManager.testCompile(sourceCode);
			getObject().setName(sourceCode.getQualifiedName());
			notifyObjectChange(C.NAME);
			errorMessage = null;
			return true;
		}
		catch (CompilerException cex) {
			errorMessage = cex.getMessage();
			return false;
		}
	}

}
