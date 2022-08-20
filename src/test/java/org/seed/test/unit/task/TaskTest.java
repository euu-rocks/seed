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
package org.seed.test.unit.task;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.seed.core.task.IntervalUnit;
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
	public void testIsEqual() {
		final Task task1 = new TaskMetadata();
		final Task task2 = new TaskMetadata();
		final Date dateStart = new Date();
		assertTrue(task1.isEqual(task2));
		
		task1.setName("test");
		((TaskMetadata) task1).setStartTime(dateStart);
		((TaskMetadata) task1).setRepeatInterval(123);
		((TaskMetadata) task1).setRepeatIntervalUnit(IntervalUnit.HOUR);
		((TaskMetadata) task1).setCronExpression("cron");
		((TaskMetadata) task1).setContent("content");
		((TaskMetadata) task1).setActive(true);
		assertFalse(task1.isEqual(task2));
		
		task2.setName("test");
		((TaskMetadata) task2).setStartTime(dateStart);
		((TaskMetadata) task2).setRepeatInterval(123);
		((TaskMetadata) task2).setRepeatIntervalUnit(IntervalUnit.HOUR);
		((TaskMetadata) task2).setCronExpression("cron");
		((TaskMetadata) task2).setContent("content");
		((TaskMetadata) task2).setActive(true);
		assertTrue(task1.isEqual(task2));
	}
	
	@Test
	public void testIsEqualParameters() {
		final Task task1 = new TaskMetadata();
		final Task task2 = new TaskMetadata();
		final TaskParameter parameter1 = new TaskParameter();
		final TaskParameter parameter2 = new TaskParameter();
		parameter1.setUid("test");
		parameter2.setUid("test");
		task1.addParameter(parameter1);
		assertFalse(task1.isEqual(task2));
		
		task2.addParameter(parameter2);
		assertTrue(task1.isEqual(task2));
		
		parameter2.setUid("other");
		assertFalse(task1.isEqual(task2));
	}
	
	@Test
	public void testIsEqualPermissions() {
		final Task task1 = new TaskMetadata();
		final Task task2 = new TaskMetadata();
		final TaskPermission permission1 = new TaskPermission();
		final TaskPermission permission2 = new TaskPermission();
		final List<TaskPermission> permissions1 = new ArrayList<>();
		final List<TaskPermission> permissions2 = new ArrayList<>();
		((TaskMetadata) task1).setPermissions(permissions1);
		((TaskMetadata) task2).setPermissions(permissions2);
		permission1.setUid("test");
		permission2.setUid("test");
		permissions1.add(permission1);
		assertFalse(task1.isEqual(task2));
		
		permissions2.add(permission2);
		assertTrue(task1.isEqual(task2));
		
		permission2.setUid("other");
		assertFalse(task1.isEqual(task2));
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
