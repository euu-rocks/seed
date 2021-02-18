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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seed.core.form.FormField;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public class ListFormSettings {
	
	private Long formId;
	
	private List<ColumnSetting> columnSettings;
	
	public Long getFormId() {
		return formId;
	}

	public void setFormId(Long formId) {
		this.formId = formId;
	}

	public List<ColumnSetting> getColumnSettings() {
		return columnSettings;
	}

	public void setColumnSetting(Long fieldId, boolean hidden) {
		Assert.notNull(fieldId, "fieldId is null");
		
		if (columnSettings == null) {
			columnSettings = new ArrayList<>();
		}
		final ColumnSetting setting = new ColumnSetting();
		setting.setFieldId(fieldId);
		setting.setHidden(hidden);
		columnSettings.add(setting);
	}
	
	public boolean hasColumnSettings() {
		return !ObjectUtils.isEmpty(columnSettings);
	}
	
	public void clearColumnSettings() {
		if (columnSettings != null) {
			columnSettings.clear();
		}
	}
	
	public ColumnSetting getColumnSetting(Long fieldId) {
		Assert.notNull(fieldId, "fieldId is null");
		
		if (columnSettings != null) {
			for (ColumnSetting setting : columnSettings) {
				if (fieldId.equals(setting.getFieldId())) {
					return setting;
				}
			}
		}
		return null;
	}
	
	public void sortFields(List<FormField> fields) {
		Assert.notNull(fields, "fields is null");
		
		if (columnSettings != null) {
			final Map<Long,FormField> fieldMap = new HashMap<>();
			fields.forEach(f -> fieldMap.put(f.getId(), f));
			fields.clear();
			for (ColumnSetting setting : columnSettings) {
				final FormField field = fieldMap.get(setting.getFieldId());
				if (field != null) {
					fields.add(field);
				}
			}
		}
	}
	
}
