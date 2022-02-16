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
package org.seed.core.form.layout.visit;

import static org.seed.core.form.layout.LayoutElementAttributes.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.seed.core.form.Form;
import org.seed.core.form.layout.LayoutElement;
import org.seed.core.form.layout.LayoutElementAttributes;
import org.seed.core.form.layout.LayoutElementClass;

public class DecoratingVisitor extends AbstractLayoutVisitor {
	
	public DecoratingVisitor(Form form) {
		super(form);
	}
	
	@Override
	public void visit(LayoutElement element) {
		if (element.isDecorated()) {
			return;
		}
		switch (element.getName()) {
			case LayoutElement.ZK:
				setRootElement(element);
				break;
			
			case LayoutElement.NORTH:
			case LayoutElement.SOUTH:
			case LayoutElement.EAST:
			case LayoutElement.WEST:
			case LayoutElement.CENTER:
				visitBorderLayoutElement(element);
				break;
			
			case LayoutElement.CELL:
				element.setContext(newContextId())
					   .setClass(LayoutElementClass.LAYOUT_GRID);
				if (element.isEmpty()) {
					element.setAlign("center")
						   .setText('(' + String.valueOf(element.getColumnIndex() + 1) + 
									',' + String.valueOf(element.getRowIndex() + 1) + ')');
				}
				addRootChild(createCellMenuPopup(element));
				break;
			
			case LayoutElement.LABEL:
				element.setContext(newContextId());
				addRootChild(createLabelMenuPopup(element));
				break;
			
			case LayoutElement.IMAGE:
				element.setAttribute(A_CONTENT, "@init(null) @converter('org.seed.ui.zk.ImageConverter')");
				element.removeAttribute(A_ONCLICK);
				element.removeAttribute(A_VISIBLE);
				if (element.getId() != null) {
					element.setContext(newContextId())
						   .setAttribute(A_TOOLTIPTEXT, getEntityField(element).getName());
					addRootChild(createFieldMenuPopup(element));
				}
				break;
				
			case LayoutElement.CHECKBOX:
				element.removeAttribute(A_CHECKED);
				element.removeAttribute(A_ONCHECK);
				element.removeAttribute(A_DISABLED);
				element.removeAttribute(A_VISIBLE);
				if (element.getId() != null) {
					element.setContext(newContextId());
					element.setAttribute(A_TOOLTIPTEXT, getEntityField(element).getName());
					addRootChild(createFieldMenuPopup(element));
				}
				break;
			
			case LayoutElement.COMBOBOX:
				element.removeAttribute(A_MODEL);
				element.removeAttribute(A_SELECTEDITEM);
				element.removeAttribute(A_ONSELECT);
				element.removeChildren(LayoutElement.TEMPLATE);
				/* falls through */
			
			case LayoutElement.BANDBOX:
				element.removeAttribute(A_BUTTONVISIBLE);
				element.removeChildren(LayoutElement.BANDPOPUP);
				/* falls through */
			
			case LayoutElement.FILEBOX:	
				element.removeAttribute(A_CONTENT);
				element.removeAttribute(A_CONTENTTYPE);
				element.removeAttribute(A_FILENAME);
				if (element.is(LayoutElement.FILEBOX)) {
					element.setAttribute(A_DISABLED, V_TRUE);
				}
				/* falls through */
				
			case LayoutElement.TEXTBOX:
				/* falls through */
			case LayoutElement.DATEBOX:
				/* falls through */
			case LayoutElement.DECIMALBOX:
				/* falls through */
			case LayoutElement.DOUBLEBOX:
				/* falls through */
			case LayoutElement.INTBOX:
				/* falls through */
			case LayoutElement.LONGBOX:
				element.removeAttribute(A_INSTANT);
				element.removeAttribute(A_READONLY);
				element.removeAttribute(A_MANDATORY);
				element.removeAttribute(A_VISIBLE);
				element.removeAttribute(A_VALUE);
				element.removeAttribute(A_ONCHANGE);
				if (element.getId() != null) {
					element.setContext(newContextId())
						   .setAttribute(A_TOOLTIPTEXT, getEntityField(element).getName());
					addRootChild(createFieldMenuPopup(element));
				}
				break;
				
			case LayoutElement.BORDERLAYOUT:
				if (element.getId() != null) {
					visitBorderLayout(element);
				}
				break;
			
			case LayoutElement.TAB:
				element.setContext(newContextId());
				addRootChild(createPopupMenu(element.getContext(), Collections.singletonList(createTabMenu(element))));
				break;
				
			case LayoutElement.TABPANEL:
				visitTabpanel(element);
				break;
				
			default:
				// do nothing
				break;
		}
		
		if (!element.is(LayoutElement.ZK)) { // zk element can't be decorated
			element.setDecorated(true);
		}
		
	}
	
	private void visitBorderLayout(LayoutElement element) {
		element.removeAttribute(A_VISIBLE);
		final LayoutElement elemListBox = element.getChild(LayoutElement.CENTER).getChild(LayoutElement.LISTBOX);
		elemListBox.setContext(newContextId());
		elemListBox.removeAttribute(A_MODEL);
		elemListBox.removeAttribute(A_SELECTEDITEM);
		elemListBox.removeAttribute(A_AUTOPAGING);
		elemListBox.removeAttribute(A_MOLD);
		elemListBox.removeChildren(A_TEMPLATE);
		element.removeChildren(LayoutElement.NORTH);
		
		if (element.getId().startsWith(LayoutElementAttributes.PRE_SUBFORM)) {
			addRootChild(createSubFormMenuPopup(elemListBox));
		}
		else if (element.getId().startsWith(LayoutElementAttributes.PRE_RELATION)) {
			addRootChild(createRelationFormMenuPopup(elemListBox));
		}
	}
	
	private void visitBorderLayoutElement(LayoutElement element) {
		switch (element.getName()) {
			case LayoutElement.NORTH:
			case LayoutElement.SOUTH:
				if (element.isEmpty() && !element.hasAttribute(A_SIZE)) {
					element.setAttribute(A_SIZE, "20%");
				}
				break;
				
			case LayoutElement.CENTER:
				// do nothing
				break;
			
			case LayoutElement.EAST:
			case LayoutElement.WEST:
				final boolean existCenter = element.getParent().existChild(LayoutElement.CENTER);
				if (element.isEmpty() && !element.hasAttribute(A_SIZE)) {
					element.setAttribute(A_SIZE, existCenter ? "20%" : "50%");
				}
				break;
			
			default:
				throw new UnsupportedOperationException(element.getName());
		}
		if (element.isEmpty()) {
			element.setClass(LayoutElementClass.LAYOUT_GRID);
		}
		element.setContext(newContextId());
		addRootChild(createBorderLayoutMenuPopup(element));
	}
	
	private void visitTabpanel(LayoutElement element) {
		element.setContext(newContextId());
		if (element.isEmpty() && element.getParent().getParent().parentIs(LayoutElement.CELL)) {
			element.setAttribute(A_STYLE, "padding: 5px;text-align:center")
				   .setText(getLabel(LABEL_EMPTY));
		}
		addRootChild(createTabPanelPopupMenu(element));
	}
	
	private LayoutElement createCellMenuPopup(LayoutElement element) {
		final List<LayoutElement> menus = new ArrayList<>(8);
		
		if (!element.hasChildren()) {
			if (fieldsAvailable()) {
				menus.add(createMenuItem("admin.layout.addfield", "z-icon-edit",
						   				 "'addField',contextid='" + element.getContext() + '\''));
			}
			menus.add(createMenuItem("admin.layout.addtext", "z-icon-paragraph",
	   				 				 "'addText',contextid='" + element.getContext() + '\''));
			menus.add(createMenuItem(LABEL_ADDTAB, ICON_FOLDER,
	 				 				 "'addTabbox',contextid='" + element.getContext() + '\''));
			menus.add(createMenuItem("admin.layout.addgrid", "z-icon-table",
		 				 			 "'addGrid',contextid='" + element.getContext() + '\''));
		}
		menus.add(createCellMenu(element.getContext()));
		menus.add(createGridMenu(element.getContext(), isRootGrid(element)));
		
		final LayoutElement elemContainer = element.getGridContainer();
		if (elemContainer != null && elemContainer.isLayoutArea()) {
			menus.add(createLayoutAreaMenu(elemContainer.getContext(), false));
		}
		return createPopupMenu(element.getContext(), menus);
	}
	
	private static LayoutElement createBorderLayoutMenuPopup(LayoutElement element) {
		final List<LayoutElement> menus = new ArrayList<>(8);
		if (element.isEmpty()) {
			menus.add(createMenuItem("admin.layout.addsubform", ICON_LIST,
 	   		  						 "'addSubForm',contextid='" + element.getContext() + '\''));
			menus.add(createMenuItem("admin.layout.addrelationform", ICON_LINK,
	  			 		 			 "'addRelationForm',contextid='" + element.getContext() + '\''));
			menus.add(createMenuItem("admin.layout.addlayout", ICON_NEWSPAPER,
		 	   	   	   	   			 "'addLayout',contextid='" + element.getContext() + '\''));
			menus.add(createMenuItem(LABEL_ADDTAB, ICON_FOLDER,
	 				 				 "'addTabbox',contextid='" + element.getContext() + '\''));
		}
		menus.add(createLayoutAreaMenu(element.getContext(), element.isEmpty()));
		menus.add(createBorderLayoutMenu(element.getContext(), isRootLayout(element.getParent())));
		return createPopupMenu(element.getContext(), menus);
	}
	
	private static LayoutElement createLayoutAreaMenu(String contextId, boolean isAreaEmpty) {
		final List<LayoutElement> menus = new ArrayList<>(4);
		
		menus.add(createMenuItem("admin.layout.editarea", ICON_WRENCH,
								 "'editBorderLayoutArea',contextid='" + contextId + '\''));
		if (isAreaEmpty) {
			menus.add(createMenuItem("admin.layout.removearea", ICON_REMOVE,
	 	   	   	   	   				 "'removeBorderLayoutArea',contextid='" + contextId + '\''));
		}
		return createMenu("label.layoutarea", "z-icon-columns",
		  		  		  menus.toArray(new LayoutElement[menus.size()]));
	}
		
	private static LayoutElement createBorderLayoutMenu(String contextId, boolean isRootLayout) {
		final List<LayoutElement> menus = new ArrayList<>(4);
		
		menus.add(createMenuItem("admin.layout.editlayout", ICON_WRENCH,
		 	   	   				 "'editBorderLayout',contextid='" + contextId + '\''));
		if (!isRootLayout) {
			menus.add(createMenuItem("admin.layout.removelayout", ICON_REMOVE,
   	   				 				 "'removeBorderLayout',contextid='" + contextId + '\''));
		}
		
		return createMenu("label.layout", ICON_NEWSPAPER,
				  		  menus.toArray(new LayoutElement[menus.size()]));
	}
	
	private static LayoutElement createLabelMenuPopup(LayoutElement element) {
		final List<LayoutElement> menus = new ArrayList<>(4);
		
		menus.add(createMenu("label.text", "z-icon-paragraph",
				createMenuItem("admin.layout.edittext", ICON_WRENCH,
		 				 	   "'editText',contextid='" + element.getContext() + '\''),
				createMenuItem("admin.layout.removetext", ICON_REMOVE,
		  				 	   "'removeText',contextid='" + element.getContext() + '\'')));
		menus.add(createCellMenu(element.getParent().getContext()));
		menus.add(createGridMenu(element.getParent().getContext(), isRootGrid(element.getParent())));
		return createPopupMenu(element.getContext(), menus);
	}
	
	private static LayoutElement createFieldMenuPopup(LayoutElement element) {
		final List<LayoutElement> menus = new ArrayList<>(4);
		
		menus.add(createMenu("label.field", "z-icon-edit",
				createMenuItem("admin.layout.editfield", ICON_WRENCH,
		 				 	   "'editField',contextid='" + element.getContext() + '\''),
				createMenuItem("admin.layout.removefield", ICON_REMOVE,
		  				 	   "'removeField',contextid='" + element.getContext() + '\'')));
		menus.add(createCellMenu(element.getParent().getContext()));
		menus.add(createGridMenu(element.getParent().getContext(), isRootGrid(element.getParent())));
		return createPopupMenu(element.getContext(), menus);
	}
	
	private static LayoutElement createSubFormMenuPopup(LayoutElement element) {
		final List<LayoutElement> menus = new ArrayList<>(2);
		
		menus.add(createMenu("label.subform", ICON_LIST,
				createMenuItem("admin.layout.editsubform", ICON_WRENCH,
							   "'editSubForm',contextid='" + element.getContext() + '\''),
				createMenuItem("admin.layout.removesubform", ICON_REMOVE,
							   "'removeSubForm',contextid='" + element.getContext() + '\'')));
		return createPopupMenu(element.getContext(), menus);
	}
	
	private static LayoutElement createRelationFormMenuPopup(LayoutElement element) {
		final List<LayoutElement> menus = new ArrayList<>(2);
		
		menus.add(createMenu("label.relation", ICON_LINK,
				  createMenuItem("admin.layout.removerelation", ICON_REMOVE,
	  				 			 "'removeRelationForm',contextid='" + element.getContext() + '\'')));
		return createPopupMenu(element.getContext(), menus);
	}
	
	private static LayoutElement createTabPanelPopupMenu(LayoutElement element) {
		final List<LayoutElement> menus = new ArrayList<>(6);
		
		menus.add(createMenuItem("admin.layout.addlayout", ICON_NEWSPAPER,
		 	   	   	   	   		 "'addLayout',contextid='" + element.getContext() + '\''));
		menus.add(createMenuItem("admin.layout.addsubform", ICON_LIST,
	   	   	   		  			 "'addSubForm',contextid='" + element.getContext() + '\''));
		menus.add(createMenuItem("admin.layout.addrelationform", ICON_LINK,
	   		  			 		 "'addRelationForm',contextid='" + element.getContext() + '\''));
		
		menus.add(createTabMenu(element.getTab()));
		if (element.getTabboxContainer().isLayoutArea()) {
			menus.add(createLayoutAreaMenu(element.getTabboxContainer().getContext(), false));
		}
		return createPopupMenu(element.getContext(), menus);
	}
	
	private static LayoutElement createTabMenu(LayoutElement element) {
		return createMenu("label.tab", ICON_FOLDER,	
				createMenuItem(LABEL_ADDTAB, "z-icon-plus",
			   	   	   	   	   "'addTab',contextid='" + element.getContext() + '\''),
				createMenuItem("admin.layout.edittab", ICON_WRENCH,
				   	   	   	   "'editTab',contextid='" + element.getContext() + '\''),
				createMenuItem("admin.layout.removetab", ICON_REMOVE,
		   	   	   	   	   	   "'removeTab',contextid='" + element.getContext() + '\''));
	}
	
	private static LayoutElement createCellMenu(String contextId) {
		return createMenu("label.cell", "z-icon-square-o",	
				createMenuItem("admin.layout.editcell", ICON_WRENCH,
				   	   	   	   "'editCell',contextid='" + contextId + '\''));
	}
	
	private static LayoutElement createGridMenu(String contextId, boolean rootGrid) {
		final List<LayoutElement> menus = new ArrayList<>();
		
		menus.add(createMenu("label.column", "z-icon-arrows-v",
				createMenuItem("admin.layout.newcolumnleft", "z-icon-arrow-left",
							   "'newColumnLeft',contextid='" + contextId + '\''),	
				createMenuItem("admin.layout.newcolumnright", "z-icon-arrow-right",
							   "'newColumnRight',contextid='" + contextId + '\''),
				createMenuItem("admin.layout.removecolumn", ICON_REMOVE,
							   "'removeColumn',contextid='" + contextId + '\'')));
		menus.add(createMenu("label.row", "z-icon-arrows-h",
				createMenuItem("admin.layout.newrowabove", "z-icon-arrow-up",
						   	   "'newRowAbove',contextid='" + contextId + '\''),	
				createMenuItem("admin.layout.newrowbelow", "z-icon-arrow-down",
						   	   "'newRowBelow',contextid='" + contextId + '\''),
				createMenuItem("admin.layout.removerow", ICON_REMOVE,
						   	   "'removeRow',contextid='" + contextId + '\'')));
		menus.add(createMenuItem("admin.layout.editgrid", ICON_WRENCH,
				   	   	   	   "'editGrid',contextid='" + contextId + '\''));
		if (!rootGrid) {
			menus.add(createMenuItem("admin.layout.removegrid", ICON_REMOVE,
	  				 				 "'removeGrid',contextid='" + contextId + '\''));
		}
		return createMenu("label.grid", "z-icon-table", 
						  menus.toArray(new LayoutElement[menus.size()]));
	}
	
	private static boolean isRootGrid(LayoutElement element) {
		return element.getGrid().parentIs(LayoutElement.ZK);
	}
	
	private static boolean isRootLayout(LayoutElement element) {
		return element.parentIs(LayoutElement.ZK);
	}
	
	private boolean fieldsAvailable() {
		return !getLayoutService().getAvailableEntityFields(getForm(), getRootElement()).isEmpty();
	}
	
	private static final String LABEL_ADDTAB   = "admin.layout.addtab";
	
	private static final String ICON_FOLDER    = "z-icon-folder";
	private static final String ICON_LINK      = "z-icon-link";
	private static final String ICON_LIST      = "z-icon-list-alt";
	private static final String ICON_NEWSPAPER = "z-icon-newspaper-o";
	private static final String ICON_REMOVE    = "z-icon-remove";
	private static final String ICON_WRENCH    = "z-icon-wrench";
	
}
