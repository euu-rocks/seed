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

import org.seed.core.data.SystemObject;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityService;
import org.seed.core.form.Form;
import org.seed.core.form.FormAction;
import org.seed.core.form.FormField;
import org.seed.core.form.FormMetadata;
import org.seed.core.form.FormOptions;
import org.seed.core.form.FormPrintout;
import org.seed.core.form.FormService;
import org.seed.core.form.FormTransformer;
import org.seed.core.form.layout.LayoutElement;
import org.seed.core.form.layout.LayoutService;
import org.seed.core.form.navigation.Menu;
import org.seed.core.form.navigation.MenuService;
import org.seed.core.user.Authorisation;
import org.seed.ui.ListFilter;

import org.springframework.util.Assert;
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
import org.zkoss.zul.Window;

public class AdminFormViewModel extends AbstractAdminViewModel<Form> {
	
	private static final String FIELDS = "fields";
	private static final String ACTIONS = "actions";
	private static final String TRANSFORMERS = "transformers";
	private static final String PRINTOUTS = "printouts";
	
	@Wire("#newFormWin")
	private Window window;
	
	@WireVariable(value="formServiceImpl")
	private FormService formService;
	
	@WireVariable(value="entityServiceImpl")
	private EntityService entityService;
	
	@WireVariable(value="layoutServiceImpl")
	private LayoutService layoutService;
	
	@WireVariable(value="menuServiceImpl")
	private MenuService menuService;
	
	private FormField field;
	
	private FormAction action;
	
	private FormTransformer transformer;
	
	private FormPrintout printout;
	
	private LayoutElement layoutRoot;
	
	public AdminFormViewModel() {
		super(Authorisation.ADMIN_FORM, "form",
			  "/admin/form/formlist.zul", 
			  "/admin/form/form.zul",
			  "/admin/form/newform.zul");
	}
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam("param") Object object) {
		super.init(object, view);
	}
	
	@Override
	protected void initObject(Form form) {
		if (form.getLayout() != null) {
			initLayout(layoutService.parseLayout(form.getLayout()));
		}
		else if (layoutService.getEditLayout(getUserName()) != null) {
			layoutService.resetEditLayout(getUserName());
			notifyChange("layoutInclude");
		}
	}
	
	@Override
	protected void initFilters() {
		final ListFilter filterEntity = getFilter(FILTERGROUP_LIST, "entity");
		filterEntity.setValueFunction(o -> ((Form) o).getEntity().getName());
		for (Form form : getObjectList()) {
			filterEntity.addValue(form.getEntity().getName());
		}
	}
	
	public FormField getField() {
		return field;
	}

	public void setField(FormField field) {
		this.field = field;
	}

	public FormAction getAction() {
		return action;
	}

	public void setAction(FormAction action) {
		this.action = action;
	}
	
	public FormTransformer getTransformer() {
		return transformer;
	}

	public void setTransformer(FormTransformer transformer) {
		this.transformer = transformer;
	}

	public FormPrintout getPrintout() {
		return printout;
	}

	public void setPrintout(FormPrintout printout) {
		this.printout = printout;
	}

	public String getActionLabel(FormAction action) {
		String label = null;
		if (action != null) {
			if (action.isCustom()) {
				if (action.getEntityFunction() != null) {
					label = action.getEntityFunction().getName();
				}
			}
			else {
				label = getLabel("button." + action.getType().name().toLowerCase());
			}
		}
		return label;
	}
	
	public String getLayoutInclude() {
		return "/generated/edit/" + getUserName() + "/" + System.currentTimeMillis();
	}
	
	public boolean existTransformers() {
		return getObject() != null && 
				(getObject().hasTransformers() || 
				 !formService.getAvailableTransformers(getObject()).isEmpty());
	}
	
	public List<Form> getTargetForms(FormTransformer transformer) {
		return transformer != null 
				? formService.findForms(transformer.getTransformer().getTargetEntity())
				: null;
	}
	
	public List<Entity> getEntities() {
		return entityService.findNonGenericEntities();
	}
	
	public List<Menu> getMenus() {
		return menuService.getTopLevelMenus();
	}
	
	@Override
	protected FormService getObjectService() {
		return formService;
	}

	@Override
	protected List<? extends SystemObject> getListSorterSource(String key) {
		switch (key) {
			case PRINTOUTS:
				return getObject().getPrintouts();
			default:
				throw new IllegalStateException("unknown list sorter key: " + key);	
		}
	}

	@Override
	protected List<? extends SystemObject> getListManagerSource(String key, int listNum) {
		switch (key) {
			case FIELDS:
				return listNum == LIST_AVAILABLE
						? formService.getAvailableFields(getObject())
						: getObject().getFields();
			
			case ACTIONS:
				return listNum == LIST_AVAILABLE
						? formService.getAvailableActions(getObject())
						: getObject().getActions();
						
			case TRANSFORMERS:
				return listNum == LIST_AVAILABLE
						? formService.getAvailableTransformers(getObject())
						: getObject().getTransformers();
			
			default:
				throw new IllegalStateException("unknown list manager key: " + key);
		}
	}

	@Override
	protected void resetProperties() {
		field = null;
		action = null;
		transformer = null;
		printout = null;
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void refreshForm(@BindingParam("elem") Component component) {
		cmdRefresh();
	}
	
	@Command
	public void newForm() {
		cmdNewObjectDialog();
	}
	
	@Command
	public void editForm() {
		cmdEditObject();
	}
	
	@Command
	public void flagDirty() {
		super.flagDirty();
	}
	
	@Command
	public void saveForm(@BindingParam("elem") Component component) {
		final boolean isCreate = getObject().isNew(); 
		adjustLists(getObject().getFields(), getListManagerList(FIELDS, LIST_SELECTED));
		adjustLists(getObject().getActions(), getListManagerList(ACTIONS, LIST_SELECTED));
		adjustLists(getObject().getTransformers(), getListManagerList(TRANSFORMERS, LIST_SELECTED));
		
		if (layoutRoot != null) {
			layoutService.undecorateLayout(getObject(), layoutRoot);
			final String layoutContent = layoutService.buildLayout(layoutRoot);
			layoutService.decorateLayout(getObject(), layoutRoot);
			((FormMetadata) getObject()).setLayoutContent(layoutContent);
			notifyChange("layoutInclude");
		}
		if (cmdSaveObject(component)) {
			if (isCreate) {
				refreshMenu();
			}
		}
	}
	
	@Command
	public void deleteForm(@BindingParam("elem") Component component) {
		cmdDeleteObject(component);
	}
	
	@Override
	public Form createObject() {
		return formService.createInstance(new FormOptions());
	}
	
	// create dialog ---------------------------------------------------------
	
	@Command
	public void createForm(@BindingParam("elem") Component elem) {
		// set module in options
		final FormOptions formOptions = getObject().getOptions();
		if (formOptions != null) {
			formOptions.setModule(getObject().getModule());
		}
		cmdInitObject(elem, window);
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
	// common --------------------------------------------------------------
	
	@Command
	@SmartNotifyChange("transformer")
	public void dropToTransformerList(@BindingParam("item") FormTransformer item,
									  @BindingParam("list") int listNum) {
		super.dropToList(TRANSFORMERS, listNum, item);
		if (listNum == LIST_AVAILABLE && item == transformer) {
			transformer = null;
		}
	}
	
	@Command
	@SmartNotifyChange("transformer")
	public void insertToTransformerList(@BindingParam("base") FormTransformer base,
			   					  		@BindingParam("item") FormTransformer item,
			   					  		@BindingParam("list") int listNum) {
		super.insertToList(TRANSFORMERS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == transformer) {
			transformer = null;
		}
	}
	
	// list form -----------------------------------------------------------
	
	@Command
	@SmartNotifyChange("field")
	public void dropToFieldList(@BindingParam("item") FormField item,
								@BindingParam("list") int listNum) {
		super.dropToList(FIELDS, listNum, item);
		if (listNum == LIST_AVAILABLE && item == field) {
			field = null;
		}
	}
	
	@Command
	@SmartNotifyChange("field")
	public void insertToFieldList(@BindingParam("base") FormField base,
			   					  @BindingParam("item") FormField item,
			   					  @BindingParam("list") int listNum) {
		super.insertToList(FIELDS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == field) {
			field = null;
		}
	}
	
	@Command
	@SmartNotifyChange("action")
	public void dropToActionList(@BindingParam("item") FormAction item,
								 @BindingParam("list") int listNum) {
		super.dropToList(ACTIONS, listNum, item);
		if (listNum == LIST_AVAILABLE && item == action) {
			action = null;
		}
	}
	
	@Command
	@SmartNotifyChange("action")
	public void insertToActionList(@BindingParam("base") FormAction base,
			   					   @BindingParam("item") FormAction item,
			   					   @BindingParam("list") int listNum) {
		super.insertToList(ACTIONS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == action) {
			action = null;
		}
	}
	
	// detail form ---------------------------------------------------------
	
	@Command
	public void addField(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/addfield.zul", newDialogParameter("addfield", contextId));
	}
	
	@Command
	public void editField(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/field_properties.zul", newDialogParameter("editfield", contextId));
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeField(@BindingParam("contextid") String contextId) {
		layoutService.removeField(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	public void editCell(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/cell_properties.zul", newDialogParameter("editcell", contextId));
	}
	
	@Command
	public void addText(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/addtext.zul", newDialogParameter("addtext", contextId));
	}
	
	@Command
	public void editText(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/text_properties.zul", newDialogParameter("edittext", contextId));
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeText(@BindingParam("contextid") String contextId) {
		layoutService.removeText(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	public void addGrid(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/addgrid.zul", newDialogParameter("addgrid", contextId));
	}
	
	@Command
	public void editGrid(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/grid_properties.zul", newDialogParameter("editgrid", contextId));
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeGrid(@BindingParam("contextid") String contextId) {
		layoutService.removeGrid(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	public void addTabbox(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/addtabbox.zul", newDialogParameter("addtabbox", contextId));
	}
	
	@Command
	public void addTab(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/addtab.zul", newDialogParameter("addtab", contextId));
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeTab(@BindingParam("contextid") String contextId) {
		layoutService.removeTab(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	public void editTab(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/tab_properties.zul", newDialogParameter("edittab", contextId));
	}
	
	@Command
	public void addSubForm(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/addsubform.zul", newDialogParameter("addsubform", contextId));
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeSubForm(@BindingParam("contextid") String contextId) {
		layoutService.removeSubForm(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	public void editSubForm(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/subform_properties.zul", newDialogParameter("editsubform", contextId));
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void newAutoLayout() {
		initLayout(layoutService.createAutoLayout(getObject()));
		flagDirty();
	}
	
	@Command
	public void newLayout() {
		showDialog("/admin/form/newlayout.zul", newDialogParameter("newlayout", null));
	}
	
	@Command
	public void addLayout(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/addlayout.zul", newDialogParameter("addlayout", contextId));
	}
	
	@Command
	public void editBorderLayout(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/borderlayout_properties.zul", newDialogParameter("editborderlayout", contextId));
	}
	
	@Command
	public void editBorderLayoutArea(@BindingParam("contextid") String contextId) {
		showDialog("/admin/form/borderlayoutarea_properties.zul", newDialogParameter("editborderlayoutarea", contextId));
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeBorderLayoutArea(@BindingParam("contextid") String contextId) {
		layoutService.removeBorderLayoutArea(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeBorderLayout(@BindingParam("contextid") String contextId) {
		layoutService.removeBorderLayout(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void newColumnLeft(@BindingParam("contextid") String contextId) {
		layoutService.newColumnLeft(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void newColumnRight(@BindingParam("contextid") String contextId) {
		layoutService.newColumnRight(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeColumn(@BindingParam("contextid") String contextId) {
		layoutService.removeColumn(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void newRowAbove(@BindingParam("contextid") String contextId) {
		layoutService.newRowAbove(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void newRowBelow(@BindingParam("contextid") String contextId) {
		layoutService.newRowBelow(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeRow(@BindingParam("contextid") String contextId) {
		layoutService.removeRow(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("printout")
	public void newPrintout() {
		printout = formService.createPrintout(getObject());
		notifyObjectChange(PRINTOUTS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("printout")
	public void removePrintout() {
		getObject().removePrintout(printout);
		notifyObjectChange(PRINTOUTS);
		printout = null;
		flagDirty();
	}
	
	@Command
	@NotifyChange({"action", "getListManagerList"})
	public void newCustomAction() {
		action = formService.createCustomAction(getObject());
		removeListManager(ACTIONS);
		flagDirty();
	}
	
	@Command
	@NotifyChange("getActionLabel")
	public void selectEntityFunction() {
		flagDirty();
	}
	
	@Command
	public void swapPrintouts(@BindingParam("base") FormPrintout base, 
						      @BindingParam("item") FormPrintout item) {
		swapItems(PRINTOUTS, base, item);
	}
	
	@GlobalCommand
	public void _refreshObject(@BindingParam("param") Long objectId) {
		refreshObject(objectId);
	}
	
	void setLayout(LayoutElement layoutRoot) {
		Assert.notNull(layoutRoot, "layoutRoot");
		
		initLayout(layoutRoot);
		refreshLayout();
		flagDirty();
	}
	
	void refreshLayout() {
		notifyChange("layoutInclude");
	}
	
	private void initLayout(LayoutElement layoutRoot) {
		Assert.notNull(layoutRoot, "layoutRoot");
		
		this.layoutRoot = layoutRoot;
		layoutService.registerEditLayout(getObject(), getUserName(), layoutRoot);
	}
	
	private LayoutDialogParameter newDialogParameter(String command, String contextId) {
		return new LayoutDialogParameter(this, getObject(), layoutRoot, command, contextId); 
	}
 	
}
