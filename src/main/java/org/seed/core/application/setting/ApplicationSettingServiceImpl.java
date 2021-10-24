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
package org.seed.core.application.setting;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class ApplicationSettingServiceImpl implements ApplicationSettingService {
	
	@Autowired
	private ApplicationSettingRepository repository;
	
	@Autowired
	private List<SettingChangeAware> changeAwareObjects;
	
	private Map<Setting, String> settingMap;
	
	@Override
	public boolean hasSetting(Setting setting) {
		return !ObjectUtils.isEmpty(getSettingMap().get(setting));
	}
	
	@Override
	public boolean hasMailSettings() {
		return hasSetting(Setting.MAIL_SERVER_HOST) && 
			   hasSetting(Setting.MAIL_SERVER_PORT);
	}
	
	@Override
	public Map<Setting, String> getSettings() {
		return new EnumMap<>(getSettingMap());
	}
	
	@Override
	public String getSetting(Setting setting) {
		final String value = getSettingOrNull(setting);
		Assert.stateAvailable(value, "value for setting " + setting);
		return value;
	}
	
	@Override
	public String getSettingOrNull(Setting setting) {
		Assert.notNull(setting, C.SETTING);
		
		return getSettingMap().get(setting);
	}
	
	@Override
	public int getIntSetting(Setting setting) {
		final String value = getSetting(setting);
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException nfex) {
			throw new IllegalStateException("setting " + setting + " illegal integer: " + value);
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_SETTINGS")
	public void setSetting(Setting setting, String value) {
		Assert.notNull(setting, C.SETTING);
		
		getSettingMap().put(setting, value);
	}
	
	@Override
	@Secured("ROLE_ADMIN_SETTINGS")
	public void saveSettings(Map<Setting, String> settings) throws ValidationException {
		Assert.notNull(settings, C.SETTING);
		
		try (Session session = repository.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				// read current settings
				final Map<Setting, ApplicationSetting> appSettings = new EnumMap<>(Setting.class);
				for (ApplicationSetting setting : repository.find(session)) {
					// and delete no longer existing ones
					if (!settings.containsKey(setting.getSetting())) {
						repository.delete(setting, session);
					}
					else {
						appSettings.put(setting.getSetting(), setting);
					}
				}
				// save settings
				for (Map.Entry<Setting, String> entry : settings.entrySet()) {
					ApplicationSetting appSetting = appSettings.get(entry.getKey());
					if (entry.getValue() != null) {
						if (appSetting == null) {
							appSetting = new ApplicationSetting();
							appSetting.setSetting(entry.getKey());
						}
						appSetting.setValue(entry.getValue());
						repository.save(appSetting, session);
					}
					else if (appSetting != null) {
						repository.delete(appSetting, session);
					}
				}
				// notify listeners
				for (SettingChangeAware changeAware : changeAwareObjects) {
					changeAware.notifyChange(null, session);
				}
				
				tx.commit();
				
				this.getSettingMap().clear();
				this.getSettingMap().putAll(settings);
			}
			catch (Exception ex) {
				if (tx != null) {
					tx.rollback();
				}
				throw new InternalException(ex);
			}
		}
	}
	
	private Map<Setting, String> getSettingMap() {
		if (settingMap == null) {
			final Map<Setting, String> map = new EnumMap<>(Setting.class);
			for (ApplicationSetting setting : repository.find()) {
				map.put(setting.getSetting(), setting.getValue());
			}
			if (!map.containsKey(Setting.MENU_MODE)) {
				map.put(Setting.MENU_MODE, "NAVIGATION");
			}
			settingMap = new ConcurrentHashMap<>(map);
		}
		return settingMap;
	}
	
}
