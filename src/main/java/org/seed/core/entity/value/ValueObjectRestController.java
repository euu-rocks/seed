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
package org.seed.core.entity.value;

import java.util.List;
import java.util.Map;

import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityAccess;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterService;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerService;
import org.seed.core.user.User;
import org.seed.core.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/rest")
public class ValueObjectRestController {
	
	@Autowired
	private EntityService entityService;
	
	@Autowired
	private FilterService filterService;
	
	@Autowired
	private TransformerService transformerService;
	
	@Autowired
	private ValueObjectService service;
	
	@Autowired
	private UserService userService;
	
	@GetMapping(value = "/{name}")
	public List<ValueObject> getObjects(@PathVariable("name") String name) {
		return service.getAllObjects(getEntity(name));
	}
	
	@GetMapping(value = "/{name}/filter/{filterid}")
	public List<ValueObject> findObjects(@PathVariable("name") String name, 
										 @PathVariable("filterid") Long filterid) {
		return service.find(getEntity(name), getFilter(filterid));
	}
	
	@PostMapping(value = "/{name}")
	public ValueObject createObject(@PathVariable("name") String name, 
									@RequestBody Map<String, Object> valueMap) {
		final Entity entity = getEntity(name);
		checkEntityAccess(entity, EntityAccess.CREATE);
		return service.createObject(entity, valueMap);
	}
	
	@GetMapping(value = "/{name}/{id}")
	public ValueObject getObject(@PathVariable("name") String name, 
								 @PathVariable("id") Long id) {
		final ValueObject object = service.getObject(getEntity(name), id);
		if (object == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, name + ' ' + id);
		}
		return object;
	}
	
	@PostMapping(value = "/{name}/{id}")
	public ValueObject updateObject(@PathVariable("name") String name, 
									@PathVariable("id") Long id,
									@RequestBody Map<String, Object> valueMap) {
		final Entity entity = getEntity(name);
		checkEntityAccess(entity, EntityAccess.WRITE);
		try {
			return service.updateObject(entity, id, valueMap);
		}
		catch (ValidationException vex) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, name + ' ' + id);
		}
	}
	
	@PostMapping(value = "/{name}/{id}/status/{statusid}")
	public ValueObject changeStatus(@PathVariable("name") String name, 
								    @PathVariable("id") Long id,
								    @PathVariable("statusid") Long statusid) {
		final Entity entity = getEntity(name);
		checkEntityAccess(entity, EntityAccess.WRITE);
		try {
			service.changeStatus(getObject(name, id), 
								 entity.getStatusById(statusid));
			return getObject(name, id);
		}
		catch (ValidationException vex) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, name + ' ' + id);
		}
	}
	
	@PostMapping(value = "/{name}/{id}/transform/{transformationid}")
	public ValueObject transformObject(@PathVariable("name") String name, 
		    						   @PathVariable("id") Long id,
		    						   @PathVariable("transformationid") Long transformationid) {
		return service.transform(getTransformer(transformationid), 
								 getObject(name, id));
		
	}
	
	@PostMapping(value = "/{name}/{id}/function/{functionid}")
	public ValueObject callFunction(@PathVariable("name") String name, 
			   						@PathVariable("id") Long id,
			   						@PathVariable("functionid") Long functionid) {
		final ValueObject object = getObject(name, id);
		service.callUserActionFunction(object, getFunction(name, functionid));
		return object;
	}
	
	@DeleteMapping(value = "/{name}/{id}")
	public void deleteObject(@PathVariable("name") String name, 
			 				 @PathVariable("id") Long id) {
		final Entity entity = getEntity(name);
		checkEntityAccess(entity, EntityAccess.DELETE);
		try {
			service.deleteObject(getObject(name, id));
		} 
		catch (ValidationException vex) {
			throw new ResponseStatusException(HttpStatus.LOCKED, name + ' ' + id);
		}
	}
	
	private Entity getEntity(String name) {
		final Entity entity = entityService.findByName(name);
		if (entity == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "entity:" + name);
		}
		checkEntityAccess(entity, EntityAccess.READ);
		return entity;
	}
	
	private Filter getFilter(Long id) {
		final Filter filter = filterService.getObject(id);
		if (filter == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "filter:" + id);
		}
		if (!filter.checkPermissions(userService.getCurrentUser(), null)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "filter:" + id);
		}
		return filter;
	}
	
	private EntityFunction getFunction(String name, Long functionId) {
		final EntityFunction function = getEntity(name).getFunctionById(functionId);
		if (function == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "function:" + functionId);
		}
		return function;
	}
	
	private Transformer getTransformer(Long id) {
		final Transformer transformer = transformerService.getObject(id);
		if (transformer == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "transformation:" + id);
		}
		if (!transformer.checkPermissions(userService.getCurrentUser(), null)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "transformation:" + id);
		}
		return transformer;
	}
	
	private void checkEntityAccess(Entity entity, EntityAccess access) {
		Assert.notNull(entity, "entity is null");
		
		final User user = userService.getCurrentUser();
		Assert.state(user != null, "user not available");
		if (!entity.checkPermissions(user, access)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, access + " " + entity.getName());
		}
	}
	
}