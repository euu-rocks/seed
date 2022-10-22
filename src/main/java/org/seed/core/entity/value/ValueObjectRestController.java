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

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.config.OpenSessionInViewFilter;
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
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/seed/rest/object")
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
	
	@ApiOperation(value = "findByEntityName", 
				  notes = "returns a list of all objects of entity with specified name")
	@GetMapping(value = "/{name}")
	public List<ValueObject> getObjects(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
										@PathVariable(C.NAME) String name) {
		Assert.notNull(session, C.SESSION);
		
		return service.getAllObjects(session, getEntity(session, name));
	}
	
	@ApiOperation(value = "findFilteredByEntityName", 
				  notes = "returns a list of entity objects with specified name filtered by filter with the given filter id")
	@GetMapping(value = "/{name}/filter/{filterid}")
	public List<ValueObject> findObjects(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
										 @PathVariable(C.NAME) String name, 
										 @PathVariable("filterid") Long filterid) {
		Assert.notNull(session, C.SESSION);
		
		return service.find(session, getEntity(session, name), getFilter(session, filterid));
	}
	
	@ApiOperation(value = "insertObject", 
				  notes = "insert a new entity object with specified name based on value map")
	@PostMapping(value = "/{name}")
	public ValueObject createObject(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
									@PathVariable(C.NAME) String name, 
									@RequestBody Map<String, Object> valueMap) {
		Assert.notNull(session, C.SESSION);
		
		final Entity entity = getEntity(session, name);
		checkEntityAccess(session, entity, EntityAccess.CREATE);
		return service.createObject(session, entity, valueMap);
	}
	
	@ApiOperation(value = "getByNameAndId", 
				  notes = "returns an entity object with specified id and entity name")
	@GetMapping(value = "/{name}/{id}")
	public ValueObject getObject(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
								 @PathVariable(C.NAME) String name, 
								 @PathVariable(C.ID) Long id) {
		Assert.notNull(session, C.SESSION);
		
		final ValueObject object = service.getObject(session, getEntity(session, name), id);
		if (object == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, name + ' ' + id);
		}
		return object;
	}
	
	@ApiOperation(value = "saveObject", 
				  notes = "updates an entity object with specified name and id based on value map")
	@PostMapping(value = "/{name}/{id}")
	public ValueObject updateObject(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
									@PathVariable(C.NAME) String name, 
									@PathVariable(C.ID) Long id,
									@RequestBody Map<String, Object> valueMap) {
		Assert.notNull(session, C.SESSION);
		
		final Entity entity = getEntity(session, name);
		checkEntityAccess(session, entity, EntityAccess.WRITE);
		try {
			return service.updateObject(session, entity, id, valueMap);
		}
		catch (ValidationException vex) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, name + ' ' + id);
		}
	}
	
	@ApiOperation(value = "changeObjectStatus", 
				  notes = "changes the entity status of an entity object with the given name and id to the status with the given status id")
	@PostMapping(value = "/{name}/{id}/status/{statusid}")
	public ValueObject changeStatus(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
									@PathVariable(C.NAME) String name, 
								    @PathVariable(C.ID) Long id,
								    @PathVariable("statusid") Long statusid) {
		Assert.notNull(session, C.SESSION);
		
		final Entity entity = getEntity(session, name);
		checkEntityAccess(session, entity, EntityAccess.WRITE);
		try {
			service.changeStatus(getObject(session, name, id), 
								 entity.getStatusById(statusid),
								 session, null);
			return getObject(session, name, id);
		}
		catch (ValidationException vex) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, name + ' ' + id);
		}
	}
	
	
	@ApiOperation(value = "transformObject", 
				  notes = "return an object based on an entity object with the given name and id transformed by a transformation with the given transformation id")
	@PostMapping(value = "/{name}/{id}/transform/{transformationid}")
	public ValueObject transformObject(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
									   @PathVariable(C.NAME) String name, 
		    						   @PathVariable(C.ID) Long id,
		    						   @PathVariable("transformationid") Long transformationid) {
		Assert.notNull(session, C.SESSION);
		
		return service.transform(getTransformer(session, transformationid), 
								 getObject(session, name, id),
								 session);
		
	}
	
	@ApiOperation(value = "callCustomFunction", 
				  notes = "calls a custom function with specific function id on an entity object with the given name and id and returns the object afterwards")
	@PostMapping(value = "/{name}/{id}/function/{functionid}")
	public ValueObject callFunction(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
									@PathVariable(C.NAME) String name, 
			   						@PathVariable(C.ID) Long id,
			   						@PathVariable("functionid") Long functionid) {
		Assert.notNull(session, C.SESSION);
		
		final ValueObject object = getObject(session, name, id);
		service.callUserActionFunction(session, object, getFunction(session, name, functionid));
		return object;
	}
	
	@ApiOperation(value = "deleteObject", 
				  notes = "deletes the entity object with specified id and entity name")
	@DeleteMapping(value = "/{name}/{id}")
	public void deleteObject(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
							 @PathVariable(C.NAME) String name, 
			 				 @PathVariable(C.ID) Long id) {
		Assert.notNull(session, C.SESSION);
		
		checkEntityAccess(session, getEntity(session, name), EntityAccess.DELETE);
		try {
			service.deleteObject(getObject(session, name, id), session, null);
		} 
		catch (ValidationException vex) {
			throw new ResponseStatusException(HttpStatus.LOCKED, name + ' ' + id);
		}
	}
	
	private Entity getEntity(Session session, String name) {
		Assert.notNull(session, C.SESSION);
		
		final Entity entity = entityService.findByName(name, session);
		if (entity == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.ENTITY + ' ' + name);
		}
		checkEntityAccess(session, entity, EntityAccess.READ);
		return entity;
	}
	
	private Filter getFilter(Session session, Long id) {
		Assert.notNull(session, C.SESSION);
		
		final Filter filter = filterService.getObject(id, session);
		if (filter == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.FILTER + ' ' + id);
		}
		if (!filter.checkPermissions(getUser(session))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, C.FILTER + ' ' + id);
		}
		return filter;
	}
	
	private EntityFunction getFunction(Session session, String name, Long functionId) {
		Assert.notNull(session, C.SESSION);
		
		final EntityFunction function = getEntity(session, name).getFunctionById(functionId);
		if (function == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.FUNCTION + ' ' + functionId);
		}
		return function;
	}
	
	private Transformer getTransformer(Session session, Long id) {
		Assert.notNull(session, C.SESSION);
		
		final Transformer transformer = transformerService.getObject(id, session);
		if (transformer == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.TRANSFORMER + ' ' + id);
		}
		if (!transformer.checkPermissions(getUser(session))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, C.TRANSFORMER + ' ' + id);
		}
		return transformer;
	}
	
	private void checkEntityAccess(Session session, Entity entity, EntityAccess access) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(entity, C.ENTITY);
		
		if (!entity.checkPermissions(getUser(session), access)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, access + " " + entity.getName());
		}
	}
	
	private User getUser(Session session) {
		final User user = userService.getCurrentUser(session);
		Assert.stateAvailable(user, C.USER);
		return user;
	}
	
}
