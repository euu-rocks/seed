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

import javax.annotation.PostConstruct;

import org.hibernate.Session;
import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import org.seed.core.api.Job;
import org.seed.core.codegen.CodeManager;
import org.seed.core.codegen.GeneratedCode;
import org.seed.core.task.AbstractJob;
import org.seed.core.task.DefaultJobContext;
import org.seed.core.task.LogLevel;
import org.seed.core.task.Task;
import org.seed.core.task.TaskResult;
import org.seed.core.task.TaskRun;
import org.seed.core.task.TaskRunLog;
import org.seed.core.task.TaskService;
import org.seed.core.util.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class JobScheduler implements JobListener {
	
	private final static Logger log = LoggerFactory.getLogger(JobScheduler.class);
	
	private final static String GROUP = "seed";
	
	@Autowired
	private SchedulerFactoryBean schedulerFactory;
	
	@Autowired
	private SessionFactoryProvider sessionFactoryProvider;
	
	@Autowired
	private CodeManager codeManager;
	
	@Autowired
	private TaskService taskService;
	
	@PostConstruct
	void init() {
		addJobListener(this);
	}
	
	public void addJobListener(JobListener listener) {
		Assert.notNull(listener, "listener is null");
		
		try {
			getScheduler()
				.getListenerManager()
					.addJobListener(listener);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void removeJobListener(JobListener listener) {
		Assert.notNull(listener, "listener is null");
		
		try {
			getScheduler()
				.getListenerManager()
					.removeJobListener(listener.getName());
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean isRunning(Task task) {
		Assert.notNull(task, "task is null");
		
		try {
			for (JobExecutionContext context : getScheduler().getCurrentlyExecutingJobs()) {
				if (context.getJobDetail().getKey().getGroup().equals(GROUP) &&
					context.getJobDetail().getKey().getName().equals(task.getUid())) {
					return true;
				}
			}
		} 
		catch (SchedulerException ex) {
			throw new RuntimeException(ex);
		}
		return false;
	}
	
	public void startJob(Task task) {
		Assert.notNull(task, "task is null");
		Assert.state(!isRunning(task), "job is already running");
		
		try {
			getScheduler().scheduleJob(createImmediateJobDetail(task), 
									   createImmediateTrigger(task));
		} 
		catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
		log.info("Job for task '" + task.getName() + "' started");
	}
	
	public void scheduleJob(Job job) {
		Assert.notNull(job, "job is null");
		
		scheduleTask(taskService.getTask(job));
	}
	
	public void scheduleTask(Task task) {
		Assert.notNull(task, "task is null");
		
		if (!task.isActive() || // deactivated
			!(task.getCronExpression() != null || // or no cron expression and no interval
			 (task.getRepeatInterval() != null && task.getRepeatIntervalUnit() != null))) {
			return;
		}
		try {
			getScheduler().scheduleJob(createJobDetail(task), createTrigger(task));
		}
		catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
		log.info("Job for task '" + task.getName() + "' scheduled");
	}
	
	public void scheduleAllTasks() {
		try {
			for (Class<GeneratedCode> jobClass : codeManager.getGeneratedClasses(Job.class)) {
				final Job job = (Job) jobClass.getDeclaredConstructor().newInstance();
				scheduleJob(job);
			}
		}
		catch (Exception ex) {
			throw new ConfigurationException("failed to schedule jobs", ex);
		}
	}
	
	public void unscheduleTask(Task task) {
		Assert.notNull(task, "task is null");
		
		try {
			getScheduler().deleteJob(JobKey.jobKey(task.getUid(), GROUP));
		} 
		catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
		log.info("Job for task '" + task.getName() + "' unscheduled");
	}

	public void unscheduleAllTasks() {
		try {
			getScheduler().clear();
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		final AbstractJob job = (AbstractJob) context.getJobInstance();
		final Task task = taskService.getTask(job);
		final TaskRun run = new TaskRun();
		run.setStartTime(new Date());
		task.addRun(run);
		taskService.saveTaskDirectly(task);
		
		final Session session = sessionFactoryProvider.getSessionFactory().openSession();
		context.put(DefaultJobContext.RUN_SESSION, session);
		context.put(DefaultJobContext.RUN_TASK, task);
		context.put(DefaultJobContext.RUN_ID, run.getId());
		if (task.hasParameters()) {
			context.put(DefaultJobContext.RUN_PARAMS, task.getParameters());
		}
		log.debug("start job: " + context.getJobDetail());
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		log.debug("vetoed job: " + context.getJobDetail());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		final AbstractJob job = (AbstractJob) context.getJobInstance();
		final Task task = taskService.getTask(job);
		final Long taskRunId = (Long) context.get(DefaultJobContext.RUN_ID);
		final TaskRun run = task.getRunById(taskRunId);
		LogLevel maxLevel = LogLevel.INFO;
		for (TaskRunLog log : (List<TaskRunLog>) context.get(DefaultJobContext.RUN_LOGS)) {
			run.addLog(log);
			if (log.getLevel().ordinal() > maxLevel.ordinal()) {
				maxLevel = log.getLevel();
			}
		}
		if (jobException != null) {
			logError(run, jobException.getCause());
			maxLevel = LogLevel.ERROR;
		}
		run.setEndTime(new Date());
		run.setResult(TaskResult.getResult(maxLevel));
		taskService.saveTaskDirectly(task);
		
		if (task.hasNotifications()) {
			taskService.sendNotifications(task, run);
		}
		log.debug("finished job: " + context.getJobDetail());
	}
	
	private Scheduler getScheduler() {
		return schedulerFactory.getScheduler();
	}
	
	private Class<?> getJobClass(Task task) {
		final Class<?> jobClass = codeManager.getGeneratedClass(task);
		Assert.state(jobClass != null, "no job class available for task: " + task.getName());
		return jobClass;
	}
	
	@SuppressWarnings("unchecked")
	private JobDetail createJobDetail(Task task) {
		return JobBuilder.newJob((Class<? extends org.quartz.Job>) getJobClass(task))
				 .withIdentity(task.getUid(), GROUP)
				 .build();
	}
	
	@SuppressWarnings("unchecked")
	private JobDetail createImmediateJobDetail(Task task) {
		return JobBuilder.newJob((Class<? extends org.quartz.Job>) getJobClass(task))
				 .withIdentity(task.getId().toString(), GROUP)
				 .build();
	}
	
	private void logError(TaskRun run, Throwable th) {
		for (String line : ExceptionUtils.stackTraceAsString(th).split("\n")) {
			if (line.contains(AbstractJob.class.getName())) {
				break;
			}
			final TaskRunLog log = new TaskRunLog();
			log.setLevel(LogLevel.ERROR);
			log.setContent(line.trim());
			run.addLog(log);
		}
	}
	
	private static Trigger createImmediateTrigger(Task task) {
		return TriggerBuilder.newTrigger()
		   		.withIdentity(task.getId().toString(), GROUP)
		   		.startNow()
		   		.build();
	}
	
	private static Trigger createTrigger(Task task) {
		final TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
														.withIdentity(task.getUid(), GROUP);
		if (task.getStartTime() != null) {
			triggerBuilder.startAt(task.getStartTime());
		}
		else {
			triggerBuilder.startNow();
		}
		if (task.getCronExpression() != null) {
			triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpression()));
		}
		else if (task.getRepeatInterval() != null && task.getRepeatIntervalUnit() != null) {
			triggerBuilder.withSchedule(CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
											.withInterval(task.getRepeatInterval(), 
														  task.getRepeatIntervalUnit().quartzInterval));
		}
		else {
			throw new IllegalStateException("no cron expression or interval");
		}
		return triggerBuilder.build();
	}
	
}
