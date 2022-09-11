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

import org.junit.jupiter.api.Test;

import org.seed.core.config.LogLevel;
import org.seed.core.task.TaskRun;
import org.seed.core.task.TaskRunLog;

class TaskRunTest {
	
	@Test
	void testAddLog() {
		final TaskRun run = new TaskRun();
		final TaskRunLog log = new TaskRunLog();
		run.addLog(log);
		
		assertNotNull(run.getLogs());
		assertSame(1, run.getLogs().size());
		assertSame(log, run.getLogs().get(0));
	}
	
	@Test
	void testGetMaxLogLevel() {
		final TaskRun run = new TaskRun();
		TaskRunLog log = new TaskRunLog();
		assertEquals(LogLevel.INFO, run.getMaxLogLevel());
		
		log.setLevel(LogLevel.INFO);
		run.addLog(log);
		assertEquals(LogLevel.INFO, run.getMaxLogLevel());
		
		log = new TaskRunLog();
		log.setLevel(LogLevel.WARNING);
		run.addLog(log);
		assertEquals(LogLevel.WARNING, run.getMaxLogLevel());
		
		log = new TaskRunLog();
		log.setLevel(LogLevel.ERROR);
		run.addLog(log);
		assertEquals(LogLevel.ERROR, run.getMaxLogLevel());
		
	}
	
}
