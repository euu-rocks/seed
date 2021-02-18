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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.seed.core.form.Form;
import org.seed.core.form.layout.LayoutElement;

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
				element.setContext(newContextId());
				if (element.isEmpty()) {
					element.setClass("alpha-layout-grid");
					if (!element.hasAttribute("size")) {
						element.setAttribute("size", "20%");
					}
				}
				addToRoot(createBorderLayoutMenuPopup(element));
				break;
				
			case LayoutElement.EAST:
			case LayoutElement.WEST:
				element.setContext(newContextId());
				if (element.isEmpty()) {
					element.setClass("alpha-layout-grid");
					final boolean existCenter = element.getParent().existChild(LayoutElement.CENTER);
					if (!element.hasAttribute("size")) {
						element.setAttribute("size", existCenter ? "20%" : "50%");
					}
				}
				addToRoot(createBorderLayoutMenuPopup(element));
				break;
				
			case LayoutElement.CENTER:
				element.setContext(newContextId());
				if (element.isEmpty()) {
					element.setClass("alpha-layout-grid");
				}
				addToRoot(createBorderLayoutMenuPopup(element));
				break;
				
			case LayoutElement.CELL:
				element.setContext(newContextId())
					   .setClass("alpha-layout-grid");
				if (element.isEmpty()) {
					element.setAlign("center")
						   .setText('(' + String.valueOf(element.getColumnIndex() + 1) + 
									',' + String.valueOf(element.getRowIndex() + 1) + ')');
				}
				addToRoot(createCellMenuPopup(element));
				break;
			
			case LayoutElement.LABEL:
				element.setContext(newContextId());
				addToRoot(createLabelMenuPopup(element));
				break;
			
			case LayoutElement.IMAGE:
				element.setAttribute("content", "@init(null) @converter('org.seed.ui.zk.ImageConverter')");
				element.removeAttribute("onClick");
				element.removeAttribute("visible");
				if (element.getId() != null) {
					element.setContext(newContextId())
						   .setAttribute("tooltiptext", getEntityField(element).getName());
					addToRoot(createFieldMenuPopup(element));
				}
				break;
				
			case LayoutElement.CHECKBOX:
				element.removeAttribute("checked");
				element.removeAttribute("onCheck");
				element.removeAttribute("disabled");
				element.removeAttribute("visible");
				if (element.getId() != null) {
					element.setContext(newContextId());
					element.setAttribute("tooltiptext", getEntityField(element).getName());
					addToRoot(createFieldMenuPopup(element));
				}
				break;
			
			case LayoutElement.COMBOBOX:
				element.removeAttribute("model");
				element.removeAttribute("selectedItem");
				element.removeAttribute("onSelect");
				element.removeChildren(LayoutElement.TEMPLATE);
				// no break on purpose
			
			case LayoutElement.BANDBOX:
				element.removeAttribute("buttonVisible");
				element.removeChildren(LayoutElement.BANDPOPUP);
				// no break on purpose
			
			case LayoutElement.FILEBOX:	
				element.removeAttribute("content");
				element.removeAttribute("contentType");
				element.removeAttribute("fileName");
				if (element.is(LayoutElement.FILEBOX)) {
					element.setAttribute("disabled", "true");
				}
				// no break on purpose
				
			case LayoutElement.TEXTBOX:
			case LayoutElement.DATEBOX:
			case LayoutElement.DECIMALBOX:
			case LayoutElement.DOUBLEBOX:
			case LayoutElement.INTBOX:
			case LayoutElement.LONGBOX:
				element.removeAttribute("instant");
				element.removeAttribute("readonly");
				element.removeAttribute("mandatory");
				element.removeAttribute("visible");
				element.removeAttribute("value");
				element.removeAttribute("onChange");
				if (element.getId() != null) {
					element.setContext(newContextId())
						   .setAttribute("tooltiptext", getEntityField(element).getName());
					addToRoot(createFieldMenuPopup(element));
				}
				break;
				
			case LayoutElement.BORDERLAYOUT:
				if (element.getId() == null) {
					break;	// no subform
				}
				element.removeAttribute("visible");
				final LayoutElement elemListBox = element.getChild(LayoutElement.CENTER).getChild(LayoutElement.LISTBOX);
				elemListBox.setContext(newContextId());
				elemListBox.removeAttribute("model");
				elemListBox.removeAttribute("selectedItem");
				elemListBox.removeAttribute("autopaging");
				elemListBox.removeAttribute("mold");
				elemListBox.removeChildren("template");
				element.removeChildren(LayoutElement.NORTH);
				addToRoot(createSubFormMenuPopup(elemListBox));
				break;
			
			case LayoutElement.TAB:
				element.setContext(newContextId());
				addToRoot(createPopupMenu(element.getContext(), 
											 Collections.singletonList(createTabMenu(element))));
				break;
				
			case LayoutElement.TABPANEL:
				element.setContext(newContextId());
				if (element.isEmpty()) {
					if (element.getParent().getParent().parentIs(LayoutElement.CELL)) {
						element.setAttribute("style", "padding: 5px;text-align:center")
							   .setText(getLabel("label.empty"));
					}
					addToRoot(createTabPanelPopupMenu(element));
				}
				break;
		}
		
		if (!element.is(LayoutElement.ZK)) { // zk element can't be decorated
			element.setDecorated(true);
		}
		
	}
	
	private LayoutElement createCellMenuPopup(LayoutElement element) {
		final List<LayoutElement> menus = new ArrayList<>();
		
		if (!element.hasChildren()) {
			if (fieldsAvailable()) {
				menus.add(createMenuItem("admin.layout.addfield", "z-icon-edit",
						   				 "'addField',contextid='" + element.getContext() + '\''));
			}
			menus.add(createMenuItem("admin.layout.addtext", "z-icon-paragraph",
	   				 				 "'addText',contextid='" + element.getContext() + '\''));
			menus.add(createMenuItem("admin.layout.addtab", "z-icon-folder",
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
		final List<LayoutElement> menus = new ArrayList<>();
		if (element.isEmpty()) {
			menus.add(createMenuItem("admin.layout.addsubform", "z-icon-list-alt",
 	   		  						 "'addSubForm',contextid='" + element.getContext() + '\''));
			menus.add(createMenuItem("admin.layout.addlayout", "z-icon-newspaper-o",
		 	   	   	   	   			 "'addLayout',contextid='" + element.getContext() + '\''));
			menus.add(createMenuItem("admin.layout.addtab", "z-icon-folder",
	 				 				 "'addTabbox',contextid='" + element.getContext() + '\''));
		}
		menus.add(createLayoutAreaMenu(element.getContext(), element.isEmpty()));
		menus.add(createBorderLayoutMenu(element.getContext(), isRootLayout(element.getParent())));
		return createPopupMenu(element.getContext(), menus);
	}
	
	private static LayoutElement createLayoutAreaMenu(String contextId, boolean isAreaEmpty) {
		final List<LayoutElement> menus = new ArrayList<>();
		
		menus.add(createMenuItem("admin.layout.editarea", "z-icon-wrench",
								 "'editBorderLayoutArea',contextid='" + contextId + '\''));
		if (isAreaEmpty) {
			menus.add(createMenuItem("admin.layout.removearea", "z-icon-remove",
	 	   	   	   	   				 "'removeBorderLayoutArea',contextid='" + contextId + '\''));
		}
		return createMenu("label.layoutarea", "z-icon-columns",
		  		  		  menus.toArray(new LayoutElement[menus.size()]));
	}
		
	private static LayoutElement createBorderLayoutMenu(String contextId, boolean isRootLayout) {
		final List<LayoutElement> menus = new ArrayList<>();
		
		menus.add(createMenuItem("admin.layout.editlayout", "z-icon-wrench",
		 	   	   				 "'editBorderLayout',contextid='" + contextId + '\''));
		if (!isRootLayout) {
			menus.add(createMenuItem("admin.layout.removelayout", "z-icon-remove",
   	   				 				 "'removeBorderLayout',contextid='" + contextId + '\''));
		}
		
		return createMenu("label.layout", "z-icon-newspaper-o",
				  		  menus.toArray(new LayoutElement[menus.size()]));
	}
	
	private static LayoutElement createLabelMenuPopup(LayoutElement element) {
		final List<LayoutElement> menus = new ArrayList<>();
		
		menus.add(createMenu("label.text", "z-icon-paragraph",
				createMenuItem("admin.layout.edittext", "z-icon-wrench",
		 				 	   "'editText',contextid='" + element.getContext() + '\''),
				createMenuItem("admin.layout.removetext", "z-icon-remove",
		  				 	   "'removeText',contextid='" + element.getContext() + '\'')));
		menus.add(createCellMenu(element.getParent().getContext()));
		menus.add(createGridMenu(element.getParent().getContext(), isRootGrid(element.getParent())));
		return createPopupMenu(element.getContext(), menus);
	}
	
	private static LayoutElement createFieldMenuPopup(LayoutElement element) {
		final List<LayoutElement> menus = new ArrayList<>();
		
		menus.add(createMenu("label.field", "z-icon-edit",
				createMenuItem("admin.layout.editfield", "z-icon-wrench",
		 				 	   "'editField',contextid='" + element.getContext() + '\''),
				createMenuItem("admin.layout.removefield", "z-icon-remove",
		  				 	   "'removeField',contextid='" + element.getContext() + '\'')));
		menus.add(createCellMenu(element.getParent().getContext()));
		menus.add(createGridMenu(element.getParent().getContext(), isRootGrid(element.getParent())));
		return createPopupMenu(element.getContext(), menus);
	}
	
	private static LayoutElement createSubFormMenuPopup(LayoutElement element) {
		final List<LayoutElement> menus = new ArrayList<>();
		
		menus.add(createMenu("label.subform", "z-icon-list-alt",
				createMenuItem("admin.layout.editsubform", "z-icon-wrench",
	 				 	   "'editSubForm',contextid='" + element.getContext() + '\''),
				createMenuItem("admin.layout.removesubform", "z-icon-remove",
	  				 	   "'removeSubForm',contextid='" + element.getContext() + '\'')));
		return createPopupMenu(element.getContext(), menus);
	}
	
	private static LayoutElement createTabPanelPopupMenu(LayoutElement element) {
		final List<LayoutElement> menus = new ArrayList<>();
		
		menus.add(createMenuItem("admin.layout.addlayout", "z-icon-newspaper-o",
		 	   	   	   	   		 "'addLayout',contextid='" + element.getContext() + '\''));
		menus.add(createMenuItem("admin.layout.addsubform", "z-icon-list-alt",
	   	   	   		  			 "'addSubForm',contextid='" + element.getContext() + '\''));
		menus.add(createTabMenu(element.getTab()));
		if (element.getTabboxContainer().isLayoutArea()) {
			menus.add(createLayoutAreaMenu(element.getTabboxContainer().getContext(), false));
		}
		return createPopupMenu(element.getContext(), menus);
	}
	
	private static LayoutElement createTabMenu(LayoutElement element) {
		return createMenu("label.tab", "z-icon-folder",	
				createMenuItem("admin.layout.addtab", "z-icon-plus",
			   	   	   	   	   "'addTab',contextid='" + element.getContext() + '\''),
				createMenuItem("admin.layout.edittab", "z-icon-wrench",
				   	   	   	   "'editTab',contextid='" + element.getContext() + '\''),
				createMenuItem("admin.layout.removetab", "z-icon-remove",
		   	   	   	   	   	   "'removeTab',contextid='" + element.getContext() + '\''));
	}
	
	private static LayoutElement createCellMenu(String contextId) {
		return createMenu("label.cell", "z-icon-square-o",	
				createMenuItem("admin.layout.editcell", "z-icon-wrench",
				   	   	   	   "'editCell',contextid='" + contextId + '\''));
	}
	
	private static LayoutElement createGridMenu(String contextId, boolean rootGrid) {
		final List<LayoutElement> menus = new ArrayList<>();
		
		menus.add(createMenu("label.column", "z-icon-arrows-v",
				createMenuItem("admin.layout.newcolumnleft", "z-icon-arrow-left",
							   "'newColumnLeft',contextid='" + contextId + '\''),	
				createMenuItem("admin.layout.newcolumnright", "z-icon-arrow-right",
							   "'newColumnRight',contextid='" + contextId + '\''),
				createMenuItem("admin.layout.removecolumn", "z-icon-remove",
							   "'removeColumn',contextid='" + contextId + '\'')));
		menus.add(createMenu("label.row", "z-icon-arrows-h",
				createMenuItem("admin.layout.newrowabove", "z-icon-arrow-up",
						   	   "'newRowAbove',contextid='" + contextId + '\''),	
				createMenuItem("admin.layout.newrowbelow", "z-icon-arrow-down",
						   	   "'newRowBelow',contextid='" + contextId + '\''),
				createMenuItem("admin.layout.removerow", "z-icon-remove",
						   	   "'removeRow',contextid='" + contextId + '\'')));
		menus.add(createMenuItem("admin.layout.editgrid", "z-icon-wrench",
				   	   	   	   "'editGrid',contextid='" + contextId + '\''));
		if (!rootGrid) {
			menus.add(createMenuItem("admin.layout.removegrid", "z-icon-remove",
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
		return !getLayoutService().getAvailableEntityFields(form, getRootElement()).isEmpty();
	}
	
}
