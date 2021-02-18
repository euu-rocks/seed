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

import org.seed.core.user.User;
import org.seed.ui.zk.vm.admin.AbstractAdminViewModel;

import org.springframework.util.Assert;

public class PwdDialogParameter {
	
	final AbstractAdminViewModel<?> parentViewModel;
	
	final User user;

	public PwdDialogParameter(AbstractAdminViewModel<?> parentViewModel, User user) {
		Assert.notNull(parentViewModel, "parentViewModel is null");
		Assert.notNull(user, "user is null");
		
		this.parentViewModel = parentViewModel;
		this.user = user;
	}
	
}
