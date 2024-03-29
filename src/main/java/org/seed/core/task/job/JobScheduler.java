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

import org.quartz.JobListener;

import org.seed.core.task.SystemTask;
import org.seed.core.task.Task;

public interface JobScheduler {
	
	void addJobListener(JobListener listener);
	
	void removeJobListener(JobListener listener);
	
	boolean isRunning(Task task);
	
	void startJob(Task task);
	
	void startSystemJob(SystemTask systemTask);
	
	void scheduleTask(Task task);
	
	void scheduleAllTasks();
	
	void unscheduleTask(Task task);
	
	void unscheduleAllTasks();
	
}
