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
package org.seed.ui.zk.vm.admin;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import javax.persistence.OptimisticLockException;

import org.seed.C;
import org.seed.core.application.setting.Setting;
import org.seed.core.data.ValidationException;
import org.seed.core.rest.RestHelper;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;

public class AdminSettingViewModel extends AbstractApplicationViewModel {
	
	public class SettingAdapter {
		
		private final Setting setting;
		
		private SettingAdapter(Setting setting) {
			this.setting = setting;
		}

		public String getValue() {
			return settings.get(setting);
		}
		
		public boolean getBooleanValue() {
			return "true".equals(getValue());
		}
		
		public void setValue(String value) {
			settings.put(setting, value);
			notifyChange("getSetting");
		}
		
		public void setBooleanValue(boolean bool) {
			setValue(bool ? "true" : "false");
		}
	}
	
	private final Map<Setting, SettingAdapter> adapterMap = 
		Collections.synchronizedMap(new EnumMap<>(Setting.class));
	
	private Map<Setting, String> settings;
	
	public SettingAdapter getSetting(String settingName) {
		final Setting setting = Setting.valueOf(settingName.toUpperCase());
		adapterMap.computeIfAbsent(setting, s -> new SettingAdapter(setting));
		return adapterMap.get(setting);
	}
	
	public String getDefaultRestDateFormat() {
		return RestHelper.DEFAULT_REST_FORMAT_DATE;
	}
	
	public String getDefaultRestDateTimeFormat() {
		return RestHelper.DEFAULT_REST_FORMAT_DATETIME;
	}
	
	@Init
	public void init() {
		settings = settingService.getSettings();
	}
	
	@Command
	public void saveSettings(@BindingParam(C.ELEM) Component component) {
		try {
			settingService.saveSettings(settings);
			showNotification(component, false, "settings.savesuccess");
			resetCurrentSession();
			resetDirty();
			refreshMenu();
		}
		catch (ValidationException vex) {
			showValidationErrors(component, "settings.savefail", vex.getErrors());
		}
		catch (OptimisticLockException olex) {
			showError(component, "settings.failstale");
		}
	}
	
	@Command
	public void refreshSettings() {
		init();
		resetDirty();
		notifyChangeAll();
	}
	
	@Command
	@Override
	public void flagDirty() {
		super.flagDirty();
	}
	
}
