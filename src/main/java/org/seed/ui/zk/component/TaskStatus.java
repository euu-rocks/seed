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
package org.seed.ui.zk.component;

import org.seed.core.task.TaskResult;

import org.zkoss.zul.A;

@SuppressWarnings("serial")
public class TaskStatus extends A {
	
	private static final String CURSOR_DEFAULT = "cursor:default";
	
	public TaskStatus() {
		setIconSclass("z-icon-circle alpha-icon-lg");
		setResult(null);
	}
	
	public void setResult(TaskResult result) {
		if (result != null) {
			switch (result) {
				case SUCCESS:
					setStyle("color:green;" + CURSOR_DEFAULT);
					break;
					
				case WARNING:
					setStyle("color:orange;" + CURSOR_DEFAULT);
					break;
					
				case ERROR:
					setStyle("color:red;" + CURSOR_DEFAULT);
					break;
			}
		}
		else {
			setStyle("color:lightgrey;" + CURSOR_DEFAULT);
		}
	}
	
}
