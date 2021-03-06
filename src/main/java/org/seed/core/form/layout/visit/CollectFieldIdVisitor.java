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

import org.seed.core.form.layout.LayoutElement;
import org.seed.core.form.layout.LayoutVisitor;

public class CollectFieldIdVisitor implements LayoutVisitor {
	
	private final List<String> fieldIdList = new ArrayList<>();

	public List<String> getFieldIdList() {
		return fieldIdList;
	}

	@Override
	public void visit(LayoutElement element) {
		switch (element.getName()) {
			case LayoutElement.BANDBOX:
			case LayoutElement.CHECKBOX:
			case LayoutElement.COMBOBOX:
			case LayoutElement.DATEBOX:
			case LayoutElement.DECIMALBOX:
			case LayoutElement.DOUBLEBOX:
			case LayoutElement.IMAGE:
			case LayoutElement.INTBOX:
			case LayoutElement.LONGBOX:
			case LayoutElement.TEXTBOX:
			case LayoutElement.FILEBOX:
				if (element.getId() != null) {
					fieldIdList.add(element.getId());
				}
		}
	}

}
