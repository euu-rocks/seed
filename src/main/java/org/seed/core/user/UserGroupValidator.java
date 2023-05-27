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

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserGroupValidator extends AbstractSystemEntityValidator<UserGroup> {
	
	@Autowired
	private UserGroupRepository repository;
	
	private List<UserGroupDependent<? extends SystemEntity>> userGroupDependents;
	
	@Override
	public void validateDelete(UserGroup userGroup) throws ValidationException {
		Assert.notNull(userGroup, C.USERGROUP);
		final ValidationErrors errors = createValidationErrors(userGroup);
		
		try (Session session = repository.openSession()) {
			for (UserGroupDependent<? extends SystemEntity> dependent : getUserGroupDependents()) {
				validateDeleteDependent(userGroup, dependent, errors, session);
			}
		}
		validate(errors);
	}
	
	private void validateDeleteDependent(UserGroup userGroup, UserGroupDependent<? extends SystemEntity> dependent,
										 ValidationErrors errors, Session session) {
		for (SystemEntity systemEntity : dependent.findUsage(userGroup, session)) {
			switch (getEntityType(systemEntity)) {
			case C.ENTITY:
				errors.addError("val.inuse.groupentity", systemEntity.getName());
				break;

			case C.FILTER:
				errors.addError("val.inuse.groupfilter", systemEntity.getName());
				break;

			case C.TRANSFORM:
				errors.addError("val.inuse.grouptransform", systemEntity.getName());
				break;

			case C.REPORT:
				errors.addError("val.inuse.groupreport", systemEntity.getName());
				break;

			case C.REST:
				errors.addError("val.inuse.grouprest", systemEntity.getName());
				break;

			case C.TASK:
				errors.addError("val.inuse.grouptask", systemEntity.getName());
				break;

			case C.USER:
				errors.addError("val.inuse.groupuser", systemEntity.getName());
				break;

			default:
				unhandledEntity(systemEntity);
			}
		}
	}
	
	private List<UserGroupDependent<? extends SystemEntity>> getUserGroupDependents() {
		if (userGroupDependents == null) {
			userGroupDependents = MiscUtils.castList(getBeans(UserGroupDependent.class));
		}
		return userGroupDependents;
	}
	
}
