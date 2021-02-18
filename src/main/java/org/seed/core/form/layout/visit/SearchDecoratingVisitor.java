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
import java.util.List;

import org.seed.core.entity.EntityField;
import org.seed.core.entity.filter.CriterionOperator;
import org.seed.core.form.Form;
import org.seed.core.form.SubForm;
import org.seed.core.form.layout.LayoutElement;
import org.springframework.util.Assert;

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
				element.removeAttribute("content");
				element.removeAttribute("contentType");
				element.removeAttribute("fileName");
				element.removeAttribute("onChange");
				element.setAttribute("disabled", "true");
				
			case LayoutElement.BANDBOX:
				element.removeAttribute("buttonVisible");
			case LayoutElement.CHECKBOX:
				element.removeAttribute("onCheck");
			case LayoutElement.COMBOBOX:
				element.removeAttribute("onSelect");
			
			case LayoutElement.DATEBOX:
			case LayoutElement.DECIMALBOX:
			case LayoutElement.DOUBLEBOX:
			case LayoutElement.IMAGE:
			case LayoutElement.INTBOX:
			case LayoutElement.LONGBOX:
			case LayoutElement.TEXTBOX:
				final String context = element.getContext();
				element.removeAttribute("instant");
				element.removeAttribute("onChange");
				element.removeAttribute("readonly");
				element.removeAttribute("mandatory");
				element.setContext(newContextId());
				addToRoot(createSearchFieldPopup(element, context));
		}
	}
	
	private LayoutElement createSearchFieldPopup(LayoutElement element, String context) {
		EntityField field = null;
		SubForm subForm = null;
		if (element.getId() != null) {
			field = form.getEntity().getFieldByUid(getId(element));
		}
		else { // sub form
			Assert.notNull(context, "context is null");
			final int idx = context.indexOf('_');
			final Long nestedEntityId = Long.parseLong(context.substring(0, idx));
			final String fieldUid = context.substring(idx + 1);
			subForm = form.getSubFormByNestedEntityId(nestedEntityId);
			field = subForm.getFieldByEntityFieldUid(fieldUid).getEntityField();
		}
		Assert.state(field != null, "field not available");
		
		final List<LayoutElement> items = new ArrayList<>();
		for (CriterionOperator operator : CriterionOperator.getOperators(field.getType())) {
			final LayoutElement elemMenuItem = new LayoutElement(LayoutElement.MENUITEM);
			elemMenuItem.setLabel(getEnumLabel(operator));
			elemMenuItem.setAttribute("autocheck", "true");
			elemMenuItem.setAttribute("checkmark", "true");
			if (subForm != null) {
				elemMenuItem.setAttribute("checked", load("vm.isCriterionChecked('" + operator.toString() + "','" + field.getUid() + "','" + subForm.getNestedEntity().getUid() + "')"));
				elemMenuItem.setAttribute("onCheck", command("'checkCriterion',elem=self,type='" + operator.toString() + "',fieldId='" + field.getUid() + "',nestedEntityId=" + subForm.getNestedEntity().getId()));
			}
			else {
				elemMenuItem.setAttribute("checked", load("vm.isCriterionChecked('" + operator.toString() + "','" + field.getUid() + "',null)"));
				elemMenuItem.setAttribute("onCheck", command("'checkCriterion',elem=self,type='" + operator.toString() + "',fieldId='" + field.getUid() + '\''));
			}
			items.add(elemMenuItem);
		}
		return createPopupMenu(element.getContext(), items);
	}
	
}
