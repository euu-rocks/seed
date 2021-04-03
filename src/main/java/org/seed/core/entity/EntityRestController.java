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
package org.seed.core.entity;

import java.util.List;

import org.seed.core.application.AbstractRestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/rest/entity")
public class EntityRestController extends AbstractRestController<Entity> {
	
	@Autowired
	private EntityService entityService;
	
	@Override
	protected EntityService getService() {
		return entityService;
	}
	
	@Override
	public List<Entity> findAll() {
		return findAll(e -> checkPermissions(e, EntityAccess.READ));
	}
	
	@Override
	public Entity get(@PathVariable("id") Long id) {
		final Entity entity = super.get(id);
		if (!checkPermissions(entity, EntityAccess.READ)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		return entity;
	}

}
