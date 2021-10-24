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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.seed.C;
import org.seed.core.util.Assert;

import org.springframework.util.ObjectUtils;
import org.xml.sax.Attributes;

public final class LayoutElement {
	
	public static final String A				= "a";
	public static final String ATTRIBUTE		= "attribute";
	public static final String BANDBOX			= "bandbox";
	public static final String BANDPOPUP		= "bandpopup";
	public static final String BORDERLAYOUT		= "borderlayout";
	public static final String BUTTON			= "button";
	public static final String CAPTION			= "caption";
	public static final String CELL				= "cell";
	public static final String CENTER 			= "center";
	public static final String CHECKBOX			= "checkbox";
	public static final String COLUMN			= "column";
	public static final String COLUMNS			= "columns";
	public static final String COMBOBOX			= "combobox";
	public static final String COMBOITEM		= "comboitem";
	public static final String DATEBOX			= "datebox";
	public static final String DECIMALBOX		= "decimalbox";
	public static final String DOUBLEBOX		= "doublebox";
	public static final String EAST 			= "east";
	public static final String FILEBOX			= "filebox"; 
	public static final String GRID				= "grid";
	public static final String GROUPBOX			= "groupbox";
	public static final String IMAGE			= "image";
	public static final String INTBOX			= "intbox";
	public static final String LABEL 			= "label";
	public static final String LONGBOX 			= "longbox";
	public static final String LISTBOX 			= "listbox";
	public static final String LISTCELL 		= "listcell";
	public static final String LISTHEAD 		= "listhead";
	public static final String LISTHEADER 		= "listheader";
	public static final String LISTITEM 		= "listitem";
	public static final String MENU 			= "menu";
	public static final String MENUITEM 		= "menuitem";
	public static final String MENUPOPUP 		= "menupopup";
	public static final String NORTH 			= "north";
	public static final String ROW 				= "row";
	public static final String ROWS 			= "rows";
	public static final String SOUTH 			= "south";
	public static final String TABBOX 			= "tabbox";
	public static final String TAB 				= "tab";
	public static final String TABS 			= "tabs";
	public static final String TABPANEL 		= "tabpanel";
	public static final String TABPANELS 		= "tabpanels";
	public static final String TEMPLATE 		= "template";
	public static final String TEXTBOX 			= "textbox";
	public static final String TOOLBAR			= "toolbar";
	public static final String TOOLBARBUTTON	= "toolbarbutton";
	public static final String WEST 			= "west";
	public static final String ZK 				= "zk";
	
	private final String name;
	
	private LayoutElement parent;
	
	private Map<String, String> attributes;
	
	private List<LayoutElement> children;
	
	private String text;
	
	private boolean decorated;
	
	public LayoutElement(String name) {
		this(name, null);
	}
	
	LayoutElement(String name, Attributes attributes) {
		Assert.notNull(name, C.NAME);
		
		this.name = name;
		if (attributes != null) {
			for (int i = 0; i < attributes.getLength(); i++) {
				setAttribute(attributes.getQName(i), attributes.getValue(i));
			}
		}
	}
	
	// copy constructor
	private LayoutElement(LayoutElement element) {
		this(element.getName());
		if (element.hasAttributes()) {
			attributes = new HashMap<>(element.getAttributes());
		}
	}
	
	public String getName() {
		return name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public LayoutElement copy() {
		return new LayoutElement(this);
	}

	public LayoutElement getParent() {
		Assert.stateAvailable(parent, "parent");
		
		return parent;
	}

	private void setParent(LayoutElement parent) {
		Assert.notNull(parent, "parent");
		Assert.state(this.parent == null, "parent reassigned");
		
		this.parent = parent;
	}
	
	public boolean isDecorated() {
		return decorated;
	}

	public void setDecorated(boolean decorated) {
		this.decorated = decorated;
	}
	
	public String getId() {
		return getAttribute(LayoutElementAttributes.A_ID);
	}
	
	public String getContext() {
		return getAttribute(LayoutElementAttributes.A_CONTEXT);
	}
	
	public LayoutElement setContext(String contextId) {
		Assert.notNull(contextId, "contextId");
		
		setAttribute(LayoutElementAttributes.A_CONTEXT, contextId);
		return this;
	}
	
	public LayoutElement setAlign(String align) {
		Assert.notNull(align, LayoutElementAttributes.A_ALIGN);
		
		setAttribute(LayoutElementAttributes.A_ALIGN, align);
		return this;
	}
	
	public LayoutElement setValign(String valign) {
		Assert.notNull(valign, LayoutElementAttributes.A_VALIGN);
		
		setAttribute(LayoutElementAttributes.A_VALIGN, valign);
		return this;
	}
	
	public LayoutElement setClass(String className) {
		Assert.notNull(className, C.CLASSNAME);
		
		setAttribute(LayoutElementAttributes.A_CLASS, className);
		return this;
	}
	
	public LayoutElement setIcon(String icon) {
		Assert.notNull(icon, C.ICON);
		
		setAttribute(LayoutElementAttributes.A_ICONSCLASS, icon + " alpha-icon-lg");
		return this;
	}
	
	public LayoutElement setLabel(String label) {
		Assert.notNull(label, LABEL);
		
		setAttribute(LABEL, label);
		return this;
	}
	
	public LayoutElement setOnClick(String onClick) {
		Assert.notNull(onClick, "onClick");
		
		setAttribute(LayoutElementAttributes.A_ONCLICK, onClick);
		return this;
	}
	
	public boolean is(String name) {
		Assert.notNull(name, C.NAME);
		
		return this.name.equals(name);
	}
	
	public boolean parentIs(String name) {
		Assert.notNull(name, C.NAME);
		
		return parent != null && parent.is(name);
	}
	
	public boolean isEmpty() {
		return !hasChildren() && text == null;
	}
	
	public boolean hasAttributes() {
		return !ObjectUtils.isEmpty(attributes);
	}
	
	public boolean hasAttribute(String name) {
		Assert.notNull(name, C.NAME);
		
		return attributes != null && attributes.keySet().contains(name);
	}
	
	public Map<String, String> getAttributes() {
		return attributes != null 
				? Collections.unmodifiableMap(attributes) 
				: null;
	}
	
	public String getAttribute(String name) {
		Assert.notNull(name, C.NAME);
		
		return attributes != null ? attributes.get(name) : null;
	}
	
	public LayoutElement setAttribute(String name, Object value) {
		Assert.notNull(name, C.NAME);
		Assert.notNull(value, C.VALUE);
		
		if (attributes == null) {
			attributes = new HashMap<>();
		}
		attributes.put(name, value.toString());
		return this;
	}
	
	public void removeAttribute(String name) {
		Assert.notNull(name, C.NAME);
		
		if (attributes != null) {
			attributes.remove(name);
		}
	}
	
	public boolean hasChildren() {
		return !ObjectUtils.isEmpty(children);
	}
	
	public boolean existChild(String elementName) {
		return getChild(elementName) != null;
	}

	public List<LayoutElement> getChildren() {
		return children != null 
				? Collections.unmodifiableList(children) 
				: null;
	}
	
	public LayoutElement getChild(String elementName) {
		Assert.notNull(elementName, "elementName");
		checkChildren();
		
		for (LayoutElement child : children) {
			if (child.is(elementName)) {
				return child;
			}
		}
		return null;
	}
	
	public LayoutElement getChildAt(int index) {
		checkChildren();
		
		return children.get(index);
	}
	
	public int getChildCount() {
		return children != null ? children.size() : 0;
	}
	
	public int getChildIndex(LayoutElement element) {
		Assert.notNull(element, C.ELEMENT);
		checkChildren();
		
		return children.indexOf(element);
	}
	
	public LayoutElement addChild(LayoutElement element) {
		return addChild(element, null);
	}
	
	public LayoutElement addChild(LayoutElement element, Integer index) {
		Assert.notNull(element, C.ELEMENT);
		
		element.setParent(this);
		if (children == null) {
			children = new CopyOnWriteArrayList<>();
		}
		if (index != null) {
			children.add(index, element);
		}
		else {
			children.add(element);
		}
		return element;
	}
	
	public void removeChild(LayoutElement element) {
		Assert.notNull(element, C.ELEMENT);
		checkChildren();
	
		children.remove(element);
	}
	
	public void removeChildAt(int index) {
		checkChildren();
		
		children.remove(index);
	}
	
	public void removeChildren() {
		children = null;
	}
	
	public void removeChildren(String name) {
		Assert.notNull(name, C.NAME);
		
		if (children != null) {
			children.removeIf(c -> c.is(name));
		}
	}
	
	public void removeFromParent() {
		getParent().removeChild(this);
	}
	
	public LayoutElement getCellNeighbor(Orientation orient) {
		Assert.notNull(orient, LayoutElementAttributes.A_ORIENT);
		checkCell();
		
		final int idxRow = getRowIndex();
		final int idxCol = getColumnIndex();
		switch (orient) {
			case LEFT:
				if (idxCol > 0) {
					return getParent().getChildAt(idxCol - 1);
				}
				break;
				
			case RIGHT:
				if (idxCol < getParent().getChildCount() - 1) {
					return getParent().getChildAt(idxCol + 1);
				}
				break;
				
			case TOP:
				if (idxRow > 0) {
					return getParent().getParent().getChildAt(idxRow - 1).getChildAt(idxCol);
				}
				break;
				
			case BOTTOM:
				if (idxRow < getParent().getParent().getChildCount() - 1) {
					return getParent().getParent().getChildAt(idxRow + 1).getChildAt(idxCol);
				}
				break;
		}
		return null;
	}
	
	public void accept(LayoutVisitor visitor) {
		Assert.notNull(visitor, "visitor");
		
		visitor.visit(this);
		if (hasChildren()) {
			for (LayoutElement child : getChildren()) {
				child.accept(visitor);
			}
		}
	}
	
	public boolean isLayoutArea() {
		return is(NORTH) || is(WEST) || is(CENTER) ||
			   is(EAST) || is(SOUTH);
	}
	
	public LayoutElement getGrid() {
		checkCell();
		//     row		   //rows	   grid
		return getParent().getParent().getParent();
	}
	
	public LayoutElement getGridCell(int column, int row) {
		Assert.state(is(GRID), "element is not a grid");
		
		final LayoutElement elemRows = getChild(ROWS);
		return elemRows != null 
				? elemRows.getChildAt(row).getChildAt(column) 
				: null;
	}
	
	public LayoutElement getGridContainer() {
		checkCell();
		
		final LayoutElement elemGrid = getGrid();
		if (elemGrid.parentIs(GROUPBOX)) {
			return elemGrid.getParent().getParent();
		}
		return elemGrid.getParent();
	}
	
	public LayoutElement getTab() {
		Assert.state(is(TABPANEL), "element is not a tabpanel");
		
		final int idx = getParent().getChildIndex(this);
		final LayoutElement elemTabs = getParent().getParent().getChild(TABS);
		return elemTabs != null 
				? elemTabs.getChildAt(idx) 
				: null;
	}
	
	public LayoutElement getTabboxContainer() {
		Assert.state(is(TAB) || is(TABPANEL), "element is not a tab or tabpanel");
		// 	   tabs		   tabbox
		return getParent().getParent().getParent();
	}
	
	public int getColumnIndex() {
		checkCell();
		//     row
		return getParent().getChildIndex(this);
	}
	
	public int getRowIndex() {
		checkCell();
		//     row		   rows
		return getParent().getParent().getChildIndex(getParent());
	}
	
	void setOrRemoveAttribute(String name, Object value) {
		Assert.notNull(name, C.NAME);
		
		if (ObjectUtils.isEmpty(value) || Boolean.FALSE.equals(value)) {
			removeAttribute(name);
		}
		else {
			if (value instanceof Boolean || value instanceof Enum) {
				value = value.toString().toLowerCase();
			}
			setAttribute(name, value);
		}
	}
	
	private void checkChildren() {
		Assert.state(hasChildren(), "element has no children");
	}
	
	private void checkCell() {
		Assert.state(is(CELL), "element is not a cell");
	}
	
}
