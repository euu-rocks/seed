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
package org.seed.ui.zk.vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.seed.C;
import org.seed.core.data.ValidationError;
import org.seed.core.form.Form;
import org.seed.core.form.FormField;
import org.seed.core.util.Assert;
import org.seed.ui.DragDropListManager;
import org.seed.ui.settings.ColumnSetting;
import org.seed.ui.settings.ListFormSettings;
import org.seed.ui.zk.ViewUtils;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Window;

public class SelectColumnsViewModel extends AbstractViewModel {
	
	private static final int LIST_AVAILABLE = 0;
	private static final int LIST_SELECTED  = 1;
	
	@Wire("#selectColumnsWin")
	private Window window;
	
	private final DragDropListManager listManager = new DragDropListManager();
	
	private AbstractFormViewModel parentVM;
	
	private ListFormSettings listSettings;
	
	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam(C.PARAM) AbstractFormViewModel param) {
		Assert.notNull(param, C.PARAM);
		
		parentVM = param;
		final Form form = parentVM.getForm();
		listSettings = ViewUtils.getSettings().getListFormSettings(form.getId());
		listSettings.sortFields(form.getFields());
		
		final List<FormField> availableFields = new ArrayList<>();
		final List<FormField> selectedFields = new ArrayList<>();
		if (form.hasFields()) {
			for (FormField field : form.getFields()) {
				final ColumnSetting columnSetting = listSettings.getColumnSetting(field.getId());
				boolean selected = field.isSelected();
				if (columnSetting != null) {
					selected = !columnSetting.isHidden();
				}
				if (selected) {
					selectedFields.add(field);
				}
				else {
					availableFields.add(field);
				}
			}
		}
		listManager.getList(LIST_AVAILABLE).addAll(availableFields);
		listManager.getList(LIST_SELECTED).addAll(selectedFields);
		
		wireComponents(view);
	}
	
	public List<FormField> getAvailableFields() {
		return getFields(LIST_AVAILABLE);
	}
	
	public List<FormField> getSelectedFields() {
		return getFields(LIST_SELECTED);
	}
	
	@Command
	@NotifyChange({"availableFields", "selectedFields"})
	public void dropToList(@BindingParam(C.ITEM) FormField field, 
						   @BindingParam(C.LIST) int listNum) {
		Assert.notNull(field, C.FIELD);
		
		listManager.drop(field, listNum);
	}
	
	@Command
	@NotifyChange({"availableFields", "selectedFields"})
	public void insertToList(@BindingParam(C.BASE) FormField base, 
							 @BindingParam(C.ITEM) FormField field, 
							 @BindingParam(C.LIST) int listNum) {
		Assert.notNull(base, C.BASE);
		Assert.notNull(field, C.FIELD);
		
		listManager.insert(base, field, listNum);
	}
	
	@Command
	public void selectColumns(@BindingParam(C.ELEM) Component elem) {
		// at least one column must be selected
		final List<FormField> selectedFields = getFields(LIST_SELECTED);
		if (selectedFields.isEmpty()) {
			final ValidationError error = new ValidationError("val.empty.selection");
			showValidationErrors(elem, "form.action.selectfail", Collections.singleton(error));
			return;
		}
		
		// set column settings
		final List<FormField> availableFields = getFields(LIST_AVAILABLE);
		listSettings.clearColumnSettings();
		selectedFields.forEach(field -> listSettings.setColumnSetting(field.getId(), false));
		availableFields.forEach(field -> listSettings.setColumnSetting(field.getId(), true));
		
		parentVM.notifyChange("layoutInclude");
		window.detach();
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
	@SuppressWarnings("unchecked")
	private List<FormField> getFields(int listNum) {
		final List<?> result = listManager.getList(listNum);
		return (List<FormField>) result;
	}
	
}
