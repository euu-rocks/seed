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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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

		"createdby", "createdon", "entityid", "entitystatus", "id", 
		"modifiedby", "modifiedon", "status_id", "version"
    };
	
	private NameUtils() {}
	
	public static boolean isKeyword(String name) {
		return name != null && 
			   Arrays.binarySearch(KEYWORDS, name.toLowerCase()) >= 0;
	}
	
	public static boolean isIllegalFieldName(String name) {
		return name != null && 
			   Arrays.binarySearch(ILLEGAL_FIELDNAMES, name.toLowerCase()) >= 0;
	}
	
	public static String getInternalName(String name) {
		if (name == null) {
			return null;
		}
		final StringBuilder buf = new StringBuilder(name.length());
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
	
	public static String getNameWithTimestamp(String name) {
		if (name == null) {
			return null;
		}
		return name + '_' + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	}
	
}
