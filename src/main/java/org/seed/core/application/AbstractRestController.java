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
package org.seed.core.application;

import java.util.List;
import java.util.function.Predicate;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.user.Authorisation;
import org.seed.core.user.User;
import org.seed.core.user.UserService;
import org.seed.core.util.Assert;
import org.seed.core.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class AbstractRestController<T extends ApplicationEntity> {
	
	@Autowired
	private UserService userService;
	
	protected List<T> getAll(Session session) {
		Assert.notNull(session, C.SESSION);
		
		return getService().getObjects(session);
	}
	
	protected List<T> getAll(Session session, Predicate<T> filter) {
		Assert.notNull(session, C.SESSION);
		
		return CollectionUtils.subList(getService().getObjects(session), filter);
	}
	
	protected T get(Session session, Long id) {
		Assert.notNull(session, C.SESSION);
		
		final T object = getService().getObject(id, session);
		if (object == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return object;
	}
	
	protected boolean checkPermissions(Session session, ApplicationEntity object) {
		Assert.notNull(session, C.SESSION);
		
		return checkPermissions(session, object, null);
	}
	
	protected boolean checkPermissions(Session session, ApplicationEntity object, Enum<?> access) {
		Assert.notNull(session, C.SESSION);
		
		return object.checkPermissions(getUser(session), access);
	}
	
	protected boolean isAuthorised(Session session, Authorisation authorisation) {
		Assert.notNull(session, C.SESSION);
		
		return getUser(session).isAuthorised(authorisation);
	}
	
	protected User getUser(Session session) {
		final User user = userService.getCurrentUser(session);
		Assert.stateAvailable(user, C.USER);
		return user;
	}
	
	protected abstract ApplicationEntityService<T> getService();
	
}
