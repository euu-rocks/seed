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

import org.springframework.util.Assert;

public enum TaskResult {
	
	SUCCESS,
	WARNING,
	ERROR;
	
	public static TaskResult getResult(LogLevel level) {
		Assert.notNull(level, "level is null");
		
		switch (level) {
			case INFO:
				return SUCCESS;
				
			case WARN:
				return WARNING;
				
			case ERROR:
				return ERROR;
				
			default:
				throw new UnsupportedOperationException(level.name());
		}
	}
	
}
