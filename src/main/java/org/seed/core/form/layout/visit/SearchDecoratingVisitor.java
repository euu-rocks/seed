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
import java.util.List;

import org.seed.C;
import org.seed.Seed;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.filter.CriterionOperator;
import org.seed.core.form.Form;
import org.seed.core.form.SubForm;
import org.seed.core.form.layout.LayoutElement;
import org.seed.core.util.Assert;

public class SearchDecoratingVisitor extends AbstractLayoutVisitor {
	
	public SearchDecoratingVisitor(Form form) {
		super(form);
	}

	@Override
	public void visit(LayoutElement element) {
		switch (element.getName()) {
			case LayoutElement.ZK:
				setRootElement(element);
				break;
			
			case LayoutElement.FILEBOX:
				element.removeAttribute(A_CONTENT);
				element.removeAttribute(A_CONTENTTYPE);
				element.removeAttribute(A_FILENAME);
				element.setAttribute(A_DISABLED, V_TRUE);
				createPopup(element);
				break;
				
			case LayoutElement.BANDBOX:
				element.removeAttribute(A_BUTTONVISIBLE);
				createPopup(element);
				break;
				
			case LayoutElement.CHECKBOX:
				element.removeAttribute(A_DISABLED);
				element.removeAttribute(A_ONCHECK);
				createPopup(element);
				break;
				
			case LayoutElement.COMBOBOX:
				element.removeAttribute(A_ONSELECT);
				createPopup(element);
				break;
			
			case LayoutElement.DATEBOX:
			case LayoutElement.DECIMALBOX:
			case LayoutElement.DOUBLEBOX:
			case LayoutElement.IMAGE:
			case LayoutElement.INTBOX:
			case LayoutElement.LONGBOX:
				createPopup(element);
				break;
				
			case LayoutElement.TEXTBOX:
				element.removeAttribute(A_INSTANT);
				createPopup(element);
				break;
			
			default:
				// do nothing
				break;
		}
	}
	
	private void createPopup(LayoutElement element) {
		element.removeAttribute(A_READONLY);
		element.removeAttribute(A_MANDATORY);
		element.removeAttribute(A_ONCHANGE);
		addRootChild(createSearchFieldPopup(element));
	}
	
	private LayoutElement createSearchFieldPopup(LayoutElement element) {
		final String context = element.getContext();
		EntityField field = null;
		SubForm subForm = null;
		if (element.getId() != null) {
			element.setContext(newContextId());
			field = getForm().getEntity().getFieldByUid(getElementId(element));
		}
		else { // sub form
			Assert.notNull(context, C.CONTEXT);
			final int idx = context.indexOf('_');
			final String nestedEntityUid = context.substring(0, idx);
			final String fieldUid = context.substring(idx + 1);
			subForm = getForm().getSubFormByNestedEntityUid(nestedEntityUid);
			field = subForm.getFieldByEntityFieldUid(fieldUid).getEntityField();
		}
		Assert.stateAvailable(field, C.FIELD);
		
		final List<LayoutElement> items = new ArrayList<>();
		for (CriterionOperator operator : CriterionOperator.getOperators(field.getType())) {
			final LayoutElement elemMenuItem = new LayoutElement(LayoutElement.MENUITEM);
			elemMenuItem.setLabel(Seed.getEnumLabel(operator));
			elemMenuItem.setAttribute("autocheck", V_TRUE);
			elemMenuItem.setAttribute("checkmark", V_TRUE);
			if (subForm != null) {
				elemMenuItem.setAttribute(A_CHECKED, load("vm.isCriterionChecked('" + operator.toString() + "','" + field.getUid() + "','" + subForm.getNestedEntity().getUid() + "')"));
				elemMenuItem.setAttribute(A_ONCHECK, command("'checkCriterion',elem=self,type='" + operator.toString() + "',fieldId='" + field.getUid() + "',nestedEntityId=" + subForm.getNestedEntity().getId()));
			}
			else {
				elemMenuItem.setAttribute(A_CHECKED, load("vm.isCriterionChecked('" + operator.toString() + "','" + field.getUid() + "',null)"));
				elemMenuItem.setAttribute(A_ONCHECK, command("'checkCriterion',elem=self,type='" + operator.toString() + "',fieldId='" + field.getUid() + '\''));
			}
			items.add(elemMenuItem);
		}
		return createPopupMenu(element.getContext(), items);
	}
	
}
