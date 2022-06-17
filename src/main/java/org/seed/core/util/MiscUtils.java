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
package org.seed.core.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import org.springframework.security.core.context.SecurityContextHolder;

public abstract class MiscUtils {
	
	public static final Charset CHARSET = StandardCharsets.UTF_8;
	
	public static final String USERNAME_SYSTEM = "system";
	
	public static final String TIMESTAMP_FORMAT = "dd-MM-yyyy_HH-mm-ss";
	
	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
	
	private static final long SEC_MINUTE = 60;
	private static final long SEC_HOUR = 60 * SEC_MINUTE;
	private static final long SEC_DAY = 24 * SEC_HOUR;
	
	private MiscUtils() {}
	
	public static String geUserName() {
		return SecurityContextHolder.getContext().getAuthentication() != null 
				? SecurityContextHolder.getContext().getAuthentication().getName()
				: USERNAME_SYSTEM;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> castList(List<?> list) {
		return (List<T>) list;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(T... objects) {
        return objects;
    }
	
	public static String toString(byte[] bytes) {
		return bytes != null 
				? new String(bytes, 0, bytes.length, CHARSET) 
				: null;
	}
	
	public static String filterString(String text, Predicate<Character> predicate) {
		return text.chars().mapToObj(c -> (char) c)
				   .filter(predicate)
				   .map(String::valueOf)
				   .collect(Collectors.joining());
	}
	
	public static String replaceAllIgnoreCase(String text, String replaceable, String replacemnet) {
		return Pattern.compile(replaceable, 
				               Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
					  .matcher(text)
					  .replaceAll(Matcher.quoteReplacement(replacemnet));
	}
	
	public static String getTimestampString() {
		return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
	}
	
	public static String formatDuration(long startTimeMs) {
		return formatDuration(startTimeMs, System.currentTimeMillis());
	}
	
	public static String formatDuration(long startTimeMs, long endTimeMs) {
		return formatDurationTime(endTimeMs - startTimeMs);
	}
	
	public static String formatDurationTime(long durationMs) {
		final StringBuilder buf = new StringBuilder();
		if (durationMs < 60000) { // < 1min
			if (durationMs < 1000) {
				buf.append(durationMs).append(" ms");
			}
			else {
				buf.append(durationMs / 1000d).append(" sec");
			}
		}
		else {
			final long durationSec = durationMs / 1000L;
			if (durationSec > SEC_DAY) {
				buf.append(durationSec / SEC_DAY).append(':');
			}
			if (durationSec > SEC_HOUR) {
				buf.append(formatDurationPart((durationSec % SEC_DAY) / SEC_HOUR))
				   .append(':');
			}
			buf.append(formatDurationPart((durationSec % SEC_HOUR) / SEC_MINUTE)).append(':')	// min
			   .append(formatDurationPart((durationSec % SEC_MINUTE)));							// sec
			if (durationSec < SEC_HOUR) {
				buf.append(" min");
			}
		}
		return buf.toString();
	}
	
	public static String formatMemorySize(long size) {
		return FileUtils.byteCountToDisplaySize(size);
	}
	
	public static String addLeadingChars(String text, char leadingChar, int textLength) {
		if (text != null && text.length() < textLength) {
			final StringBuilder buf = new StringBuilder(text);
			while (buf.length() < textLength) {
				buf.insert(0, leadingChar);
			}
			return buf.toString();
		}
		return text;
	}
	
	private static String formatDurationPart(long duration) {
		return addLeadingChars(String.valueOf(duration), '0', 2);
	}
	
}
