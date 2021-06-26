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

import org.seed.C;
import org.seed.core.application.setting.ApplicationSettingService;
import org.seed.core.application.setting.Setting;
import org.seed.core.config.FullTextSearchProvider;
import org.seed.core.config.Limits;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.form.Form;
import org.seed.core.report.Report;
import org.seed.core.report.ReportFormat;
import org.seed.core.report.ReportService;
import org.seed.core.user.User;
import org.seed.core.user.UserService;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.ui.FormParameter;
import org.seed.ui.TabParameterMap;
import org.seed.ui.ViewParameterMap;
import org.seed.ui.zk.convert.Converters;
import org.seed.ui.zk.convert.DateTimeConverter;
import org.seed.ui.zk.convert.FileIconConverter;
import org.seed.ui.zk.convert.ImageConverter;
import org.seed.ui.zk.convert.StringConverter;
import org.seed.ui.zk.convert.TimeConverter;
import org.seed.ui.zk.convert.ValueConverter;

import org.springframework.security.core.context.SecurityContextHolder;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;

public abstract class AbstractApplicationViewModel extends AbstractViewModel {
	
	protected static final String ZUL_PATH = "~./zul";
	
	@WireVariable(value="applicationSettingServiceImpl")
	protected ApplicationSettingService settingService;
	
	@WireVariable(value="reportServiceImpl")
	protected ReportService reportService;
	
	@WireVariable(value="userServiceImpl")
	protected UserService userService;
	
	@WireVariable(value="fullTextSearchProvider")
	private FullTextSearchProvider fullTextSearch;
	
	@WireVariable(value="limits")
	private Limits limits;
	
	private boolean dirty;
	
	public final int getLimit(String limitName) {
		return limits.getLimit(limitName);
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
		return Converters.IMAGE_CONVERTER;
	}
	
	public final FileIconConverter getFileIconConverter() {
		return Converters.FILEICON_CONVERTER;
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
	
	public final List<Report> getUserReports() {
		return reportService.getReports(getUser());
	}
	
	public boolean isFullTextSearchAvailable() {
		return fullTextSearch.isFullTextSearchAvailable();
	}
	
	public final boolean isDirty() {
		return dirty;
	}

	public void flagDirty() {
		if (!dirty) {
			setDirty(true);
		}
	}
	
	protected final void resetDirty() {
		if (dirty) {
			setDirty(false);
		}
	}
	
	protected final User getUser() {
		User user = getSessionObject(C.USER);
		if (user == null) {
			user = userService.getCurrentUser();
			Assert.stateAvailable(user, C.USER);
			setSessionObject(C.USER, user);
		}
		return user;
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends Component> T getComponentById(String id) {
		Assert.notNull(id, C.ID);
		
		return (T) getComponent("/inc/" + id);
	}
	
	protected final void refreshMenu() {
		globalCommand("globalRefreshMenu", null);
	}
	
	protected final void downloadReport(Report report, ReportFormat format) 
		throws ValidationException {
		Filedownload.save(reportService.generateReport(report, format),
						  format.contentType,
						  report.getName() + '.' + format.fileType);
	}
	
	private void setDirty(boolean dirty) {
		this.dirty = dirty;
		notifyChange("isDirty");
	}
	
	protected static void openTab(Form form, ValueObject object) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(object, C.OBJECT);
		
		globalCommand("globalOpenTab", new TabParameterMap(form.getName(), 
														   "/form/detailform.zul", null, 
														   new FormParameter(form, object)));
	}
	
	protected static void showDialog(String view, Object param) {
		Assert.notNull(view, "view");
		
		createComponents(ZUL_PATH + view, param);
	}
	
	protected static void showView(String view, Object param) {
		Assert.notNull(view, "view");
	
		globalCommand("globalShowView", new ViewParameterMap(view, param));
	}
	
	protected static void logout() {
		Sessions.getCurrent().invalidate();
		SecurityContextHolder.clearContext();
		redirect("/seed");
	}
	
	protected static String getUserName() {
		return MiscUtils.geUserName();
	}
	
}
