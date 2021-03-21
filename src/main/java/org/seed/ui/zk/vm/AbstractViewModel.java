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
package org.seed.ui.zk.vm;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.seed.core.data.ValidationError;
import org.seed.core.form.LabelProvider;
import org.seed.core.util.ExceptionUtils;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Messagebox;

@VariableResolver(DelegatingVariableResolver.class)
abstract class AbstractViewModel {
	
	@WireVariable(value="ZKLabelProvider")
	private LabelProvider labelProvider;
	
	public String getLabel(String key, String ...params) {
		return labelProvider.getLabel(key, params);
	}
	
	public String getEnumLabel(Enum<?> enm) {
		return labelProvider.getEnumLabel(enm);
	}
	
	protected LabelProvider getLabelProvider() {
		return labelProvider;
	}
	
	protected final boolean hasSessionObject(String name) {
		Assert.notNull(name, "name is null");
		
		return Sessions.getCurrent().hasAttribute(name);
	}
	
	@SuppressWarnings("unchecked")
	protected final <T> T getSessionObject(String name) {
		Assert.notNull(name, "name is null");
		
		return (T) Sessions.getCurrent().getAttribute(name);
	}
	
	protected final void setSessionObject(String name, Object object) {
		Assert.notNull(name, "name is null");
		Assert.notNull(object, "object is null");
		
		Sessions.getCurrent().setAttribute(name, object);
	}
	
	protected final void removeSessionObject(String name) {
		Assert.notNull(name, "name is null");
		
		Sessions.getCurrent().removeAttribute(name);
	}
	
	protected final void confirm(String questionKey, final Component elem, final Object confirmParam, String ...params) {
		Assert.notNull(questionKey, "questionKey is null");
		
		Messagebox.show(getLabel(questionKey, params), 
						getLabel("question.sure"), 
						Messagebox.YES | Messagebox.NO, 
						Messagebox.QUESTION, 
						Messagebox.NO, 
						new EventListener<Event>() {
			
			public void onEvent(Event event) throws Exception {
				confirmed(Messagebox.ON_YES.equals(event.getName()), elem, confirmParam); 
			}
		});
	}
	
	protected void confirmed(boolean confirmed, Component elem, Object confirmParam) {
		throw new UnsupportedOperationException("method must be overwritten");
	}
	
	protected final void wireComponents(Component component) {
		Assert.notNull(component, "component is null");
		
		Selectors.wireComponents(component, this, false);
	}
	
	protected final void notifyChange(String ...properties) {
		notifyObjectChange(this, properties);
	}
	
	protected final void notifyChangeAll() {
		notifyChange("*");
	}
	
	protected static void createComponents(String view, Object param) {
		Assert.notNull(view, "view is null");
		
		Executions.createComponents(view, null, createParameterMap(param));
	}
	
	protected static Component getComponent(String path) {
		Assert.notNull(path, "path is null");
		
		return Path.getComponent(path);
	}
	
	protected static int getChildIndex(Component component) {
		Assert.notNull(component, "component is null");
		
		return component.getParent().getChildren().indexOf(component);
	}
	
	protected static InputStream getMediaStream(Media media, Charset charset) {
		Assert.notNull(media, "media is null");
		
		return media.isBinary()
				? media.getStreamData()
				: new ByteArrayInputStream(media.getStringData()
												.getBytes(charset != null 
															? charset 
															: Charset.defaultCharset()));
	}
	
	protected static void globalCommand(String command, Object param) {
		Assert.notNull(command, "command is null");
		
		BindUtils.postGlobalCommand(null, null, command, createParameterMap(param));
	}
	
	protected static void notifyObjectChange(Object object, String ...properties) {
		Assert.notNull(object, "object is null");
		
		BindUtils.postNotifyChange(null, null, object, properties);
	}
	
	protected static void redirect(String url) {
		Assert.notNull(url, "url is null");
		
		Executions.getCurrent().sendRedirect(url);
	}
	
	protected final void showNotification(Component component, boolean warning, String msgKey, String ...params) {
		Assert.notNull(msgKey, "msgKey is null");
		
		Clients.showNotification("<nobr>" + getLabel(msgKey, params) + "</nobr>", 
								 warning ? Clients.NOTIFICATION_TYPE_WARNING : Clients.NOTIFICATION_TYPE_INFO, 
								 component, 
								 "after_center",
								 warning ? 3000 : 2000,  // milliseconds
								 true); // closable
	}
	
	protected final void showValidationErrors(Component component, String errorKey, Set<ValidationError> validationErrors) {
		Assert.notNull(validationErrors, "validationErrors is null");
		
		final boolean isList = errorKey != null || validationErrors.size() > 1;
		final StringBuilder buf = new StringBuilder();
		if (errorKey != null) {
			buf.append(getLabel("val.error.header", getLabel(errorKey)));
		}
		if (isList) {
			buf.append("<ul>");
		}
		for (ValidationError error : validationErrors) {
			if (isList) {
				buf.append("<li>");
			}
			buf.append("<nobr>");
			if (ObjectUtils.isEmpty(error.getParameters())) {
				buf.append(getLabel(error.getError()));
			}
			else {
				final String[] params = error.getParameters();
				for (int i = 0; i < params.length; i++) {
					if (params[i].startsWith("label.")) {
						params[i] = getLabel(params[i]);
					}
				}
				buf.append(getLabel(error.getError(), params));
			}
			buf.append("</nobr>");
			if (isList) {
				buf.append("</li>");
			}
		}
		if (isList) {
			buf.append("</ul>");
		}
		Clients.showNotification(buf.toString(),
				 				 Clients.NOTIFICATION_TYPE_WARNING, 
				 				 component, 
				 				 "after_center",
				 				 5000 + (validationErrors.size() * 1000),
				 				 true); // closable
	}
	
	protected final void showError(Component component, String msgKey, String ...params) {
		Assert.notNull(msgKey, "msgKey is null");
		
		showError(component, "<nobr>" + getLabel(msgKey, params) + "</nobr>");
	}
	
	protected final void showError(Component component, Exception ex) {
		final String msg = StringUtils.hasText(ex.getMessage()) 
							? ex.getMessage()
							: ExceptionUtils.stackTraceAsString(ex);
		showError(component, msg);
	}
	
	protected final void showErrorMessage(String message) {
		Messagebox.show(message, getLabel("label.erroroccurred"), 
						Messagebox.OK, Messagebox.ERROR);
	}
	
	protected final void showWarnMessage(String message) {
		Messagebox.show(message, getLabel("label.warning"), 
						Messagebox.OK, Messagebox.EXCLAMATION);
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, Object> createParameterMap(Object param) {
		return param != null 
				? param instanceof Map 
					? (Map<String, Object>) param 
					: Collections.singletonMap("param", param)
				: null;
	}
	
	private void showError(Component component, String message) {
		Clients.showNotification(message, 
				 Clients.NOTIFICATION_TYPE_ERROR, 
				 component, 
				 "after_center",
				 5000,  // milliseconds
				 true); // closable
	}
	
}
