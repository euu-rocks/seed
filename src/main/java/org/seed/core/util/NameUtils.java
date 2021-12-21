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

import java.util.Arrays;
import java.util.UUID;

public abstract class NameUtils {
	
	private static final String[] KEYWORDS = {

		"abstract", "assert", "boolean", "break", "byte", "case", "catch", 
		"char", "class", "const", "continue", "default", "do", "double", 
		"else", "enum", "extends", "false", "final", "finally", "float", "for", 
		"goto", "if", "implements", "import", "instanceof", "int", "interface", 
		"long", "native", "new", "null", "package", "private", "protected", 
		"public", "return", "short", "static", "strictfp", "super", "switch",
        "synchronized", "this", "throw", "throws", "transient", "true", "try", 
        "void", "volatile", "while" 
    };
	
	private static final String[] ILLEGAL_FIELDNAMES = {

		"createdby", "createdon", "entityid", "entitystatus", "id", "lastmodified", 
		"modifiedby", "modifiedon", "status_id", "uid", "version"
    };
	
	private static final String[] TRUE_VALUES = {

		"1", "j", "ja", "on", "true", "y", "yes"
    };
	
	private NameUtils() {}
	
	public static boolean isKeyword(String name) {
		return name != null && find(KEYWORDS, name);
	}
	
	public static boolean isIllegalFieldName(String name) {
		return isKeyword(name) || 
				(name != null && find(ILLEGAL_FIELDNAMES, name));
	}
	
	public static boolean booleanValue(String value) {
		return value != null && find(TRUE_VALUES, value);
	}
	
	public static String getInternalName(String name) {
		if (name == null) {
			return null;
		}
		final StringBuilder buf = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			final char ch = name.charAt(i);
			switch (ch) {
				case 'Ä':
					buf.append("Ae");
					break;
					
				case 'Ö':
					buf.append("Oe");
					break;
					
				case 'Ü':
					buf.append("Ue");
					break;
					
				case 'ä':
					buf.append("ae");
					break;
					
				case 'ö':
					buf.append("oe");
					break;
					
				case 'ü':
					buf.append("ue");
					break;
					
				case 'ß':
					buf.append("ss");
					break;
					
				case ' ':
				case '.':
				case '-':
				case '_':
					buf.append('_');
					break;
					
				default:
					if ((ch >= 'a' && ch <= 'z') ||
					    (ch >= 'A' && ch <= 'Z') ||
					    (ch >= '0' && ch <= '9')) {
						buf.append(ch);
					}
			}
		}
		return buf.toString();
	}
	
	public static String getRandomName() {
		return UUID.randomUUID().toString();
	}
	
	private static boolean find(String[] vocabulary, String text) {
		return Arrays.binarySearch(vocabulary, text.toLowerCase()) >= 0;
	}
	
}
