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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seed.core.config.ApplicationContextProvider;
import org.seed.core.data.FieldType;
import org.seed.core.entity.EntityField;
import org.seed.core.form.LabelProvider;

import org.springframework.util.Assert;

public abstract class LayoutUtils {
	
	private static final Map<FieldType, String> mapElements = new HashMap<>(16, 1f);
	
	private static final Map<BorderLayoutArea, String> mapBorderLayoutArea = new HashMap<>(8, 1f);
	
	private static LabelProvider labelProvider;
	
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
	
	public static String bind(String property) {
		Assert.notNull(property, "property is null");
		
		return "@bind(" + property + ')';
 	}
	
	public static String command(String command) {
		Assert.notNull(command, "command is null");
		
		return "@command(" + command + ')';
 	}
	
	public static String converter(String converter) {
		Assert.notNull(converter, "converter is null");
		
		return "@converter(" + converter + ')';
	}
	
	public static String load(String property) {
		Assert.notNull(property, "property is null");
		
		return "@load(" + property + ')';
 	}
	
	public static String getLabel(String key) {
		return labelProvider().getLabel(key);
	}
	
	public static String getEnumLabel(Enum<?> enm) {
		return labelProvider().getEnumLabel(enm);
	}
	
	public static LayoutElement createZK() {
		return new LayoutElement(LayoutElement.ZK);
	}
	
	public static LayoutElement createGrid(int numColumns, int numRows, String title) {
		final LayoutElement elemGrid = new LayoutElement(LayoutElement.GRID);
		final LayoutElement elemRows = elemGrid.addChild(new LayoutElement(LayoutElement.ROWS));
		elemGrid.setClass("alpha-noborder");
		elemGrid.addChild(createColumns(numColumns));
		for (int i = 0; i < numRows; i++) {
			elemRows.addChild(createRow(numColumns));
		}
		if (title != null) {
			return createGroupbox(title, elemGrid);
		}
		return elemGrid;
	}
	
	public static LayoutElement createGroupbox(String title, LayoutElement elemGrid) {
		Assert.notNull(title, "title is null");
		Assert.notNull(elemGrid, "elemGrid is null");
		
		final LayoutElement elemGoupbox = new LayoutElement(LayoutElement.GROUPBOX);
		final LayoutElement elemCaption = elemGoupbox.addChild(new LayoutElement(LayoutElement.CAPTION));
		elemCaption.setLabel(title);
		elemGoupbox.addChild(elemGrid);
		return elemGoupbox;
	}
	
	public static LayoutElement createBandbox(EntityField field) {
		Assert.notNull(field, "field is null");
		
		final LayoutElement elemBandbox = new LayoutElement(LayoutElement.BANDBOX);
		elemBandbox.setAttribute("id", field.getUid());
		elemBandbox.setAttribute("hflex", "1");
		elemBandbox.setOrRemoveAttribute("mandatory", field.isMandatory());
		return elemBandbox;
	}
	
	public static LayoutElement createBandpopup() {
		return new LayoutElement(LayoutElement.BANDPOPUP);
	}
	
	public static LayoutElement createBorderLayout(BorderLayoutProperties layoutProperties) {
		Assert.notNull(layoutProperties, "layoutProperties is null");
		
		final LayoutElement elemLayout = new LayoutElement(LayoutElement.BORDERLAYOUT);
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
		Assert.notNull(area, "area is null");
		
		final LayoutElement element = new LayoutElement(mapBorderLayoutArea.get(area));
		element.setAttribute("border", "0");
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
		Assert.notNull(label, "label is null");
		
		final LayoutElement elemComboitem = new LayoutElement(LayoutElement.COMBOITEM);
		elemComboitem.setLabel(label);
		return elemComboitem;
	}
	
	public static LayoutElement createLabel(String text) {
		Assert.notNull(text, "text is null");
		
		final LayoutElement elemLabel = new LayoutElement(LayoutElement.LABEL);
		if (text.contains("\n")) {
			elemLabel.setAttribute("pre", "true");
			elemLabel.addChild(createLabelAttribute(text));
		}
		else {
			elemLabel.setAttribute("value", text);
		}
		return elemLabel;
	}
	
	public static LayoutElement createLabelAttribute(String text) {
		Assert.notNull(text, "text is null");
		
		final LayoutElement elemAttr = new LayoutElement(LayoutElement.ATTRIBUTE);
		elemAttr.setAttribute("name", "value");
		elemAttr.setText(text);
		return elemAttr;
	}
	
	public static LayoutElement createFormField(EntityField field) {
		Assert.notNull(field, "field is null");
		Assert.state(mapElements.containsKey(field.getType()), "unsupported type " + field.getType());
		
		final LayoutElement elemField = new LayoutElement(mapElements.get(field.getType()));
		elemField.setAttribute("id", field.getUid());
		
		if (field.getType().isDateTime()) {
			elemField.setAttribute("format", "long+medium");
		}
		else if (field.getType().isTextLong()) {
			elemField.setAttribute("rows", "3");
		}
		if (!field.getType().isBinary()) {
			elemField.setAttribute("hflex", "1");
		}
		return elemField;
	}
	
	public static LayoutElement createButton(String icon) {
		Assert.notNull(icon, "icon is null");
		
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
		Assert.notNull(contextId, "contextId is null");
		Assert.notNull(menus, "menus is null");
		
		final LayoutElement elemPopup = new LayoutElement(LayoutElement.MENUPOPUP);
		elemPopup.setAttribute("id", contextId);
		for (LayoutElement menu : menus) {
			elemPopup.addChild(menu);
		}
		return elemPopup;
	}
	
	public static LayoutElement createMenu(String label, String icon, LayoutElement ...menuItems) {
		Assert.notNull(label, "label is null");
		
		final LayoutElement elemMenu = new LayoutElement(LayoutElement.MENU);
		elemMenu.setLabel(getLabel(label));
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
		Assert.notNull(label, "label is null");
		Assert.notNull(command, "command is null");
		
		final LayoutElement elemMenuItem = new LayoutElement(LayoutElement.MENUITEM);
		elemMenuItem.setLabel(getLabel(label));
		if (icon != null) {
			elemMenuItem.setIcon(icon);
		}
		elemMenuItem.setOnClick(command(command));
		return elemMenuItem;
	}
	
	public static LayoutElement createTabbox(String title) {
		final LayoutElement elemTabbox = new LayoutElement(LayoutElement.TABBOX);
		elemTabbox.setAttribute("vflex", "1");
		elemTabbox.setAttribute("hflex", "1");
		elemTabbox.setClass("alpha-tabbox");
		final LayoutElement elemTabs = elemTabbox.addChild(new LayoutElement(LayoutElement.TABS));
		elemTabs.addChild(createTab(title));
		final LayoutElement elemPanels = elemTabbox.addChild(new LayoutElement(LayoutElement.TABPANELS));
		elemPanels.addChild(createTabpanel());
		return elemTabbox;
	}
	
	public static LayoutElement createTab(String title) {
		Assert.notNull(title, "title is null");
		
		final LayoutElement elemTab = new LayoutElement(LayoutElement.TAB);
		elemTab.setLabel(title);
		return elemTab;
	}
	
	public static LayoutElement createTabpanel() {
		return new LayoutElement(LayoutElement.TABPANEL);
	}
	
	public static LayoutElement createListBox() {
		return new LayoutElement(LayoutElement.LISTBOX);
	}
	
	public static LayoutElement createToolbar(String children) {
		Assert.notNull(children, "children is null");
		
		final LayoutElement elemToolbar = new LayoutElement(LayoutElement.TOOLBAR);
		elemToolbar.setAttribute("children", children);
		return elemToolbar;
	}
	
	public static LayoutElement createToolbarButton(String label, String command, String icon) {
		Assert.notNull(label, "label is null");
		Assert.notNull(command, "command is null");
		
		final LayoutElement elemButton = new LayoutElement(LayoutElement.TOOLBARBUTTON);
		elemButton.setLabel(label);
		elemButton.setOnClick(command(command));
		if (icon != null) {
			elemButton.setAttribute("iconSclass", icon);
		}
		return elemButton;
	}
	
	public static LayoutElement createListFormList() {
		final LayoutElement elemListbox = createListBox();
		elemListbox.setAttribute("model", load("vm.listModel"));
		elemListbox.setAttribute("selectedItem", bind("vm.object"));
		elemListbox.setAttribute("onSelect", command("'selectObject'"));
		elemListbox.setAttribute("vflex", "true");
		elemListbox.setAttribute("style", "margin:5px");
		return elemListbox;
	}
	
	public static LayoutElement createListHead(boolean sizable) {
		final LayoutElement elemListhead = new LayoutElement(LayoutElement.LISTHEAD);
		if (sizable) {
			elemListhead.setAttribute("sizable", "true");
		}
		return elemListhead;
	}
	
	public static LayoutElement createListHeader(String label, String hflex, String style) {
		Assert.notNull(label, "label is null");
		
		final LayoutElement elemListheader = new LayoutElement(LayoutElement.LISTHEADER);
		elemListheader.setLabel(label);
		if (hflex != null) {
			elemListheader.setAttribute("hflex", hflex);
		}
		if (style != null) {
			elemListheader.setAttribute("style", style);
		}
		return elemListheader;
	}
	
	public static LayoutElement createListItem(String doubleClickAction) {
		final LayoutElement elemListitem = new LayoutElement(LayoutElement.LISTITEM);
		if (doubleClickAction != null) {
			elemListitem.setAttribute("onDoubleClick", command(doubleClickAction));
		}
		return elemListitem;
	}
	
	public static LayoutElement createListCell(String property, String icon, String style) {
		Assert.notNull(property, "property is null");
		
		final LayoutElement elemListcell = new LayoutElement(LayoutElement.LISTCELL);
		if (icon != null) {
			elemListcell.setAttribute("iconSclass", icon);
		}
		final LayoutElement elemLabel = elemListcell.addChild(createLabel(property));
		if (style != null) {
			elemLabel.setAttribute("style", style);
		}
		return elemListcell;
	}
	
	public static LayoutElement createImageListCell(String property) {
		Assert.notNull(property, "property is null");
		
		final LayoutElement elemListcell = new LayoutElement(LayoutElement.LISTCELL);
		final LayoutElement elemImage = elemListcell.addChild(new LayoutElement(LayoutElement.IMAGE));
		elemImage.setAttribute("content", property);
		return elemListcell;
	}
	
	public static LayoutElement createTemplate(String name, String var) {
		Assert.notNull(name, "name is null");
		Assert.notNull(var, "var is null");
		
		final LayoutElement elemTemplate = new LayoutElement(LayoutElement.TEMPLATE);
		elemTemplate.setAttribute("name", name);
		elemTemplate.setAttribute("var", var);
		return elemTemplate;
	}
	
	private static LabelProvider labelProvider() {
		if (labelProvider == null) {
			labelProvider = ApplicationContextProvider.getBean(LabelProvider.class);
		}
		return labelProvider;
	}
	
}
