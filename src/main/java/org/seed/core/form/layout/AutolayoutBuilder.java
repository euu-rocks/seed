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

import static org.seed.core.form.layout.LayoutElementAttributes.*;
import static org.seed.core.util.CollectionUtils.subList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.seed.C;
import org.seed.core.data.Order;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityRelation;
import org.seed.core.form.AutolayoutType;
import org.seed.core.form.Form;
import org.seed.core.form.SubForm;
import org.seed.core.util.Assert;
import org.seed.core.util.NameUtils;

class AutolayoutBuilder extends LayoutUtils {
	
	private final List<EntityFieldGroup> fieldGroups = new ArrayList<>();
	
	private final List<EntityField> fieldsWithoutGroup = new ArrayList<>();
	
	private final Entity entity;
	
	private final Form form;
	
	AutolayoutBuilder(Entity entity, Form form) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(form, C.FORM);
		
		this.entity = entity;
		this.form = form;
		
		final Set<EntityFieldGroup> usedFieldGroups = new HashSet<>();
		for (EntityField entityField : entity.getAllFields()) {
			if (entityField.getFieldGroup() != null) {
				usedFieldGroups.add(entity.getFieldGroupById(entityField.getFieldGroup().getId()));
			}
			else {
				fieldsWithoutGroup.add(entityField);
			}
		}
		fieldGroups.addAll(usedFieldGroups);
		Order.sort(fieldGroups);
	}
	
	LayoutElement build() {
		final AutolayoutType type = form.getAutolayoutType() != null 
										? form.getAutolayoutType() 
										: AutolayoutType.defaultType();
		int numFieldGroups = fieldGroups.size();
		if (!fieldsWithoutGroup.isEmpty()) {
			numFieldGroups++;
		}
		LayoutElement elemMainGrid = null;
		switch (type) {
			case SINGLE_COLUMN:
				elemMainGrid = createSingleColumnLayout(numFieldGroups);
				break;
				
			case TWO_COLUMNS_HORIZONTAL:
				elemMainGrid = createHorizontalLayout(2, numFieldGroups);
				break;
				
			case TWO_COLUMNS_VERTICAL:
				elemMainGrid = createVerticalLayout(2, numFieldGroups);
				break;
				
			case THREE_COLUMNS_HORIZONTAL:
				elemMainGrid = createHorizontalLayout(3, numFieldGroups);
				break;
				
			case THREE_COLUMNS_VERTICAL:
				elemMainGrid =  createVerticalLayout(3, numFieldGroups);
				break;
				
			default:
				throw new UnsupportedOperationException(type.name());
		}
		final LayoutElement elemZK = createZK();
		if (entity.hasNesteds() || entity.hasAllRelations()) {
			elemZK.addChild(buildSubForms(elemMainGrid));
		}
		else {
			elemZK.addChild(elemMainGrid);
		}
		return elemZK;
	}
	
	private LayoutElement buildSubForms(LayoutElement elemMainGrid) {
		// main grid
		final LayoutElement elemLayout = createBorderLayout();
		elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.NORTH)).addChild(elemMainGrid);
		// tabs
		final LayoutElement elemTabbox = new LayoutElement(LayoutElement.TABBOX);
		elemTabbox.setAttribute(A_HFLEX, V_1)
				  .setAttribute(A_VFLEX, V_1)
				  .setClass(LayoutElementClass.TABBOX);
		elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.CENTER)).addChild(elemTabbox);
		final LayoutElement elemTabs = elemTabbox.addChild(createTabs());
		final LayoutElement elemPanels = elemTabbox.addChild(createTabpanels());
		// sub forms
		if (form.hasSubForms()) {
			for (SubForm subForm : form.getSubForms()) {
				elemTabs.addChild(createTab(subForm.getNestedEntity().getName()));
				LayoutServiceImpl.buildSubForm(subForm, elemPanels.addChild(createTabpanel(subForm.getNestedEntity().getName())));
			}
		}
		// relations
		for (EntityRelation relation : entity.getAllRelations()) {
			elemTabs.addChild(createTab(relation.getName()));
			LayoutServiceImpl.buildRelationForm(relation, elemPanels.addChild(createTabpanel(relation.getName())));
		}
		return elemLayout;
	}
	
	private LayoutElement createSingleColumnLayout(int numFieldGroups) {
		final LayoutElement grid = createGrid(1, numFieldGroups, null);
		int row = 0;
		// fields without group
		if (!fieldsWithoutGroup.isEmpty()) {
			grid.getGridCell(0, row++)
				.setValign(V_TOP)
				.addChild(buildFieldGrid(fieldsWithoutGroup, entity.getName()));
		}
		// field groups
		if (!fieldGroups.isEmpty()) {
			for (EntityFieldGroup fieldGroup : fieldGroups) {
				grid.getGridCell(0, row++)
					.setValign(V_TOP)
					.addChild(buildFieldGrid(entity.getAllFieldsByGroup(fieldGroup), 
											 fieldGroup.getName()));
			}
		}
		return grid;
	}
	
	private LayoutElement createHorizontalLayout(int columns, int numFieldGroups) {
		final int numGridRows = numFieldGroups / columns + numFieldGroups % columns;
		final LayoutElement grid = createGrid(columns, numGridRows, null);
		int col = 0;
		int row = 0;
		// fields without group
		if (!fieldsWithoutGroup.isEmpty()) {
			grid.getGridCell(col++, 0)
				.setValign(V_TOP)
				.addChild(buildFieldGrid(fieldsWithoutGroup, entity.getName()));
		}
		// field groups
		if (!fieldGroups.isEmpty()) {
			for (EntityFieldGroup fieldGroup : fieldGroups) {
				grid.getGridCell(col, row)
					.setValign(V_TOP)
					.addChild(buildFieldGrid(entity.getAllFieldsByGroup(fieldGroup), 
											 fieldGroup.getName()));
				if (++col == columns) {
					col = 0;
					row++;
				}
			}
		}
		return grid;
	}
	
	private LayoutElement createVerticalLayout(int columns, int numFieldGroups) {
		final LayoutElement mainGrid = createGrid(columns, 1, null);
		int numColumnGroups = numFieldGroups / columns;
		if (numColumnGroups * columns < numFieldGroups) {
			numColumnGroups++;
		}
		int idx = 0;
		for (int col = 0; col < columns; col++) {
			final LayoutElement colGrid = createGrid(1, numColumnGroups, null);
			mainGrid.getGridCell(col, 0)
					.setValign(V_TOP)
					.addChild(colGrid);
			int row = 0;
			if (col == 0 && !fieldsWithoutGroup.isEmpty()) {
				colGrid.getGridCell(0, row++)
					   .setValign(V_TOP)
					   .addChild(buildFieldGrid(fieldsWithoutGroup, entity.getName()));
			}
			while (idx < fieldGroups.size() && row < numColumnGroups) {
				final EntityFieldGroup fieldGroup = fieldGroups.get(idx);
				colGrid.getGridCell(0, row++)
					   .setValign(V_TOP)
					   .addChild(buildFieldGrid(entity.getAllFieldsByGroup(fieldGroup), 
							   					fieldGroup.getName()));
				idx++;
			}
		}
		return mainGrid;
	}
	
	private static LayoutElement buildFieldGrid(List<EntityField> fields, String name) {
		Assert.notNull(fields, "fields");
		Assert.notNull(name, C.NAME);
		
		final LayoutElement elemGrid = new LayoutElement(LayoutElement.GRID);
		final LayoutElement elemRows = elemGrid.addChild(createRows());
		final LayoutElement elemColumns = createColumns(2);
		elemColumns.getChildAt(0).setAttribute(A_HFLEX, V_MIN);
		elemGrid.setClass(LayoutElementClass.NO_BORDER).addChild(elemColumns);
		for (EntityField entityField : subList(fields, field -> !field.getType().isBinary())) {
			final LayoutElement elemRow = elemRows.addChild(new LayoutElement(LayoutElement.ROW));
			final String testClass = NameUtils.getInternalName(entityField.getName().trim())
												.replace('_', '-').toLowerCase();
			// label column
			LayoutElement elemCell = elemRow.addChild(createCell());
			elemCell.setAlign(V_RIGHT)
					.setValign(V_TOP)
					.setAttribute(A_SCLASS, testClass + "-labelcell")
					.addChild(createLabel(entityField.getName()));
			// field column
			elemCell = elemRow.addChild(createCell());
			elemCell.setAttribute(A_SCLASS, testClass + "-fieldcell")
					.addChild(createFormField(entityField));
		}
		return createGroupbox(name, elemGrid);
	}
	
}
