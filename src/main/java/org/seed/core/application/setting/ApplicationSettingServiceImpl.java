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

import static org.seed.core.util.CollectionUtils.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.InternalException;
import org.seed.Seed;
import org.seed.core.config.UpdatableConfiguration;
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
	private ApplicationSettingValidator validator;
	
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
				final var appSettings = getCurrentSettings(settings, session);
				// save settings
				for (var entry : settings.entrySet()) {
					ApplicationSetting appSetting = appSettings.get(entry.getKey());
					if (entry.getValue() != null) {
						if (appSetting == null) {
							appSetting = new ApplicationSetting();
							appSetting.setSetting(entry.getKey());
						}
						appSetting.setValue(entry.getValue());
						validator.validateSave(appSetting);
						repository.save(appSetting, session);
					}
					else if (appSetting != null) {
						repository.delete(appSetting, session);
					}
				}
				// notify listeners
				changeAwareObjects.forEach(aware -> aware.notifyChange(null, session));
				tx.commit();
				resetSettingMap(settings);
			}
			catch (ValidationException vex) {
				throw vex;
			}
			catch (Exception ex) {
				if (tx != null) {
					tx.rollback();
				}
				throw new InternalException(ex);
			}
		}
		Seed.getBean(UpdatableConfiguration.class).updateConfiguration(true);
	}
	
	private Map<Setting, ApplicationSetting> getCurrentSettings(Map<Setting, String> settings, Session session) {
		final var appSettings = new EnumMap<Setting, ApplicationSetting>(Setting.class);
		for (ApplicationSetting setting : repository.find(session)) {
			// delete no longer existing setting
			if (!settings.containsKey(setting.getSetting())) {
				repository.delete(setting, session);
			}
			else {
				appSettings.put(setting.getSetting(), setting);
			}
		}
		return appSettings;
	}
	
	private void resetSettingMap(Map<Setting, String> settings) {
		this.getSettingMap().clear();
		filterAndForEach(settings.entrySet(), 
						 entry -> entry.getValue() != null, 
						 entry -> this.getSettingMap().put(entry.getKey(), entry.getValue()));
	}
	
	private Map<Setting, String> getSettingMap() {
		if (settingMap == null) {
			final var map = convertedMap(repository.find(), 
										 ApplicationSetting::getSetting, 
										 ApplicationSetting::getValue);
			map.computeIfAbsent(Setting.MENU_MODE, mode -> "NAVIGATION");
			settingMap = new ConcurrentHashMap<>(map);
		}
		return settingMap;
	}
	
}
