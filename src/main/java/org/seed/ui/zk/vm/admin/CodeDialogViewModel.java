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

import java.util.regex.Pattern;

import org.seed.C;
import org.seed.core.application.ContentObject;
import org.seed.core.codegen.CodeManager;
import org.seed.core.codegen.CodeUtils;
import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.compile.CompilerException;
import org.seed.core.entity.EntityFunction;
import org.seed.core.util.Assert;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
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

public class CodeDialogViewModel extends AbstractApplicationViewModel {
	
	@Wire("#codeDialogWin")
	private Window window;
	
	@WireVariable(value="codeManagerImpl")
	private CodeManager codeManager;
	
	private AbstractAdminViewModel<?> parentViewModel;
	
	private ContentObject contentObject;
	
	private String errorMessage;
	
	private String originalContent;
	
	private int functionStartRow;
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public String getContent() {
		return contentObject.getContent();
	}

	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam(C.PARAM) CodeDialogParameter param) {
		Assert.notNull(param, C.PARAM);
		wireComponents(view);
		
		parentViewModel = param.parentViewModel;
		contentObject = param.contentObject;
		originalContent = contentObject.getContent();
		Assert.hasText(originalContent, C.CODE);
	}
	
	@Command
	public void cancel() {
		contentObject.setContent(originalContent);
		window.detach();
	}
	
	@Command
	@NotifyChange({C.CONTENT, "errorMessage"})
	public void reset() {
		errorMessage = null;
		contentObject.setContent(originalContent);
	}
	
	@Command
	@SmartNotifyChange("errorMessage")
	public void compile(@BindingParam(C.CODE) String code,
						@BindingParam(C.ELEM) Component component) {
		if (!validateCode(code, component)) {
			return;
		}
		try {
			codeManager.testCompile(createSourceCode(code));
			if (parentViewModel.getObject().isNew()) {
				removeClass(code);
			}
			errorMessage = null;
			showNotification(component, false, "admin.compile.success");
		}
		catch (CompilerException cex) {
			errorMessage = formatError(cex.getMessage());
		}
	}
	
	@Command
	@SmartNotifyChange("errorMessage")
	public void applyCode(@BindingParam(C.CODE) String code,
						  @BindingParam(C.ELEM) Component component) {
		if (!validateCode(code, component)) {
			return;
		}
		try {
			codeManager.testCompile(createSourceCode(code));
			if (parentViewModel.getObject().isNew()) {
				removeClass(code);
			}
			errorMessage = null;
			if (!ObjectUtils.nullSafeEquals(code, originalContent)) {
				parentViewModel.flagDirty();
			}
			window.detach();
		}
		catch (CompilerException cex) {
			errorMessage = formatError(cex.getMessage());
		}
	}
	
	private boolean validateCode(String code, Component component) {
		if (!StringUtils.hasText(code)) {
			showNotification(component, true, "admin.compile.nocode");
			errorMessage = null;
			return false;
		}
		// check package
		else if (originalContent.contains("package ")) {
			final String packageName = CodeUtils.extractPackageName(CodeUtils.extractQualifiedName(code));
			if (!packageName.equals(CodeUtils.extractPackageName(CodeUtils.extractQualifiedName(originalContent)))) {
				showNotification(component, true, "val.illegal.packagerename");
				return false;
			}
		}
		return true;
	}
	
	private SourceCode createSourceCode(String code) {
		functionStartRow = -1;
		contentObject.setContent(code);
		final SourceCode sourceCode = parentViewModel.getSourceCode(contentObject);
		if (contentObject instanceof EntityFunction) {
			final EntityFunction entityFunction = (EntityFunction) contentObject;
			final Pattern pattern = Pattern.compile(FUNC_PRE + entityFunction.getInternalName() + FUNC_POST);
			functionStartRow = CodeUtils.getLineNumber(sourceCode.getContent(), pattern) - 
	   		   		   		   CodeUtils.getLineNumber(code, pattern);
		}
		return sourceCode;
	}
	
	private void removeClass(String code) {
		codeManager.removeClass(CodeUtils.extractQualifiedName(code));
	}
	
	// replace line numbers for member functions
	private String formatError(String error) {
		if (functionStartRow < 0) {
			return error;
		}
		final StringBuilder buf = new StringBuilder();
		for (String part : error.split(ERROR_SEP)) {
			final int idx = part.indexOf(':') + 1;
			int line;
			try {
				line = Integer.parseInt(part.substring(idx));
				line -= functionStartRow;
			}
			catch (Exception ex) {
				buf.append(part);
				break;
			}
			buf.append(part.substring(0, idx))
			   .append(line).append(ERROR_SEP);
		}
		return buf.toString();
	}
	
	private static final String ERROR_SEP = ": error:";
	private static final String FUNC_PRE = ".*\\s";			// anything and a whitespace
	private static final String FUNC_POST = "\\s*\\(.*";	// zero or more whitespaces and ( and anything
	
}
