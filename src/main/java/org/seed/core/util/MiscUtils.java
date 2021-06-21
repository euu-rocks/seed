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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StreamUtils;

public abstract class MiscUtils {
	
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
			return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
		} 
	}
	
	public static String getResourceAsText(Resource resource) throws IOException {
		try (InputStream inputStream = resource.getInputStream()) {
			return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
		} 
	}
	
	public static InputStream getStringAsStream(String string) {
		return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
	}
	
	public static boolean booleanProperty(String property) {
		return "true".equalsIgnoreCase(property) ||
			   "yes".equalsIgnoreCase(property) ||
			   "1".equals(property);
	}
	
}
