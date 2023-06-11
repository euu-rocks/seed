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
import org.seed.core.entity.transfer.Transfer;
import org.seed.core.entity.transfer.TransferAccess;
import org.seed.core.entity.transfer.TransferResult;
import org.seed.core.entity.transfer.TransferService;
import org.seed.core.util.MiscUtils;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;

public class TransferViewModel extends AbstractApplicationViewModel {
	
	@WireVariable(value="transferServiceImpl")
	private TransferService transferService;
	
	private Transfer transfer;

	public Transfer getTransfer() {
		return transfer;
	}

	public void setTransfer(Transfer transfer) {
		this.transfer = transfer;
	}
	
	public List<Transfer> getTransfers() {
		return transferService.getTransfers(getUser(), currentSession());
	}
	
	public boolean isImportEnabled(Transfer transfer) {
		return transfer.checkPermissions(getUser(), TransferAccess.IMPORT);
	}
	
	@Command
	public void refresh() {
		notifyChange("transfers");
	}
	
	@Command
	public void exportTransfer() {
		final String fileName = transfer.getName() + '_' + MiscUtils.getTimestampString() +
								transfer.getFormat().fileExtension;
		Filedownload.save(transferService.doExport(transfer), transfer.getFormat().contentType, fileName);
	}
	
	@Command
	public void importTransfer(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx,
							   @BindingParam(C.ELEM) Component component) {
		showDialog("/admin/transfer/importdialog.zul", new TransferDialogParameter(this));
	}
	
	public void setTransferResult(TransferResult transferResult) {
		showDialog("/admin/transfer/importresult.zul", new TransferDialogParameter(this, transferResult));
	}
	
}
