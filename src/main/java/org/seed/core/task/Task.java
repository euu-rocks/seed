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

import java.util.Date;
import java.util.List;

import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApprovableObject;
import org.seed.core.codegen.GeneratedObject;

public interface Task extends ApplicationEntity, GeneratedObject, ApprovableObject<TaskPermission> {
	
	final static String PACKAGE_NAME = "org.seed.generated.task";
	
	String getInternalName();
	
	Date getStartTime();
	
	Integer getRepeatInterval();
	
	IntervalUnit getRepeatIntervalUnit();
	
	String getCronExpression();
	
	String getContent();
	
	void setContent(String content);
	
	boolean isActive();
	
	boolean hasParameters();
	
	TaskParameter getParameterByUid(String uid);
	
	List<TaskParameter> getParameters();
	
	void addParameter(TaskParameter parameter);
	
	void removeParameter(TaskParameter parameter);
	
	TaskPermission getPermissionByUid(String uid);
	
	boolean hasNotifications();
	
	List<TaskNotification> getNotifications();
	
	void addNotification(TaskNotification notification);
	
	void removeNotification(TaskNotification notification);
	
	List<TaskRun> getRuns();
	
	TaskRun getRunById(Long id);
	
	void addRun(TaskRun run);
	
	TaskResult getLastResult();
	
}
