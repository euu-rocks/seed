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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import org.seed.C;
import org.seed.InternalException;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StreamUtils;

public abstract class MiscUtils {
	
	public static final Charset CHARSET = StandardCharsets.UTF_8;
	
	public static final String USERNAME_SYSTEM = "system";
	
	public static final String TIMESTAMP_FORMAT = "dd-MM-yyyy_HH-mm-ss";
	
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
	
	public static <T> T instantiate(Class<T> clas) {
		try {
			return clas.getDeclaredConstructor().newInstance();
		} 
		catch (Exception ex) {
			throw new InternalException(ex);
		}
	}
	
	public static <B> List<B> getBeans(ApplicationContext applicationContext, Class<B> type) {
		Assert.notNull(applicationContext, C.CONTEXT);
		Assert.notNull(type, C.TYPE);
		
		return applicationContext.getBeansOfType(type).values()
									.stream().collect(Collectors.toList());
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
		final long durationMs = endTimeMs - startTimeMs;
		final StringBuilder buf = new StringBuilder();
		if (durationMs < 60000) { // < 1min
			if (durationMs < 1000) {
				buf.append(durationMs).append(" ms");
			}
			else {
				buf.append(((double) durationMs) / 1000d).append(" sec");
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
	
	public static String getResourceAsText(Resource resource) throws IOException {
		return getStreamAsText(resource.getInputStream());
	}
	
	public static InputStream getStringAsStream(String string) {
		return new ByteArrayInputStream(string.getBytes(CHARSET));
	}
	
	public static boolean booleanValue(String property) {
		return "true".equalsIgnoreCase(property) ||
			   "yes".equalsIgnoreCase(property) ||
			   "ja".equalsIgnoreCase(property) ||
			   "on".equalsIgnoreCase(property) ||
			   "1".equals(property);
	}
	
	public static String compress(String text) {
		if (text != null) {
			try {
				final byte[] compressedBytes = compress(text.getBytes(CHARSET));
				return new String(Base64.getEncoder().encode(compressedBytes), CHARSET);
			} 
			catch (IOException ex) {
				throw new InternalException(ex);
			}
		}
		return text;
	}
	
	public static String decompress(String compressedText) {
		if (compressedText != null) {
			try {
				final byte[] decompressedBytes = decompress(Base64.getDecoder().decode(compressedText));
				return toString(decompressedBytes);
			} 
			catch (IOException ex) {
				throw new InternalException(ex);
			}  
		}
		return compressedText;
	}
	
	public static byte[] compress(byte[] bytes) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (bytes != null) {
			try (DeflaterOutputStream deflaterStream = new DeflaterOutputStream(out)) {
		        deflaterStream.write(bytes);    
		    }
		}
	    return out.toByteArray();
	}
	
	public static byte[] decompress(byte[] bytes) throws IOException {
	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    if (bytes != null) {
		    try (InflaterOutputStream inflaterStream = new InflaterOutputStream(out)) {
		        inflaterStream.write(bytes);    
		    }
	    }
	    return out.toByteArray();
	}
	
	private static String formatDurationPart(long duration) {
		return addLeadingChars(String.valueOf(duration), '0', 2);
	}
	
	private static String getStreamAsText(InputStream stream) throws IOException {
		try (InputStream inputStream = stream) {
			return StreamUtils.copyToString(inputStream, CHARSET);
		}
	}
	
}
