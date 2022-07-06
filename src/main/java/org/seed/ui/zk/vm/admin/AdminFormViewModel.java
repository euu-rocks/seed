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

import org.apache.commons.collections4.ListUtils;

import org.seed.C;
import org.seed.core.data.SystemObject;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.filter.Filter;
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
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.ui.ListFilter;

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
	
	private static final String FIELDS         = "fields";
	private static final String ACTIONS        = "actions";
	private static final String TRANSFORMERS   = "transformers";
	private static final String PRINTOUTS      = "printouts";
	private static final String LAYOUT_INCLUDE = "layoutInclude";
	private static final String CONTEXT_ID     = "contextid"; 
	
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
	
	private boolean existFilters;
	
	public AdminFormViewModel() {
		super(Authorisation.ADMIN_FORM, C.FORM,
			  "/admin/form/formlist.zul", 
			  "/admin/form/form.zul",
			  "/admin/form/newform.zul");
	}
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam(C.PARAM) Object object) {
		super.init(object, view);
	}
	
	@Override
	protected void initObject(Form form) {
		super.initObject(form);
		if (form.getLayout() != null) {
			initLayout(layoutService.parseLayout(form.getLayout()));
		}
		else if (layoutService.getEditLayout(getUserName()) != null) {
			layoutService.resetEditLayout(getUserName());
			notifyChange(LAYOUT_INCLUDE);
		}
	}
	
	@Override
	protected void initFilters() {
		final ListFilter<Form> filterEntity = getFilter(FILTERGROUP_LIST, C.ENTITY);
		filterEntity.setValueFunction(o -> o.getEntity().getName());
		final ListFilter<Form> filterFilter = getFilter(FILTERGROUP_LIST, C.FILTER);
		filterFilter.setValueFunction(o -> o.getFilter() != null ? o.getFilter().getName() : null);
		
		for (Form form : getObjectList()) {
			filterEntity.addValue(form.getEntity().getName());
			if (form.getFilter() != null) {
				filterFilter.addValue(form.getFilter().getName());
				existFilters = true;
			}
		}
		getFilter(FILTERGROUP_LIST, "autoLayout").setBooleanFilter(true);
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
	
	public List<Filter> getFilters() {
		return formService.getFilters(getObject());
	}

	public boolean existFilters() {
		return existFilters;
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
		return "/generated/edit/" + getUserName() + '/' + System.currentTimeMillis();
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
	protected List<SystemObject> getListSorterSource(String key) {
		if (PRINTOUTS.equals(key)) {
			return MiscUtils.castList(getObject().getPrintouts());
		}
		else {
			throw new IllegalStateException("unknown list sorter key: " + key);	
		}
	}

	@Override
	protected List<SystemObject> getListManagerSource(String key, int listNum) {
		switch (key) {
			case FIELDS:
				return MiscUtils.castList(getObject().getSelectedFields(listNum == LIST_SELECTED));
			
			case ACTIONS:
				return MiscUtils.castList(listNum == LIST_AVAILABLE
							? formService.getAvailableActions(getObject())
							: getObject().getActions());
						
			case TRANSFORMERS:
				return MiscUtils.castList(listNum == LIST_AVAILABLE
							? formService.getAvailableTransformers(getObject())
							: getObject().getTransformers());
			
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
	public void refreshForm(@BindingParam(C.ELEM) Component component) {
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
	@Override
	public void flagDirty() {
		super.flagDirty();
	}
	
	String getLayoutContent() {
		if (layoutRoot != null) {
			layoutService.undecorateLayout(getObject(), layoutRoot);
			final String layoutContent = layoutService.buildLayout(layoutRoot);
			layoutService.decorateLayout(getObject(), layoutRoot);
			return layoutContent;
		}
		return null;
	}
	
	@Command
	public void saveForm(@BindingParam(C.ELEM) Component component) {
		final boolean isCreate = getObject().isNew(); 
		adjustLists(getObject().getFields(), getAllAndMarkSelectedFields());
		adjustLists(getObject().getActions(), getListManagerList(ACTIONS, LIST_SELECTED));
		adjustLists(getObject().getTransformers(), getListManagerList(TRANSFORMERS, LIST_SELECTED));
		
		if (layoutRoot != null) {
			((FormMetadata) getObject()).setLayoutContent(getLayoutContent());
			notifyChange(LAYOUT_INCLUDE);
		}
		if (cmdSaveObject(component) && isCreate) {
			refreshMenu();
		}
	}
	
	@Command
	public void deleteForm(@BindingParam(C.ELEM) Component component) {
		cmdDeleteObject(component);
	}
	
	@Override
	public Form createObject() {
		return formService.createInstance(new FormOptions());
	}
	
	// create dialog ---------------------------------------------------------
	
	@Command
	public void createForm(@BindingParam(C.ELEM) Component elem) {
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
	@SmartNotifyChange(C.TRANSFORMER)
	public void dropToTransformerList(@BindingParam(C.ITEM) FormTransformer item,
									  @BindingParam(C.LIST) int listNum) {
		super.dropToList(TRANSFORMERS, listNum, item);
		if (listNum == LIST_AVAILABLE && item == transformer) {
			transformer = null;
		}
	}
	
	@Command
	@SmartNotifyChange(C.TRANSFORMER)
	public void insertToTransformerList(@BindingParam(C.BASE) FormTransformer base,
			   					  		@BindingParam(C.ITEM) FormTransformer item,
			   					  		@BindingParam(C.LIST) int listNum) {
		super.insertToList(TRANSFORMERS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == transformer) {
			transformer = null;
		}
	}
	
	// list form -----------------------------------------------------------
	
	@Command
	@SmartNotifyChange(C.FIELD)
	public void dropToFieldList(@BindingParam(C.ITEM) FormField item,
								@BindingParam(C.LIST) int listNum) {
		super.dropToList(FIELDS, listNum, item);
		if (listNum == LIST_AVAILABLE && item == field) {
			field = null;
		}
	}
	
	@Command
	@SmartNotifyChange(C.FIELD)
	public void insertToFieldList(@BindingParam(C.BASE) FormField base,
			   					  @BindingParam(C.ITEM) FormField item,
			   					  @BindingParam(C.LIST) int listNum) {
		super.insertToList(FIELDS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == field) {
			field = null;
		}
	}
	
	@Command
	@SmartNotifyChange(C.ACTION)
	public void dropToActionList(@BindingParam(C.ITEM) FormAction item,
								 @BindingParam(C.LIST) int listNum) {
		super.dropToList(ACTIONS, listNum, item);
		if (listNum == LIST_AVAILABLE && item == action) {
			action = null;
		}
	}
	
	@Command
	@SmartNotifyChange(C.ACTION)
	public void insertToActionList(@BindingParam(C.BASE) FormAction base,
			   					   @BindingParam(C.ITEM) FormAction item,
			   					   @BindingParam(C.LIST) int listNum) {
		super.insertToList(ACTIONS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == action) {
			action = null;
		}
	}
	
	// detail form ---------------------------------------------------------
	
	@Command
	public void addField(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/addfield.zul", newDialogParameter("addfield", contextId));
	}
	
	@Command
	public void addRichTextField(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/addrichtextfield.zul", newDialogParameter("addrichtextfield", contextId));
	}
	
	@Command
	public void editField(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/field_properties.zul", newDialogParameter("editfield", contextId));
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeField(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.removeField(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	public void editCell(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/cell_properties.zul", newDialogParameter("editcell", contextId));
	}
	
	@Command
	public void addText(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/addtext.zul", newDialogParameter("addtext", contextId));
	}
	
	@Command
	public void editText(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/text_properties.zul", newDialogParameter("edittext", contextId));
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeText(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.removeText(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	public void addGrid(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/addgrid.zul", newDialogParameter("addgrid", contextId));
	}
	
	@Command
	public void editGrid(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/grid_properties.zul", newDialogParameter("editgrid", contextId));
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeGrid(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.removeGrid(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	public void addTabbox(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/addtabbox.zul", newDialogParameter("addtabbox", contextId));
	}
	
	@Command
	public void addTab(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/addtab.zul", newDialogParameter("addtab", contextId));
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeTab(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.removeTab(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	public void editTab(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/tab_properties.zul", newDialogParameter("edittab", contextId));
	}
	
	@Command
	public void addSubForm(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/addsubform.zul", newDialogParameter("addsubform", contextId));
	}
	
	@Command
	public void addRelationForm(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/addrelationform.zul", newDialogParameter("addrelationform", contextId));
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeSubForm(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.removeSubForm(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeRelationForm(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.removeRelationForm(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	public void editSubForm(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/subform_properties.zul", newDialogParameter("editsubform", contextId));
	}
	
	@Command
	public void editLayoutSource() {
		showDialog("/admin/form/layout_source.zul", this);
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void newAutoLayout() {
		initLayout(layoutService.createAutoLayout(getObject().getEntity(), getObject()));
		flagDirty();
	}
	
	@Command
	public void newLayout() {
		showDialog("/admin/form/newlayout.zul", newDialogParameter("newlayout", null));
	}
	
	@Command
	public void addLayout(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/addlayout.zul", newDialogParameter("addlayout", contextId));
	}
	
	@Command
	public void editBorderLayout(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/borderlayout_properties.zul", newDialogParameter("editborderlayout", contextId));
	}
	
	@Command
	public void editBorderLayoutArea(@BindingParam(CONTEXT_ID) String contextId) {
		showDialog("/admin/form/borderlayoutarea_properties.zul", newDialogParameter("editborderlayoutarea", contextId));
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeBorderLayoutArea(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.removeBorderLayoutArea(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeBorderLayout(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.removeBorderLayout(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void newColumnLeft(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.newColumnLeft(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void newColumnRight(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.newColumnRight(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeColumn(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.removeColumn(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void newRowAbove(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.newRowAbove(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void newRowBelow(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.newRowBelow(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange("layoutInclude")
	public void removeRow(@BindingParam(CONTEXT_ID) String contextId) {
		layoutService.removeRow(getObject(), layoutRoot, contextId);
		flagDirty();
	}
	
	@Command
	@NotifyChange(C.PRINTOUT)
	public void newPrintout() {
		printout = formService.createPrintout(getObject());
		notifyObjectChange(PRINTOUTS);
		flagDirty();
	}
	
	@Command
	@NotifyChange(C.PRINTOUT)
	public void removePrintout() {
		getObject().removePrintout(printout);
		notifyObjectChange(PRINTOUTS);
		printout = null;
		flagDirty();
	}
	
	@Command
	@NotifyChange({C.ACTION, LISTMANAGER_LIST})
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
	public void swapPrintouts(@BindingParam(C.BASE) FormPrintout base, 
						      @BindingParam(C.ITEM) FormPrintout item) {
		swapItems(PRINTOUTS, base, item);
	}
	
	@GlobalCommand
	public void globalRefreshObject(@BindingParam(C.PARAM) Long objectId) {
		refreshObject(objectId);
	}
	
	void setLayout(LayoutElement layoutRoot) {
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		
		initLayout(layoutRoot);
		refreshLayout();
		flagDirty();
	}
	
	void refreshLayout() {
		notifyChange(LAYOUT_INCLUDE);
	}
	
	private List<FormField> getAllAndMarkSelectedFields() {
		final List<FormField> availabaleFields = MiscUtils.castList(getListManagerList(FIELDS, LIST_AVAILABLE));
		availabaleFields.forEach(f -> f.setSelected(false));
		final List<FormField> selectedFields = MiscUtils.castList(getListManagerList(FIELDS, LIST_SELECTED));
		selectedFields.forEach(f -> f.setSelected(true));
		return ListUtils.union(availabaleFields, selectedFields);
	}
	
	private void initLayout(LayoutElement layoutRoot) {
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		
		this.layoutRoot = layoutRoot;
		layoutService.registerEditLayout(getObject(), getUserName(), layoutRoot);
	}
	
	private LayoutDialogParameter newDialogParameter(String command, String contextId) {
		return new LayoutDialogParameter(this, getObject(), layoutRoot, command, contextId); 
	}
 	
}
