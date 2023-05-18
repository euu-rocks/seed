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
package org.seed.core.rest;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.seed.C;
import org.seed.Seed;
import org.seed.core.application.setting.ApplicationSettingService;
import org.seed.core.application.setting.Setting;
import org.seed.core.data.ValidationException;
import org.seed.core.data.ValidationError;

public abstract class RestHelper {
	
	public static final String DEFAULT_REST_FORMAT_DATE			 = "dd.MM.yyyy";
	public static final String DEFAULT_REST_FORMAT_DATETIME 	 = "dd.MM.yyyy HH:mm:ss";
	
	private RestHelper() {}
	
	public static String getRestDateFormat() {
		final ApplicationSettingService settingService = Seed.getBean(ApplicationSettingService.class);
		if (settingService.hasSetting(Setting.REST_FORMAT_DATE)) {
			return settingService.getSetting(Setting.REST_FORMAT_DATE);
		}
		return DEFAULT_REST_FORMAT_DATE;
	}
	
	public static String getRestDateTimeFormat() {
		final ApplicationSettingService settingService = Seed.getBean(ApplicationSettingService.class);
		if (settingService.hasSetting(Setting.REST_FORMAT_DATETIME)) {
			return settingService.getSetting(Setting.REST_FORMAT_DATETIME);
		}
		return DEFAULT_REST_FORMAT_DATETIME;
	}
	
	public static boolean parseBooleanValue(Object value, String fieldName) throws ValidationException {
		if (value instanceof Boolean) {
			return (boolean) value;
		}
		else if (value instanceof String) {
			if (Boolean.TRUE.toString().equalsIgnoreCase((String) value)) {
				return true;
			}
			else if (Boolean.FALSE.toString().equalsIgnoreCase((String) value)) {
				return false;
			}
		}
		throw new ValidationException(buildError(fieldName, value));
	}
	
	public static Date parseDateValue(Object value, String fieldName) throws ValidationException {
		if (value instanceof Date) {
			return (Date) value;
		}
		else if (value instanceof String) {
			try {
				return new SimpleDateFormat(getRestDateFormat()).parse(value.toString());
			} 
			catch (ParseException e) {
				throw new ValidationException(buildError(fieldName, value));
			}
		}
		throw new ValidationException(buildError(fieldName, value));
	}
	
	public static Date parseDateTimeValue(Object value, String fieldName) throws ValidationException {
		if (value instanceof Date) {
			return (Date) value;
		}
		else if (value instanceof String) {
			try {
				return new SimpleDateFormat(getRestDateTimeFormat()).parse(value.toString());
			} 
			catch (ParseException e) {
				throw new ValidationException(buildError(fieldName, value));
			}
		}
		throw new ValidationException(buildError(fieldName, value));
	}
	
	public static BigDecimal parseDecimalValue(Object value, String fieldName) throws ValidationException {
		if (value instanceof BigDecimal) {
			return (BigDecimal) value;
		}
		else if (value instanceof Number) {
			return parseBigDecimal(((Number) value).toString(), fieldName);
		}
		else if (value instanceof String) {
			return parseBigDecimal((String) value, fieldName);
		}
		throw new ValidationException(buildError(fieldName, value));
	}
	
	public static Double parseDoubleValue(Object value, String fieldName) throws ValidationException {
		if (value instanceof Double) {
			return (Double) value;
		}
		else if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		else if (value instanceof String) {
			try {
				return Double.parseDouble((String) value);
			}
			catch (NumberFormatException ex) {
				throw new ValidationException(buildError(fieldName, value));
			}
		}
		throw new ValidationException(buildError(fieldName, value));
	}
	
	public static Integer parseIntegerValue(Object value, String fieldName) throws ValidationException {
		if (value instanceof Integer) {
			return (Integer) value;
		}
		else if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		else if (value instanceof String) {
			try {
				return Integer.parseInt((String) value);
			}
			catch (NumberFormatException ex) {
				throw new ValidationException(buildError(fieldName, value));
			}	
		}
		throw new ValidationException(buildError(fieldName, value));
	}
	
	public static Long parseLongValue(Object value, String fieldName) throws ValidationException {
		if (value instanceof Long) {
			return (Long) value;
		}
		else if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		else if (value instanceof String) {
			try {
				return Long.parseLong(value.toString());
			}
			catch (NumberFormatException ex) {
				throw new ValidationException(buildError(fieldName, value));
			}
		}
		throw new ValidationException(buildError(fieldName, value));
	}
	
	public static Long parseReferenceId(Object value, String fieldName) throws ValidationException {
		if (!(value instanceof Map)) {
			throw new ValidationException(buildError(fieldName, value));
		}
		@SuppressWarnings("unchecked")
		final Object object = ((Map<String, Object>) value).get(C.ID);
		if (!(object instanceof Number)) {
			throw new ValidationException(buildError(fieldName, value));
		}
		return ((Number) object).longValue();
	}
	
	private static BigDecimal parseBigDecimal(String value, String fieldName) throws ValidationException {
		try {
			return new BigDecimal(value);
		}
		catch (NumberFormatException ex) {
			throw new ValidationException(buildError(fieldName, value));
		}
	}
	
	private static ValidationError buildError(String fieldName, Object value) {
		return new ValidationError(null, "val.illegal.field", fieldName, value.toString());
	}
	
}
