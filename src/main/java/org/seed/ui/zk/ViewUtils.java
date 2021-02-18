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
package org.seed.ui.zk;

import org.seed.ui.settings.ViewSettings;

import org.springframework.util.Assert;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

public abstract class ViewUtils {
	
	private final static String SESSION_VIEWSETTINGS = "viewSettings";
	
	public static ViewSettings getSettings() {
		return getSettings(Sessions.getCurrent());
	}
	
	public static ViewSettings getSettings(Session session) {
		Assert.notNull(session, "session is null");
		
		ViewSettings settings = (ViewSettings) session.getAttribute(SESSION_VIEWSETTINGS);
		if (settings == null) {
			settings = new ViewSettings();
			session.setAttribute(SESSION_VIEWSETTINGS, settings);
		}
		return settings;
	}
	
}
