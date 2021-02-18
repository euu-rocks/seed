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

import org.seed.core.data.ValidationException;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.form.Form;
import org.seed.core.report.Report;
import org.seed.core.report.ReportFormat;
import org.seed.core.report.ReportService;
import org.seed.core.user.User;
import org.seed.core.user.UserService;
import org.seed.core.util.MiscUtils;
import org.seed.ui.FormParameter;
import org.seed.ui.TabParameterMap;
import org.seed.ui.ViewParameterMap;
import org.seed.ui.zk.FileIconConverter;
import org.seed.ui.zk.ImageConverter;
import org.seed.ui.zk.StringConverter;
import org.seed.ui.zk.TimeConverter;
import org.seed.ui.zk.ValueConverter;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;

public abstract class AbstractApplicationViewModel extends AbstractViewModel {
	
	private static final StringConverter STRING_CONVERTER = new StringConverter();
	
	private static final ValueConverter VALUE_CONVERTER = new ValueConverter();
	
	private static final TimeConverter TIME_CONVERTER = new TimeConverter();
	
	private static final ImageConverter IMAGE_CONVERTER = new ImageConverter();
	
	private static final FileIconConverter FILEICON_CONVERTER = new FileIconConverter();
	
	protected static final String ZUL_PATH = "~./zul";
	
	@WireVariable(value="reportServiceImpl")
	protected ReportService reportService;
	
	@WireVariable(value="userServiceImpl")
	protected UserService userService;
	
	private boolean dirty;
	
	public final StringConverter getStringConverter() {
		return STRING_CONVERTER;
	}
	
	public final ValueConverter getValueConverter() {
		return VALUE_CONVERTER;
	}
	
	public final TimeConverter getTimeConverter() {
		return TIME_CONVERTER;
	}
	
	public final ImageConverter getImageConverter() {
		return IMAGE_CONVERTER;
	}
	
	public final FileIconConverter getFileIconConverter() {
		return FILEICON_CONVERTER;
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
		User user = getSessionObject("user");
		if (user == null) {
			user = userService.getCurrentUser();
			Assert.state(user != null, "user not available");
			setSessionObject("user", user);
		}
		return user;
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends Component> T getComponentById(String id) {
		Assert.notNull(id, "id is null");
		
		return (T) getComponent("/inc/" + id);
	}
	
	protected final void refreshMenu() {
		globalCommand("_refreshMenu", null);
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
		globalCommand("_contentDirty", dirty); 
	}
	
	protected static void openTab(Form form, ValueObject object) {
		Assert.notNull(form, "form is null");
		Assert.notNull(object, "object is null");
		
		globalCommand("_openTab", new TabParameterMap(form.getName(), 
													  "/form/detailform.zul", null, 
													  new FormParameter(form, object)));
	}
	
	protected static void showDialog(String view, Object param) {
		Assert.notNull(view, "view is null");
		
		createComponents(ZUL_PATH + view, param);
	}
	
	protected static void showView(String view, Object param) {
		Assert.notNull(view, "view is null");
	
		globalCommand("_showView", new ViewParameterMap(view, param));
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
