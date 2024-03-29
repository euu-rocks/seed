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

import static org.seed.ui.zk.component.ComponentUtils.*;

import org.seed.core.task.TaskResult;

import org.zkoss.zul.A;

public class TaskStatus extends A {
	
	private static final long serialVersionUID = 5036600708041436064L;

	public TaskStatus() {
		setIconSclass(ICON_CIRCLE);
		setResult(null);
	}
	
	public void setResult(TaskResult result) {
		if (result != null) {
			switch (result) {
				case SUCCESS:
					setStyle(STYLE_INFO);
					break;
					
				case WARNING:
					setStyle(STYLE_WARNIUNG);
					break;
					
				case ERROR:
					setStyle(STYLE_ERROR);
					break;
					
				default:
					throw new UnsupportedOperationException(result.name());
			}
		}
		else {
			setStyle(STYLE_UNDEFINED);
		}
	}
	
}
