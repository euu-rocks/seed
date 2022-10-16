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
package org.seed.ui.zk;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.seed.C;
import org.seed.LabelProvider;
import org.seed.core.application.setting.ApplicationSettingService;
import org.seed.core.application.setting.Setting;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zkoss.math.BigDecimals;
import org.zkoss.text.DateFormats;
import org.zkoss.util.Locales;
import org.zkoss.util.resource.Labels;

@Component
public class ZKLabelProvider implements LabelProvider {
	
	@Autowired
	private ApplicationSettingService settingService;
	
	@Override
	public String getLabel(String key, String ...params) {
		Assert.notNull(key, C.KEY);
		
		return params != null 
				? Labels.getLabel(key, params) 
				: Labels.getLabel(key);
	}

	@Override
	public String getEnumLabel(Enum<?> enm) {
		return enm != null 
				? Labels.getLabel(getEnumLabelKey(enm)) 
				: null;
	}
	
	@Override
	public String formatBoolean(Boolean bool) {
		return bool != null
				? Labels.getLabel(getBooleanKey(bool))
				: emptyString();
	}
	
	@Override
	public String formatDate(Date date) {
		return date != null
				? dateFormat().format(date)
				: emptyString();
	}
	
	@Override
	public String formatDateTime(Date date) {
		return date != null
				? dateTimeFormat().format(date)
				: emptyString();
	}
	
	@Override
	public String formatTime(Date time) {
		return time != null
				? timeFormat().format(time)
				: emptyString();
	}
	
	@Override
	public String formatBigDecimal(BigDecimal decimal) {
		return decimal != null
				? BigDecimals.toLocaleString(decimal, Locales.getCurrent())
				: emptyString();
	}
	
	private DateFormat dateFormat() {
		return applyTimeZone(new SimpleDateFormat(
				DateFormats.getDateFormat(DateFormat.DEFAULT, Locales.getCurrent(), "yyyy/MM/dd"),
				Locales.getCurrent()));
	}
	
	private DateFormat dateTimeFormat() {
		return applyTimeZone(new SimpleDateFormat(
				DateFormats.getDateTimeFormat(DateFormat.DEFAULT, DateFormat.DEFAULT, Locales.getCurrent(), "yyyy/MM/dd HH:mm:ss"),
				Locales.getCurrent()));
	}
	
	private DateFormat timeFormat() {
		return applyTimeZone(new SimpleDateFormat(
				DateFormats.getTimeFormat(DateFormat.DEFAULT, Locales.getCurrent(), "HH:mm:ss"),
				Locales.getCurrent()));
	}
	
	private DateFormat applyTimeZone(DateFormat dateFormat) {
		if (settingService.hasSetting(Setting.APPLICATION_TIMEZONE)) {
			dateFormat.setTimeZone(TimeZone.getTimeZone(settingService.getSetting(Setting.APPLICATION_TIMEZONE)));
		}
		return dateFormat;
	}
	
	private static String getBooleanKey(boolean bool) {
		return bool ? "boolean.true" : "boolean.false";
	}
	
	private static String getEnumLabelKey(Enum<?> enm) {
		final String[] parts = enm.getClass().getName().toLowerCase().split("\\.");
		return parts[parts.length - 2] + '.' + 
			   parts[parts.length - 1] + '.' + 
			   enm.name().toLowerCase();
	}

}
