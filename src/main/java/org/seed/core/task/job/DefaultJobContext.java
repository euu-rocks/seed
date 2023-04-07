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
package org.seed.core.task.job;

import static org.seed.core.util.CollectionUtils.firstMatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.quartz.JobExecutionContext;

import org.seed.core.api.ClientProvider;
import org.seed.core.api.JobContext;
import org.seed.core.config.LogLevel;
import org.seed.core.entity.value.event.ValueObjectFunctionContext;
import org.seed.core.task.Task;
import org.seed.core.task.TaskParameter;
import org.seed.core.task.TaskRunLog;
import org.seed.core.util.Assert;

class DefaultJobContext extends ValueObjectFunctionContext
	implements JobContext {
	
	static final String RUN_ID 		= "RUN_ID"; 
	static final String RUN_TASK 	= "RUN_TASK"; 
	static final String RUN_LOGS 	= "RUN_LOGS"; 
	static final String RUN_PARAMS 	= "RUN_PARAMS"; 
	static final String RUN_SESSION	= "RUN_SESSION"; 
	
	private final List<TaskRunLog> logs = new ArrayList<>();
	
	private final List<TaskParameter> parameters;
	
	@SuppressWarnings("unchecked")
	DefaultJobContext(JobExecutionContext context) {
		super((Session) context.get(RUN_SESSION), 
			  ((Task) context.get(RUN_TASK)).getModule());
		parameters = (List<TaskParameter>) context.get(RUN_PARAMS);
		context.put(RUN_LOGS, logs);
	}
	
	@Override
	public ClientProvider getClientProvider() {
		throw new UnsupportedOperationException("client provider not available in job context");
	}
	
	@Override
	public String getJobParameter(String name) {
		Assert.notNull(name, "parameter name");
		
		final TaskParameter parameter = firstMatch(parameters, param -> param.getName().equalsIgnoreCase(name));
		return parameter != null ? parameter.getValue() : null;
	}
	
	@Override
	public Integer getJobParameterAsInt(String name) {
		final String param = getJobParameter(name);
		if (param != null) {
			try {
				return Integer.parseInt(param);
			}
			catch (NumberFormatException nfe) {
				throw new IllegalStateException("parameter '" + name + "' is not an integer: " + param);
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
	public Integer getJobParameterAsInt(String name, Integer defaultValue) {
		final Integer value = getJobParameterAsInt(name);
		return value != null ? value : defaultValue;
	}
	
	@Override
	public boolean hasJobParameter(String name) {
		return getJobParameter(name) != null;
	}
	
	@Override
	public void log(String message) {
		logInfo(message);
	}
	
	@Override
	public void logInfo(String message) {
		log(LogLevel.INFO, message);
	}
	
	@Override
	public void logWarn(String message) {
		log(LogLevel.WARNING, message);
	}
	
	@Override
	public void logError(String message) {
		log(LogLevel.ERROR, message);
	}
	
	private void log(LogLevel level, String message) {
		if (message != null) {
			final var log = new TaskRunLog();
			log.setMoment(new Date());
			log.setLevel(level);
			if (message.length() > 1024) {
				message = message.substring(0, 1020) + "...";
			}
			log.setContent(message);
			logs.add(log);
		}
	}
	
}
