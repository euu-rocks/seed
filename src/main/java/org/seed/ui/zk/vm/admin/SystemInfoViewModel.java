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

import org.hibernate.stat.Statistics;

import org.seed.C;
import org.seed.Seed;
import org.seed.core.config.SessionProvider;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Statistic;

public class SystemInfoViewModel extends AbstractApplicationViewModel {
	
	@WireVariable(value="seed")
	private Seed seed;
	
	@WireVariable(value="defaultSessionProvider")
	private SessionProvider sessionProvider;
	
	public String getVersion() {
		return seed.getVersion();
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
	
	public Statistic getZkStatistic() {
		final Statistic statistic = (Statistic) WebApps.getCurrent().getConfiguration().getMonitor();
		Assert.stateAvailable(statistic, "statistic");
		
		return statistic;
	}
	
	public Statistics getHbnStatistic() {
		return sessionProvider.getStatistics();
	}
	
	@Command
	public void refresh(@BindingParam(C.ELEM) Component component) {
		notifyChangeAll();
	}
	
	public static String formatAverage(Double average) {
		return String.valueOf(average.intValue());
	}
	
}
