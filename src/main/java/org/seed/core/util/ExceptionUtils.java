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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;

public interface ExceptionUtils {
	
	static Throwable getRootCause(Throwable throwable) {
		Assert.notNull(throwable, "throwable");
		
		if (throwable.getCause() == null || throwable.getCause() == throwable) {
			return throwable;
		}
		return getRootCause(throwable.getCause());
	}
	
	static String getRootCauseMessage(Throwable throwable) {
		return getRootCause(throwable).getLocalizedMessage();
	}
	
	static String getStackTraceAsString(Throwable throwable) {
		Assert.notNull(throwable, "throwable");
		
		final StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}
	
	static boolean isUniqueConstraintViolation(Exception ex) {
		return ex instanceof PersistenceException && 
			   ex.getCause() instanceof ConstraintViolationException &&
			   ((ConstraintViolationException) ex.getCause()).getSQLException()
			   	.getMessage().contains("unique constraint");
	}
	
	static Tupel<String,String> getUniqueConstraintDetails(Exception ex) {
		final String message = ((ConstraintViolationException) ex.getCause())
								.getSQLException().getMessage();
		String column = "?";
		String value = "?";
		final int idxDetail = message.indexOf("Detail:");
		if (idxDetail > 0) {
			final String detailMessage = message.substring(idxDetail);
			final Matcher matcher = Pattern.compile("\\((.*?)\\)").matcher(detailMessage);
			if (matcher.find()) {
				column = matcher.group(1);
				if (matcher.find()) {
					value = matcher.group(1);
				}
			}
		}
		return new Tupel<>(column, value);
	}
	
}
