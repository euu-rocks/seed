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

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.api.Job;
import org.seed.core.codegen.CodeManager;
import org.seed.core.codegen.GeneratedCode;
import org.seed.core.config.LogLevel;
import org.seed.core.config.SessionProvider;
import org.seed.core.task.AbstractTaskRun;
import org.seed.core.task.SystemTask;
import org.seed.core.task.SystemTaskRun;
import org.seed.core.task.SystemTaskRunLog;
import org.seed.core.task.Task;
import org.seed.core.task.TaskResult;
import org.seed.core.task.TaskRun;
import org.seed.core.task.TaskRunLog;
import org.seed.core.task.TaskService;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;
import org.seed.core.util.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class DefaultJobScheduler 
	implements JobScheduler, JobListener, ApplicationContextAware {
	
	private static final Logger log = LoggerFactory.getLogger(DefaultJobScheduler.class);
	
	@Autowired
	private SchedulerFactoryBean schedulerFactory;
	
	@Autowired
	private CodeManager codeManager;
	
	@Autowired
	private TaskService taskService;
	
	@Autowired
	private SessionProvider sessionProvider;
	
	@Autowired
	private JobStatistics jobStatistics;
	
	private ApplicationContext applicationContext;
	
	@PostConstruct
	private void init() {
		addJobListener(this);
		log.info("DefaultJobScheduler created");
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
	
	@Override
	public void addJobListener(JobListener listener) {
		Assert.notNull(listener, "listener");
		try {
			getScheduler()
				.getListenerManager()
					.addJobListener(listener);
		} 
		catch (SchedulerException ex) {
			throw new InternalException(ex);
		}
	}
	
	@Override
	public boolean isRunning(Task task) {
		Assert.notNull(task, C.TASK);
		try {
			for (JobExecutionContext context : getScheduler().getCurrentlyExecutingJobs()) {
				if (context.getJobDetail().getKey().getGroup().equals(C.SEED) &&
					context.getJobDetail().getKey().getName().equals(task.getUid())) {
					return true;
				}
			}
		} 
		catch (SchedulerException ex) {
			throw new InternalException(ex);
		}
		return false;
	}
	
	@Override
	public void removeJobListener(JobListener listener) {
		Assert.notNull(listener, "listener");
		try {
			getScheduler()
				.getListenerManager()
					.removeJobListener(listener.getName());
		} 
		catch (SchedulerException ex) {
			throw new InternalException(ex);
		}
	}
	
	@Override
	public void startJob(Task task) {
		Assert.notNull(task, C.TASK);
		Assert.state(!isRunning(task), "job is already running");
		try {
			getScheduler().scheduleJob(createImmediateJobDetail(task), 
									   createImmediateTrigger(task));
		} 
		catch (SchedulerException ex) {
			throw new InternalException(ex);
		}
		log.info("Job for task '{}' started", task.getName());
	}
	
	@Override
	public void startSystemJob(SystemTask systemTask) {
		Assert.notNull(systemTask, C.SYSTEMTASK);
		try {
			getScheduler().scheduleJob(createImmediateJobDetail(systemTask), 
									   createImmediateTrigger(systemTask));
		} 
		catch (SchedulerException ex) {
			throw new InternalException(ex);
		}
		if (log.isInfoEnabled()) {
			log.info("Job for system task '{}' started", systemTask.name());
		}
	}
	
	@Override
	public void scheduleTask(Task task) {
		Assert.notNull(task, C.TASK);
		
		if (!task.isActive() || // deactivated
			!(task.getCronExpression() != null || // or no cron expression and no interval
			 (task.getRepeatInterval() != null && task.getRepeatIntervalUnit() != null))) {
			return;
		}
		try {
			getScheduler().scheduleJob(createJobDetail(task), createTrigger(task));
		}
		catch (SchedulerException ex) {
			throw new InternalException(ex);
		}
		log.info("Job for task '{}' scheduled", task.getName());
	}
	
	@Override
	public void scheduleAllTasks() {
		try {
			for (Class<GeneratedCode> jobClass : codeManager.getGeneratedClasses(Job.class)) {
				scheduleJob((Job) BeanUtils.instantiate(jobClass));
			}
		}
		catch (Exception ex) {
			throw new InternalException(ex);
		}
	}
	
	@Override
	public void unscheduleTask(Task task) {
		Assert.notNull(task, C.TASK);
		try {
			getScheduler().deleteJob(JobKey.jobKey(task.getUid(), C.SEED));
		} 
		catch (SchedulerException ex) {
			throw new InternalException(ex);
		}
		log.info("Job for task '{}' unscheduled", task.getName());
	}
	
	@Override
	public void unscheduleAllTasks() {
		try {
			getScheduler().clear();
		} 
		catch (SchedulerException ex) {
			throw new InternalException(ex);
		}
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		if (context.getJobInstance() instanceof Job) {
			prepareJob(context);
		}
		else {	// system job
			prepareSystemJob(context);
		}
		log.debug("start job: {}", context.getJobDetail());
	}
	
	private void prepareJob(JobExecutionContext context) {
		final Job job = (Job) context.getJobInstance();
		final Session session = sessionProvider.getSession();
		final Task task = getTask(job, session);
		final TaskRun run = taskService.createRun(task);
		taskService.saveTaskDirectly(task, session);
		
		context.put(DefaultJobContext.RUN_SESSION, session);
		context.put(DefaultJobContext.RUN_TASK, task);
		context.put(DefaultJobContext.RUN_ID, run.getId());
		if (task.hasParameters()) {
			context.put(DefaultJobContext.RUN_PARAMS, task.getParameters());
		}
	}
	
	private void prepareSystemJob(JobExecutionContext context) {
		final AbstractSystemJob job = (AbstractSystemJob) context.getJobInstance();
		final SystemTaskRun run = taskService.createRun(job.getSytemTask());
		taskService.saveSystemTaskRun(run);
		
		Assert.stateAvailable(applicationContext, "applicationContext");
		context.put(AbstractSystemJob.APPLICATION_CONTEXT, applicationContext);
		context.put(AbstractSystemJob.SYSTEMTASK_RUN, run);
	}
	
	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		log.debug("vetoed job: {}", context.getJobDetail());
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		if (context.getJobInstance() instanceof Job) {
			finalizeJob(context, jobException);
		}
		else { // system job
			finalizeSystemJob(context, jobException);
		}
		log.debug("finished job: {}", context.getJobDetail());
	}
	
	@SuppressWarnings("unchecked")
	private void finalizeJob(JobExecutionContext context, JobExecutionException jobException) {
		final Job job = (Job) context.getJobInstance();
		final Long taskRunId = (Long) context.get(DefaultJobContext.RUN_ID);
		final Session session = (Session) context.get(DefaultJobContext.RUN_SESSION);
		Assert.stateAvailable(session, C.SESSION);
		
		try (session) {
			final Task task = getTask(job, session);
			final TaskRun run = task.getRunById(taskRunId);
			Assert.stateAvailable(run, "run " + taskRunId);
			
			final List<TaskRunLog> logs = (List<TaskRunLog>) context.get(DefaultJobContext.RUN_LOGS);
			if (logs != null) {
				logs.forEach(run::addLog);
			}
			LogLevel maxLevel = run.getMaxLogLevel();
			if (jobException != null) {
				logError(run, jobException.getCause());
				maxLevel = LogLevel.ERROR;
			}
			run.setEndTime(new Date());
			run.setResult(TaskResult.getResult(maxLevel));
			
			jobStatistics.registerRun(run);
			taskService.saveTaskDirectly(task, session);
			
			if (task.hasNotifications()) {
				taskService.sendNotifications(task, run);
			}
		}
	}
	
	private void finalizeSystemJob(JobExecutionContext context, JobExecutionException jobException) {
		final SystemTaskRun run = (SystemTaskRun) context.get(AbstractSystemJob.SYSTEMTASK_RUN);
		LogLevel maxLevel = run.getMaxLogLevel();
		if (jobException != null) {
			logError(run, jobException.getCause());
			maxLevel = LogLevel.ERROR;
		}
		run.setEndTime(new Date());
		run.setResult(TaskResult.getResult(maxLevel));
		taskService.saveSystemTaskRun(run);
	}
	
	private Class<?> getJobClass(Task task) {
		final Class<?> jobClass = codeManager.getGeneratedClass(task);
		Assert.state(jobClass != null, "no job class available for task: " + task.getName());
		return jobClass;
	}
	
	private Scheduler getScheduler() {
		return schedulerFactory.getScheduler();
	}
	
	private Task getTask(Job job) {
		final Task task = taskService.getTask(job);
		Assert.stateAvailable(task, "task for job " + job.getClass().getName());
		return task;
	}
	
	private Task getTask(Job job, Session session) {
		final Task task = taskService.getTask(session, job);
		Assert.stateAvailable(task, "task for job " + job.getClass().getName());
		return task;
	}
	
	private void logError(AbstractTaskRun run, Throwable throwable) {
		for (String line : ExceptionUtils.getStackTraceAsString(throwable).split("\n")) {
			if (line.contains(AbstractJob.class.getName())) {
				break;
			}
			if (run instanceof TaskRun) {
				log((TaskRun) run, LogLevel.ERROR, line);
			}
			else {
				log((SystemTaskRun) run, LogLevel.ERROR, line);
			}
		}
	}
	
	private void scheduleJob(Job job) {
		scheduleTask(getTask(job));
	}
	
	@SuppressWarnings("unchecked")
	private JobDetail createImmediateJobDetail(Task task) {
		return JobBuilder.newJob((Class<? extends org.quartz.Job>) getJobClass(task))
				 .withIdentity(task.getId().toString(), C.SEED)
				 .build();
	}
	
	private JobDetail createImmediateJobDetail(SystemTask systemTask) {
		return JobBuilder.newJob(taskService.getSystemJobClass(systemTask))
				.withIdentity(systemTask.name(), C.SEED)
				.build();
	}
	
	@SuppressWarnings("unchecked")
	private JobDetail createJobDetail(Task task) {
		return JobBuilder.newJob((Class<? extends org.quartz.Job>) getJobClass(task))
				 .withIdentity(task.getUid(), C.SEED)
				 .build();
	}
	
	private static Trigger createImmediateTrigger(Task task) {
		return createImmediateTrigger(task.getId().toString());
	}
	
	private static Trigger createImmediateTrigger(SystemTask systemTask) {
		return createImmediateTrigger(systemTask.name());
	}
	
	private static Trigger createImmediateTrigger(String name) {
		return TriggerBuilder.newTrigger()
		   		.withIdentity(name, C.SEED)
		   		.startNow()
		   		.build();
	}
	
	private static Trigger createTrigger(Task task) {
		final TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
														.withIdentity(task.getUid(), C.SEED);
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
	
	private static void log(TaskRun run, LogLevel level, String message) {
		final TaskRunLog runLog = new TaskRunLog();
		runLog.setMoment(new Date());
		runLog.setLevel(level);
		runLog.setContent(message);
		run.addLog(runLog);
	}
	
	private static void log(SystemTaskRun run, LogLevel level, String message) {
		final SystemTaskRunLog runLog = new SystemTaskRunLog();
		runLog.setLevel(level);
		runLog.setContent(message);
		run.addLog(runLog);
	}
	
}
