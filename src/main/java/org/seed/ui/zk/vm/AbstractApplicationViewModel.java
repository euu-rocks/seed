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

import java.util.List;
import java.util.TimeZone;

import org.hibernate.Session;

import org.seed.C;
import org.seed.Seed;
import org.seed.core.application.setting.ApplicationSettingService;
import org.seed.core.application.setting.Setting;
import org.seed.core.config.ApplicationProperties;
import org.seed.core.config.FullTextSearchProvider;
import org.seed.core.config.Limits;
import org.seed.core.config.OpenSessionInViewFilter;
import org.seed.core.config.SessionProvider;
import org.seed.core.customcode.CustomCode;
import org.seed.core.customcode.CustomLib;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationException;
import org.seed.core.data.datasource.IDataSource;
import org.seed.core.data.dbobject.DBObject;
import org.seed.core.entity.Entity;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.transfer.Transfer;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.form.Form;
import org.seed.core.form.navigation.Menu;
import org.seed.core.report.Report;
import org.seed.core.report.ReportFormat;
import org.seed.core.report.ReportService;
import org.seed.core.task.Task;
import org.seed.core.user.User;
import org.seed.core.user.UserService;
import org.seed.core.util.Assert;
import org.seed.ui.FormParameter;
import org.seed.ui.TabParameterMap;
import org.seed.ui.ViewParameterMap;
import org.seed.ui.zk.convert.Converters;
import org.seed.ui.zk.convert.DateTimeConverter;
import org.seed.ui.zk.convert.DurationConverter;
import org.seed.ui.zk.convert.FileIconConverter;
import org.seed.ui.zk.convert.ImageConverter;
import org.seed.ui.zk.convert.MemorySizeConverter;
import org.seed.ui.zk.convert.StringConverter;
import org.seed.ui.zk.convert.TimeConverter;
import org.seed.ui.zk.convert.ValueConverter;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;

public abstract class AbstractApplicationViewModel extends AbstractViewModel {
	
	protected static final String DEFAULT_APPLICATION_NAME = StringUtils.capitalize(C.SEED);
	
	private static final String INCLUDE_PATH = "/inc/";  //NOSONAR
	
	@WireVariable(value="defaultSessionProvider")
	private SessionProvider sessionProvider;
	
	@WireVariable(value="applicationProperties")
	private ApplicationProperties applicationProperties;
	
	@WireVariable(value="applicationSettingServiceImpl")
	protected ApplicationSettingService settingService;
	
	@WireVariable(value="fullTextSearchProvider")
	protected FullTextSearchProvider fullTextSearchProvider;
	
	@WireVariable(value="reportServiceImpl")
	protected ReportService reportService;
	
	@WireVariable(value="userServiceImpl")
	protected UserService userService;
	
	@WireVariable(value="limits")
	private Limits limits;
	
	private boolean dirty;
	
	public final int getLimit(String limitName) {
		return limits.getLimit(limitName);
	}
	
	public final String getDefaultApplicationName() {
		return DEFAULT_APPLICATION_NAME;
	}
	
	public final String getDefaultApplicationTimeZone() {
		return TimeZone.getDefault().getID();
	}
	
	protected final String getSetting(Setting setting) {
		return settingService.getSetting(setting);
	}
	
	protected final String getSettingOrNull(Setting setting) {
		return settingService.getSettingOrNull(setting);
	}
	
	public final StringConverter getStringConverter() {
		return Converters.STRING_CONVERTER;
	}
	
	public final ImageConverter getImageConverter() {
		return Converters.getImageConverter();
	}
	
	public final FileIconConverter getFileIconConverter() {
		return Converters.getFileIconConverter();
	}
	
	public final ValueConverter getValueConverter() {
		return Converters.getValueConverter();
	}
	
	public final DateTimeConverter getDateTimeConverter() {
		return Converters.getDateTimeConverter();
	}
	
	public final TimeConverter getTimeConverter() {
		return Converters.getTimeConverter();
	}
	
	public final DurationConverter getDurationConverter() {
		return Converters.getDurationConverter();
	}
	
	public final MemorySizeConverter getMemorySizeConverter() {
		return Converters.getMemorySizeConverter();
	}
	
	public final List<Report> getUserReports() {
		return reportService.getReports(getUser(), currentSession());
	}
	
	public final boolean isAPIJavadocAvailable() {
		return applicationProperties.hasProperty(Seed.PROP_EXTERN_API_JAVADOC_URL);
	}
	
	public boolean isFullTextSearchAvailable() {
		return fullTextSearchProvider.isFullTextSearchAvailable();
	}
	
	public final boolean isDirty() {
		return dirty;
	}

	public void flagDirty() {
		if (!dirty) {
			setDirty(true);
		}
	}
	
	protected final String getEntityName(SystemEntity systemEntity) {
		if (systemEntity == null) {
			return null;
		}
		else if (systemEntity instanceof Entity) {
			return getLabel("label.entity");
		}
		else if (systemEntity instanceof Filter) {
			return getLabel("label.filter");
		}
		else if (systemEntity instanceof Transfer) {
			return getLabel("label.transfer");
		}
		else if (systemEntity instanceof Transformer) {
			return getLabel("label.transformer");
		}
		else if (systemEntity instanceof Form) {
			return getLabel("label.form");
		}
		else if (systemEntity instanceof Menu) {
			return getLabel("label.menu");
		}
		else if (systemEntity instanceof Task) {
			return getLabel("label.job");
		}
		else if (systemEntity instanceof DBObject) {
			return getLabel("label.dbobject");
		}
		else if (systemEntity instanceof IDataSource) {
			return getLabel("label.datasource");
		}
		else if (systemEntity instanceof Report) {
			return getLabel("label.report");
		}
		else if (systemEntity instanceof CustomCode) {
			return getLabel("label.customcode");
		}
		else if (systemEntity instanceof CustomLib) {
			return getLabel("label.customlib");
		}
		else {
			throw new UnsupportedOperationException(systemEntity.getClass().getSimpleName());
		}
	}
	
	protected void showAPIJavadoc() {
		openNewTab(applicationProperties.getProperty(Seed.PROP_EXTERN_API_JAVADOC_URL));
	}
	
	protected final void resetDirty() {
		if (dirty) {
			setDirty(false);
		}
	}
	
	protected final User getUser() {
		User user = getSessionObject(C.USER);
		if (user == null) {
			user = userService.getCurrentUser(currentSession());
			Assert.stateAvailable(user, C.USER);
			setSessionObject(C.USER, user);
		}
		return user;
	}
	
	protected final void resetCurrentSession() {
		setRequestAttribute(OpenSessionInViewFilter.ATTR_SESSION, sessionProvider.getSession());
	}
	
	protected final void downloadReport(Report report, ReportFormat format) 
		throws ValidationException {
		Filedownload.save(reportService.generateReport(report, format, currentSession()),
						  format.contentType,
						  report.getName() + '.' + format.fileType);
	}
	
	private void setDirty(boolean dirty) {
		this.dirty = dirty;
		notifyChange("isDirty");
	}
	
	@SuppressWarnings("unchecked")
	protected static <T extends Component> T getComponentById(String id) {
		Assert.notNull(id, C.ID);
		
		return (T) getComponent(INCLUDE_PATH.concat(id));
	}
	
	protected static void refreshMenu() {
		globalCommand("globalRefreshMenu", null);
	}
	
	protected static Session currentSession() {
		final Session session = getRequestObject(OpenSessionInViewFilter.ATTR_SESSION);
		Assert.stateAvailable(session, "current session");
		return session;
	}
	
	protected static void openTab(Form form, ValueObject object) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(object, C.OBJECT);
		
		globalCommand("globalOpenTab", new TabParameterMap(form.getName(), 
														   "/form/detailform.zul", null, 
														   new FormParameter(form, object)));
	}
	
	protected static void showView(String view, Object param) {
		Assert.notNull(view, C.VIEW);
	
		globalCommand("globalShowView", new ViewParameterMap(view, param));
	}
	
	protected static void logout() {
		Sessions.getCurrent().invalidate();
		SecurityContextHolder.clearContext();
		redirect("/seed");
	}
	
}
