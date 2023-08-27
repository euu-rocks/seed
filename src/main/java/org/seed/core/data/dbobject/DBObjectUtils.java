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
package org.seed.core.data.dbobject;

import java.util.regex.Pattern;

public abstract class DBObjectUtils {
	
	private static final Pattern PATTERN_QUOTED_TEXT = Pattern.compile("\\\".*?\\\"|\\'.*?\\'|`.*`");
	
	private static final Pattern PATTERN_SQL_COMMENT = Pattern.compile("--.*|/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
	
	private static final Pattern PATTERN_TRIGGER_TABLE = Pattern.compile("\\s+on\\s+([^\\s]+)");
	
	private static final String NAME_NEIGHBOR = "[^a-zA-Z0-9_.-]";
	
	private static final String NAME_PREFIX = "(^|" + NAME_NEIGHBOR + ")";
	
	private static final String NAME_SUFFIX = "($|" + NAME_NEIGHBOR + ")";
	
	private DBObjectUtils() {};
	
	public static boolean containsName(String text, String name) {
		return text != null && name != null &&
			   Pattern.compile(NAME_PREFIX + name + NAME_SUFFIX, Pattern.CASE_INSENSITIVE)
			   		  .matcher(removeMatches(PATTERN_SQL_COMMENT, removeMatches(PATTERN_QUOTED_TEXT, text)))
			   		  .find();
	}
	
	public static String getTriggerTable(String text) {
		if (text != null) {
			final var matcher = PATTERN_TRIGGER_TABLE.matcher(text.toLowerCase());
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return null;
	}
	
	public static String removeQuotedText(String text) {
		return text != null
				? removeMatches(PATTERN_QUOTED_TEXT, text)
				: null;
	}
	
	public static String removeSqlComments(String text) {
		return text != null
				? removeMatches(PATTERN_SQL_COMMENT, text)
				: null;
	}
	
	private static String removeMatches(Pattern pattern, String text) {
		return pattern.matcher(text).replaceAll("");
	}
	
}
