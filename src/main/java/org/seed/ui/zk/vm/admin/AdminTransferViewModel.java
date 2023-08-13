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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.seed.C;
import org.seed.core.data.SystemObject;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.transfer.CharEncoding;
import org.seed.core.entity.transfer.NestedTransfer;
import org.seed.core.entity.transfer.Newline;
import org.seed.core.entity.transfer.Transfer;
import org.seed.core.entity.transfer.TransferAccess;
import org.seed.core.entity.transfer.TransferElement;
import org.seed.core.entity.transfer.TransferPermission;
import org.seed.core.entity.transfer.TransferService;
import org.seed.core.user.Authorisation;
import org.seed.core.entity.transfer.TransferFormat;
import org.seed.core.entity.transfer.TransferResult;
import org.seed.core.util.MiscUtils;
import org.seed.ui.zk.vm.TransferDialogParameter;

import org.springframework.util.ObjectUtils;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

import com.opencsv.ICSVWriter;

public class AdminTransferViewModel extends AbstractAdminViewModel<Transfer> {
	
	private static final String ELEMENTS = "elements";
	private static final String NESTEDS  = "nesteds";
	private static final String NESTEDELEMENTS  = "nestedelements";
	private static final String PERMISSIONS = "permissions";
	
	@Wire("#newTransferWin")
	private Window window;
	
	@WireVariable(value="transferServiceImpl")
	private TransferService transferService;
	
	@WireVariable(value="entityServiceImpl")
	private EntityService entityService;
	
	private TransferElement element;
	
	private TransferPermission permission;
	
	private NestedTransfer nestedTransfer;
	
	private TransferElement nestedElement;
	
	private List<TransferElement> transferElements;
	
	private List<NestedTransfer> nestedTransfers;
	
	public AdminTransferViewModel() {
		super(Authorisation.ADMIN_ENTITY, C.TRANSFER,
			  "/admin/transfer/transferlist.zul", 
			  "/admin/transfer/transfer.zul",
			  "/admin/transfer/newtransfer.zul");
	}
	
	public List<TransferElement> getElements() {
		return transferElements;
	}

	public List<NestedTransfer> getNesteds() {
		return nestedTransfers;
	}
	
	public TransferElement getElement() {
		return element;
	}

	public void setElement(TransferElement element) {
		this.element = element;
	}
	
	public TransferPermission getPermission() {
		return permission;
	}

	public void setPermission(TransferPermission permission) {
		this.permission = permission;
	}

	public NestedTransfer getNested() {
		return nestedTransfer;
	}

	public void setNested(NestedTransfer nested) {
		this.nestedTransfer = nested;
	}

	public TransferElement getNestedElement() {
		return nestedElement;
	}

	public void setNestedElement(TransferElement nestedElement) {
		this.nestedElement = nestedElement;
	}

	public TransferFormat[] getTransferFormats() {
		return TransferFormat.values();
	}
	
	public TransferAccess[] getTransferAccesses() {
		return TransferAccess.values();
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
					 @ExecutionArgParam(C.PARAM) Object object) {
		super.init(object, view);
	}
	
	@Override
	protected void initObject(Transfer transfer) {
		super.initObject(transfer);
		transferElements = new ArrayList<>(transferService.getMainObjectElements(transfer));
		nestedTransfers = new ArrayList<>(transferService.getNestedTransfers(transfer));
	}
	
	@Override
	protected void initFilters() {
		final var filterEntity = getFilter(FILTERGROUP_LIST, C.ENTITY);
		filterEntity.setValueFunction(o -> o.getEntity().getName());
		final var filterType = getFilter(FILTERGROUP_LIST, C.TYPE);
		filterType.setValueFunction(o -> getEnumLabel(o.getFormat()));
		for (Transfer transfer : getObjectList()) {
			filterEntity.addValue(transfer.getEntity().getName());
			filterType.addValue(getEnumLabel(transfer.getFormat()));
		}
	}
	
	public String getElementName(TransferElement element) {
		if (element != null) {
			return element.getEntityField() != null
					? element.getEntityField().getName()
					: getEnumLabel(element.getSystemField());
		}
		return null;
	}
	
	public boolean hasFormat(TransferElement element) {
		return element != null && 
				(element.getFieldType().isDate() ||
				 element.getFieldType().isDateTime());
	}

	@Override
	protected TransferService getObjectService() {
		return transferService;
	}
	
	public List<Entity> getEntities() {
		return entityService.findNonGenericEntities(currentSession());
	}
	
	@Command
	public void createTransfer(@BindingParam(C.ELEM) Component elem) {
		cmdInitObject(elem, window);
	}
	
	@Command
	@NotifyChange(ELEMENTS)
	public void selectAllElements() {
		selectAll(ELEMENTS);
		adjustLists(transferElements, getListManagerList(ELEMENTS, LIST_SELECTED));
	}
	
	@Command
	@NotifyChange(NESTEDS)
	public void selectAllNesteds() {
		selectAll(NESTEDS);
		adjustLists(nestedTransfers, getListManagerList(NESTEDS, LIST_SELECTED));
	}
	
	@Command
	public void selectAllNestedElements() {
		selectAll(NESTEDELEMENTS);
		adjustLists(nestedTransfer.getElements(), getListManagerList(NESTEDELEMENTS, LIST_SELECTED));
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
	public void refreshTransfer(@BindingParam(C.ELEM) Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteTransfer(@BindingParam(C.ELEM) Component component) {
		cmdDeleteObject(component);
	}
	
	@Command
	public void saveTransfer(@BindingParam(C.ELEM) Component component) {
		transferService.adjustElements(getObject(), transferElements, nestedTransfers);
		adjustLists(getObject().getPermissions(), getListManagerList(PERMISSIONS, LIST_SELECTED));
		if (cmdSaveObject(component)) {
			refreshMenu();
		}
	}
	
	@Command
	public void exportTransfer() {
		final String fileName = getObject().getName() + '_' + MiscUtils.getTimestampString() +
								getObject().getFormat().fileExtension;
		Filedownload.save(transferService.doExport(getObject()), getObject().getFormat().contentType, fileName);
	}
	
	@Command
	public void importTransfer(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx,
							   @BindingParam(C.ELEM) Component component) {
		showDialog("/admin/transfer/importdialog.zul", new TransferDialogParameter(this));
	}
	
	public void setTransferResult(TransferResult transferResult) {
		showDialog("/admin/transfer/importresult.zul", new TransferDialogParameter(this, transferResult));
	}
	
	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, null, notifyObject);
	}
	
	@Command
	@SmartNotifyChange(C.ELEMENT)
	public void dropToElementList(@BindingParam(C.ITEM) TransferElement item,
								  @BindingParam(C.LIST) int listNum) {
		dropToList(ELEMENTS, listNum, item);
		if (listNum == LIST_SELECTED) {
			element = item;
		}
		else if (item == element) {
			element = null;
		}
		adjustLists(transferElements, getListManagerList(ELEMENTS, LIST_SELECTED));
	}
	
	@Command
	@SmartNotifyChange(C.ELEMENT)
	public void insertToElementList(@BindingParam(C.BASE) TransferElement base,
				  				    @BindingParam(C.ITEM) TransferElement item,
				  				    @BindingParam(C.LIST) int listNum) {
		insertToList(ELEMENTS, listNum, base, item);
		if (listNum == LIST_SELECTED) {
			element = item;
		}
		else if (item == element) {
			element = null;
		}
		adjustLists(transferElements, getListManagerList(ELEMENTS, LIST_SELECTED));
	}
	
	@Command
	@SmartNotifyChange("nestedElement")
	public void dropToNestedElementList(@BindingParam(C.ITEM) TransferElement item,
										@BindingParam(C.LIST) int listNum) {
		dropToList(NESTEDELEMENTS, listNum, item);
		if (listNum == LIST_SELECTED) {
			nestedElement = item;
		}
		else if (item == nestedElement) {
			nestedElement = null;
		}
		adjustLists(nestedTransfer.getElements(), getListManagerList(NESTEDELEMENTS, LIST_SELECTED));
	}
	
	@Command
	@SmartNotifyChange("nestedElement")
	public void insertToNestedElementList(@BindingParam(C.BASE) TransferElement base,
				  						  @BindingParam(C.ITEM) TransferElement item,
				  						  @BindingParam(C.LIST) int listNum) {
		insertToList(NESTEDELEMENTS, listNum, base, item);
		if (listNum == LIST_SELECTED) {
			nestedElement = item;
		}
		else if (item == nestedElement) {
			nestedElement = null;
		}
		adjustLists(nestedTransfer.getElements(), getListManagerList(NESTEDELEMENTS, LIST_SELECTED));
	}
	
	@Command
	@NotifyChange({C.NESTED, NESTEDS})
	public void dropToNestedList(@BindingParam(C.ITEM) NestedTransfer item,
								 @BindingParam(C.LIST) int listNum) {
		dropToList(NESTEDS, listNum, item);
		if (listNum == LIST_AVAILABLE) {
			item.removeElements();
			if (item == nestedTransfer) {
				nestedTransfer = null;
			}
		}
		adjustLists(nestedTransfers, getListManagerList(NESTEDS, LIST_SELECTED));
		removeListManager(NESTEDELEMENTS);
	}
	
	@Command
	@NotifyChange({C.NESTED, NESTEDS})
	public void insertToNestedList(@BindingParam(C.BASE) NestedTransfer base,
				  				   @BindingParam(C.ITEM) NestedTransfer item,
				  				   @BindingParam(C.LIST) int listNum) {
		insertToList(NESTEDS, listNum, base, item);
		if (listNum == LIST_AVAILABLE) {
			item.removeElements();
			if (item == nestedTransfer) {
				nestedTransfer = null;
			}
		}
		adjustLists(nestedTransfers, getListManagerList(NESTEDS, LIST_SELECTED));
		removeListManager(NESTEDELEMENTS);
	}
	
	@Command
	@SmartNotifyChange(C.PERMISSION)
	public void dropToPermissionList(@BindingParam(C.ITEM) TransferPermission item,
									 @BindingParam(C.LIST) int listNum) {
		dropToList(PERMISSIONS, listNum, item);
		if (listNum == LIST_SELECTED) {
			permission = item;
		}
		else if (listNum == LIST_AVAILABLE && item == permission) {
			this.permission = null;
		}
	}
	
	@Command
	@SmartNotifyChange(C.PERMISSION)
	public void insertToPermissionList(@BindingParam(C.BASE) TransferPermission base,
									   @BindingParam(C.ITEM) TransferPermission item,
									   @BindingParam(C.LIST) int listNum) {
		insertToList(PERMISSIONS, listNum, base, item);
		if (listNum == LIST_SELECTED) {
			permission = item;
		}
		else if (listNum == LIST_AVAILABLE && item == permission) {
			permission = null;
		}
	}
	
	@Command
	@SmartNotifyChange(C.NESTED)
	public void selectNestedElements() {
		if (nestedTransfer == null && !ObjectUtils.isEmpty(nestedTransfers)) {
			nestedTransfer = nestedTransfers.get(0);
		}
	}
	
	@Command
	@NotifyChange({LISTMANAGER_LIST, "nestedElement"})
	public void selectNestedTransfer() {
		removeListManager(NESTEDELEMENTS);
		nestedElement = null;
	}
	
	@GlobalCommand
	public void globalRefreshObject(@BindingParam(C.PARAM) Long objectId) {
		refreshObject(objectId);
	}

	@Override
	protected List<SystemObject> getListManagerSource(String key, int listNum) {
		switch (key) {
			case ELEMENTS:
				return MiscUtils.castList(listNum == LIST_AVAILABLE
						? transferService.getAvailableElements(getObject(), transferElements)
						: transferElements);
				
			case NESTEDS:
				return MiscUtils.castList(listNum == LIST_AVAILABLE
						? transferService.getAvailableNesteds(getObject(), nestedTransfers)
						: nestedTransfers);
			
			case NESTEDELEMENTS:
				if (nestedTransfer == null) {
					return Collections.emptyList();
				}
				return MiscUtils.castList(listNum == LIST_AVAILABLE
						? transferService.getAvailableNestedElements(nestedTransfer, nestedTransfer.getElements())
						: nestedTransfer.getElements());
			
			case PERMISSIONS:
				return MiscUtils.castList(listNum == LIST_AVAILABLE 
						? transferService.getAvailablePermissions(getObject(), currentSession()) 
						: getObject().getPermissions());
				
			default:
				throw new IllegalStateException("unknown list manager key: " + key);
		}
	}

	@Override
	protected void resetProperties() {
		element = null;
		transferElements = null;
		nestedTransfer = null;
		nestedTransfers = null;
		nestedElement = null;
		permission = null;
	}
	
}
