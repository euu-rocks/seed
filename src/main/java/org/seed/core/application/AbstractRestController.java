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
import java.util.stream.Collectors;

import org.seed.core.user.Authorisation;
import org.seed.core.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

public abstract class AbstractRestController<T extends ApplicationEntity> {
	
	@Autowired
	private UserService userService;
	
	@GetMapping
	public List<T> getAll() {
		return getService().getObjects();
	}
	
	protected List<T> getAll(Predicate<T> filter) {
		return getService().getObjects().stream()
						   .filter(filter).collect(Collectors.toList());
	}
	
	@GetMapping(value = "/{id}")
	public T get(@PathVariable("id") Long id) {
		final T object = getService().getObject(id);
		if (object == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return object;
	}
	
	protected boolean checkPermissions(ApplicationEntity object) {
		return checkPermissions(object, null);
	}
	
	protected boolean checkPermissions(ApplicationEntity object, Enum<?> access) {
		return object.checkPermissions(userService.getCurrentUser(), access);
	}
	
	protected boolean isAuthorised(Authorisation authorisation) {
		return userService.getCurrentUser().isAuthorised(authorisation);
	}
	
	protected abstract ApplicationEntityService<T> getService();
	
}
