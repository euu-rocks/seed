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

import org.seed.core.task.Task;
import org.seed.core.task.TaskService;
import org.seed.core.task.job.JobScheduler;

import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class TaskViewModel extends AbstractApplicationViewModel {
	
	@WireVariable(value="taskServiceImpl")
	private TaskService taskService;
	
	@WireVariable(value="jobScheduler")
	private JobScheduler jobScheduler;
	
	private Task task;
	
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public List<Task> getTasks() {
		return taskService.getTasks(getUser());
	}
	
	@Command
	public void runTask() {
		jobScheduler.startJob(task);
	}
	
}
