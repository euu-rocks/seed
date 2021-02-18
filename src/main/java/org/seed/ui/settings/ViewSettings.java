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
package org.seed.ui.settings;

import java.util.ArrayList;
import java.util.List;

import org.seed.core.form.FormField;
import org.seed.core.form.FormSettings;

import org.springframework.util.Assert;

public class ViewSettings implements FormSettings {
	
	private List<ListFormSettings> listFormSettings;
	
	public ListFormSettings getListFormSettings(Long formId) {
		Assert.notNull(formId, "formId is null");
		
		if (listFormSettings == null) {
			listFormSettings = new ArrayList<>();
		}
		for (ListFormSettings settings : listFormSettings) {
			if (formId.equals(settings.getFormId())) {
				return settings;
			}
		}
		final ListFormSettings settings = new ListFormSettings();
		settings.setFormId(formId);
		listFormSettings.add(settings);
		return settings;
	}
	
	@Override
	public boolean isFormFieldVisible(FormField formField) {
		Assert.notNull(formField, "formField is null");
		
		final ListFormSettings listSettings = getListFormSettings(formField.getForm().getId());
		final ColumnSetting columnSetting = listSettings.getColumnSetting(formField.getId());
		if (columnSetting != null) {
			return !columnSetting.isHidden();
		}
		return true;
	}
	
	@Override
	public void sortFields(List<FormField> fields) {
		Assert.notNull(fields, "fields is null");
		
		if (fields.size() > 1) {
			final ListFormSettings listSettings = getListFormSettings(fields.get(0).getForm().getId());
			listSettings.sortFields(fields);
		}
	}
	
}
