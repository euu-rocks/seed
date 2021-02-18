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

import javax.annotation.PostConstruct;

import org.seed.core.form.LabelProvider;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.zkoss.math.BigDecimals;
import org.zkoss.text.DateFormats;
import org.zkoss.util.Locales;
import org.zkoss.util.TimeZones;
import org.zkoss.util.resource.Labels;

@Component
public class ZKLabelProvider implements LabelProvider {
	
	private static final String EMPTY = "";
	
	private static final Locale LOCALE = Locales.getCurrent();
	
	private static final Map<Enum<?>, String> cacheEnumLabel = Collections.synchronizedMap(new HashMap<>());
	
	private final DateFormat DATE_FORMAT = new SimpleDateFormat(
			DateFormats.getDateTimeFormat(DateFormat.DEFAULT, DateFormat.DEFAULT, LOCALE, "yyyy/MM/dd HH:mm:ss"),
			LOCALE);
	
	private final DateFormat TIME_FORMAT = new SimpleDateFormat(
			DateFormats.getTimeFormat(DateFormat.DEFAULT, LOCALE, "HH:mm:ss"),
			LOCALE);
	
	@PostConstruct
	private void init() {
		DATE_FORMAT.setTimeZone(TimeZones.getCurrent());
		TIME_FORMAT.setTimeZone(TimeZones.getCurrent());
	}
	
	@Override
	public String getLabel(String key, String ...params) {
		Assert.notNull(key, "key is null");
		
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
		String label = cacheEnumLabel.get(enm);
		if (label == null) {
			final String[] parts = enm.getClass().getName().toLowerCase().split("\\.");
			final String key = parts[parts.length - 2] + '.' + 
					 	 	   parts[parts.length - 1] + '.' + 
					 	 	   enm.name().toLowerCase();
			label = getLabel(key);
			cacheEnumLabel.put(enm, label);
		}
		return label;
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
		synchronized (DATE_FORMAT) {
			return DATE_FORMAT.format(date);
		}
	}
	
	@Override
	public String formatTime(Date time) {
		if (time == null) {
			return EMPTY;
		}
		synchronized (TIME_FORMAT) {
			return TIME_FORMAT.format(time);
		}
	}
	
	@Override
	public String formatBigDecimal(BigDecimal decimal) {
		if (decimal == null) {
			return EMPTY;
		}
		return BigDecimals.toLocaleString(decimal, LOCALE);
	}

}
