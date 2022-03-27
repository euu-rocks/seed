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

import org.hibernate.Session;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.seed.C;
import org.seed.core.config.SessionProvider;
import org.seed.core.task.LogLevel;
import org.seed.core.task.SystemTask;
import org.seed.core.task.SystemTaskRun;
import org.seed.core.task.SystemTaskRunLog;
import org.seed.core.util.Assert;

import org.springframework.context.ApplicationContext;

public abstract class AbstractSystemJob implements Job {
	
	public static final String APPLICATION_CONTEXT = "APPLICATION_CONTEXT";
	
	public static final String SYSTEMTASK_RUN = "SYSTEMTASK_RUN";
	
	private final SystemTask systemTask;
	
	private SystemTaskRun taskRun;
	
	private ApplicationContext applicationContext;
	
	protected AbstractSystemJob(SystemTask systemTask) {
		Assert.notNull(systemTask, C.SYSTEMTASK);
		
		this.systemTask = systemTask;
	}

	public SystemTask getSytemTask() {
		return systemTask;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		applicationContext = (ApplicationContext) context.get(APPLICATION_CONTEXT);
		taskRun = (SystemTaskRun) context.get(SYSTEMTASK_RUN);
		Assert.stateAvailable(applicationContext, "application context");
		Assert.stateAvailable(taskRun, "system task run");
		
		init();
		execute();
	}
	
	protected abstract void init();
	
	protected abstract void execute();
	
	protected Session getSession() {
		return getBean(SessionProvider.class).getSession();
	}
	
	protected <T> T getBean(Class<T> typeClass) {
		final T bean = applicationContext.getBean(typeClass);
		Assert.stateAvailable(bean, "bean " + typeClass.getName());
		
		return applicationContext.getBean(typeClass);
	}
	
	protected void logInfo(String content) {
		log(LogLevel.INFO, content);
	}
	
	protected void logWarning(String content) {
		log(LogLevel.WARN, content);
	}
	
	protected void logError(String content) {
		log(LogLevel.ERROR, content);
	}
	
	private void log(LogLevel level, String content) {
		final SystemTaskRunLog log = new SystemTaskRunLog();
		log.setLevel(level);
		if (content != null && content.length() > 1024) {
			content = content.substring(0, 1020) + "...";
		}
		log.setContent(content);
		taskRun.addLog(log);
	}
	
}
