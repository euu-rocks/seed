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
package org.seed.core.user;

import java.util.List;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.springframework.stereotype.Component;

@Component
public class UserGroupValidator extends AbstractSystemEntityValidator<UserGroup> {
	
	private List<UserGroupDependent<? extends SystemEntity>> userGroupDependents;
	
	@Override
	public void validateDelete(UserGroup userGroup) throws ValidationException {
		Assert.notNull(userGroup, C.USERGROUP);
		
		final ValidationErrors errors = new ValidationErrors();
		for (UserGroupDependent<? extends SystemEntity> dependent : getUserGroupDependents()) {
			for (SystemEntity systemEntity : dependent.findUsage(userGroup)) {
				switch (getEntityType(systemEntity)) {
					case "entity":
						errors.addError("val.inuse.groupentity", systemEntity.getName());
						break;
						
					case "filter":
						errors.addError("val.inuse.groupfilter", systemEntity.getName());
						break;
						
					case "transform":
						errors.addError("val.inuse.grouptransform", systemEntity.getName());
						break;
						
					case "report":
						errors.addError("val.inuse.groupreport", systemEntity.getName());
						break;
						
					case "rest":
						errors.addError("val.inuse.grouprest", systemEntity.getName());
						break;
						
					case "task":
						errors.addError("val.inuse.grouptask", systemEntity.getName());
						break;
						
					case "user":
						errors.addError("val.inuse.groupuser", systemEntity.getName());
						break;
					
					default:
						unhandledEntity(systemEntity);
				}
			}
		}
		validate(errors);
	}
	
	private List<UserGroupDependent<? extends SystemEntity>> getUserGroupDependents() {
		if (userGroupDependents == null) {
			userGroupDependents = MiscUtils.castList(getBeans(UserGroupDependent.class));
		}
		return userGroupDependents;
	}
	
}
