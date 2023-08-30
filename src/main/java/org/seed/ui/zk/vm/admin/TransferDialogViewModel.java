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

import java.util.List;

import org.seed.C;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.data.FileObject;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.transfer.ImportOptions;
import org.seed.core.entity.transfer.Transfer;
import org.seed.core.entity.transfer.TransferError;
import org.seed.core.entity.transfer.TransferResult;
import org.seed.core.entity.transfer.TransferService;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;
import org.seed.ui.zk.vm.TransferDialogParameter;
import org.seed.ui.zk.vm.TransferViewModel;

import org.springframework.util.ObjectUtils;
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

public class TransferDialogViewModel extends AbstractApplicationViewModel {
	
	private static final String IMPORT_FAILED  = "admin.transfer.importfail";
	private static final String IMPORT_SUCCESS = "admin.transfer.importsuccess";
	
	@Wire("#transferDialogWin")
	private Window window;
	
	@WireVariable(value="transferServiceImpl")
	private TransferService transferService;
	
	private AdminTransferViewModel adminTransferViewModel;
	
	private AdminModuleViewModel moduleViewModule;
	
	private TransferViewModel transferViewModel;
	
	private ImportOptions importOptions;
	
	private ImportAnalysis importAnalysis;
	
	private Transfer transfer;
	
	private TransferResult transferResult;
	
	private FileObject importFile;

	private List<ValidationError> importErrors;
	
	private ValidationError importError;
	
	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam(C.PARAM) TransferDialogParameter param) {
		Assert.notNull(param, C.PARAM);
		wireComponents(view);
		
		if (param.parentViewModel instanceof AdminTransferViewModel) {
			adminTransferViewModel = (AdminTransferViewModel) param.parentViewModel;
			transfer = adminTransferViewModel.getObject();
			init(param);
		}
		else if (param.parentViewModel instanceof AdminModuleViewModel) {
			moduleViewModule = (AdminModuleViewModel) param.parentViewModel;
			importAnalysis = param.importAnalysis;
		}
		else if (param.parentViewModel instanceof TransferViewModel) {
			transferViewModel = (TransferViewModel) param.parentViewModel;
			transfer = transferViewModel.getTransfer();
			init(param);
		}
	}
	
	private void init(TransferDialogParameter param) {
		if (param.transferResult != null) {
			transferResult = param.transferResult;
			importOptions = transferResult.getOptions();
		}
		else {
			importOptions = transferService.createImportOptions(transfer); 
			importFile = new FileObject();
		}
	}
	
	public ImportOptions getOptions() {
		return importOptions;
	}

	public ImportAnalysis getImportAnalysis() {
		return importAnalysis;
	}

	public FileObject getImportFile() {
		return importFile;
	}
	
	public TransferResult getTransferResult() {
		return transferResult;
	}

	public List<ValidationError> getImportErrors() {
		return importErrors;
	}

	public ValidationError getImportError() {
		return importError;
	}

	public void setImportError(ValidationError importError) {
		this.importError = importError;
	}

	public boolean hasIdentifier() {
		return transfer.getIdentifierField() != null;
	}
	
	public boolean isResultSuccess() {
		return importOptions.isAllOrNothing()
				? !transferResult.hasErrors()
				: transferResult.getSuccessfulTransfers() > 0;
	}
	
	public boolean isResultFail() {
		return importOptions.isAllOrNothing()
				? transferResult.hasErrors()
				: transferResult.hasErrors() && transferResult.getSuccessfulTransfers() == 0;
	}
	
	public String getModuleImportResult() {
		if (importAnalysis.hasChanges()) {
			return importAnalysis.getNumChanges() > 1
					? getLabel("admin.module.resultnumchanges", String.valueOf(importAnalysis.getNumChanges()))
					: getLabel("admin.module.resultonechange"); 
		}
		return getLabel("admin.module.resultnochanges");
	}
	
	public String getResultText() {
		if (importOptions.isAllOrNothing()) {
			return transferResult.hasErrors()
					? getLabel(IMPORT_FAILED)
					: getLabel(IMPORT_SUCCESS);
		}
		if (transferResult.getSuccessfulTransfers() > 0) {
			return transferResult.hasErrors()
					? getLabel("admin.transfer.importpartlysuccess")
					: getLabel(IMPORT_SUCCESS);
		}
		else if (transferResult.getFailedTransfers() == 0) {
			return getLabel("admin.transfer.importnothing");
		}
		return getLabel(IMPORT_FAILED);
	}
	
	public String formatError(ValidationError error) {
		return error != null 
				? MiscUtils.removeHTMLTags(MiscUtils.formatValidationError(error)) 
				: null;
	}
	
	public String getErrorObject(ValidationError error) {
		return error != null 
				? getEntityName(error.getEntity()) + ' ' + error.getEntity().getName() 
				: null;
	}
	
	public String getErrorDetail(TransferError error) {
		if (error.validationError != null) {
			if (ObjectUtils.isEmpty(error.validationError.getParameters())) {
				return MiscUtils.removeHTMLTags(getLabel(error.validationError.getError()));
			}
			else {
				final String[] params = error.validationError.getParameters();
				for (int i = 0; i < params.length; i++) {
					if (params[i].startsWith("label.")) {
						params[i] = getLabel(params[i]);
					}
				}
				return MiscUtils.removeHTMLTags(getLabel(error.validationError.getError(), params));
			}
		}
		else if (error.message != null) {
			return error.message;
		}
		else {
			return getLabel("val.transfer." + error.type.name().toLowerCase(), 
							error.fieldName, error.value);
		}
	}
	
	public String getStyle(ImportAnalysis.ChangeType changeType) {
		String color = null;
		switch (changeType) {
			case NEW:
				color = "99ff99";
				break;
			case MODIFY:
				color = "ffcc99";
				break;
			case DELETE:
				color = "ff9999";
		}
		return "background-color:#" + color;
	}

	@Command
	public void cancel() {
		window.detach();
	}
	
	@Command
	public void importTransfer(@BindingParam(C.ELEM) Component component) {
		try {
			final TransferResult result = transferService.doImport(transfer, importOptions, importFile);
			window.detach();
			if (adminTransferViewModel != null) {
				adminTransferViewModel.setTransferResult(result);
			}
			else if (transferViewModel != null) {
				transferViewModel.setTransferResult(result);
			}
		}
		catch (ValidationException vex) {
			showValidationErrors(component, IMPORT_FAILED, vex.getErrors());
		}
	}
	
	@Command
	public void importModule(@BindingParam(C.ELEM) Component component) {
		final ImportResult result = moduleViewModule.importModule(importAnalysis.getModule(), component);
		if (result.success) {
			window.detach();
		}
		else {
			importErrors = result.errors;
			importError = importErrors != null ? importErrors.get(0) : null;
			notifyChange("importErrors");
		}
	}
	
}
