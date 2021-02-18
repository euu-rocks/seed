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

import org.seed.core.data.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/rest/entities")
public class EntityRestController {
	
	@Autowired
	private EntityService entityService;
	
	@GetMapping
	public List<Entity> findAll() {
		return entityService.findAllObjects();
	}
	
	@GetMapping(value = "/{id}")
	public Entity getEntity(@PathVariable("id") Long id) {
		final Entity entity = entityService.getObject(id);
		if (entity == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return entity;
	}
	
	@PutMapping(value = "/{id}")
	public Entity updateEntity(@RequestBody Entity entity, 
							   @PathVariable Long id) {
		// TODO
		
		return null;
	}
	
	@DeleteMapping(value = "/{id}")
	public void deleteEntity(@PathVariable Long id) {
		final Entity entity = entityService.getObject(id);
		if (entity == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		try {
			entityService.deleteObject(entity);
		} 
		catch (ValidationException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
