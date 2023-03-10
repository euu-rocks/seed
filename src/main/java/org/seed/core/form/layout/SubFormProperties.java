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

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.seed.C;
import org.seed.core.data.AbstractSystemObject;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.form.Form;
import org.seed.core.form.FormActionType;
import org.seed.core.form.SubForm;
import org.seed.core.form.SubFormAction;
import org.seed.core.form.SubFormField;
import org.seed.core.util.Assert;

public class SubFormProperties {
	
	public final SubForm subForm;
	
	private final List<SubFormColumn> columns;
	
	private final List<SubFormAction> actions = new ArrayList<>();
	
	public SubFormProperties(SubForm subForm) {
		Assert.notNull(subForm, C.SUBFORM);
		
		this.subForm = subForm;
		columns = convertedList(subForm.getFields(), SubFormColumn::new);
		if (subForm.hasActions()) {
			actions.addAll(subForm.getActions());
		}
	}
	
	public String getNestedEntityName() {
		return subForm.getNestedEntity().getName();
	}
	
	public List<SubFormColumn> getColumns() {
		return columns;
	}
	
	public List<SubFormAction> getActions() {
		return actions;
	}

	public void applyToSubForm() {
		if (subForm.hasFields()) {
			subForm.getFields().clear();
		}
		if (subForm.hasActions()) {
			subForm.getActions().clear();
		}
		for (SubFormColumn column : columns) {
			column.applyToField();
			subForm.addField(column.subFormField);
		}
		for (SubFormAction action : actions) {
			subForm.addAction(action);
		}
	}
	
	public List<SubFormColumn> getAvailableColumns() {
		return filterAndConvert(subForm.getNestedEntity().getFields(true), 
								not(this::existColumn), 
								this::createSubFormColumn);
	}
	
	public List<SubFormAction> getAvailableActions() {
		return filterAndConvert(FormActionType.values(), 
								actionType -> actionType.isVisibleAtSubform && 
											  actionType != FormActionType.CUSTOM &&
											  subForm.getActionByType(actions, actionType) == null, 
								this::createSubFormAction);
	}
	
	public SubFormAction createCustomAction() {
		final SubFormAction action = new SubFormAction();
		action.setSubForm(subForm);
		action.setType(FormActionType.CUSTOM);
		actions.add(action);
		return action;
	}
	
	private SubFormAction createSubFormAction(FormActionType actionType) {
		final SubFormAction action = new SubFormAction();
		action.setSubForm(subForm);
		action.setType(actionType);
		return action;
	}

	private SubFormColumn createSubFormColumn(EntityField entityField) {
		final SubFormField field = new SubFormField();
		field.setSubForm(subForm);
		field.setEntityField(entityField);
		return new SubFormColumn(field);
	}
	
	private boolean existColumn(EntityField entityField) {
		return anyMatch(columns, column -> column.subFormField.getEntityField().equals(entityField));
	}
	
	public static class SubFormColumn extends AbstractSystemObject {
		
		public final SubFormField subFormField;
		
		private Transformer transformer;
		
		private Form detailForm;
		
		private Filter filter;
		
		private String label;
		
		private String width;
		
		private String height;
		
		private String style;
		
		private String labelStyle;
		
		private boolean readonly;
		
		private boolean bandbox;
		
		private SubFormColumn(SubFormField subFormField) {
			Assert.notNull(subFormField, "subFormField");
			
			this.subFormField = subFormField;
			transformer = subFormField.getTransformer();
			detailForm = subFormField.getDetailForm();
			filter = subFormField.getFilter();
			label = subFormField.getLabel();
			width = subFormField.getWidth();
			height = subFormField.getHeight();
			style = subFormField.getStyle();
			labelStyle = subFormField.getLabel();
			readonly = subFormField.isReadonly();
			bandbox = subFormField.isBandbox();
		}

		public Transformer getTransformer() {
			return transformer;
		}

		public void setTransformer(Transformer transformer) {
			this.transformer = transformer;
		}

		public Form getDetailForm() {
			return detailForm;
		}

		public void setDetailForm(Form detailForm) {
			this.detailForm = detailForm;
		}

		public Filter getFilter() {
			return filter;
		}

		public void setFilter(Filter filter) {
			this.filter = filter;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getWidth() {
			return width;
		}

		public void setWidth(String width) {
			this.width = width;
		}

		public String getHeight() {
			return height;
		}

		public void setHeight(String height) {
			this.height = height;
		}

		public String getStyle() {
			return style;
		}

		public void setStyle(String style) {
			this.style = style;
		}

		public String getLabelStyle() {
			return labelStyle;
		}

		public void setLabelStyle(String labelStyle) {
			this.labelStyle = labelStyle;
		}
		
		public boolean isReadonly() {
			return readonly;
		}

		public void setReadonly(boolean readonly) {
			this.readonly = readonly;
		}

		public boolean isBandbox() {
			return bandbox;
		}

		public void setBandbox(boolean bandbox) {
			this.bandbox = bandbox;
		}

		public EntityField getEntityField() {
			return subFormField.getEntityField();
		}

		private void applyToField() {
			subFormField.setTransformer(transformer);
			subFormField.setDetailForm(detailForm);
			subFormField.setFilter(filter);
			subFormField.setLabel(label);
			subFormField.setWidth(width);
			subFormField.setHeight(height);
			subFormField.setStyle(style);
			subFormField.setLabelStyle(labelStyle);
			subFormField.setReadonly(readonly);
			subFormField.setBandbox(bandbox);
		}
	}
}
