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
package org.seed.ui.zk.vm;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import org.seed.C;
import org.seed.core.task.Task;
import org.seed.core.task.TaskService;
import org.seed.core.task.job.JobScheduler;
import org.seed.core.util.NameUtils;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Destroy;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class TaskViewModel extends AbstractApplicationViewModel
	implements JobListener {
	
	@WireVariable(value="taskServiceImpl")
	private TaskService taskService;
	
	@WireVariable(value="defaultJobScheduler")
	private JobScheduler jobScheduler;
	
	private Task task;
	
	// JobListener name
	private String listenerName;
	
	// JobListener changes
	private boolean jobStatusChanged;
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam(C.PARAM) Object object) {
		jobScheduler.addJobListener(this);
	}
	
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public List<Task> getTasks() {
		return taskService.getTasks(getUser(), currentSession());
	}
	
	@Command
	public void refresh() {
		notifyChange("tasks");
	}
	
	@Command
	public void runTask() {
		jobScheduler.startJob(task);
	}
	
	@Command
	public void checkJobStatus() {
		if (jobStatusChanged) {
			refresh();
			jobStatusChanged = false;
		}
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
		jobStatusChanged = true;
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		jobStatusChanged = true;
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		jobStatusChanged = true;
	}
	
	@Destroy
	public void destroy() {
		jobScheduler.removeJobListener(this);
	}
	
}
