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

import java.util.Date;

import org.seed.core.task.TaskRun;

import org.springframework.stereotype.Component;

@Component
public class JobStatistics {
	
	private int totalRuns;
	
	private int successfulRuns;
	
	private int failedRuns;
	
	private long totalRunsDurationTime;
	
	private long longestRunDurationTime;
	
	private Date lastJobRunDate;
	
	public int getTotalRuns() {
		return totalRuns;
	}

	public int getSuccessfulRuns() {
		return successfulRuns;
	}

	public int getFailedRuns() {
		return failedRuns;
	}

	public long getTotalRunsDurationTime() {
		return totalRunsDurationTime;
	}

	public long getLongestRunDurationTime() {
		return longestRunDurationTime;
	}
	
	public Date getLastJobRunDate() {
		return lastJobRunDate;
	}

	public long getAverageDurationTime() {
		return totalRuns > 0 
				? totalRunsDurationTime / totalRuns 
				: 0;
	}

	void registerRun(TaskRun taskRun) {
		totalRunsDurationTime += taskRun.getDuration();
		lastJobRunDate = taskRun.getEndTime();
		if (taskRun.getDuration() > longestRunDurationTime) {
			longestRunDurationTime = taskRun.getDuration();
		}
		
		totalRuns++;
		if (taskRun.getResult().failed()) {
			failedRuns++;
		}
		else {
			successfulRuns++;
		}
	}
	
}
