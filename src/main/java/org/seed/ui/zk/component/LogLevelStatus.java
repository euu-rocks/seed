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

import org.seed.core.config.LogLevel;

import org.zkoss.zul.A;

@SuppressWarnings("serial")
public class LogLevelStatus extends A {
	
	public LogLevelStatus() {
		setIconSclass(ICON_CIRCLE);
		setLogLevel(null);
	}
	
	public void setLogLevel(LogLevel level) {
		if (level != null) {
			switch (level) {
				case INFO:
					setStyle(STYLE_INFO);
					break;
					
				case WARNING:
					setStyle(STYLE_WARNIUNG);
					break;
					
				case ERROR:
					setStyle(STYLE_ERROR);
					break;
				
				default:
					throw new UnsupportedOperationException(level.name());
			}
		}
		else {
			setStyle(STYLE_ERROR);
		}
	}
	
}
