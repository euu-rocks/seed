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

import org.hibernate.Session;

import org.seed.C;
import org.seed.Seed;
import org.seed.core.data.AbstractSystemEntityRepository;
import org.seed.core.task.TaskService;
import org.seed.core.util.Assert;

import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends AbstractSystemEntityRepository<User> {
	
	protected UserRepository() {
		super(UserMetadata.class);
	}
	
	@Override
	public void delete(User user, Session session) {
		Assert.notNull(user, C.USER);
		Assert.notNull(session, C.SESSION);
		
		((UserMetadata) user).setUserGroups(null);
		Seed.getBean(TaskService.class).removeNotifications(user, session);
		super.delete(user, session);
	}
	
	Session openSession() {
		return super.getSession();
	}
	
}
