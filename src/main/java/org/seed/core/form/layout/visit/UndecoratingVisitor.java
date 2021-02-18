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

import java.util.Collections;

import org.seed.core.entity.EntityField;
import org.seed.core.form.Form;
import org.seed.core.form.FormFieldExtra;
import org.seed.core.form.SubForm;
import org.seed.core.form.SubFormField;
import org.seed.core.form.layout.BorderLayoutArea;
import org.seed.core.form.layout.LayoutElement;

import org.springframework.util.Assert;

public class UndecoratingVisitor extends AbstractLayoutVisitor {
	
	public UndecoratingVisitor(Form form) {
		super(form);
	}

	@Override
	public void visit(LayoutElement element) {
		if (!element.is(LayoutElement.ZK) && !element.isDecorated()) {
			return;
		}
		
		element.removeAttribute("context");
		element.removeAttribute("tooltiptext");
		// remove text if element has new child
		if (element.hasChildren() && element.getText() != null) {
			element.setText(null);
		}
		EntityField entityField;
		FormFieldExtra fieldExtra;
		switch (element.getName()) {
			case LayoutElement.ZK:
				setRootElement(element);
				break;
			
			case LayoutElement.NORTH:
			case LayoutElement.EAST:
			case LayoutElement.CENTER:
			case LayoutElement.WEST:
			case LayoutElement.SOUTH:
				element.setClass("alpha-noborder");
				break;
				
			case LayoutElement.CELL:
				element.removeAttribute("class");
				if (element.getText() != null) {
					element.setText(null);
					element.removeAttribute("align");
				}
				break;
			
			case LayoutElement.IMAGE:
				entityField = getEntityField(element);
				element.setAttribute("content", load(propertyName(entityField)) + ' ' + 
												converter("vm.imageConverter"));
				element.setAttribute("visible", load(isVisible(entityField)));
				if (!entityField.isCalculated()) {
					element.setOnClick(command("'editImage', fieldId='" + entityField.getUid() +'\''));
				}
				break;
				
			case LayoutElement.FILEBOX:
				entityField = getEntityField(element);
				element.setAttribute("content", bind(propertyName(entityField) + ".content"));
				element.setAttribute("contentType", bind(propertyName(entityField) + ".contentType"));
				element.setAttribute("fileName", bind(propertyName(entityField) + ".name"));
				element.setAttribute("visible", load(isVisible(entityField)));
				element.setAttribute("readonly", load(isReadonly(entityField)));
				element.setAttribute("mandatory", load(isMandatory(entityField)));
				element.setAttribute("onChange", command(onChange(entityField)));
				element.removeAttribute("disabled");
				break;
				
			case LayoutElement.CHECKBOX:
				entityField = getEntityField(element);
				element.setAttribute("checked", entityField.isCalculated() 
												? load(propertyName(entityField)) 
												: bind(propertyName(entityField)));
				element.setAttribute("visible", load(isVisible(entityField)));
				element.setAttribute("disabled", load(isReadonly(entityField)));
				if (!entityField.isCalculated()) {
					element.setAttribute("onCheck", command(onChange(entityField)));
				}
				break;
				
			case LayoutElement.COMBOBOX:
				entityField = getEntityField(element);
				Assert.state(entityField.getType().isReference(), "field is not a reference field");
				element.setAttribute("model", load("vm.getReferenceValues('" + entityField.getUid() + "')"));
				element.setAttribute("value", load(propertyName(entityField) + '.' + 
												   referenceName(entityField)));
				element.setAttribute("visible", load(isVisible(entityField)));
				element.setAttribute("readonly", load(isReadonly(entityField)));
				element.setAttribute("mandatory", load(isMandatory(entityField)));
				element.setAttribute("selectedItem", bind(propertyName(entityField)));
				element.setAttribute("onSelect", command(onChange(entityField)));
				element.addChild(createTemplate("model", entityField.getInternalName()))
					   .addChild(createComboitem(load(entityField.getInternalName() + '.' + 
							   						  referenceName(entityField))));
				fieldExtra  = getFieldExtra(entityField);
				if (fieldExtra != null && fieldExtra.getDetailForm() != null) {
					element.setContext(newContextId());
					addToRoot(createReferencePopup(element.getContext(), element.getId()));
				}
				break;
			
			case LayoutElement.BANDBOX:
				entityField = getEntityField(element);
				Assert.state(entityField.getType().isReference(), "field is not a reference field");
				element.setAttribute("value", load(propertyName(entityField) + '.' + 
												   referenceName(entityField)));
				element.setAttribute("visible", load(isVisible(entityField)));
				element.setAttribute("readonly", load(isReadonly(entityField)));
				element.setAttribute("mandatory", load(isMandatory(entityField)));
				element.setAttribute("buttonVisible", load('!'+isReadonly(entityField)));
				final LayoutElement elemListbox = element.addChild(createBandpopup())
														 .addChild(createListBox());
				elemListbox.setAttribute("model", load("vm.getReferenceListModel('" + entityField.getUid() + 
														"')) @template(empty each ? 'empty' : 'model'"));
				elemListbox.setAttribute("selectedItem", bind(propertyName(entityField)));
				elemListbox.setAttribute("onSelect", command(onChange(entityField)));
				elemListbox.setAttribute("height", "300px");
				elemListbox.setAttribute("width", "350px");
				elemListbox.addChild(createListHead(true));
				elemListbox.addChild(createTemplate("empty", entityField.getReferenceEntity().getInternalName()))
				   		   .addChild(createListItem(null))		   
				   		   .addChild(createListCell('[' + getLabel("label.empty") + ']', null, null));
				elemListbox.addChild(createTemplate("model", entityField.getReferenceEntity().getInternalName()))
						   .addChild(createListItem(null))		   
						   .addChild(createListCell(load(entityField.getReferenceEntity().getInternalName() + '.' + 
								   						 referenceName(entityField)), null, null));
				fieldExtra = getFieldExtra(entityField);
				if (fieldExtra != null && fieldExtra.getDetailForm() != null) {
					element.setContext(newContextId());
					addToRoot(createReferencePopup(element.getContext(), element.getId()));
				}
				break;
			
			case LayoutElement.TEXTBOX:	
			case LayoutElement.DATEBOX:
			case LayoutElement.DECIMALBOX:
			case LayoutElement.DOUBLEBOX:
			case LayoutElement.INTBOX:
			case LayoutElement.LONGBOX:
				entityField = getEntityField(element);
				element.setAttribute("visible", load(isVisible(entityField)));
				element.setAttribute("readonly", load(isReadonly(entityField)));
				element.setAttribute("mandatory", load(isMandatory(entityField)));
				element.setAttribute("value", entityField.isCalculated() 
												? load(propertyName(entityField)) 
												: bind(propertyName(entityField)));
				if (!(entityField.isCalculated() || entityField.getType().isAutonum())) {
					element.setAttribute("onChange", command(onChange(entityField)));
					element.setAttribute("instant", "true");
				}
				break;
				
			case LayoutElement.TABPANEL:
				if (element.getText() != null) {
					element.setText(null);
					element.removeAttribute("style");
				}
				break;
				
			case LayoutElement.BORDERLAYOUT:
				if (element.getId() == null) {
					break;
				}
				// create sub form
				final SubForm subForm = getSubForm(element);
				element.setAttribute("visible", load("vm.isSubFormVisible('" + subForm.getNestedEntity().getUid() + "')"));
				final LayoutElement elemListBox = element.getChild(LayoutElement.CENTER).getChild(LayoutElement.LISTBOX);
				elemListBox.setAttribute("model", load("vm.object." + subForm.getNestedEntity().getInternalName()));
				elemListBox.setAttribute("selectedItem", bind(selectedSubFormObject(subForm)));
				elemListBox.setAttribute("onSelect", command("'selectSubFormObject'"));
				elemListBox.setAttribute("nonselectableTags", "");
				elemListBox.setAttribute("autopaging", "true");
				elemListBox.setAttribute("mold", "paging");
				final LayoutElement elemListitem = elemListBox.addChild(createTemplate("model", subForm.getNestedEntity().getInternalName()))
															  .addChild(createListItem(null));
				if (subForm.hasActions()) {
					final LayoutElement elemNorth = element.addChild(createBorderLayoutArea(BorderLayoutArea.NORTH), 0);
					final LayoutElement elemToolbar = elemNorth.addChild(createToolbar("@init(vm.getSubFormActions('" + subForm.getNestedEntity().getUid() + 
																					"')) @template(empty each.type.listTemplate ? 'default' : each.type.listTemplate)"));
					final LayoutElement elemActionTemplate = elemToolbar.addChild(createTemplate("default", "action"));
					final LayoutElement elemActionTemplateSelect = elemToolbar.addChild(createTemplate("select", "action"));
					final LayoutElement elemToolbarButton = createToolbarButton("@init(vm.getActionLabel(action))", // label
							"'callSubFormAction',nestedId='" + subForm.getNestedEntity().getUid() + "',action=action,elem=self", // command
							"@init(action.type.icon.concat(' z-icon-fw alpha-icon-lg'))"); // icon
					elemToolbarButton.setAttribute("enable", load("vm.isActionEnabled(action)"));
					final LayoutElement elemToolbarButtonSelect = elemToolbarButton.copy();
					elemToolbarButtonSelect.setAttribute("visible", load("!empty " + selectedSubFormObject(subForm)));
					elemActionTemplate.addChild(elemToolbarButton);
					elemActionTemplateSelect.addChild(elemToolbarButtonSelect);
				}
				
				if (subForm.hasFields()) {
					final String nestedName = subForm.getNestedEntity().getInternalName();
					for (SubFormField subFormField : subForm.getFields()) {
						final EntityField nestedEntityField = subFormField.getEntityField();
						final String subFormPropertyName = nestedName + '.' + nestedEntityField.getInternalName();
						// bandbox
						if (subFormField.isBandbox()) {
							final LayoutElement elemField = elemListitem.addChild(new LayoutElement(LayoutElement.LISTCELL))
																		.addChild(createBandbox(nestedEntityField));
							elemField.setContext(subForm.getNestedEntity().getId() + "_" + elemField.getId());
							elemField.removeAttribute("id");
							elemField.setAttribute("value", load(subFormPropertyName + '.' + 
																 referenceName(nestedEntityField))); 
							elemField.setAttribute("readonly", load(isReadonly(nestedEntityField)));
							elemField.setAttribute("mandatory", load(isMandatory(nestedEntityField)));
							elemField.setAttribute("buttonVisible", load('!'+isReadonly(nestedEntityField)));
							
							final LayoutElement elemList = elemField.addChild(createBandpopup())
									 								.addChild(createListBox());
							elemList.setAttribute("model", load("vm.getNestedReferenceListModel('" + subForm.getNestedEntity().getUid() + "','" + 
														   		nestedEntityField.getUid()+'\'') + ") @template(empty each ? 'empty' : 'model')");
							elemList.setAttribute("selectedItem", bind(subFormPropertyName));
							elemList.setAttribute("onSelect", command(onNestedChange(nestedName, nestedEntityField)));
							elemList.setAttribute("height", "300px");
							elemList.setAttribute("width", "350px");
							elemList.addChild(createListHead(true));
							elemList.addChild(createTemplate("empty", nestedEntityField.getReferenceEntity().getInternalName()))
									   .addChild(createListItem(null))		   
									   .addChild(createListCell('[' + getLabel("label.empty") + ']', null, null));
							elemList.addChild(createTemplate("model", nestedEntityField.getReferenceEntity().getInternalName()))
								   .addChild(createListItem(null))		   
								   .addChild(createListCell(load(nestedEntityField.getReferenceEntity().getInternalName() + '.' + 
										   						 referenceName(nestedEntityField)), null, null));
							if (subFormField.getDetailForm() != null) {
								addToRoot(createReferencePopup(elemField.getContext(), nestedEntityField.getUid()));
							}
							continue;
						}
						
						final LayoutElement elemField = elemListitem.addChild(new LayoutElement(LayoutElement.LISTCELL))
																	.addChild(createFormField(nestedEntityField));
						elemField.setContext(subForm.getNestedEntity().getId() + "_" + elemField.getId());
						elemField.removeAttribute("id");
						switch (nestedEntityField.getType()) {
							case DATE:
							case DATETIME:
							case DECIMAL:
							case DOUBLE:
							case INTEGER:
							case LONG:
							case TEXT:
							case TEXTLONG:
								elemField.setAttribute("value", nestedEntityField.isCalculated() 
																	? load(subFormPropertyName) 
																	: bind(subFormPropertyName));
								elemField.setAttribute("readonly", load(isReadonly(nestedEntityField)));
								elemField.setAttribute("mandatory", load(isMandatory(nestedEntityField)));
								if (!nestedEntityField.isCalculated()) {
									elemField.setAttribute("instant", "true");
									elemField.setAttribute("onChange", command(onNestedChange(nestedName, nestedEntityField)));
								}
								break;
							
							case BINARY:
								elemField.removeAttribute("hflex");
								elemField.setAttribute("content", load(subFormPropertyName) + ' ' + 
																  converter("vm.imageConverter"));
								elemField.setAttribute("width", subFormField.getWidth());
								elemField.setAttribute("height", subFormField.getHeight());
								if (!nestedEntityField.isCalculated()) {
									elemField.setOnClick(command("'editImage', fieldId='" + nestedEntityField.getUid() + 
																		    "', nestedObject=" + nestedName));
								}
								break;
								
							case BOOLEAN:
								elemField.setAttribute("checked", nestedEntityField.isCalculated() 
																	? load(subFormPropertyName) 
																	: bind(subFormPropertyName));
								elemField.setAttribute("disabled", load(isReadonly(nestedEntityField)));
								if (!nestedEntityField.isCalculated()) {
									elemField.setAttribute("onCheck", command(onNestedChange(nestedName, nestedEntityField)));
								}
								break;
							
							case REFERENCE:
								elemField.setAttribute("model", load("vm.getNestedReferenceValues('" + 
																		subForm.getNestedEntity().getUid() + "','" + 
																		nestedEntityField.getUid() + "')"));
								elemField.setAttribute("onSelect", command(onNestedChange(nestedName, nestedEntityField)));
								elemField.setAttribute("selectedItem", bind(subFormPropertyName));
								elemField.setAttribute("value", load(subFormPropertyName + '.' + 
																	 referenceName(nestedEntityField)));
								elemField.setAttribute("readonly", load(isReadonly(nestedEntityField)));
								elemField.setAttribute("mandatory", load(isMandatory(nestedEntityField)));
								elemField.addChild(createTemplate("model", nestedEntityField.getInternalName()))
					                     .addChild(createComboitem(load(nestedEntityField.getInternalName() + '.' + 
					                    		 						referenceName(nestedEntityField))));
					            if (subFormField.getDetailForm() != null) {
									addToRoot(createReferencePopup(elemField.getContext(), nestedEntityField.getUid()));
								}
								break;
								
							case FILE:
								elemField.setAttribute("content", bind(subFormPropertyName + ".content"));
								elemField.setAttribute("contentType", bind(subFormPropertyName + ".contentType"));
								elemField.setAttribute("fileName", bind(subFormPropertyName + ".name"));
								elemField.setAttribute("readonly", load(isReadonly(nestedEntityField)));
								elemField.setAttribute("mandatory", load(isMandatory(nestedEntityField)));
								elemField.setAttribute("onChange", command(onNestedChange(nestedName, nestedEntityField)));
								break;
							
							default:
								throw new UnsupportedOperationException(nestedEntityField.getType().name());
						}
					}
				}
				break;
		}
	
		element.setDecorated(false);
	}
	
	private static LayoutElement createReferencePopup(String context, String fieldUid) {
		final LayoutElement elemMenuItem = 
				createMenuItem("label.showreference", "z-icon-share alpha-icon-lg", 
							   "'showReference',fieldId='" + fieldUid + '\'');
		elemMenuItem.setAttribute("disabled", load("vm.isReferenceEmpty('" + fieldUid + "')"));
		return createPopupMenu(context, Collections.singletonList(elemMenuItem));
	}
	
	private static String propertyName(EntityField entityField) {
		return "vm.object." + entityField.getInternalName();
	}
	
	private static String referenceName(EntityField entityField) {
		return "getReferenceIdentifier('" + entityField.getReferenceEntityField().getInternalName() + "')";
	}
	
	private static String isReadonly(EntityField entityField) {
		return "vm.isFieldReadonly('" + entityField.getUid() + "')";
	}
	
	private static String isMandatory(EntityField entityField) {
		return "vm.isFieldMandatory('" + entityField.getUid() + "')";
	}
	
	private static String isVisible(EntityField entityField) {
		return "vm.isFieldVisible('" + entityField.getUid() + "')";
	}
	
	private static String onChange(EntityField entityField) {
		return "'objectChanged',fieldId='" + entityField.getUid() + "',elem=self";
	}
	
	private static String onNestedChange(String nestedName, EntityField entityField) {
		return "'nestedChanged',nested=" + nestedName + ",fieldId='" + entityField.getUid() + "',elem=self";
	}
	
	private static String selectedSubFormObject(SubForm subForm) {
		return "vm.getSubForm('" + subForm.getNestedEntity().getUid() + "').selectedObject";
	}

}
