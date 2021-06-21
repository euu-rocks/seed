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

import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.entity.transfer.TransferResult;
import org.seed.core.util.Assert;

class TransferDialogParameter {
	
	private static final String PARENT_VIEWMODEL = "parentViewModel";
	
	final AbstractAdminViewModel<?> parentViewModel;
	
	final TransferResult transferResult;
	
	final ImportAnalysis importAnalysis;

	public TransferDialogParameter(AdminTransferViewModel parentViewModel) {
		Assert.notNull(parentViewModel, PARENT_VIEWMODEL);
		
		this.parentViewModel = parentViewModel;
		this.transferResult = null;
		this.importAnalysis = null;
	}

	public TransferDialogParameter(AdminTransferViewModel parentViewModel, TransferResult transferResult) {
		Assert.notNull(parentViewModel, PARENT_VIEWMODEL);
		Assert.notNull(transferResult, "transferResult");
		
		this.parentViewModel = parentViewModel;
		this.transferResult = transferResult;
		importAnalysis = null;
	}

	public TransferDialogParameter(AdminModuleViewModel parentViewModel, ImportAnalysis importAnalysis) {
		Assert.notNull(parentViewModel, PARENT_VIEWMODEL);
		Assert.notNull(importAnalysis, "importAnalysis");
		
		this.parentViewModel = parentViewModel;
		this.importAnalysis = importAnalysis;
		transferResult = null;
	}

}
