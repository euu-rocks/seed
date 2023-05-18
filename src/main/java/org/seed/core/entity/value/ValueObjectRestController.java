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
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.core.config.OpenSessionInViewFilter;
import org.seed.core.data.FieldAccess;
import org.seed.core.data.FileObject;
import org.seed.core.data.QueryCursor;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityAccess;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterService;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerService;
import org.seed.core.user.User;
import org.seed.core.user.UserService;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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
	private UserService userService;
	
	@Autowired
	private ValueObjectService service;
	
	@ApiOperation(value = "findByEntityName", 
				  notes = "returns a list of all objects of entity with the specified name")
	@GetMapping(value = "/{name}")
	public List<ValueObject> getObjects(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
										@PathVariable(C.NAME) String name) {
		Assert.notNull(session, C.SESSION);
		final Entity entity = getEntity(session, name);
		final User user = getUser(session);
		
		try {
			return removeInvisibleFields(service.getAllObjects(session, entity), entity, user);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
	
	@ApiOperation(value = "loadChunkByEntityName", 
				  notes = "returns a chunk of objects of entity with the specified name starting from the specified index with the specified size")
	@GetMapping(value = "/{name}/{index}/{size}")
	public List<ValueObject> getObjectChunk(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
											@PathVariable(C.NAME) String name,
											@PathVariable("index") Integer index,
											@PathVariable("size") Integer size) {
		Assert.notNull(session, C.SESSION);
		final Entity entity = getEntity(session, name);
		final User user = getUser(session);
		
		QueryCursor<ValueObject> cursor;
		try {
			cursor = service.createCursor(entity, size);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
		if (index >= cursor.getTotalCount()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Index is too big " + index);
		}
		cursor.setIndex(index);
		try {
			return removeInvisibleFields(service.loadChunk(cursor), entity, user);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
	
	@ApiOperation(value = "countByEntityName", 
			  notes = "returns the total number of all objects of entity with the specified name")
	@GetMapping(value = "/{name}/count")
	public Long countObjects(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
							 @PathVariable(C.NAME) String name) {
		Assert.notNull(session, C.SESSION);
		final Entity entity = getEntity(session, name);
		
		try {
			return service.count(entity, session);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
	
	@ApiOperation(value = "findFilteredByEntityName", 
				  notes = "returns a list of entity objects with the specified name filtered by the filter with the given filter id")
	@GetMapping(value = "/{name}/filter/{filterid}")
	public List<ValueObject> findObjects(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
										 @PathVariable(C.NAME) String name, 
										 @PathVariable("filterid") Long filterid) {
		Assert.notNull(session, C.SESSION);
		final Entity entity = getEntity(session, name);
		final Filter filter = getFilter(session, filterid);
		final User user = getUser(session);
		
		try {
			return removeInvisibleFields(service.find(session, entity, filter), entity, user);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
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
		
		try {
			return service.createObject(session, entity, valueMap);
		}
		catch (ValidationException vex) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, vex.getMessage());
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
	
	@ApiOperation(value = "validateObject", 
				  notes = "validates a new entity object with specified name based on value map")
	@PostMapping(value = "/{name}/validate")
	public List<String> validateObject(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
									   @PathVariable(C.NAME) String name, 
									   @RequestBody Map<String, Object> valueMap) {
		Assert.notNull(session, C.SESSION);
		final Entity entity = getEntity(session, name);
		checkEntityAccess(session, entity, EntityAccess.CREATE);
		
		try {
			return service.validateObject(session, entity, null, valueMap);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
	
	@ApiOperation(value = "getByNameAndId", 
				  notes = "returns an entity object with specified id and entity name")
	@GetMapping(value = "/{name}/{id}")
	public ValueObject getObject(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
								 @PathVariable(C.NAME) String name, 
								 @PathVariable(C.ID) Long id) {
		Assert.notNull(session, C.SESSION);
		final Entity entity = getEntity(session, name);
		final User user = getUser(session);
		final ValueObject object = getObjectByNameAndId(session, name, id);
		try {
			return service.removeInvisibleFields(object, entity, user);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
	
	@ApiOperation(value = "getObjectContent",
			      notes = "streams the file contents of the field with the specified field name of the entity with the specified name and id")
	@GetMapping(value = "/{name}/{id}/content/{fieldname}")
	public ResponseEntity<ByteArrayResource> getFieldContent(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
			 												 @PathVariable(C.NAME) String name, 
			 												 @PathVariable(C.ID) Long id,
			 												 @PathVariable("fieldname") String fieldName) {
		return stream(session, name, id, fieldName, false);
	}
	
	@ApiOperation(value = "downloadObjectFile", 
				  notes = "downloads the file of the field with the specified field name of the entity with the specified name and id")
	@GetMapping(value = "/{name}/{id}/file/{fieldname}")
	public ResponseEntity<ByteArrayResource> getFieldFile(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
			 											  @PathVariable(C.NAME) String name, 
			 											  @PathVariable(C.ID) Long id,
			 											  @PathVariable("fieldname") String fieldName) {
		return stream(session, name, id, fieldName, true);
	}
	
	@ApiOperation(value = "uploadObjectFile", 
		  	  	  notes = "uploads the file specified by parameter 'file' in the field with specific field name of the entity with specified name and id")
	@PostMapping(value = "/{name}/{id}/file/{fieldname}")
	public ValueObject saveFieldFile(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
									 @PathVariable(C.NAME) String name, 
									 @PathVariable(C.ID) Long id,
									 @PathVariable("fieldname") String fieldName,
									 @RequestParam("file") MultipartFile file) {
		if (file == null) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "no file");
		}
		final EntityField field = getEntityField(session, name, fieldName);
		checkEntityAccess(session, field.getEntity(), EntityAccess.WRITE);
		if (!field.getType().isFile()) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "field type is not FILE");
		}
		
		final User user = getUser(session);
		final ValueObject object = getObjectByNameAndId(session, name, id);
		checkFieldAccess(field.getEntity(), field, object, user, FieldAccess.WRITE);
		
		final FileObject fileObject = new FileObject();
		fileObject.setName(file.getOriginalFilename());
		fileObject.setContentType(file.getContentType());
		try {
			fileObject.setContent(file.getBytes());
			return service.saveFieldContent(object, field, fileObject, session);
		}
		catch (ValidationException vex) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, vex.getMessage());
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
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
		final User user = getUser(session);
		checkEntityAccess(session, entity, EntityAccess.WRITE);
		
		try {
			return service.removeInvisibleFields(service.updateObject(session, entity, id, valueMap), entity, user);
		}
		catch (ValidationException vex) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, vex.getMessage());
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
	
	@ApiOperation(value = "validateObject", 
			  	  notes = "validates an entity object with specified name and id based on value map")
	@PostMapping(value = "/{name}/{id}/validate")
	public List<String> validateObject(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
									   @PathVariable(C.NAME) String name, 
									   @PathVariable(C.ID) Long id,
									   @RequestBody Map<String, Object> valueMap) {
		Assert.notNull(session, C.SESSION);
		final Entity entity = getEntity(session, name);
		checkEntityAccess(session, entity, EntityAccess.WRITE);
		
		try {
			return service.validateObject(session, entity, id, valueMap);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
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
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			service.changeStatus(getObjectByNameAndId(session, name, id), 
								 getStatus(entity, statusid), 
								 session, null);
			tx.commit();
			return service.removeInvisibleFields(getObjectByNameAndId(session, name, id), entity, getUser(session));
		}
		catch (Exception ex) {
			if (tx != null) {
				tx.rollback();
			}
			if (ex instanceof ResponseStatusException) {
				throw (ResponseStatusException) ex;
			}
			else if (ex instanceof ValidationException) {
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
			}
			else {
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
			}
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
		
		final var transformer = getTransformer(session, transformationid);
		final User user = getUser(session);
		final ValueObject sourceObject = getObjectByNameAndId(session, name, id);
		
		try {
			final var targetObject = service.transform(transformer, sourceObject, session);
			return transformer.getTargetEntity().checkPermissions(user, EntityAccess.READ)
					? service.removeInvisibleFields(targetObject, transformer.getTargetEntity(), user)
					: null;
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
	
	@ApiOperation(value = "callCustomFunction", 
				  notes = "calls a custom function with specific function id on an entity object with the given name and id and returns the object afterwards")
	@PostMapping(value = "/{name}/{id}/function/{functionid}")
	public ValueObject callFunction(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
									@PathVariable(C.NAME) String name, 
			   						@PathVariable(C.ID) Long id,
			   						@PathVariable("functionid") Long functionid) {
		Assert.notNull(session, C.SESSION);
		final Entity entity = getEntity(session, name);
		final EntityFunction function = getFunction(session, name, functionid);
		final ValueObject object = getObjectByNameAndId(session, name, id);
		final User user = getUser(session);
		
		try {
			service.callUserActionFunction(session, object, function);
			return service.removeInvisibleFields(object, entity, user);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
	
	@ApiOperation(value = "deleteObject", 
				  notes = "deletes the entity object with specified id and entity name")
	@DeleteMapping(value = "/{name}/{id}")
	public void deleteObject(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
							 @PathVariable(C.NAME) String name, 
			 				 @PathVariable(C.ID) Long id) {
		Assert.notNull(session, C.SESSION);
		checkEntityAccess(session, getEntity(session, name), EntityAccess.DELETE);
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			service.deleteObject(getObjectByNameAndId(session, name, id), session, null);
			tx.commit();
		} 
		catch (Exception ex) {
			if (tx != null) {
				tx.rollback();
			}
			if (ex instanceof ResponseStatusException) {
				throw (ResponseStatusException) ex;
			}
			else if (ex instanceof ValidationException) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
			}
			else {
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
			}
		}
	}
	
	@ApiOperation(value = "deleteObjectFile", 
			  	  notes = "deletes the file of the field with specific field name of the entity object with specified id and entity name")
	@DeleteMapping(value = "/{name}/{id}/file/{fieldname}")
	public ValueObject deleteFieldFile(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
									   @PathVariable(C.NAME) String name, 
									   @PathVariable(C.ID) Long id,
									   @PathVariable("fieldname") String fieldName) {
		final EntityField field = getEntityField(session, name, fieldName);
		checkEntityAccess(session, field.getEntity(), EntityAccess.WRITE);
		if (!field.getType().isFile()) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "field type is not FILE");
		}
		final User user = getUser(session);
		final ValueObject object = getObjectByNameAndId(session, name, id);
		checkFieldAccess(field.getEntity(), field, object, user, FieldAccess.WRITE);
		
		try {
			return service.saveFieldContent(object, field, null, session);
		}
		catch (ValidationException vex) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, vex.getMessage());
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
	
	private ResponseEntity<ByteArrayResource> stream(Session session, String name, Long id, String fieldName, boolean download) {
		final EntityField field = getEntityField(session, name, fieldName);
		if (!field.getType().isFile()) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
		}
		final ValueObject object = getObjectByNameAndId(session, name, id);
		final User user = getUser(session);
		checkFieldAccess(field.getEntity(), field, object, user, FieldAccess.READ);
		
		final FileObject file = service.getValue(object, field);
		if (file == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no file available");
		}
		
		final BodyBuilder builder = ResponseEntity.ok();
		if (download) {
			builder.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
				   .contentType(MediaType.APPLICATION_OCTET_STREAM);
		}
		else {
			builder.contentType(MediaType.parseMediaType(file.getContentType()));
		}
		return builder.body(new ByteArrayResource(file.getContent()));
	}
	
	private EntityField getEntityField(Session session, String name, String fieldName) {
		final EntityField field = getEntity(session, name).getFieldByName(fieldName);
		if (field == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.FIELD + ' ' + fieldName);
		}
		return field;
	}
	
	private EntityFunction getFunction(Session session, String name, Long functionId) {
		final EntityFunction function = getEntity(session, name).getFunctionById(functionId);
		if (function == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.FUNCTION + ' ' + functionId);
		}
		return function;
	}
	
	private EntityStatus getStatus(Entity entity, Long statusId) {
		final EntityStatus status = entity.getStatusById(statusId);
		if (status == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.STATUS + ' ' + statusId);
		}
		return status;
	}
	
	private Entity getEntity(Session session, String name) {
		Entity entity = null;
		try {
			entity = entityService.findByName(name, session);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
		if (entity == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.ENTITY + ' ' + name);
		}
		checkEntityAccess(session, entity, EntityAccess.READ);
		return entity;
	}
	
	private Filter getFilter(Session session, Long id) {
		Filter filter = null;
		try {
			filter = filterService.getObject(id, session);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
		if (filter == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.FILTER + ' ' + id);
		}
		if (!filter.checkPermissions(getUser(session))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, C.FILTER + ' ' + id);
		}
		return filter;
	}
	
	private Transformer getTransformer(Session session, Long id) {
		Transformer transformer = null;
		try {
			transformer = transformerService.getObject(id, session);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
		if (transformer == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.TRANSFORMER + ' ' + id);
		}
		if (!transformer.checkPermissions(getUser(session))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, C.TRANSFORMER + ' ' + id);
		}
		return transformer;
	}
	
	private ValueObject getObjectByNameAndId(Session session, String name, Long id) {
		final Entity entity = getEntity(session, name);
		ValueObject object = null;
		try {
			object = service.getObject(session, entity, id);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
		if (object == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, name + ' ' + id);
		}
		return object;
	}
	
	private User getUser(Session session) {
		User user = null;
		try {
			user = userService.getCurrentUser(session);
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user not authorized");
		}
		return user;
	}
	
	private void checkEntityAccess(Session session, Entity entity, EntityAccess access) {
		if (!entity.checkPermissions(getUser(session), access)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, entity.getName());
		}
	}
	
	private void checkFieldAccess(Entity entity, EntityField field, ValueObject object, User user, FieldAccess access) {
		if (!entity.checkFieldAccess(field, user, object.getEntityStatus(), access)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, entity.getName() + '.' + field.getInternalName());
		}
	}
	
	private List<ValueObject> removeInvisibleFields(List<ValueObject> objects, Entity entity, User user) {
		objects.forEach(object -> service.removeInvisibleFields(object, entity, user));
		return objects;
	}
	
}
