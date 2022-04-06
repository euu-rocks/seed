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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.seed.core.config.LogLevel;
import org.seed.core.util.Assert;

@Entity
@Table(name = "sys_systemtask_run")
public class SystemTaskRun extends AbstractTaskRun {
	
	private SystemTask systemTask;
	
	@OneToMany(mappedBy = "run",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	private List<SystemTaskRunLog> logs;
	
	public SystemTask getSystemTask() {
		return systemTask;
	}

	public void setSystemTask(SystemTask systemTask) {
		this.systemTask = systemTask;
	}

	public List<SystemTaskRunLog> getLogs() {
		return logs;
	}

	public void addLog(SystemTaskRunLog log) {
		Assert.notNull(log, "log");
		
		if (logs == null) {
			logs = new ArrayList<>();
		}
		log.setRun(this);
		logs.add(log);
	}
	
	public void setLogs(List<SystemTaskRunLog> logs) {
		this.logs = logs;
	}
	
	public LogLevel getMaxLogLevel() {
		return getMaxLogLevel(logs);
	}

}
