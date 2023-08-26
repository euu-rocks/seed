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
package org.seed.core.config;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.seed.Seed;
import org.seed.core.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SystemLog {
	
	private static final Logger log = LoggerFactory.getLogger(SystemLog.class);
	
	private final List<LogEntry> entries = new CopyOnWriteArrayList<>();
	
	private volatile boolean errorOccured = false;
	
	public List<LogEntry> getEntries() {
		return entries;
	}
	
	public boolean hasErrorOccured() {
		return errorOccured;
	}

	public void resetErrorFlag() {
		errorOccured = false;
	}

	public void logInfo(String labelKey, String ...params) {
		log(LogLevel.INFO, labelKey, null, params);
	}
	
	public void logWarn(String labelKey, String ...params) {
		log(LogLevel.WARNING, labelKey, null, params);
	}
	
	public void logWarn(String labelKey, Throwable throwable, String ...params) {
		log(LogLevel.WARNING, labelKey, throwable, params);
	}
	
	public void logError(String labelKey, String ...params) {
		log(LogLevel.ERROR, labelKey, null, params);
	}
	
	public void logError(String labelKey, Throwable throwable, String ...params) {
		log(LogLevel.ERROR, labelKey, throwable, params);
	}
	
	private void log(LogLevel level, String labelKey, 
					 Throwable throwable, String ...params) {
		String details = null;
		if (level == LogLevel.ERROR) {
			errorOccured = true;
		}
		if (throwable != null) {
			details = ExceptionUtils.getStackTrace(throwable);
		}
		entries.add(new LogEntry(level, labelKey, details, params));
	}
	
	public static void logError(Throwable throwable) {
		try {
			final var systemLog = Seed.getBean(SystemLog.class);
			final var callerInfo = 
				StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
						   .walk(frames -> 
						   		frames.skip(1)
						   		.findFirst()
								.map(frame -> frame.getDeclaringClass().getSimpleName() + '.' + 
											  frame.getMethodName() + " line:" + 
											  frame.getLineNumber()));
			if (callerInfo.isPresent()) {
				systemLog.logError("systemlog.error.message", throwable, callerInfo.get());
			}
			else {
				systemLog.logError("systemlog.error.unknown", throwable);
			}
		}
		catch (Exception ex) {
			log.warn("log error", ex);
		}
	}

	public static class LogEntry {
		
		private final LogLevel level;
		
		private final Date time;
		
		private final String labelKey;
		
		private final String details;
		
		private final String[] params;

		private LogEntry(LogLevel level, String labelKey, 
						 String details, String ...params) {
			Assert.notNull(level, "level");
			Assert.notNull(labelKey, "labelKey");
			
			this.time = new Date();
			this.level = level;
			this.labelKey = labelKey;
			this.details = details;
			this.params = params;
		}

		public LogLevel getLevel() {
			return level;
		}

		public Date getTime() {
			return time;
		}

		public String getDetails() {
			return details;
		}
		
		public String getMessage() {
			return Seed.getLabel(labelKey, params);
		}
		
	}
	
}
