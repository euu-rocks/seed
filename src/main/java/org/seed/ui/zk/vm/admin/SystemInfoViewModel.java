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
package org.seed.ui.zk.vm.admin;

import java.util.Date;
import java.util.List;

import org.hibernate.stat.Statistics;

import org.seed.C;
import org.seed.core.config.DatabaseInfo;
import org.seed.core.config.SchemaManager;
import org.seed.core.config.SessionProvider;
import org.seed.core.config.SystemLog;
import org.seed.core.config.SystemLog.LogEntry;
import org.seed.core.task.job.JobStatistics;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.springframework.core.env.Environment;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Statistic;

public class SystemInfoViewModel extends AbstractApplicationViewModel {
	
	@WireVariable(value="environment")
	private Environment environment;
	
	@WireVariable(value="defaultSessionProvider")
	private SessionProvider sessionProvider;
	
	@WireVariable(value="schemaManager")
	private SchemaManager schemaManager;
	
	@WireVariable(value="jobStatistics")
	private JobStatistics jobStatistics;
	
	@WireVariable(value="systemLog")
	private SystemLog systemLog;
	
	private LogEntry logEntry;
	
	public String getVersion() {
		return environment.getProperty("info.app.version");
	}
	
	public LogEntry getLogEntry() {
		return logEntry;
	}

	public void setLogEntry(LogEntry logEntry) {
		this.logEntry = logEntry;
	}

	public List<LogEntry> getLogEntries() {
		return systemLog.getEntries();
	}
	
	public Date getStartTime() {
		return new Date(getZkStatistic().getStartTime());
	}
	
	public Date getConfigStartTime() {
		return new Date(getHbnStatistic().getStartTime());
	}
	
	public String getMaxQueryTime() {
		return MiscUtils.formatDurationTime(getHbnStatistic().getQueryExecutionMaxTime());
	}
	
	public String getUpTime() {
		return MiscUtils.formatDuration(getZkStatistic().getStartTime());
	}
	
	public String getOsInfo() {
		return System.getProperty("os.name") + " (" + 
			   System.getProperty("os.arch") + ')';
	}
	
	public String getJvmInfo() {
		return System.getProperty("java.vm.name");
	}
	
	public String getJvmVersionInfo() {
		return System.getProperty("java.version") + " (" + 
			   System.getProperty("java.version.date") + ')';
	}
	
	public String getMemMaxInfo() {
		return MiscUtils.formatMemorySize(Runtime.getRuntime().maxMemory());
	}
	
	public String getMemAllocInfo() {
		return MiscUtils.formatMemorySize(Runtime.getRuntime().totalMemory());
	}
	
	public String getMemUsedInfo() {
		return MiscUtils.formatMemorySize(Runtime.getRuntime().totalMemory() - 
										  Runtime.getRuntime().freeMemory());
	}
	
	public String getJobTotalDuration() {
		return jobStatistics.getTotalRunsDurationTime() > 0 
				? MiscUtils.formatDurationTime(jobStatistics.getTotalRunsDurationTime()) 
				: null;
	}
	
	public String getJobLongestDuration() {
		return jobStatistics.getLongestRunDurationTime() > 0 
				? MiscUtils.formatDurationTime(jobStatistics.getLongestRunDurationTime())
				: null;	
	}
	
	public String getJobAverageDuration() {
		return jobStatistics.getAverageDurationTime() > 0 
				? MiscUtils.formatDurationTime(jobStatistics.getAverageDurationTime())
				: null;
	}
	
	public Statistic getZkStatistic() {
		final Statistic statistic = (Statistic) WebApps.getCurrent().getConfiguration().getMonitor();
		Assert.stateAvailable(statistic, "statistic");
		
		return statistic;
	}
	
	public Statistics getHbnStatistic() {
		return sessionProvider.getStatistics();
	}
	
	public JobStatistics getJobStatistics() {
		return jobStatistics;
	}
	
	public DatabaseInfo getDbInfo() {
		return schemaManager.getDatabaseInfo();
	}
	
	@Command
	public void refresh(@BindingParam(C.ELEM) Component component) {
		notifyChangeAll();
	}
	
	public String formatAverage(Integer total, Double average) {
		if (new Date().getTime() - getZkStatistic().getStartTime() < 3600000) { // < 1h
			return String.valueOf(total);
		}
		return String.valueOf(average.intValue());
	}
	
}
