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

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.xml.sax.Attributes;

public class LayoutElement {
	
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
		Assert.notNull(name, "name is null");
		
		this.name = name;
	}
	
	LayoutElement(String name, Attributes attributes) {
		this(name);
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
		Assert.state(parent != null, "parent not available");
		
		return parent;
	}

	private void setParent(LayoutElement parent) {
		Assert.notNull(parent, "parent is null");
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
		return getAttribute("id");
	}
	
	public String getContext() {
		return getAttribute("context");
	}
	
	public LayoutElement setContext(String contextId) {
		Assert.notNull(contextId, "contextId is null");
		
		setAttribute("context", contextId);
		return this;
	}
	
	public LayoutElement setAlign(String align) {
		Assert.notNull(align, "align is null");
		
		setAttribute("align", align);
		return this;
	}
	
	public LayoutElement setValign(String valign) {
		Assert.notNull(valign, "valign is null");
		
		setAttribute("valign", valign);
		return this;
	}
	
	public LayoutElement setClass(String className) {
		Assert.notNull(className, "className is null");
		
		setAttribute("class", className);
		return this;
	}
	
	public LayoutElement setIcon(String icon) {
		Assert.notNull(icon, "icon is null");
		
		setAttribute("iconSclass", icon + " alpha-icon-lg");
		return this;
	}
	
	public LayoutElement setLabel(String label) {
		Assert.notNull(label, "label is null");
		
		setAttribute("label", label);
		return this;
	}
	
	public LayoutElement setOnClick(String onClick) {
		Assert.notNull(onClick, "onClick is null");
		
		setAttribute("onClick", onClick);
		return this;
	}
	
	public boolean is(String name) {
		Assert.notNull(name, "name is null");
		
		return this.name.equals(name);
	}
	
	public boolean parentIs(String name) {
		Assert.notNull(name, "name is null");
		
		return parent != null && parent.is(name);
	}
	
	public boolean isEmpty() {
		return !hasChildren() && text == null;
	}
	
	public boolean hasAttributes() {
		return !ObjectUtils.isEmpty(attributes);
	}
	
	public boolean hasAttribute(String name) {
		Assert.notNull(name, "name is null");
		
		return attributes != null ? attributes.keySet().contains(name) : false;
	}
	
	public Map<String, String> getAttributes() {
		return attributes != null ? Collections.unmodifiableMap(attributes) : null;
	}
	
	public String getAttribute(String name) {
		Assert.notNull(name, "name is null");
		
		return attributes != null ? attributes.get(name) : null;
	}
	
	public LayoutElement setAttribute(String name, Object value) {
		Assert.notNull(name, "name is null");
		Assert.notNull(value, "value is null");
		
		if (attributes == null) {
			attributes = new HashMap<>();
		}
		attributes.put(name, value.toString());
		return this;
	}
	
	public void removeAttribute(String name) {
		Assert.notNull(name, "name is null");
		
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
		return children != null ? Collections.unmodifiableList(children) : null;
	}
	
	public LayoutElement getChild(String elementName) {
		Assert.notNull(elementName, "elementName is null");
		Assert.state(hasChildren(), "element has no children");
		
		for (LayoutElement child : children) {
			if (child.is(elementName)) {
				return child;
			}
		}
		return null;
	}
	
	public LayoutElement getChildAt(int index) {
		Assert.state(hasChildren(), "element has no children");
		
		return children.get(index);
	}
	
	public int getChildCount() {
		return children != null ? children.size() : 0;
	}
	
	public int getChildIndex(LayoutElement element) {
		Assert.notNull(element, "element is null");
		Assert.state(hasChildren(), "element has no children");
		
		return children.indexOf(element);
	}
	
	public LayoutElement addChild(LayoutElement element) {
		return addChild(element, null);
	}
	
	public LayoutElement addChild(LayoutElement element, Integer index) {
		Assert.notNull(element, "element is null");
		
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
		Assert.notNull(element, "element is null");
		Assert.state(hasChildren(), "element has no children");
	
		children.remove(element);
	}
	
	public void removeChildAt(int index) {
		Assert.state(hasChildren(), "element has no children");
		
		children.remove(index);
	}
	
	public void removeChildren() {
		children = null;
	}
	
	public void removeChildren(String name) {
		Assert.notNull(name, "name is null");
		
		if (children != null) {
			children.removeIf(c -> c.is(name));
		}
	}
	
	public void removeFromParent() {
		getParent().removeChild(this);
	}
	
	public LayoutElement getCellNeighbor(Orientation orient) {
		Assert.notNull(orient, "orient is null");
		Assert.state(is(CELL), "element is not a cell");
		
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
		Assert.notNull(visitor, "visitor is null");
		
		visitor.visit(this);
		if (hasChildren()) {
			for (LayoutElement child : children) {
				child.accept(visitor);
			}
		}
	}
	
	public boolean isLayoutArea() {
		return is(NORTH) || is(WEST) || is(CENTER) ||
			   is(EAST) || is(SOUTH);
	}
	
	public LayoutElement getGrid() {
		Assert.state(is(CELL), "element is not a cell");
		//     row		   //rows	   grid
		return getParent().getParent().getParent();
	}
	
	public LayoutElement getGridCell(int column, int row) {
		Assert.state(is(GRID), "element is not a cell");
		
		return getChild(ROWS).getChildAt(row).getChildAt(column);
	}
	
	public LayoutElement getGridContainer() {
		Assert.state(is(CELL), "element is not a cell");
		
		final LayoutElement elemGrid = getGrid();
		if (elemGrid.parentIs(GROUPBOX)) {
			return elemGrid.getParent().getParent();
		}
		return elemGrid.getParent();
	}
	
	public LayoutElement getTab() {
		Assert.state(is(TABPANEL), "element is not a tabpanel");
		
		final int idx = getParent().getChildIndex(this);
		//     tabpanels tabbox
		return getParent().getParent().getChild(TABS).getChildAt(idx);
	}
	
	public LayoutElement getTabboxContainer() {
		Assert.state(is(TAB) || is(TABPANEL), "element is not a tab or tabpanel");
		// 	   tabs		   tabbox
		return getParent().getParent().getParent();
	}
	
	public int getColumnIndex() {
		Assert.state(is(CELL), "element is not a cell");
		//     row
		return getParent().getChildIndex(this);
	}
	
	public int getRowIndex() {
		Assert.state(is(CELL), "element is not a cell");
		//     row		   rows
		return getParent().getParent().getChildIndex(getParent());
	}
	
	void setOrRemoveAttribute(String name, Object value) {
		Assert.notNull(name, "name is null");
		
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
	
}
