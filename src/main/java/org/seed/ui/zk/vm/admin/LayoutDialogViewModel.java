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
import org.seed.core.data.FieldType;
import org.seed.core.data.SystemObject;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterService;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerService;
import org.seed.core.form.Form;
import org.seed.core.form.FormFieldExtra;
import org.seed.core.form.FormService;
import org.seed.core.form.SubForm;
import org.seed.core.form.SubFormAction;
import org.seed.core.form.layout.Alignment;
import org.seed.core.form.layout.BorderLayoutProperties;
import org.seed.core.form.layout.LayoutElement;
import org.seed.core.form.layout.LayoutElementAttributes;
import org.seed.core.form.layout.LayoutService;
import org.seed.core.form.layout.LayoutType;
import org.seed.core.form.layout.Orientation;
import org.seed.core.form.layout.SubFormProperties;
import org.seed.core.form.layout.TextfieldType;
import org.seed.core.form.layout.BorderLayoutProperties.LayoutAreaProperties;
import org.seed.core.form.layout.LabelProperties;
import org.seed.core.form.layout.SubFormProperties.SubFormColumn;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

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

public class LayoutDialogViewModel extends AbstractAdminViewModel<Form> {
	
	private static final String FIELDS = "fields";
	private static final String ACTIONS = "actions";
	private static final String ERROR_APPLY_PROPERTIES = "admin.layout.applypropertiesfail";
	private static final String ERROR_ADD_TAB = "admin.layout.addtabfail";
	
	@Wire("#layoutDialogWin")
	private Window window;
	
	@WireVariable(value="formServiceImpl")
	private FormService formService;
	
	@WireVariable(value="filterServiceImpl")
	private FilterService filterService;
	
	@WireVariable(value="transformerServiceImpl")
	private TransformerService transformerService;
	
	@WireVariable(value="layoutServiceImpl")
	private LayoutService layoutService;
	
	private LayoutDialogParameter parameter;
	
	private LayoutElement element;
	
	private LayoutElementAttributes properties;
	
	private LayoutElementAttributes tabboxProperties;
	
	private BorderLayoutProperties borderLayoutProperties;
	
	private LayoutAreaProperties layoutAreaProperties;
	
	private List<LayoutElementAttributes> columnProperties;
	
	private LayoutElementAttributes selectedColumn;
	
	private LayoutType layoutType;
	
	private NestedEntity nestedEntity;
	
	private EntityField entityField;
	
	private Filter filter;
	
	private Transformer transformer;
	
	private SubFormProperties subFormProperties;
	
	private SubFormColumn subFormColumn;
	
	private SubFormAction subFormAction;
	
	private Form detailForm;
	
	private Orientation orient;
	
	private Alignment align;
	
	private Alignment valign;
	
	private Integer gridColumns;
	
	private Integer gridRows;
	
	private String width;
	
	private String height;
	
	private String text;
	
	private boolean readonly;
	
	private boolean bandbox;
	
	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam(C.PARAM) LayoutDialogParameter param) {
		Assert.notNull(param, C.PARAM);
		parameter = param;
		wireComponents(view);
		
		switch (parameter.command) {
			case "addlayout":
				element = getContextElement();
				/* falls through */
			case "newlayout":
				borderLayoutProperties = new BorderLayoutProperties();
				break;
				
			case "addfield":
				final LayoutDialogPreferences prefs = getPreferences();
				orient = prefs.getLabelOrientation();
				align = prefs.getLabelAlign();
				valign = prefs.getLabelValign();
				break;
				
			case "addsubform":
				element = getContextElement();
				break;
				
			case "edittab":
				element = getContextElement();
				tabboxProperties = new LayoutElementAttributes(element.getParent().getParent());
				break;
				
			case "editcell":
				element = getContextElement();
				break;
			
			case "editgrid":
				element = getContextElement().getGrid();
				
				if (element.parentIs(LayoutElement.GROUPBOX)) {
					text = element.getParent().getChild(LayoutElement.CAPTION).getAttribute(C.LABEL);
				}
				
				columnProperties = new ArrayList<>();
				for (LayoutElement elemColumn : element.getChild(LayoutElement.COLUMNS).getChildren()) {
					columnProperties.add(new LayoutElementAttributes(elemColumn));
				}
				selectedColumn = columnProperties.get(0);
				break;
			
			case "editborderlayout":
				element = getContextElement().getParent();
				Assert.state(element.is(LayoutElement.BORDERLAYOUT), "element is no borderlayout");
				
				borderLayoutProperties = new BorderLayoutProperties(element);
				layoutAreaProperties = borderLayoutProperties.getNorth();
				break;
				
			case "editborderlayoutarea":
				element = getContextElement();
				layoutAreaProperties = BorderLayoutProperties.createAreaProperties(element);
				break;
				
			case "edittext":
				element = getContextElement();
				Assert.state(element.is(LayoutElement.LABEL), "element is no label");
				
				if (element.hasChildren()) {
					text = element.getChildAt(0).getText();
				}
				else {
					text = element.getAttribute(C.VALUE);
				}
				break;
				
			case "editfield":
				element = getContextElement();
				final String fieldId = element.getId();
				Assert.stateAvailable(fieldId, "field id");
				
				entityField = parameter.form.getEntity().getFieldByUid(fieldId);
				final FormFieldExtra fieldExtra = parameter.form.getFieldExtra(entityField);
				if (fieldExtra != null) {
					readonly = fieldExtra.isReadonly();
					filter = fieldExtra.getFilter();
					transformer = fieldExtra.getTransformer();
					detailForm = fieldExtra.getDetailForm();
				}
				bandbox = element.is(LayoutElement.BANDBOX);
				break;
				
			case "editsubform":
				element = getContextElement();
				final String nestedId = element.getParent().getParent().getId();
				Assert.stateAvailable(nestedId, "nested id");
				final SubForm subForm = parameter.form.getSubFormByNestedEntityUid(nestedId);
				Assert.stateAvailable(subForm, "sub form for nested " + nestedId);
				
				nestedEntity = subForm.getNestedEntity();
				subFormProperties = new SubFormProperties(subForm);
				break;
				
			default:
				throw new UnsupportedOperationException(parameter.command);
				
		}
		if (element != null) {
			properties = new LayoutElementAttributes(element);
		}
	}
	
	public LayoutElementAttributes getProperties() {
		return properties;
	}

	public LayoutElementAttributes getTabboxProperties() {
		return tabboxProperties;
	}

	public LayoutAreaProperties getLayoutAreaProperties() {
		return layoutAreaProperties;
	}

	public void setLayoutAreaProperties(LayoutAreaProperties layoutAreaProperties) {
		this.layoutAreaProperties = layoutAreaProperties;
	}

	public BorderLayoutProperties getBorderLayoutProperties() {
		return borderLayoutProperties;
	}

	public List<LayoutElementAttributes> getColumnProperties() {
		return columnProperties;
	}

	public LayoutElementAttributes getSelectedColumn() {
		return selectedColumn;
	}

	public void setSelectedColumn(LayoutElementAttributes selectedColumn) {
		this.selectedColumn = selectedColumn;
	}

	public LayoutType getLayoutType() {
		return layoutType;
	}

	public void setLayoutType(LayoutType layoutType) {
		this.layoutType = layoutType;
	}

	public NestedEntity getNestedEntity() {
		return nestedEntity;
	}

	public void setNestedEntity(NestedEntity nestedEntity) {
		this.nestedEntity = nestedEntity;
	}

	public EntityField getEntityField() {
		return entityField;
	}

	public void setEntityField(EntityField entityField) {
		this.entityField = entityField;
	}

	public SubFormColumn getSubFormColumn() {
		return subFormColumn;
	}

	public void setSubFormColumn(SubFormColumn subFormColumn) {
		this.subFormColumn = subFormColumn;
	}

	public SubFormAction getSubFormAction() {
		return subFormAction;
	}

	public void setSubFormAction(SubFormAction subFormAction) {
		this.subFormAction = subFormAction;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public Transformer getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	public Form getDetailForm() {
		return detailForm;
	}

	public void setDetailForm(Form detailForm) {
		this.detailForm = detailForm;
	}

	public Orientation getOrient() {
		return orient;
	}

	public void setOrient(Orientation orient) {
		this.orient = orient;
	}

	public Alignment getAlign() {
		return align;
	}

	public void setAlign(Alignment align) {
		this.align = align;
	}

	public Alignment getValign() {
		return valign;
	}

	public void setValign(Alignment valign) {
		this.valign = valign;
	}

	public Integer getGridColumns() {
		return gridColumns;
	}

	public void setGridColumns(Integer gridColumns) {
		this.gridColumns = gridColumns;
	}

	public Integer getGridRows() {
		return gridRows;
	}

	public void setGridRows(Integer gridRows) {
		this.gridRows = gridRows;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public boolean isBandbox() {
		return bandbox;
	}

	public void setBandbox(boolean bandbox) {
		this.bandbox = bandbox;
	}
	
	@Override
	public String getTitle() {
		return getLabel("admin.layout." + parameter.command);
	}
	
	public LayoutElement getElement() {
		return element;
	}

	public Alignment[] getAligns() {
		return Alignment.getAligns();
	}
	
	public Alignment[] getValigns() {
		return Alignment.getValigns();
	}
	
	public TextfieldType[] getTextfieldTypes() {
		return TextfieldType.values();
	}
	
	public Orientation[] getOrientations() {
		return Orientation.values();
	}
	
	public LayoutType[] getLayoutTypes() {
		return LayoutType.values();
	}
	
	public List<EntityField> getAvailableEntityFields() {
		return layoutService.getAvailableEntityFields(parameter.form, 
													  parameter.layoutRoot);
	}
	
	public List<NestedEntity> getAvailableNesteds() {
		return layoutService.getAvailableNesteds(parameter.form, 
												 parameter.layoutRoot);
	}
	
	public List<Filter> getAvailableFilters(SubFormColumn subFormColumn) {
		return subFormColumn != null
				? getAvailableFilters(subFormColumn.subFormField.getEntityField())
				: Collections.emptyList();
	}
	
	public List<Filter> getAvailableFilters(EntityField entityField) {
		return entityField != null && entityField.getType() == FieldType.REFERENCE 
				? filterService.findFilters(entityField.getReferenceEntity())
				: Collections.emptyList();
	}
	
	public List<Transformer> getAvailableTransformers(EntityField entityField) {
		return entityField != null && entityField.getType() == FieldType.REFERENCE 
				? transformerService.findTransformers(entityField.getReferenceEntity(),
						   							  entityField.getEntity())
				: Collections.emptyList();
	}
	
	public List<Transformer> getAvailableTransformers(SubFormColumn subFormColumn) {
		return subFormColumn != null 
				? getAvailableTransformers(subFormColumn.subFormField.getEntityField())
				: Collections.emptyList();
	}
	
	public List<Form> getDetailForms(Entity entity) {
		return entity != null 
				? formService.findForms(entity) 
				: Collections.emptyList();
	}
	
	public String getNestedEntityName() {
		return subFormProperties.getNestedEntityName();
	}
	
	public String getActionLabel(SubFormAction action) {
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
	
	@Command
	@NotifyChange({"subFormAction", LISTMANAGER_LIST})
	public void newCustomAction() {
		subFormAction = subFormProperties.createCustomAction();
		removeListManager(ACTIONS);
	}
	
	@Command
	@NotifyChange("getActionLabel")
	public void selectEntityFunction() {
		// do nothing, just notify
	}
	
	@Command
	@SmartNotifyChange("subFormColumn")
	public void dropToFieldList(@BindingParam(C.ITEM) SubFormColumn item,
								@BindingParam(C.LIST) int listNum) {
		super.dropToList(FIELDS, listNum, item);
		if (listNum == LIST_AVAILABLE && item == subFormColumn) {
			subFormColumn = null;
		}
	}
	
	@Command
	@SmartNotifyChange("subFormColumn")
	public void insertToFieldList(@BindingParam(C.BASE) SubFormColumn base,
			   					  @BindingParam(C.ITEM) SubFormColumn item,
			   					  @BindingParam(C.LIST) int listNum) {
		super.insertToList(FIELDS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == subFormColumn) {
			subFormColumn = null;
		}
	}
	
	@Command
	@SmartNotifyChange("subFormAction")
	public void dropToActionList(@BindingParam(C.ITEM) SubFormAction item,
								 @BindingParam(C.LIST) int listNum) {
		super.dropToList(ACTIONS, listNum, item);
		if (listNum == LIST_AVAILABLE && item == subFormAction) {
			subFormAction = null;
		}
	}
	
	@Command
	@SmartNotifyChange("subFormAction")
	public void insertToActionList(@BindingParam(C.BASE) SubFormAction base,
								   @BindingParam(C.ITEM) SubFormAction item,
								   @BindingParam(C.LIST) int listNum) {
		super.insertToList(ACTIONS, listNum, base, item);
		if (listNum == LIST_AVAILABLE && item == subFormAction) {
			subFormAction = null;
		}
	}
	
	@Command
	public void addField(@BindingParam(C.ELEM) Component elem) {
		try {
			layoutService.addField(parameter.form, entityField, new LabelProperties(orient, align, valign),
					   			   width, height, parameter.layoutRoot, parameter.contextId);
			
			final LayoutDialogPreferences prefs = getPreferences();
			prefs.setLabelOrientation(orient);
			prefs.setLabelAlign(align);
			prefs.setLabelValign(valign);
			refreshAndClose();
		}
		catch (ValidationException vex) {
			showValidationErrors(elem, "admin.layout.addfieldfail", vex.getErrors());
		}
	}
	
	@Command
	public void applyProperties(@BindingParam(C.ELEM) Component elem) {
		try {
			if (element.is(LayoutElement.LABEL)) {
				layoutService.setLabelText(element, text);
			}
			else if (element.is(LayoutElement.TAB)) {
				tabboxProperties.applyTo(element.getParent().getParent());
			}
			else if (entityField != null) {
				applyFieldProperties();
			}
			layoutService.applyProperties(element, properties);
			refreshAndClose();
		}
		catch (ValidationException vex) {
			showValidationErrors(elem, ERROR_APPLY_PROPERTIES, vex.getErrors());
		}
	}
	
	private void applyFieldProperties() {
		if ((element.is(LayoutElement.COMBOBOX) && bandbox) ||
			(element.is(LayoutElement.BANDBOX) && !bandbox)) {
			element = layoutService.replaceCombobox(parameter.form, entityField, 
													parameter.layoutRoot, parameter.contextId);
		}
		FormFieldExtra fieldExtra = parameter.form.getFieldExtra(entityField);
		if (readonly || filter != null || transformer != null || detailForm != null) {
			if (fieldExtra == null) {
				fieldExtra = new FormFieldExtra();
				fieldExtra.setEntityField(entityField);
				parameter.form.addFieldExtra(fieldExtra);
			}
			fieldExtra.setReadonly(readonly);
			fieldExtra.setFilter(filter);
			fieldExtra.setTransformer(transformer);
			fieldExtra.setDetailForm(detailForm);
		}
		else if (fieldExtra != null) {
			parameter.form.removeFieldExtra(fieldExtra);
		}
	}
	
	@Command
	public void applyGridProperties(@BindingParam(C.ELEM) Component elem) {
		try {
			layoutService.setGridTitle(parameter.form, parameter.layoutRoot, element, text);
			final LayoutElement elemColumns = element.getChild(LayoutElement.COLUMNS);
			for (int i = 0; i < columnProperties.size(); i++) {
				layoutService.applyProperties(elemColumns.getChildAt(i), 
											  columnProperties.get(i));
			}
			refreshAndClose();
		}
		catch (ValidationException vex) {
			showValidationErrors(elem, ERROR_APPLY_PROPERTIES, vex.getErrors());
		}
	}
	
	@Command
	public void applyBorderLayoutProperties(@BindingParam(C.ELEM) Component elem) {
		try {
			layoutService.applyBorderLayoutProperties(parameter.form, parameter.layoutRoot, 
													  element, borderLayoutProperties);
			refreshAndClose();
		}
		catch (ValidationException vex) {
			showValidationErrors(elem, ERROR_APPLY_PROPERTIES, vex.getErrors());
		}
	}
	
	@Command
	public void applyBorderLayoutAreaProperties(@BindingParam(C.ELEM) Component elem) {
		try {
			layoutService.applyBorderLayoutAreaProperties(element, layoutAreaProperties);
			refreshAndClose();
		}
		catch (ValidationException vex) {
			showValidationErrors(elem, ERROR_APPLY_PROPERTIES, vex.getErrors());
		}
	}
	
	@Command
	public void applySubFormProperties(@BindingParam(C.ELEM) Component elem) {
		adjustLists(subFormProperties.getColumns(), getListManagerList(FIELDS, LIST_SELECTED));
		adjustLists(subFormProperties.getActions(), getListManagerList(ACTIONS, LIST_SELECTED));
		layoutService.applySubFormProperties(parameter.form, parameter.layoutRoot, 
											 element, subFormProperties);
		refreshAndClose();
	}
	
	@Command
	public void addText(@BindingParam(C.ELEM) Component elem) {
		try {
			layoutService.addText(parameter.form, text, 
					  			  parameter.layoutRoot, parameter.contextId);
			refreshAndClose();
		}
		catch (ValidationException vex) {
			showValidationErrors(elem, "admin.layout.addtextfail", vex.getErrors());
		}
	}
	
	@Command
	public void addTabbox(@BindingParam(C.ELEM) Component elem) {
		try {
			layoutService.addTabbox(parameter.form, text, 
									parameter.layoutRoot, parameter.contextId);
			refreshAndClose();
		}
		catch (ValidationException vex) {
			showValidationErrors(elem, ERROR_ADD_TAB, vex.getErrors());
		}
	}
	
	@Command
	public void addTab(@BindingParam(C.ELEM) Component elem) {
		try {
			layoutService.addTab(parameter.form, text, 
								 parameter.layoutRoot, parameter.contextId);
			refreshAndClose();
		}
		catch (ValidationException vex) {
			showValidationErrors(elem, ERROR_ADD_TAB, vex.getErrors());
		}
	}
	
	@Command
	public void addLayout(@BindingParam(C.ELEM) Component elem) {
		try {
			if (layoutType == null) {
				throw new ValidationException(new ValidationErrors().addEmptyField("label.layouttype"));
			}
			switch (layoutType) {
				case BORDERLAYOUT:
					layoutService.addBorderLayout(parameter.form, borderLayoutProperties, 
							  					  parameter.layoutRoot, parameter.contextId);
					break;
					
				case GRID:
					layoutService.addGrid(parameter.form, gridColumns, gridRows, text, 
										  parameter.layoutRoot, parameter.contextId);
					break;
				
				default:
					Assert.stateIllegal("unknown layoutType:" + layoutType);
			}
			refreshAndClose();
		}
		catch (ValidationException vex) {
			showValidationErrors(elem, ERROR_ADD_TAB, vex.getErrors());
		}
	}

	@Command
	public void createLayout(@BindingParam(C.ELEM) Component elem) {
		try {
			if (layoutType == null) {
				throw new ValidationException(new ValidationError("val.empty.field", "label.layouttype"));
			}
			LayoutElement layoutRoot = null;
			switch (layoutType) {
				case BORDERLAYOUT:
					layoutRoot = layoutService.createBorderLayout(borderLayoutProperties);
					break;
					
				case GRID:
					layoutRoot = layoutService.createGridLayout(gridColumns, gridRows);
					break;
					
				default:
					Assert.stateIllegal("unknown layoutType:" + layoutType);
			}
			parentVM().setLayout(layoutRoot);
			window.detach();
		}
		catch (ValidationException vex) {
			showValidationErrors(elem, "admin.form.newlayoutfail", vex.getErrors());
		}
	}
	
	@Command
	public void addGrid(@BindingParam(C.ELEM) Component elem) {
		try {
			layoutService.addGrid(parameter.form, gridColumns, gridRows, text,
								  parameter.layoutRoot, parameter.contextId);
			refreshAndClose();
		}
		catch (ValidationException vex) {
			showValidationErrors(elem, "admin.layout.addgridfail", vex.getErrors());
		}
	}
	
	@Command
	public void addSubForm(@BindingParam(C.ELEM) Component elem) {
		try {
			layoutService.addSubForm(parameter.form, nestedEntity, 
									 parameter.layoutRoot, parameter.contextId);
			refreshAndClose();
		}
		catch (ValidationException vex) {
			showValidationErrors(elem, "admin.layout.addsubformfail", vex.getErrors());
		}
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
	private AdminFormViewModel parentVM() {
		return parameter.parentViewModel;
	}
	
	private LayoutElement getContextElement() {
		return layoutService.getElementByContextId(parameter.layoutRoot, parameter.contextId);
	}
	
	private LayoutDialogPreferences getPreferences() {
		LayoutDialogPreferences prefs = getSessionObject("layoutPreferences");
		if (prefs == null) {
			prefs = new LayoutDialogPreferences();
			setSessionObject("layoutPreferences", prefs);
		}
		return prefs;
	}
	
	private void refreshAndClose() {
		parentVM().flagDirty();
		parentVM().refreshLayout();
		window.detach();
	}

	@Override
	protected List<SystemObject> getListManagerSource(String key, int listNum) {
		switch (key) {
			case FIELDS:
				return MiscUtils.castList(listNum == LIST_AVAILABLE
						? subFormProperties.getAvailableColumns()
						: subFormProperties.getColumns());
			
			case ACTIONS:
				return MiscUtils.castList(listNum == LIST_AVAILABLE
						? subFormProperties.getAvailableActions()
						: subFormProperties.getActions());
						
			default:
				throw new IllegalStateException("unknown list manager key: " + key);
		}
	}
	
	@Override
	protected FormService getObjectService() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected void resetProperties() {
		throw new UnsupportedOperationException();
	}
	
}
