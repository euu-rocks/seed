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
import java.util.HashMap;
import java.util.Map;

import javax.persistence.OptimisticLockException;

import org.seed.core.application.setting.ApplicationSettingService;
import org.seed.core.application.setting.Setting;
import org.seed.core.data.ValidationException;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;

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
		
		public void setBooleanValue(boolean b) {
			setValue(b ? "true" : "false");
		}
	}
	
	private final Map<Setting, SettingAdapter> adapterMap = Collections.synchronizedMap(new HashMap<>());
	
	@WireVariable(value="applicationSettingServiceImpl")
	private ApplicationSettingService settingService;
	
	private Map<Setting, String> settings;
	
	public SettingAdapter getSetting(String settingName) {
		final Setting setting = Setting.valueOf(settingName.toUpperCase());
		SettingAdapter adapter = adapterMap.get(setting);
		if (adapter == null) {
			adapter = new SettingAdapter(setting);
			adapterMap.put(setting, adapter);
		}
		return adapter;
	}
	
	@Init
	public void init() {
		settings = settingService.getSettings();
	}
	
	@Command
	public void saveSettings(@BindingParam("elem") Component component) {
		try {
			settingService.saveSettings(settings);
			showNotification(component, false, "settings.savesuccess");
			resetDirty();
			refreshMenu();
		}
		catch (ValidationException vex) {
			showValidationErrors(component, "settings.savefail", vex.getErrors());
		}
		catch (OptimisticLockException olex) {
			final String errMsgKey = "settings.failstale";
			showError(component, errMsgKey);
		}
	}
	
	@Command
	public void refreshSettings() {
		init();
		resetDirty();
		notifyChangeAll();
	}
	
	@Command
	public void flagDirty() {
		super.flagDirty();
	}
	
}
