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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;

public abstract class ExceptionUtils {
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -3338292458819471313L;
	
	private ExceptionUtils() {}
	
	public static Throwable getRootCause(Throwable th) {
		if (th.getCause() == null || th.getCause() == th) {
			return th;
		}
		return getRootCause(th.getCause());
	}
	
	public static String getRootCauseMessage(Throwable th) {
		return getRootCause(th).getLocalizedMessage();
	}
	
	public static String stackTraceAsString(Throwable th) {
		final StringWriter stringWriter = new StringWriter();
		th.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}
	
	public static boolean isThrownAt(Throwable th, String pattern) {
		try (Scanner scanner = new Scanner(stackTraceAsString(th))) {
			if (scanner.hasNextLine()) {
				scanner.nextLine(); // skip first line
				if (scanner.hasNextLine() && 
					scanner.nextLine().contains(pattern)) {
					return true;
				}
			}
		}
		return false;
	}
	
}
