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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import org.seed.InternalException;

import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StreamUtils;

public abstract class MiscUtils {
	
	public static final Charset CHARSET = StandardCharsets.UTF_8;
	
	private static final String USERNAME_SYSTEM = "system";
	
	private MiscUtils() {}
	
	public static String geUserName() {
		return SecurityContextHolder.getContext().getAuthentication() != null 
				? SecurityContextHolder.getContext().getAuthentication().getName()
				: USERNAME_SYSTEM;
	}
	
	public static String printArray(String[] elements) {
		if (elements == null) {
			return "";
		}
		if (elements.length == 1) {
			return elements[0];
		}
		final StringBuilder buf = new StringBuilder();
		for (String element : elements) {
			if (buf.length() > 0) {
				buf.append(", ");
			}
			buf.append(element);
		}
		return buf.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> cast(List<?> list) {
		return (List<T>) list;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(T... objects) {
        return objects;
    }
	
	public static <T> T instantiate(Class<T> clas) 
			throws InstantiationException, IllegalAccessException,
				   InvocationTargetException, NoSuchMethodException {
		return clas.getDeclaredConstructor().newInstance();
	}
	
	public static String filterString(String text, Predicate<Character> predicate) {
		return text.chars().mapToObj(c -> (char) c)
				   .filter(predicate)
				   .map(String::valueOf)
				   .collect(Collectors.joining());
	}
	
	public static String replaceAllIgnoreCase(String text, String replaceable, String replacemnet) {
		return Pattern.compile(replaceable, Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
					  .matcher(text)
					  .replaceAll(Matcher.quoteReplacement(replacemnet));
	}
	
	public static String formatDuration(long startTime) {
		final long duration = System.currentTimeMillis() - startTime;
		return duration < 1000 
				? duration + " ms"
				: (((double) duration) / 1000d) + " sec";
	}
	
	public static String getFileAsText(File file) throws IOException {
		try (InputStream inputStream = new FileInputStream(file)) {
			return StreamUtils.copyToString(inputStream, CHARSET);
		} 
	}
	
	public static String getResourceAsText(Resource resource) throws IOException {
		try (InputStream inputStream = resource.getInputStream()) {
			return StreamUtils.copyToString(inputStream, CHARSET);
		} 
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
				return new String(decompressedBytes, CHARSET);
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
	
}
