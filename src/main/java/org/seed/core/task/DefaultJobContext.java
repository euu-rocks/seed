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
package org.seed.core.task;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.quartz.JobExecutionContext;

import org.seed.core.api.JobContext;
import org.seed.core.entity.value.event.ValueObjectFunctionContext;

public class DefaultJobContext extends ValueObjectFunctionContext
	implements JobContext {
	
	public static final String RUN_ID = "RUN_ID"; 
	
	public static final String RUN_TASK = "RUN_TASK"; 
	
	public static final String RUN_LOGS = "RUN_LOGS"; 
	
	public static final String RUN_PARAMS = "RUN_PARAMS"; 
	
	public static final String RUN_SESSION = "RUN_SESSION"; 
	
	private final List<TaskRunLog> logs = new ArrayList<>();
	
	private final List<TaskParameter> params;
	
	@SuppressWarnings("unchecked")
	DefaultJobContext(JobExecutionContext context) {
		super((Session) context.get(RUN_SESSION), 
			  ((Task) context.get(RUN_TASK)).getModule());
		params = (List<TaskParameter>) context.get(RUN_PARAMS);
		context.put(RUN_LOGS, logs);
	}
	
	@Override
	public String getJobParameter(String name) {
		if (params != null) {
			for (TaskParameter param : params) {
				if (param.getName().equalsIgnoreCase(name)) {
					return param.getValue();
				}
			}
		}
		return null;
	}
	
	@Override
	public String getJobParameter(String name, String defaultValue) {
		final String value = getJobParameter(name);
		return value != null ? value : defaultValue;
	}
	
	@Override
	public boolean hasJobParameter(String name) {
		return getJobParameter(name) != null;
	}
	
	@Override
	public void logInfo(String content) {
		log(LogLevel.INFO, content);
	}
	
	@Override
	public void logWarning(String content) {
		log(LogLevel.WARN, content);
	}
	
	@Override
	public void logError(String content) {
		log(LogLevel.ERROR, content);
	}
	
	private void log(LogLevel level, String content) {
		final TaskRunLog log = new TaskRunLog();
		log.setLevel(level);
		if (content != null && content.length() > 1024) {
			content = content.substring(0, 1020) + "...";
		}
		log.setContent(content);
		logs.add(log);
	}
	
}
