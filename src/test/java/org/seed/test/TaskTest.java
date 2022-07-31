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
package org.seed.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.seed.core.task.Task;
import org.seed.core.task.TaskMetadata;
import org.seed.core.task.TaskNotification;
import org.seed.core.task.TaskParameter;
import org.seed.core.task.TaskPermission;
import org.seed.core.task.TaskRun;

public class TaskTest {
	
	@Test
	public void testAddNotification() {
		final Task task = new TaskMetadata();
		final TaskNotification notification = new TaskNotification();
		assertFalse(task.hasNotifications());
		
		task.addNotification(notification);
		
		assertSame(notification.getTask(), task);
		
		assertTrue(task.hasNotifications());
		assertSame(task.getNotifications().size(), 1);
		assertSame(task.getNotifications().get(0), notification);
	}
	
	@Test
	public void testAddParameter() {
		final Task task = new TaskMetadata();
		final TaskParameter parameter = new TaskParameter();
		assertFalse(task.hasParameters());
		
		task.addParameter(parameter);
		
		assertSame(parameter.getTask(), task);
		
		assertTrue(task.hasParameters());
		assertSame(task.getParameters().size(), 1);
		assertSame(task.getParameters().get(0), parameter);
	}
	
	@Test
	public void testAddRun() {
		final Task task = new TaskMetadata();
		final TaskRun run = new TaskRun();
		assertNull(task.getRuns());
		
		task.addRun(run);
		
		assertSame(run.getTask(), task);
		assertSame(task.getRuns().size(), 1);
		assertSame(task.getRuns().get(0), run);
	}
	
	@Test
	public void testGetParameterByUid() {
		final Task task = new TaskMetadata();
		final TaskParameter parameter = new TaskParameter();
		parameter.setUid("other");
		task.addParameter(parameter);
		
		assertNull(task.getParameterByUid("test"));
		
		parameter.setUid("test");
		
		assertSame(task.getParameterByUid("test"), parameter);
	}
	
	@Test
	public void testGetPermissionByUid() {
		final Task task = new TaskMetadata();
		final TaskPermission permission = new TaskPermission();
		final List<TaskPermission> permissions = new ArrayList<>();
		permission.setUid("other");
		permissions.add(permission);
		((TaskMetadata)task).setPermissions(permissions);
		
		assertNull(task.getPermissionByUid("test"));
		
		permission.setUid("test");
		
		assertSame(task.getPermissionByUid("test"), permission);
	}
	
	@Test
	public void testGetRunById() {
		final Task task = new TaskMetadata();
		final TaskRun run = new TaskRun();
		run.setId(987L);
		task.addRun(run);
		
		assertNull(task.getRunById(123L));
		
		run.setId(123L);
		
		assertSame(task.getRunById(123L), run);
	}
	
	@Test
	public void testRemoveNotification() {
		final Task task = new TaskMetadata();
		final TaskNotification notification = new TaskNotification();
		task.addNotification(notification);
		assertSame(task.getNotifications().size(), 1);
		
		task.removeNotification(notification);
		
		assertFalse(task.hasNotifications());
		assertSame(task.getNotifications().size(), 0);
	}
	
	@Test
	public void testRemoveParameter() {
		final Task task = new TaskMetadata();
		final TaskParameter parameter = new TaskParameter();
		task.addParameter(parameter);
		assertTrue(task.hasParameters());
		
		task.removeParameter(parameter);
		
		assertFalse(task.hasParameters());
		assertSame(task.getParameters().size(), 0);
	}
	
}
