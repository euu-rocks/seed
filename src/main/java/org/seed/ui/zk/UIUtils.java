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

import java.util.Collections;
import java.util.Map;

import org.seed.C;
import org.seed.Seed;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;
import org.seed.core.util.MiscUtils;

import org.zkoss.bind.BindUtils;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Statistic;
import org.zkoss.zul.Messagebox;

public abstract class UIUtils {
	
	private static final String ZUL_PATH = "~./zul";
	
	private static final String MARKER_SCHEMAERROR_WARNING = " MARKER_SCHEMAERROR_WARNING";
	
	protected UIUtils() {}
	
	public static byte[] getBytes(Media media) {
		if (media != null) {
			return media.isBinary() 
					? media.getByteData()
					: media.getStringData().getBytes(MiscUtils.CHARSET);
		}
		return MiscUtils.EMPTY_BYTE_ARRAY;
	}
	
	public static Statistic getStatistic() {
		final Statistic statistic = (Statistic) 
			WebApps.getCurrent().getConfiguration().getMonitor();
		Assert.stateAvailable(statistic, "statistic");
		
		return statistic;
	}
	
	public static Component getComponent(String path) {
		Assert.notNull(path, C.PATH);
		
		return Path.getComponent(path);
	}
	
	public static String getZulPath(String view) {
		Assert.notNull(view, C.VIEW);
		
		return ZUL_PATH.concat(view);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getRequestObject(String name) {
		Assert.notNull(name, C.NAME);
		
		return (T) Executions.getCurrent().getAttribute(name);
	}
	
	public static void setRequestAttribute(String name, Object value) {
		Assert.notNull(name, C.NAME);
		
		Executions.getCurrent().setAttribute(name, value);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getSessionObject(String name) {
		Assert.notNull(name, C.NAME);
		
		return (T) Sessions.getCurrent().getAttribute(name);
	}
	
	public static void setSessionObject(String name, Object object) {
		Assert.notNull(name, C.NAME);
		Assert.notNull(object, C.OBJECT);
		
		Sessions.getCurrent().setAttribute(name, object);
	}
	
	public static int getSessionKeepAliveIntervalMs() {
		final int sessionTimeoutSec = Sessions.getCurrent().getMaxInactiveInterval();
		if (sessionTimeoutSec > 60) {
			return (sessionTimeoutSec - 60) * 1000;
		}
		return (sessionTimeoutSec / 4) * 3000;
	}
	
	public static String getTestClass(Object object, String suffix) {
		if (object != null && suffix != null) {
			final String name = BeanUtils.callGetter(object, "internalName");
			if (name != null) {
				return name.replace('_', '-').toLowerCase().concat(suffix);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static void showDialog(String view, Object param) {
		Executions.createComponents(getZulPath(view), null, 
				param instanceof Map 
					? (Map<String, Object>) param 
					: Collections.singletonMap(C.PARAM, param));
	}
	
	public static void showErrorMessage(String title, String message) {
		Messagebox.show(message, title, Messagebox.OK, Messagebox.ERROR);
	}
	
	public static void showWarnMessage(String title, String message) {
		Messagebox.show(message, title, Messagebox.OK, Messagebox.EXCLAMATION);
	}
	
	public static void showWarnMessage(String titleKey, String[] messageKeys) {
		final var buf = new StringBuilder();
		for (String messageKey : messageKeys) {
			if (buf.length() > 0) {
				buf.append("\n\n");
			}
			buf.append(Seed.getLabel(messageKey));
		}
		showWarnMessage(Seed.getLabel(titleKey), buf.toString());
	}
	
	public static void showError(Component component, String message) {
		showNotification(component, Clients.NOTIFICATION_TYPE_ERROR, 5000, message);
	}
	
	public static void showNotification(Component component, String type, int duration, String message) {
		Clients.showNotification(message, type, component, "after_center", duration, true);
	}
	
	public static void showSchemaErrorWarning() {
		// show only once per session lifetime
		if (getSessionObject(MARKER_SCHEMAERROR_WARNING) == null) {
			setSessionObject(MARKER_SCHEMAERROR_WARNING, C.TRUE);
			showWarnMessage("warn.schemaupdate.title",
					new String[] {"warn.schemaupdate.message1",
								  "warn.schemaupdate.message2",
								  "warn.schemaupdate.message3"});
		}
	}
	
	public static void globalCommand(String command, Map<String, Object> paramMap) {
		Assert.notNull(command, C.COMMAND);
		
		BindUtils.postGlobalCommand(null, null, command, paramMap);
	}
	
	public static void notifyChange(Object object, String ...properties) {
		Assert.notNull(object, C.OBJECT);
		
		BindUtils.postNotifyChange(object, properties);
	}
	
	public static void redirect(String url) {
		Assert.notNull(url, C.URL);
		
		Executions.getCurrent().sendRedirect(url);
	}
	
	public static void openNewTab(String url) {
		Assert.notNull(url, C.URL);
		
		Executions.getCurrent().sendRedirect(url, "new");
	}
	
	public static void wireComponents(Component component, Object model) {
		Assert.notNull(component, C.COMPONENT);
		
		Selectors.wireComponents(component, model, false);
	}
	
}
