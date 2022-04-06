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

import javax.persistence.MappedSuperclass;

import org.seed.core.config.LogLevel;
import org.seed.core.data.AbstractSystemObject;

@MappedSuperclass
public abstract class AbstractTaskRun extends AbstractSystemObject {
	
	private Date startTime;
	
	private Date endTime;
	
	private TaskResult result;

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public long getDuration() {
		return startTime != null && endTime != null 
				? endTime.getTime() - startTime.getTime() 
				: -1L;
	}

	public TaskResult getResult() {
		return result;
	}

	public void setResult(TaskResult result) {
		this.result = result;
	}
	
	protected static <T extends AbstractTaskRunLog> LogLevel getMaxLogLevel(List<T> logs) {
		LogLevel maxLevel = LogLevel.INFO;
		if (logs != null) {
			for (T log : logs) {
				if (log.getLevel().ordinal() > maxLevel.ordinal()) {
					maxLevel = log.getLevel();
				}
			}
		}
		return maxLevel;
	}
	
}
