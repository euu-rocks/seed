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
package org.seed.core.form.layout;

import static org.seed.core.form.layout.LayoutElementAttributes.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.seed.C;
import org.seed.Seed;
import org.seed.core.data.FieldType;
import org.seed.core.entity.EntityField;
import org.seed.core.util.Assert;
import org.seed.core.util.NameUtils;

public abstract class LayoutUtils {
	
	private static final Map<FieldType, String> mapElements = new EnumMap<>(FieldType.class);
	
	private static final Map<BorderLayoutArea, String> mapBorderLayoutArea = new EnumMap<>(BorderLayoutArea.class);
	
	static {
		mapElements.put(FieldType.AUTONUM,   LayoutElement.TEXTBOX);
		mapElements.put(FieldType.BINARY,    LayoutElement.IMAGE);
		mapElements.put(FieldType.BOOLEAN,   LayoutElement.CHECKBOX);
		mapElements.put(FieldType.DATE, 	 LayoutElement.DATEBOX);
		mapElements.put(FieldType.DATETIME,  LayoutElement.DATEBOX);
		mapElements.put(FieldType.DECIMAL,   LayoutElement.DECIMALBOX);
		mapElements.put(FieldType.DOUBLE, 	 LayoutElement.DOUBLEBOX);
		mapElements.put(FieldType.FILE,	  	 LayoutElement.FILEBOX);
		mapElements.put(FieldType.INTEGER,   LayoutElement.INTBOX);
		mapElements.put(FieldType.LONG, 	 LayoutElement.LONGBOX);
		mapElements.put(FieldType.REFERENCE, LayoutElement.COMBOBOX);
		mapElements.put(FieldType.TEXT, 	 LayoutElement.TEXTBOX);
		mapElements.put(FieldType.TEXTLONG,  LayoutElement.TEXTBOX);
		
		mapBorderLayoutArea.put(BorderLayoutArea.NORTH,  LayoutElement.NORTH);
		mapBorderLayoutArea.put(BorderLayoutArea.WEST,	 LayoutElement.WEST);
		mapBorderLayoutArea.put(BorderLayoutArea.CENTER, LayoutElement.CENTER);
		mapBorderLayoutArea.put(BorderLayoutArea.EAST,   LayoutElement.EAST);
		mapBorderLayoutArea.put(BorderLayoutArea.SOUTH,  LayoutElement.SOUTH);
	}
	
	protected LayoutUtils() {}
	
	public static String bind(String property) {
		Assert.notNull(property, C.PROPERTY);
		
		return "@bind(" + property + ')';
 	}
	
	public static String command(String command) {
		Assert.notNull(command, C.COMMAND);
		
		return "@command(" + command + ')';
 	}
	
	public static String converter(String converter) {
		Assert.notNull(converter, "converter");
		
		return "@converter(" + converter + ')';
	}
	
	public static String load(String property) {
		Assert.notNull(property, C.PROPERTY);
		
		return "@load(" + property + ')';
 	}
	
	public static LayoutElement createZK() {
		return new LayoutElement(LayoutElement.ZK);
	}
	
	public static LayoutElement createGrid(int numColumns, int numRows, String title) {
		final LayoutElement elemGrid = new LayoutElement(LayoutElement.GRID);
		final LayoutElement elemRows = elemGrid.addChild(createRows());
		elemGrid.setClass(LayoutElementClass.NO_BORDER)
				.addChild(createColumns(numColumns));
		for (int i = 0; i < numRows; i++) {
			elemRows.addChild(createRow(numColumns));
		}
		if (title != null) {
			return createGroupbox(title, elemGrid);
		}
		return elemGrid;
	}
	
	public static LayoutElement createGroupbox(String title, LayoutElement elemGrid) {
		Assert.notNull(title, "title");
		Assert.notNull(elemGrid, "elemGrid");
		
		final LayoutElement elemGoupbox = new LayoutElement(LayoutElement.GROUPBOX);
		final LayoutElement elemCaption = elemGoupbox.addChild(new LayoutElement(LayoutElement.CAPTION));
		elemCaption.setLabel(title);
		elemGoupbox.addChild(elemGrid);
		return elemGoupbox;
	}
	
	public static LayoutElement createBandbox(EntityField field) {
		Assert.notNull(field, C.FIELD);
		
		final LayoutElement elemBandbox = new LayoutElement(LayoutElement.BANDBOX);
		elemBandbox.setAttribute(A_ID, field.getUid())
				   .setAttribute(A_HFLEX, V_1)
				   .setOrRemoveAttribute(A_MANDATORY, field.isMandatory());
		return elemBandbox;
	}
	
	public static LayoutElement createBandpopup() {
		return new LayoutElement(LayoutElement.BANDPOPUP);
	}
	
	public static LayoutElement createBorderLayout() {
		return new LayoutElement(LayoutElement.BORDERLAYOUT);
	}
	
	public static LayoutElement createRows() {
		return new LayoutElement(LayoutElement.ROWS);
	}
	
	public static LayoutElement createTabs() {
		return new LayoutElement(LayoutElement.TABS);
	}
	
	public static LayoutElement createTabpanels() {
		return new LayoutElement(LayoutElement.TABPANELS);
	}
	
	public static LayoutElement createBorderLayout(BorderLayoutProperties layoutProperties) {
		Assert.notNull(layoutProperties, "layoutProperties");
		
		final LayoutElement elemLayout = createBorderLayout();
		if (layoutProperties.getNorth().isVisible()) {
			elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.NORTH));
		}
		if (layoutProperties.getEast().isVisible()) {
			elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.EAST));
		}
		if (layoutProperties.getCenter().isVisible()) {
			elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.CENTER));
		}
		if (layoutProperties.getWest().isVisible()) {
			elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.WEST));
		}
		if (layoutProperties.getSouth().isVisible()) {
			elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.SOUTH));
		}
		return elemLayout;
	}
	
	public static LayoutElement createBorderLayoutArea(BorderLayoutArea area) {
		Assert.notNull(area, "area");
		
		final LayoutElement element = new LayoutElement(mapBorderLayoutArea.get(area));
		element.setAttribute(A_BORDER, V_0);
		return element;
	}
	
	public static LayoutElement createRow(int columns) {
		final LayoutElement elemRow = new LayoutElement(LayoutElement.ROW);
		for (int i = 0; i < columns; i++) {
			elemRow.addChild(createCell());
		}
		return elemRow;
	}
	
	public static LayoutElement createCell() {
		return new LayoutElement(LayoutElement.CELL);
	}
	
	public static LayoutElement createComboitem(String label) {
		Assert.notNull(label, C.LABEL);
		
		final LayoutElement elemComboitem = new LayoutElement(LayoutElement.COMBOITEM);
		elemComboitem.setLabel(label);
		return elemComboitem;
	}
	
	public static LayoutElement createLabel(String text) {
		Assert.notNull(text, C.TEXT);
		
		final LayoutElement elemLabel = new LayoutElement(LayoutElement.LABEL);
		elemLabel.setAttribute(A_SCLASS, NameUtils.getInternalName(text.trim()).replace('_', '-').toLowerCase() + "-label");
		if (text.contains("\n")) {
			elemLabel.setAttribute(A_PRE, V_TRUE)
					 .addChild(createLabelAttribute(text));
		}
		else {
			elemLabel.setAttribute(A_VALUE, text);
		}
		return elemLabel;
	}
	
	public static LayoutElement createLabelAttribute(String text) {
		Assert.notNull(text, C.TEXT);
		
		final LayoutElement elemAttr = new LayoutElement(LayoutElement.ATTRIBUTE);
		elemAttr.setAttribute(A_NAME, C.VALUE)
				.setText(text);
		return elemAttr;
	}
	
	public static LayoutElement createFormField(EntityField field) {
		Assert.notNull(field, C.FIELD);
		
		final LayoutElement elemField = createElement(field.getType());
		elemField.setAttribute(A_ID, field.getUid())
				 .setAttribute(A_SCLASS, NameUtils.getInternalName(field.getName().trim()).replace('_', '-').toLowerCase() + "-field");
		if (field.getType().isDateTime()) {
			elemField.setAttribute("format", "medium+medium");
		}
		else if (field.getType().isTextLong()) {
			elemField.setAttribute(A_ROWS, "3");
		}
		if (!field.getType().isBinary()) {
			elemField.setAttribute(A_HFLEX, V_1);
		}
		return elemField;
	}
	
	public static LayoutElement createRichTextField(EntityField field) {
		Assert.notNull(field, C.FIELD);
		
		final LayoutElement elemField = new LayoutElement(LayoutElement.RICHTEXTAREA);
		elemField.setAttribute(A_ID, field.getUid());
		return elemField;
	}
	
	public static LayoutElement createButton(String icon) {
		Assert.notNull(icon, C.ICON);
		
		final LayoutElement elemButton = new LayoutElement(LayoutElement.BUTTON);
		elemButton.setIcon(icon);
		return elemButton;
	}
	
	public static LayoutElement createColumn() {
		return new LayoutElement(LayoutElement.COLUMN);
	}
	
	public static LayoutElement createColumns(int columns) {
		final LayoutElement elemColumns = new LayoutElement(LayoutElement.COLUMNS);
		for (int i = 0; i < columns; i++) {
			elemColumns.addChild(createColumn());
		}
		return elemColumns;
	}
	
	public static LayoutElement createPopupMenu(String contextId, List<LayoutElement> menus) {
		Assert.notNull(contextId, "contextId");
		Assert.notNull(menus, "menus");
		
		final LayoutElement elemPopup = new LayoutElement(LayoutElement.MENUPOPUP);
		elemPopup.setAttribute(A_ID, contextId);
		for (LayoutElement menu : menus) {
			elemPopup.addChild(menu);
		}
		return elemPopup;
	}
	
	public static LayoutElement createMenu(String label, String icon, LayoutElement ...menuItems) {
		Assert.notNull(label, C.LABEL);
		
		final LayoutElement elemMenu = new LayoutElement(LayoutElement.MENU);
		elemMenu.setLabel(Seed.getLabel(label));
		if (icon != null) {
			elemMenu.setIcon(icon);
		}
		final LayoutElement elemPopup = elemMenu.addChild(new LayoutElement(LayoutElement.MENUPOPUP));
		for (LayoutElement menuItem : menuItems) {
			elemPopup.addChild(menuItem);
		}
		return elemMenu;
	}
	
	public static LayoutElement createMenuItem(String label, String icon, String command) {
		Assert.notNull(label, C.LABEL);
		Assert.notNull(command, C.COMMAND);
		
		final LayoutElement elemMenuItem = new LayoutElement(LayoutElement.MENUITEM)
												.setLabel(Seed.getLabel(label))
												.setOnClick(command(command));
		if (icon != null) {
			elemMenuItem.setIcon(icon);
		}
		return elemMenuItem;
	}
	
	public static LayoutElement createTabbox(String title) {
		final LayoutElement elemTabbox = new LayoutElement(LayoutElement.TABBOX);
		elemTabbox.setAttribute(A_HFLEX, V_1)
				  .setAttribute(A_VFLEX, V_1)
				  .setClass(LayoutElementClass.TABBOX);
		elemTabbox.addChild(createTabs()).addChild(createTab(title));
		elemTabbox.addChild(createTabpanels()).addChild(createTabpanel());
		return elemTabbox;
	}
	
	public static LayoutElement createTab(String title) {
		Assert.notNull(title, "title");
		
		final LayoutElement elemTab = new LayoutElement(LayoutElement.TAB);
		elemTab.setLabel(title)
			   .setAttribute(A_SCLASS, NameUtils.getInternalName(title).replace('_', '-').toLowerCase() + "-tab");
		return elemTab;
	}
	
	public static LayoutElement createTabpanel() {
		return new LayoutElement(LayoutElement.TABPANEL);
	}
	
	public static LayoutElement createListBox() {
		return new LayoutElement(LayoutElement.LISTBOX);
	}
	
	public static LayoutElement createToolbar(String children) {
		Assert.notNull(children, "children");
		
		final LayoutElement elemToolbar = new LayoutElement(LayoutElement.TOOLBAR);
		elemToolbar.setAttribute("children", children);
		return elemToolbar;
	}
	
	public static LayoutElement createToolbarButton(String label, String command, String icon) {
		Assert.notNull(label, C.LABEL);
		Assert.notNull(command, C.COMMAND);
		
		final LayoutElement elemButton = new LayoutElement(LayoutElement.TOOLBARBUTTON);
		elemButton.setLabel(label)
				  .setOnClick(command(command));
		if (icon != null) {
			elemButton.setAttribute(A_ICONSCLASS, icon);
		}
		return elemButton;
	}
	
	public static LayoutElement createListFormList() {
		final LayoutElement elemListbox = createListBox();
		elemListbox.setAttribute(A_MODEL, load("vm.listModel"))
				   .setAttribute(A_SELECTEDITEM, bind("vm.object"))
				   .setAttribute(A_ONSELECT, command("'selectObject'"))
				   .setAttribute(A_VFLEX, V_TRUE)
				   .setAttribute(A_STYLE, "margin:5px");
		return elemListbox;
	}
	
	public static LayoutElement createListHead(boolean sizable) {
		final LayoutElement elemListhead = new LayoutElement(LayoutElement.LISTHEAD);
		if (sizable) {
			elemListhead.setAttribute(A_SIZABLE, V_TRUE);
		}
		return elemListhead;
	}
	
	public static LayoutElement createListHeader(String label, String hflex, String style) {
		Assert.notNull(label, C.LABEL);
		
		final LayoutElement elemListheader = new LayoutElement(LayoutElement.LISTHEADER);
		elemListheader.setLabel(label);
		if (hflex != null) {
			elemListheader.setAttribute(A_HFLEX, hflex);
		}
		if (style != null) {
			elemListheader.setAttribute(A_STYLE, style);
		}
		return elemListheader;
	}
	
	public static LayoutElement createListItem(String doubleClickAction) {
		final LayoutElement elemListitem = new LayoutElement(LayoutElement.LISTITEM);
		if (doubleClickAction != null) {
			elemListitem.setAttribute(A_ONDOUBLECLICK, command(doubleClickAction));
		}
		return elemListitem;
	}
	
	public static LayoutElement createListCell(String property, String icon, String style, String testClass) {
		Assert.notNull(property, C.PROPERTY);
		
		final LayoutElement elemListcell = new LayoutElement(LayoutElement.LISTCELL);
		elemListcell.setAttribute(A_SCLASS, testClass);
		if (icon != null) {
			elemListcell.setAttribute(A_ICONSCLASS, icon);
		}
		final LayoutElement elemLabel = elemListcell.addChild(createLabel(property));
		elemLabel.removeAttribute(A_SCLASS);
		if (style != null) {
			elemLabel.setAttribute(A_STYLE, style);
		}
		return elemListcell;
	}
	
	public static LayoutElement createImageListCell(String property) {
		Assert.notNull(property, C.PROPERTY);
		
		final LayoutElement elemListcell = new LayoutElement(LayoutElement.LISTCELL);
		final LayoutElement elemImage = elemListcell.addChild(new LayoutElement(LayoutElement.IMAGE));
		elemImage.setAttribute(A_CONTENT, property);
		return elemListcell;
	}
	
	public static LayoutElement createTemplate(String name, String variable) {
		Assert.notNull(name, C.NAME);
		Assert.notNull(variable, A_VAR);
		
		return new LayoutElement(LayoutElement.TEMPLATE)
					.setAttribute(A_NAME, name)
					.setAttribute(A_VAR, variable);
	}
	
	private static LayoutElement createElement(FieldType type) {
		final String element = mapElements.get(type);
		Assert.state(element != null, "unsupported type " + type);
		return new LayoutElement(element);
	}
	
}
