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

import org.seed.core.codegen.CodeManager;
import org.seed.core.codegen.CodeUtils;
import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.compile.CompilerException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.codegen.EntityFunctionCodeProvider;
import org.seed.core.entity.codegen.EntitySourceCodeProvider;
import org.seed.core.entity.transform.TransformerFunction;
import org.seed.core.entity.transform.codegen.TransformerFunctionCodeProvider;
import org.seed.core.task.Task;
import org.seed.core.task.codegen.TaskCodeProvider;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.springframework.util.Assert;
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
	
	@WireVariable(value="entitySourceCodeProvider")
	private EntitySourceCodeProvider entitySourceCodeProvider;
	
	@WireVariable(value="entityFunctionCodeProvider")
	private EntityFunctionCodeProvider entityFunctionCodeProvider;
	
	@WireVariable(value="transformerFunctionCodeProvider")
	private TransformerFunctionCodeProvider transformerFunctionCodeProvider;
	
	@WireVariable(value="taskCodeProvider")
	private TaskCodeProvider taskCodeProvider;
	
	@WireVariable(value="codeManagerImpl")
	private CodeManager codeManager;
	
	private AbstractAdminViewModel<?> parentViewModel;
	
	private TransformerFunction transformerFunction;
	
	private EntityFunction entityFunction;
	
	private Task task;
	
	private String errorMessage;
	
	private String originalContent;
	
	private int functionStartRow;
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public EntityFunction getFunction() {
		return entityFunction;
	}
	
	public String getContent() {
		return entityFunction != null
				? entityFunction.getContent()
				: transformerFunction != null 
					? transformerFunction.getContent()
					: task.getContent();
	}

	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam("param") CodeDialogParameter param) {
		Assert.notNull(param, "param is null");
		wireComponents(view);
		
		parentViewModel = param.parentViewModel;
		transformerFunction = param.transformerFunction;
		entityFunction = param.entityFunction;
		task = param.task;
		
		if (entityFunction != null) {
			if (entityFunction.getContent() == null) {
				entityFunction.setContent(entityFunctionCodeProvider.getFunctionTemplate(entityFunction));
			}
			originalContent = entityFunction.getContent();
		}
		else if (transformerFunction != null) {
			if (transformerFunction.getContent() == null) {
				transformerFunction.setContent(transformerFunctionCodeProvider.getFunctionTemplate(transformerFunction));
			}
			originalContent = transformerFunction.getContent();
		}
		else if (task != null) {
			if (task.getContent() == null) {
				task.setContent(taskCodeProvider.getFunctionTemplate(task));
			}
			originalContent = task.getContent();
		}
	}
	
	@Command
	@NotifyChange({"content", "errorMessage"})
	public void reset() {
		errorMessage = null;
		if (entityFunction != null) {
			entityFunction.setContent(originalContent);
		}
		if (transformerFunction != null) {
			transformerFunction.setContent(originalContent);
		}
		else if (task != null) {
			task.setContent(originalContent);
		}
	}
	
	@Command
	@SmartNotifyChange("errorMessage")
	public void compile(@BindingParam("code") String code,
						@BindingParam("elem") Component component) {
		if (!StringUtils.hasText(code)) {
			showNotification(component, true, "admin.compile.nocode");
			errorMessage = null;
			return;
		}
		try {
			codeManager.testCompile(createSourceCode(code));
			errorMessage = null;
			showNotification(component, false, "admin.compile.success");
		}
		catch (CompilerException cex) {
			errorMessage = formatError(cex.getMessage());
		}
	}
	
	@Command
	@SmartNotifyChange("errorMessage")
	public void applyCode(@BindingParam("code") String code,
						  @BindingParam("elem") Component component) {
		if (!StringUtils.hasText(code)) {
			showNotification(component, true, "admin.compile.nocode");
			errorMessage = null;
			return;
		}
		try {
			codeManager.testCompile(createSourceCode(code));
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
	
	@Command
	public void cancel() {
		if (entityFunction != null) {
			entityFunction.setContent(originalContent);
		}
		else if (transformerFunction != null) {
			transformerFunction.setContent(originalContent);
		}
		else if (task != null) {
			task.setContent(originalContent);
		}
		window.detach();
	}
	
	private SourceCode<?> createSourceCode(String code) {
		functionStartRow = -1;
		SourceCode<?> sourceCode = null;
		Pattern pattern = null;
		if (entityFunction != null) {
			entityFunction.setContent(code);
			if (entityFunction.isCallback()) {
				return entityFunctionCodeProvider.getFunctionSource(entityFunction);
			}
			else {
				sourceCode = entitySourceCodeProvider.getEntitySource((Entity) parentViewModel.getObject());
				pattern = Pattern.compile(FUNC_PRE + entityFunction.getInternalName() + FUNC_POST);
			}
		}
		else if (transformerFunction != null) {
			transformerFunction.setContent(code);
			return transformerFunctionCodeProvider.getFunctionSource(transformerFunction);
		}
		else if (task != null) {
			task.setContent(code);
			return taskCodeProvider.getTaskSource(task);
		}
		if (sourceCode != null && pattern != null) {
			functionStartRow = CodeUtils.getLineNumber(sourceCode.getContent(), pattern) - 
			   		   		   CodeUtils.getLineNumber(code, pattern);
		}
		return sourceCode;
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
	
	private final static String ERROR_SEP = ": error:";
	private final static String FUNC_PRE = ".*\\s";			// anything and a whitespace
	private final static String FUNC_POST = "\\s*\\(.*";	// zero or more whitespaces and ( and anything
	
}
