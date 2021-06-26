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
import org.seed.core.form.layout.LayoutElementAttributes;
import org.seed.core.form.layout.LayoutElementClass;
import org.seed.core.util.Assert;

public class UndecoratingVisitor extends AbstractLayoutVisitor {
	
	public UndecoratingVisitor(Form form) {
		super(form);
	}

	@Override
	public void visit(LayoutElement element) {
		if (!element.is(LayoutElement.ZK) && !element.isDecorated()) {
			return;
		}
		
		element.removeAttribute(LayoutElementAttributes.A_CONTEXT);
		element.removeAttribute(LayoutElementAttributes.A_TOOLTIPTEXT);
		// remove text if element has new child
		if (element.hasChildren() && element.getText() != null) {
			element.setText(null);
		}
		switch (element.getName()) {
			case LayoutElement.ZK:
				setRootElement(element);
				break;
			
			case LayoutElement.TABPANEL:
				if (element.getText() != null) {
					element.setText(null);
					element.removeAttribute(LayoutElementAttributes.A_STYLE);
				}
				break;
				
			case LayoutElement.BORDERLAYOUT:
				if (element.getId() != null) {
					createSubForm(element);
				}
				break;
				
			case LayoutElement.NORTH:
			case LayoutElement.EAST:
			case LayoutElement.CENTER:
			case LayoutElement.WEST:
			case LayoutElement.SOUTH:
				element.setClass(LayoutElementClass.NO_BORDER);
				break;
				
			case LayoutElement.CELL:
				visitCell(element);
				break;
			
			case LayoutElement.IMAGE:
				visitImage(element);
				break;
				
			case LayoutElement.FILEBOX:
				visitFilebox(element);
				break;
				
			case LayoutElement.CHECKBOX:
				visitCheckbox(element);
				break;
				
			case LayoutElement.COMBOBOX:
				visitCombobox(element);
				break;
			
			case LayoutElement.BANDBOX:
				visitBandbox(element);
				break;
			
			case LayoutElement.TEXTBOX:	
			case LayoutElement.DATEBOX:
			case LayoutElement.DECIMALBOX:
			case LayoutElement.DOUBLEBOX:
			case LayoutElement.INTBOX:
			case LayoutElement.LONGBOX:
				final EntityField entityField = getEntityField(element);
				element.setAttribute(LayoutElementAttributes.A_VISIBLE, load(isVisible(entityField)));
				element.setAttribute(LayoutElementAttributes.A_READONLY, load(isReadonly(entityField)));
				element.setAttribute(LayoutElementAttributes.A_MANDATORY, load(isMandatory(entityField)));
				element.setAttribute(LayoutElementAttributes.A_VALUE, value(entityField, propertyName(entityField)));
				if (!(entityField.isCalculated() || entityField.getType().isAutonum())) {
					element.setAttribute(LayoutElementAttributes.A_ONCHANGE, command(onChange(entityField)));
					element.setAttribute(LayoutElementAttributes.A_INSTANT, "true");
				}
				break;
				
			default:
				// do nothing
				break;
		}
		element.setDecorated(false);
	}
	
	private void visitCell(LayoutElement element) {
		element.removeAttribute(LayoutElementAttributes.A_CLASS);
		if (element.getText() != null) {
			element.setText(null);
			element.removeAttribute(LayoutElementAttributes.A_ALIGN);
		}
	}
	
	private void visitImage(LayoutElement element) {
		final EntityField entityField = getEntityField(element);
		element.setAttribute(LayoutElementAttributes.A_CONTENT, load(propertyName(entityField)) + ' ' + 
							 									converter("vm.imageConverter"));
		element.setAttribute(LayoutElementAttributes.A_VISIBLE, load(isVisible(entityField)));
		if (!entityField.isCalculated()) {
			element.setOnClick(command("'editImage', fieldId='" + entityField.getUid() +'\''));
		}
	}
	
	private void visitFilebox(LayoutElement element) {
		final EntityField entityField = getEntityField(element);
		element.setAttribute(LayoutElementAttributes.A_CONTENT, bind(propertyName(entityField) + ".content"));
		element.setAttribute(LayoutElementAttributes.A_CONTENTTYPE, bind(propertyName(entityField) + ".contentType"));
		element.setAttribute(LayoutElementAttributes.A_FILENAME, bind(propertyName(entityField) + ".name"));
		element.setAttribute(LayoutElementAttributes.A_VISIBLE, load(isVisible(entityField)));
		element.setAttribute(LayoutElementAttributes.A_READONLY, load(isReadonly(entityField)));
		element.setAttribute(LayoutElementAttributes.A_MANDATORY, load(isMandatory(entityField)));
		element.setAttribute(LayoutElementAttributes.A_ONCHANGE, command(onChange(entityField)));
		element.removeAttribute(LayoutElementAttributes.A_DISABLED);
	}
	
	private void visitCheckbox(LayoutElement element) {
		final EntityField entityField = getEntityField(element);
		element.setAttribute(LayoutElementAttributes.A_CHECKED, entityField.isCalculated() 
																? load(propertyName(entityField)) 
																: bind(propertyName(entityField)));
		element.setAttribute(LayoutElementAttributes.A_VISIBLE, load(isVisible(entityField)));
		element.setAttribute(LayoutElementAttributes.A_DISABLED, load(isReadonly(entityField)));
		if (!entityField.isCalculated()) {
			element.setAttribute(LayoutElementAttributes.A_ONCHECK, command(onChange(entityField)));
		}
	}
	
	private void visitCombobox(LayoutElement element) {
		final EntityField entityField = getEntityField(element);
		Assert.state(entityField.getType().isReference(), "field is not a reference field");
		element.setAttribute(LayoutElementAttributes.A_MODEL, load("vm.getReferenceValues('" + entityField.getUid() + "')"));
		element.setAttribute(LayoutElementAttributes.A_VALUE, load(identifier(propertyName(entityField))));
		element.setAttribute(LayoutElementAttributes.A_VISIBLE, load(isVisible(entityField)));
		element.setAttribute(LayoutElementAttributes.A_READONLY, load(isReadonly(entityField)));
		element.setAttribute(LayoutElementAttributes.A_MANDATORY, load(isMandatory(entityField)));
		element.setAttribute(LayoutElementAttributes.A_SELECTEDITEM, bind(propertyName(entityField)));
		element.setAttribute(LayoutElementAttributes.A_ONSELECT, command(onChange(entityField)));
		element.addChild(createTemplate(LayoutElementAttributes.A_MODEL, entityField.getInternalName()))
			   .addChild(createComboitem(load(identifier(entityField.getInternalName()))));
		
		final FormFieldExtra fieldExtra  = getFieldExtra(entityField);
		if (fieldExtra != null && fieldExtra.getDetailForm() != null) {
			element.setContext(newContextId());
			addToRoot(createReferencePopup(element.getContext(), element.getId()));
		}
	}
	
	private void visitBandbox(LayoutElement element) {
		final EntityField entityField = getEntityField(element);
		Assert.state(entityField.getType().isReference(), "field is not a reference field");
		element.setAttribute(LayoutElementAttributes.A_VALUE, load(identifier(propertyName(entityField))));
		element.setAttribute(LayoutElementAttributes.A_VISIBLE, load(isVisible(entityField)));
		element.setAttribute(LayoutElementAttributes.A_READONLY, load(isReadonly(entityField)));
		element.setAttribute(LayoutElementAttributes.A_MANDATORY, load(isMandatory(entityField)));
		element.setAttribute(LayoutElementAttributes.A_BUTTONVISIBLE, load('!' + isReadonly(entityField)));
		final LayoutElement elemListbox = element.addChild(createBandpopup())
												 .addChild(createListBox());
		elemListbox.setAttribute(LayoutElementAttributes.A_MODEL, load("vm.getReferenceListModel('" + entityField.getUid() + 
												"')) @template(empty each ? 'empty' : 'model'"));
		elemListbox.setAttribute(LayoutElementAttributes.A_SELECTEDITEM, bind(propertyName(entityField)));
		elemListbox.setAttribute(LayoutElementAttributes.A_ONSELECT, command(onChange(entityField)));
		elemListbox.setAttribute(LayoutElementAttributes.A_HEIGHT, "300px");
		elemListbox.setAttribute(LayoutElementAttributes.A_WIDTH, "350px");
		elemListbox.addChild(createListHead(true));
		elemListbox.addChild(createTemplate("empty", entityField.getReferenceEntity().getInternalName()))
		   		   .addChild(createListItem(null))		   
		   		   .addChild(createListCell('[' + getLabel("label.empty") + ']', null, null));
		elemListbox.addChild(createTemplate(LayoutElementAttributes.A_MODEL, entityField.getReferenceEntity().getInternalName()))
				   .addChild(createListItem(null))		   
				   .addChild(createListCell(load(identifier(entityField.getReferenceEntity().getInternalName())) , null, null));
		
		final FormFieldExtra fieldExtra = getFieldExtra(entityField);
		if (fieldExtra != null && fieldExtra.getDetailForm() != null) {
			element.setContext(newContextId());
			addToRoot(createReferencePopup(element.getContext(), element.getId()));
		}
	}
	
	private void createSubForm(LayoutElement element) {
		// create sub form
		final SubForm subForm = getSubForm(element);
		element.setAttribute(LayoutElementAttributes.A_VISIBLE, load("vm.isSubFormVisible('" + subForm.getNestedEntity().getUid() + "')"));
		final LayoutElement elemListBox = element.getChild(LayoutElement.CENTER).getChild(LayoutElement.LISTBOX);
		elemListBox.setAttribute(LayoutElementAttributes.A_MODEL, load("vm.object." + subForm.getNestedEntity().getInternalName()));
		elemListBox.setAttribute(LayoutElementAttributes.A_SELECTEDITEM, bind(selectedSubFormObject(subForm)));
		elemListBox.setAttribute(LayoutElementAttributes.A_ONSELECT, command("'selectSubFormObject'"));
		elemListBox.setAttribute("nonselectableTags", "");
		elemListBox.setAttribute(LayoutElementAttributes.A_AUTOPAGING, "true");
		elemListBox.setAttribute(LayoutElementAttributes.A_MOLD, "paging");
		final LayoutElement elemListitem = elemListBox.addChild(createTemplate(LayoutElementAttributes.A_MODEL, subForm.getNestedEntity().getInternalName()))
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
			elemToolbarButtonSelect.setAttribute(LayoutElementAttributes.A_VISIBLE, load("!empty " + selectedSubFormObject(subForm)));
			elemActionTemplate.addChild(elemToolbarButton);
			elemActionTemplateSelect.addChild(elemToolbarButtonSelect);
		}
		
		if (subForm.hasFields()) {
			final String nestedName = subForm.getNestedEntity().getInternalName();
			for (SubFormField subFormField : subForm.getFields()) {
				createSubFormField(subFormField, nestedName, elemListitem);
			}
		}
	}
	
	private void createSubFormField(SubFormField subFormField, String nestedName, LayoutElement elemListitem) {
		final SubForm subForm = subFormField.getSubForm();
		final EntityField nestedEntityField = subFormField.getEntityField();
		final String subFormPropertyName = nestedName + '.' + nestedEntityField.getInternalName();
		// bandbox
		if (subFormField.isBandbox()) {
			createSubFormBandboxField(subFormField, nestedName, subFormPropertyName, nestedEntityField, elemListitem);
			return;
		}
		
		final LayoutElement elemField = elemListitem.addChild(new LayoutElement(LayoutElement.LISTCELL))
													.addChild(createFormField(nestedEntityField));
		elemField.setContext(subFormContext(subForm, elemField));
		elemField.removeAttribute(LayoutElementAttributes.A_ID);
		switch (nestedEntityField.getType()) {
			case DATE:
			case DATETIME:
			case DECIMAL:
			case DOUBLE:
			case INTEGER:
			case LONG:
			case TEXT:
			case TEXTLONG:
				elemField.setAttribute(LayoutElementAttributes.A_VALUE, value(nestedEntityField, subFormPropertyName));
				elemField.setAttribute(LayoutElementAttributes.A_READONLY, load(isReadonly(nestedEntityField)));
				elemField.setAttribute(LayoutElementAttributes.A_MANDATORY, load(isMandatory(nestedEntityField)));
				if (!nestedEntityField.isCalculated()) {
					elemField.setAttribute(LayoutElementAttributes.A_INSTANT, "true");
					elemField.setAttribute(LayoutElementAttributes.A_ONCHANGE, command(onNestedChange(nestedName, nestedEntityField)));
				}
				break;
			
			case BINARY:
				elemField.removeAttribute(LayoutElementAttributes.A_HFLEX);
				elemField.setAttribute(LayoutElementAttributes.A_CONTENT, load(subFormPropertyName) + ' ' + 
												  converter("vm.imageConverter"));
				elemField.setAttribute(LayoutElementAttributes.A_WIDTH, subFormField.getWidth());
				elemField.setAttribute(LayoutElementAttributes.A_HEIGHT, subFormField.getHeight());
				if (!nestedEntityField.isCalculated()) {
					elemField.setOnClick(command("'editImage', fieldId='" + nestedEntityField.getUid() + 
														    "', nestedObject=" + nestedName));
				}
				break;
				
			case BOOLEAN:
				elemField.setAttribute(LayoutElementAttributes.A_CHECKED, nestedEntityField.isCalculated() 
																			? load(subFormPropertyName) 
																			: bind(subFormPropertyName));
				elemField.setAttribute(LayoutElementAttributes.A_DISABLED, load(isReadonly(nestedEntityField)));
				if (!nestedEntityField.isCalculated()) {
					elemField.setAttribute(LayoutElementAttributes.A_ONCHECK, command(onNestedChange(nestedName, nestedEntityField)));
				}
				break;
			
			case REFERENCE:
				elemField.setAttribute(LayoutElementAttributes.A_MODEL, load("vm.getNestedReferenceValues('" + 
																		subForm.getNestedEntity().getUid() + "','" + 
																		nestedEntityField.getUid() + "')"));
				elemField.setAttribute(LayoutElementAttributes.A_ONSELECT, command(onNestedChange(nestedName, nestedEntityField)));
				elemField.setAttribute(LayoutElementAttributes.A_SELECTEDITEM, bind(subFormPropertyName));
				elemField.setAttribute(LayoutElementAttributes.A_VALUE, load(identifier(subFormPropertyName)));
				elemField.setAttribute(LayoutElementAttributes.A_READONLY, load(isReadonly(nestedEntityField)));
				elemField.setAttribute(LayoutElementAttributes.A_MANDATORY, load(isMandatory(nestedEntityField)));
				elemField.addChild(createTemplate(LayoutElementAttributes.A_MODEL, nestedEntityField.getInternalName()))
						 .addChild(createComboitem(load(identifier(nestedEntityField.getInternalName()))));
	            if (subFormField.getDetailForm() != null) {
					addToRoot(createReferencePopup(elemField.getContext(), nestedEntityField.getUid()));
				}
				break;
				
			case FILE:
				elemField.setAttribute(LayoutElementAttributes.A_CONTENT, bind(subFormPropertyName + ".content"));
				elemField.setAttribute(LayoutElementAttributes.A_CONTENTTYPE, bind(subFormPropertyName + ".contentType"));
				elemField.setAttribute(LayoutElementAttributes.A_FILENAME, bind(subFormPropertyName + ".name"));
				elemField.setAttribute(LayoutElementAttributes.A_READONLY, load(isReadonly(nestedEntityField)));
				elemField.setAttribute(LayoutElementAttributes.A_MANDATORY, load(isMandatory(nestedEntityField)));
				elemField.setAttribute(LayoutElementAttributes.A_ONCHANGE, command(onNestedChange(nestedName, nestedEntityField)));
				break;
			
			default:
				throw new UnsupportedOperationException(nestedEntityField.getType().name());
		}
	}
	
	private void createSubFormBandboxField(SubFormField subFormField, String nestedName,String subFormPropertyName, 
										   EntityField nestedEntityField, LayoutElement elemListitem) {
		final LayoutElement elemField = elemListitem.addChild(new LayoutElement(LayoutElement.LISTCELL))
				.addChild(createBandbox(nestedEntityField));
		elemField.setContext(subFormContext(subFormField.getSubForm(), elemField));
		elemField.removeAttribute(LayoutElementAttributes.A_ID);
		elemField.setAttribute(LayoutElementAttributes.A_VALUE, load(identifier(subFormPropertyName))); 
		elemField.setAttribute(LayoutElementAttributes.A_READONLY, load(isReadonly(nestedEntityField)));
		elemField.setAttribute(LayoutElementAttributes.A_MANDATORY, load(isMandatory(nestedEntityField)));
		elemField.setAttribute(LayoutElementAttributes.A_BUTTONVISIBLE, load('!' + isReadonly(nestedEntityField)));

		final LayoutElement elemList = elemField.addChild(createBandpopup())
				.addChild(createListBox());
		elemList.setAttribute(LayoutElementAttributes.A_MODEL, load("vm.getNestedReferenceListModel('" + subFormField.getSubForm().getNestedEntity().getUid() + "','" + 
				nestedEntityField.getUid() + '\'') + ") @template(empty each ? 'empty' : 'model')");
		elemList.setAttribute(LayoutElementAttributes.A_SELECTEDITEM, bind(subFormPropertyName));
		elemList.setAttribute(LayoutElementAttributes.A_ONSELECT, command(onNestedChange(nestedName, nestedEntityField)));
		elemList.setAttribute(LayoutElementAttributes.A_HEIGHT, "300px");
		elemList.setAttribute(LayoutElementAttributes.A_WIDTH, "350px");
		elemList.addChild(createListHead(true));
		elemList.addChild(createTemplate("empty", nestedEntityField.getReferenceEntity().getInternalName()))
			.addChild(createListItem(null))		   
			.addChild(createListCell('[' + getLabel("label.empty") + ']', null, null));
		elemList.addChild(createTemplate(LayoutElementAttributes.A_MODEL, nestedEntityField.getReferenceEntity().getInternalName()))
			.addChild(createListItem(null))		   
			.addChild(createListCell(load(identifier(nestedEntityField.getReferenceEntity().getInternalName())), null, null));
		if (subFormField.getDetailForm() != null) {
			addToRoot(createReferencePopup(elemField.getContext(), nestedEntityField.getUid()));
		}
	}
	
	private static LayoutElement createReferencePopup(String context, String fieldUid) {
		final LayoutElement elemMenuItem = 
				createMenuItem("label.showreference", "z-icon-share alpha-icon-lg", 
							   "'showReference',fieldId='" + fieldUid + '\'');
		elemMenuItem.setAttribute(LayoutElementAttributes.A_DISABLED, load("vm.isReferenceEmpty('" + fieldUid + "')"));
		return createPopupMenu(context, Collections.singletonList(elemMenuItem));
	}
	
	private static String subFormContext(SubForm subForm, LayoutElement element) {
		return subForm.getNestedEntity().getId() + "_" + element.getId();
	}
	
	private static String value(EntityField entityField, String propertyName) {
		if (entityField.isCalculated()) {
			return load(propertyName);
		}
		final String value = bind(propertyName);
		if (entityField.isTextField()) {
			return value + ' ' + converter("vm.stringConverter");
		}
		return value;
	}
	
	private static String identifier(String name) {
		return "vm.getIdentifier(" + name + ')';
	}
	
	private static String propertyName(EntityField entityField) {
		return "vm.object." + entityField.getInternalName();
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
