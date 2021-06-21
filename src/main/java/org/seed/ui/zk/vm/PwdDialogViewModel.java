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
import org.seed.core.user.User;
import org.seed.core.util.Assert;
import org.seed.ui.zk.vm.admin.AbstractAdminViewModel;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Window;

public class PwdDialogViewModel extends AbstractApplicationViewModel {
	
	@Wire("#pwdDialogWin")
	private Window window;
	
	private AbstractAdminViewModel<?> parentViewModel;
	
	private User user;
	
	private String password;
	
	private String passwordRepeated;
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordRepeated() {
		return passwordRepeated;
	}

	public void setPasswordRepeated(String passwordRepeated) {
		this.passwordRepeated = passwordRepeated;
	}

	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam("param") PwdDialogParameter param) {
		Assert.notNull(param, "param");
		wireComponents(view);
		
		parentViewModel = param.parentViewModel;
		user = param.user;
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
	@Command
	public void applyPwd(@BindingParam("elem") Component component) {
		try {
			userService.setPassword(user, password, passwordRepeated);
			parentViewModel.flagDirty();
			window.detach();
		}
		catch (ValidationException vex) {
			showValidationErrors(component, "admin.user.setpwdfail", vex.getErrors());
		}
	}
	
}
