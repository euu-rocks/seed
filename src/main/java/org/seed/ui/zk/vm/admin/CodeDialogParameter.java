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
package org.seed.ui.zk.vm.admin;

import org.seed.core.application.ContentObject;
import org.seed.core.util.Assert;

import org.springframework.util.StringUtils;

class CodeDialogParameter {
	
	final AbstractAdminViewModel<?> parentViewModel;
	
	final ContentObject contentObject;
	
	public CodeDialogParameter(AbstractAdminViewModel<?> parentViewModel, ContentObject contentObject) {
		Assert.notNull(parentViewModel, "parentViewModel");
		Assert.notNull(contentObject, "contentObject");
		Assert.state(StringUtils.hasText(contentObject.getContent()), "no content");
		
		this.parentViewModel = parentViewModel;
		this.contentObject = contentObject;
	}
	
}
