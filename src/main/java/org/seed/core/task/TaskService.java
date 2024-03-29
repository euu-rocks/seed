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

import java.util.List;

import org.hibernate.Session;

import org.seed.core.api.Job;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.codegen.GeneratedCode;
import org.seed.core.task.job.AbstractSystemJob;
import org.seed.core.user.User;

public interface TaskService extends ApplicationEntityService<Task> {
	
	TaskParameter createParameter(Task task);
	
	TaskRun createRun(Task task);
	
	SystemTaskRun createRun(SystemTask systemTask);
	
	TaskNotification createNotification(Task task);
	
	Task getTask(Job job);
	
	Task getTask(Session session, Job job);
	
	List<Task> getTasks(User user, Session session);
	
	Class<GeneratedCode> getJobClass(Task task);
	
	List<Class<GeneratedCode>> getJobClasses();
	
	SystemTaskRun getLastSystemTaskRun(SystemTask systemTask, Session session);
	
	List<SystemTaskRun> getSystemTaskRuns(SystemTask systemTask, Session session);
	
	List<TaskPermission> getAvailablePermissions(Task task, Session session);
	
	<T extends AbstractSystemJob> Class<T> getSystemJobClass(SystemTask systemTask);
	
	void saveTaskDirectly(Task task, Session session);
	
	void saveSystemTaskRun(SystemTaskRun run);
	
	void sendNotifications(Task task, TaskRun run);
	
}
