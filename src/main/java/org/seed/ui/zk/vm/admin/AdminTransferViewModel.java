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

import org.seed.core.application.ContentObject;
import org.seed.core.codegen.SourceCode;
import org.seed.core.data.SystemObject;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.transfer.CharEncoding;
import org.seed.core.entity.transfer.Newline;
import org.seed.core.entity.transfer.Transfer;
import org.seed.core.entity.transfer.TransferElement;
import org.seed.core.entity.transfer.TransferService;
import org.seed.core.user.Authorisation;
import org.seed.core.entity.transfer.TransferFormat;
import org.seed.core.entity.transfer.TransferResult;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.NameUtils;
import org.seed.ui.ListFilter;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

import com.opencsv.ICSVWriter;

public class AdminTransferViewModel extends AbstractAdminViewModel<Transfer> {
	
	private static final String ELEMENTS = "elements";
	
	@Wire("#newTransferWin")
	private Window window;
	
	@WireVariable(value="transferServiceImpl")
	private TransferService transferService;
	
	@WireVariable(value="entityServiceImpl")
	private EntityService entityService;
	
	private TransferElement element;
	
	public AdminTransferViewModel() {
		super(Authorisation.ADMIN_ENTITY, "transfer",
			  "/admin/transfer/transferlist.zul", 
			  "/admin/transfer/transfer.zul",
			  "/admin/transfer/newtransfer.zul");
	}
	
	public TransferFormat[] getTransferFormats() {
		return TransferFormat.values();
	}
	
	public CharEncoding[] getEncodings() {
		return CharEncoding.values();
	}
	
	public Newline[] getNewlines() {
		return Newline.values();
	}
	
	public String getDefaultSeparatorChar() {
		return String.valueOf(ICSVWriter.DEFAULT_SEPARATOR);
	}
	
	public String getDefaultQuoteChar() {
		return String.valueOf(ICSVWriter.DEFAULT_QUOTE_CHARACTER);
	}
	
	public String getDefaultEscapeChar() {
		return String.valueOf(ICSVWriter.DEFAULT_ESCAPE_CHARACTER);
	}
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam("param") Object object) {
		super.init(object, view);
	}
	
	@Override
	protected void initFilters() {
		final ListFilter<Transfer> filterEntity = getFilter(FILTERGROUP_LIST, "entity");
		filterEntity.setValueFunction(o -> o.getEntity().getName());
		for (Transfer transfer : getObjectList()) {
			filterEntity.addValue(transfer.getEntity().getName());
		}
	}
	
	public TransferElement getElement() {
		return element;
	}

	public void setElement(TransferElement element) {
		this.element = element;
	}

	@Override
	protected TransferService getObjectService() {
		return transferService;
	}
	
	public List<Entity> getEntities() {
		return entityService.findNonGenericEntities();
	}
	
	@Command
	public void createTransfer(@BindingParam("elem") Component elem) {
		cmdInitObject(elem, window);
	}
	
	@Command
	public void selectAllElements() {
		selectAll(ELEMENTS);
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
	@Command
	public void newTransfer() {
		cmdNewObjectDialog();
	}
	
	@Command
	public void editTransfer() {
		cmdEditObject();
	}
	
	@Command
	public void refreshTransfer(@BindingParam("elem") Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteTransfer(@BindingParam("elem") Component component) {
		cmdDeleteObject(component);
	}
	
	@Command
	public void saveTransfer(@BindingParam("elem") Component component) {
		adjustLists(getObject().getElements(), getListManagerList(ELEMENTS, LIST_SELECTED));
		
		cmdSaveObject(component);
	}
	
	@Command
	public void exportTransfer() {
		final String fileName = NameUtils.getNameWithTimestamp(getObject().getName()) + ".csv";
		Filedownload.save(transferService.doExport(getObject()), "text/csv", fileName);
	}
	
	@Command
	public void importTransfer(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx,
							   @BindingParam("elem") Component component) {
		showDialog("/admin/transfer/importdialog.zul", new TransferDialogParameter(this));
	}
	
	protected void setTransferResult(TransferResult transferResult) {
		showDialog("/admin/transfer/importresult.zul", new TransferDialogParameter(this, transferResult));
	}
	
	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, null, notifyObject);
	}
	
	@Command
	@SmartNotifyChange("element")
	public void dropToElementList(@BindingParam("item") TransferElement item,
								  @BindingParam("list") int listNum) {
		super.dropToList(ELEMENTS, listNum, item);
		if (listNum == LIST_AVAILABLE && item == element) {
			this.element = null;
		}
	}
	
	@Command
	@SmartNotifyChange("element")
	public void insertToElementList(@BindingParam("base") TransferElement base,
				  				    @BindingParam("item") TransferElement item,
				  				    @BindingParam("list") int listNum) {
		super.insertToList(ELEMENTS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == element) {
			this.element = null;
		}
	}
	
	@GlobalCommand
	public void globalRefreshObject(@BindingParam("param") Long objectId) {
		refreshObject(objectId);
	}

	@Override
	protected List<SystemObject> getListManagerSource(String key, int listNum) {
		if (ELEMENTS.equals(key)) {
			return MiscUtils.cast(listNum == LIST_AVAILABLE
					? transferService.getAvailableElements(getObject())
				    : getObject().getElements());
		}
		else {
			throw new IllegalStateException("unknown list manager key: " + key);
		}
	}

	@Override
	protected void resetProperties() {
		element = null;
	}
	
	@Override
	protected SourceCode getSourceCode(ContentObject contentObject) {
		throw new UnsupportedOperationException();
	}

}
