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
package org.seed.core.form.layout;

import static org.seed.core.util.CollectionUtils.filterAndForEach;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

class LayoutCache {
	
	private static final long TIMER_CHECK_INTERVAL		 =  60 * 60 * 1000; // 1h
	private static final long MAX_TIME_SINCE_LAST_ACCESS = 180 * 60 * 1000; // 3h
	
	private final Map<String, Long> timestampMap = new ConcurrentHashMap<>();
	private final Map<String, LayoutElement> layoutMap = new ConcurrentHashMap<>();
	
	LayoutCache() {
		new Timer("LayoutCacheTask").schedule(new RemoveAbandonedTask(), 
											  TIMER_CHECK_INTERVAL, TIMER_CHECK_INTERVAL);
	}
	
	void registerLayout(String name, LayoutElement root) {
		updateTimestamp(name);
		layoutMap.put(name, root);
	}
	
	LayoutElement getLayout(String name) {
		if (timestampMap.containsKey(name)) {
			updateTimestamp(name);
		}
		return layoutMap.get(name);
	}
	
	void removeLayout(String name) {
		timestampMap.remove(name);
		layoutMap.remove(name);
	}
	
	private void updateTimestamp(String name) {
		timestampMap.put(name, System.currentTimeMillis());
	}
	
	private class RemoveAbandonedTask extends TimerTask {
		
		@Override
		public void run() {
			final long minTime = System.currentTimeMillis() - MAX_TIME_SINCE_LAST_ACCESS;
			filterAndForEach(timestampMap.entrySet(), 
							 entry -> entry.getValue() < minTime, 
							 entry -> removeLayout(entry.getKey()));
		}
		
	}

}
