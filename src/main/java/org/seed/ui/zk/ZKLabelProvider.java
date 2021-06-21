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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.seed.C;
import org.seed.core.form.LabelProvider;
import org.seed.core.util.Assert;
import org.springframework.stereotype.Component;
import org.zkoss.math.BigDecimals;
import org.zkoss.text.DateFormats;
import org.zkoss.util.resource.Labels;

@Component
public class ZKLabelProvider implements LabelProvider {
	
	private static final String EMPTY = "";
	
	private static final Locale LOCALE = Locale.getDefault();
	
	private static final TimeZone TIMEZONE = TimeZone.getDefault();
	
	private static final Map<Enum<?>, String> cacheEnumLabel = Collections.synchronizedMap(new HashMap<>());
	
	private final DateFormat dateFormat = new SimpleDateFormat(
			DateFormats.getDateFormat(DateFormat.DEFAULT, LOCALE, "yyyy/MM/dd"),
			LOCALE);
	
	private final DateFormat dateTimeFormat = new SimpleDateFormat(
			DateFormats.getDateTimeFormat(DateFormat.DEFAULT, DateFormat.DEFAULT, LOCALE, "yyyy/MM/dd HH:mm:ss"),
			LOCALE);
	
	private final DateFormat timeFormat = new SimpleDateFormat(
			DateFormats.getTimeFormat(DateFormat.DEFAULT, LOCALE, "HH:mm:ss"),
			LOCALE);
	
	@PostConstruct
	private void init() {
		dateFormat.setTimeZone(TIMEZONE);
		dateTimeFormat.setTimeZone(TIMEZONE);
		timeFormat.setTimeZone(TIMEZONE);
	}
	
	@Override
	public String getLabel(String key, String ...params) {
		Assert.notNull(key, C.KEY);
		
		final String label = params != null 
								? Labels.getLabel(key, params) 
								: Labels.getLabel(key);
				
		Assert.state(label != null, "no label found for key: " + key);
		return label;
	}

	@Override
	public String getEnumLabel(Enum<?> enm) {
		if (enm == null) {
			return null;
		}
		synchronized (this) {
			String label = cacheEnumLabel.get(enm);
			if (label == null) {
				label = getLabel(getEnumKey(enm));
				cacheEnumLabel.put(enm, label);
			}
			return label;
		}
	}
	
	@Override
	public String formatBoolean(Boolean bool) {
		if (bool == null) {
			return EMPTY;
		}
		return getLabel(bool.booleanValue() ? "boolean.true" : "boolean.false");
	}
	
	@Override
	public String formatDate(Date date) {
		if (date == null) {
			return EMPTY;
		}
		synchronized (dateFormat) {
			return dateFormat.format(date);
		}
	}
	
	@Override
	public String formatDateTime(Date date) {
		if (date == null) {
			return EMPTY;
		}
		synchronized (dateTimeFormat) {
			return dateTimeFormat.format(date);
		}
	}
	
	@Override
	public String formatTime(Date time) {
		if (time == null) {
			return EMPTY;
		}
		synchronized (timeFormat) {
			return timeFormat.format(time);
		}
	}
	
	@Override
	public String formatBigDecimal(BigDecimal decimal) {
		if (decimal == null) {
			return EMPTY;
		}
		return BigDecimals.toLocaleString(decimal, LOCALE);
	}
	
	private static String getEnumKey(Enum<?> enm) {
		final String[] parts = enm.getClass().getName().toLowerCase().split("\\.");
		return parts[parts.length - 2] + '.' + 
			   parts[parts.length - 1] + '.' + 
			   enm.name().toLowerCase();
	}

}
