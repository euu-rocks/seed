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
package org.seed.ui.zk.vm.admin;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import org.seed.C;
import org.seed.core.task.SystemTask;
import org.seed.core.task.SystemTaskRun;
import org.seed.core.task.TaskResult;
import org.seed.core.task.TaskService;
import org.seed.core.task.job.AbstractSystemJob;
import org.seed.core.task.job.JobScheduler;
import org.seed.core.util.NameUtils;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Destroy;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class SystemTaskViewModel extends AbstractApplicationViewModel
	implements JobListener {
	
	@WireVariable(value="taskServiceImpl")
	private TaskService taskService;
	
	@WireVariable(value="defaultJobScheduler")
	private JobScheduler jobScheduler;
	
	private Map<SystemTask, SystemTaskRun> mapLastRun;
	
	private String listenerName;
	
	private SystemTask systemTask;
	
	private SystemTaskRun taskRun;
	
	// JobListener changes
	private boolean jobStatusChanged;
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam(C.PARAM) Object object) {
		systemTask = (SystemTask) object;
		if (systemTask == null) { 			// list view
			mapLastRun = new ConcurrentHashMap<>();
		}
		jobScheduler.addJobListener(this);
	}
	
	public SystemTask getSystemTask() {
		return systemTask;
	}

	public void setSystemTask(SystemTask systemTask) {
		this.systemTask = systemTask;
	}

	public SystemTaskRun getRun() {
		return taskRun;
	}

	public void setRun(SystemTaskRun taskRun) {
		this.taskRun = taskRun;
	}

	public SystemTask[] getSystemTasks() {
		return SystemTask.values();
	}
	
	public Date getLastRunTime(SystemTask systemTask) {
		final SystemTaskRun run = getLastRun(systemTask);
		return run != null ? run.getStartTime() : null;
	}
	
	public TaskResult getLastRunResult(SystemTask systemTask) {
		final SystemTaskRun run = getLastRun(systemTask);
		return run != null ? run.getResult() : null;
	}
	
	public List<SystemTaskRun> getRuns() {
		return taskService.getSystemTaskRuns(systemTask);
	}
	
	@Command
	public void showRuns() {
		showView("/admin/systemtask/systemtask.zul", systemTask);
	}
	
	@Command
	public void checkJobStatus() {
		if (jobStatusChanged) {
			notifyChangeAll();
			jobStatusChanged = false;
		}
	}
	
	@Command
	public void startTask() {
		jobScheduler.startSystemJob(systemTask);
	}

	@Override
	public String getName() {
		if (listenerName == null) {
			listenerName = NameUtils.getRandomName();
		}
		return listenerName;
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		// do nothing
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		// do nothing
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		if (context.getJobInstance() instanceof AbstractSystemJob) {
			final AbstractSystemJob job = (AbstractSystemJob) context.getJobInstance();
			if (mapLastRun != null) {
				mapLastRun.remove(job.getSytemTask());
			}
			jobStatusChanged = true;
		}
	}
	
	@Destroy
	public void destroy() {
		jobScheduler.removeJobListener(this);
	}
	
	private SystemTaskRun getLastRun(SystemTask task) {
		SystemTaskRun run = null;
		if (mapLastRun != null) {
			run = mapLastRun.get(task);
			if (run == null) {
				run = taskService.getLastSystemTaskRun(task);
				if (run != null) {
					mapLastRun.put(task, run);
				}
			}
		}
		return run;
	}
	
}
