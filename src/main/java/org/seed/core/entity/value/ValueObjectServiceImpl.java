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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import org.seed.core.data.AbstractSystemEntity;
import org.seed.core.data.Cursor;
import org.seed.core.data.FieldType;
import org.seed.core.data.FileObject;
import org.seed.core.data.Sort;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityDependent;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.filter.CriterionOperator;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterService;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.value.event.ValueObjectEvent;
import org.seed.core.entity.value.event.ValueObjectEventHandler;
import org.seed.core.entity.value.event.ValueObjectFunctionContext;
import org.seed.core.util.Tupel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

@Service
public class ValueObjectServiceImpl 
	implements ValueObjectService, EntityDependent, ValueObjectDependent {
	
	// dummy object; only last part of package name is important ("value")
	private final static SystemEntity VALUE_ENTITY = new AbstractSystemEntity() {};
	
	private final static int CHUNK_SIZE = 50;
	
	@Autowired
	private EntityService entityService;
	
	@Autowired
	private FilterService filterService;
	
	@Autowired
	private ValueObjectRepository repository;
	
	@Autowired
	private ValueObjectTransformer objectTransformer;
	
	@Autowired
	private ValueObjectEventHandler eventHandler;
	
	@Autowired
	private ValueObjectAccess objectAccess;
	
	@Autowired
	private ValueObjectValidator validator;
	
	@Autowired
	private FullTextSearch fullTextSearch;
	
	@Override
	public ValueObject createInstance(Entity entity, Session session, ValueObjectFunctionContext functionContext) {
		return repository.createInstance(entity, session, functionContext);
	}
	
	@Override
	public boolean notifyChange(ValueObject object) {
		return repository.notifyChange(object);
	}
	
	@Override
	public long count(Session session, Class<?> entityClass) {
		return repository.count(session, entityClass);
	}
	
	@Override
	public boolean existObjects(Entity entity) {
		return repository.exist(entity, null);
	}
	
	@Override
	public void copyFields(ValueObject sourceObject, 
						   ValueObject targetObject,
						   List<EntityField> entityFields) {
		Assert.notNull(sourceObject, "sourceObject is null");
		Assert.notNull(targetObject, "targetObject is null");
		Assert.state(!ObjectUtils.isEmpty(entityFields), "entityFields are empty");
		
		for (EntityField entityField : entityFields) {
			setValue(targetObject, entityField, getValue(sourceObject, entityField));
		}
	}
	
	@Override
	public ValueObject createInstance(Entity entity) {
		try (Session session = repository.getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				final ValueObject object = repository.createInstance(entity, session, null);
				tx.commit();
				return object;
			}
			catch (Exception ex) {
				if (tx != null) {
					tx.rollback();
				}
				throw ex;
			}
		}
	}
	
	@Override
	public Cursor createFullTextSearchCursor(Entity entity, String fullTextQueryString) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(fullTextQueryString, "fullTextQueryString is null");
		
		final List<Tupel<Long,Long>> fullTextSearchResult = fullTextSearch.query(entity, fullTextQueryString);
		return new Cursor(fullTextSearchResult, fullTextSearchResult.size(), CHUNK_SIZE);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Cursor createCursor(Entity entity, @Nullable Filter filter, Sort ...sort) {
		Assert.notNull(entity, "entity is null");
		
		try (Session session = repository.getSession()) {
			if (filter != null && filter.getHqlQuery() != null) {
				final Query query = session.createQuery("select count(*) " + filter.getHqlQuery());
				final Long totalSize = (Long) query.uniqueResult();
				return new Cursor(filter.getHqlQuery(), totalSize.intValue(), CHUNK_SIZE);
			}
			CriteriaQuery query = repository.buildQuery(session, entity, filter, true);
			final Long totalSize = repository.querySingleResult(session, query);
			query = repository.buildQuery(session, entity, filter, false, sort);
			return new Cursor(query, totalSize.intValue(), CHUNK_SIZE);
		}
	}
	
	@Override
	@SuppressWarnings({ "rawtypes" })
	public Cursor createCursor(ValueObject searchObject, Map<Long, Map<String, CriterionOperator>> criteriaMap, Sort ...sort) {
		Assert.notNull(searchObject, "searchObject is null");
		Assert.notNull(criteriaMap, "criteriaMap is null");
		
		try (Session session = repository.getSession()) {
			CriteriaQuery query = repository.buildQuery(session, searchObject, criteriaMap, true);
			final Long totalSize = repository.querySingleResult(session, query);
			query = repository.buildQuery(session, searchObject, criteriaMap, false, sort);
			return new Cursor(query, totalSize.intValue(), CHUNK_SIZE);
		}
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<ValueObject> loadChunk(Cursor cursor) {
		Assert.notNull(cursor, "cursor is null");
		
		if (cursor.getFullTextResult() != null) {
			return loadFullTextChunk(cursor);
		}
		try (Session session = repository.getSession()) {
			final Query query = cursor.getHqlQuery() != null
								? session.createQuery(cursor.getHqlQuery())
								: session.createQuery(cursor.getQuery());
			query.setFirstResult(cursor.getStartIndex());
			query.setMaxResults(cursor.getChunkSize());
			return query.getResultList();
		}
	}
	
	@Override
	public List<FileObject> getFileObjects(ValueObject object) {
		Assert.notNull(object, "object is null");
		
		final List<FileObject> fileObjects = new ArrayList<>();
		final Entity entity = repository.getEntity(object);
		collectFileObjects(object, entity, fileObjects);
		if (entity.hasAllNesteds()) {
			for (NestedEntity nested : entity.getAllNesteds()) {
				if (nested.getNestedEntity().hasAllFields() && hasNestedObjects(object, nested)) {
					for (ValueObject nestedObject : getNestedObjects(object, nested)) {
						collectFileObjects(nestedObject, nested.getNestedEntity(), fileObjects);
					}
				}
			}
		}
		return fileObjects;
	}
	
	@Override
	public String callUserActionFunction(ValueObject object, EntityFunction function) {
		Assert.notNull(object, "object is null");
		Assert.notNull(function, "function is null");
		
		String message = null;
		try (Session session = repository.getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				message = eventHandler.processUserEvent(new ValueObjectEvent(object, function, session));
				tx.commit();
				return message;
			}
			catch (Exception ex) {
				if (tx != null) {
					tx.rollback();
				}
				throw ex;
			}
		}
	}
	
	@Override
	public void preallocateFileObjects(ValueObject object) {
		setFileObjects(object, true);
	}
	
	@Override
	public boolean isEmpty(ValueObject object, EntityField field) {
		return getValue(object, field) == null;
	}
	
	@Override
	public Object getValue(ValueObject object, EntityField field) {
		return objectAccess.getValue(object, field);
	}
	
	@Override
	public void setValue(ValueObject object, EntityField field, Object value) {
		objectAccess.setValue(object, field, value);
	}
	
	@Override
	public List<ValueObject> getNestedObjects(ValueObject object, NestedEntity nested) {
		return objectAccess.getNestedObjects(object, nested);
	}
	
	@Override
	public boolean hasNestedObjects(ValueObject object, NestedEntity nested) {
		return objectAccess.hasNestedObjects(object, nested);
	}
	
	@Override
	public ValueObject addNestedInstance(ValueObject object, NestedEntity nested) {
		return objectAccess.addNestedInstance(object, nested);
	}
	
	@Override
	public void removeNestedObject(ValueObject object, NestedEntity nested, ValueObject nestedObject) {
		objectAccess.removeNestedObject(object, nested, nestedObject);
	}
	
	@Override
	public ValueObject getObject(Entity entity, Long objectId) {
		return repository.get(entity, objectId);
	}
	
	@Override
	public ValueObject getObject(Session session, Class<?> entityClass, Long id) {
		return repository.get(session, entityClass, id);
	}

	@Override
	public List<ValueObject> getAllObjects(Entity entity) {
		return repository.findAll(entity);
	}
	
	@Override
	public List<ValueObject> getAllObjects(Session session, Class<?> entityClass) {
		return repository.findAll(session, entityClass);
	}
	
	@Override
	public List<ValueObject> find(Session session, CriteriaQuery<?> query) {
		return repository.find(session, query);
	}
	
	@Override
	public List<ValueObject> find(Entity entity, Filter filter) {
		return repository.find(entity, filter);
	}
	
	@Override
	public ValueObject findUnique(Entity entity, EntityField entityField, Object value) {
		return findUnique(null, entity, entityField, value);
	}
	
	@Override
	public ValueObject findUnique(Session session, Entity entity, EntityField entityField, Object value) {
		Assert.notNull(entityField, "entityField is null");
		Assert.state(entityField.isUnique(), "entityField is not unique");
		
		final Filter filter = filterService.createFieldFilter(entity, entityField, value);
		return session != null 
				? repository.findUnique(session, entity, filter)
				: repository.findUnique(entity, filter);
	}
	
	@Override
	public void reloadObject(ValueObject object) {
		repository.reload(object);
	}
	
	@Override
	public void deleteObject(ValueObject object) throws ValidationException {
		validator.validateDelete(object);
		clearEmptyFileObjects(object);
		repository.delete(object);
	}
	
	@Override
	public void deleteObject(ValueObject object, Session session, ValueObjectFunctionContext functionContext) 
			throws ValidationException {
		validator.validateDelete(object);
		repository.delete(object, session, functionContext);
	}

	@Override
	public void saveObject(ValueObject object) throws ValidationException {
		validator.validateSave(object);
		repository.save(object);
		fullTextSearch.index(object);
	}
	
	@Override
	public void saveObject(ValueObject object, List<FileObject> deletedFiles) throws ValidationException {
		validator.validateSave(object);
		clearEmptyFileObjects(object);
		try (Session session = repository.getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				repository.save(object, session, null);
				if (deletedFiles != null) {
					deletedFiles.forEach(file -> session.delete(file));
				}
				tx.commit();
				fullTextSearch.index(object);
			}
			catch (Exception ex) {
				if (tx != null) {
					tx.rollback();
				}
				throw ex;
			}
		}
	}
	
	@Override
	public void saveObject(ValueObject object, Session session, ValueObjectFunctionContext functionContext) 
			throws ValidationException {
		validator.validateSave(object);
		repository.save(object, session, functionContext);
		fullTextSearch.index(object);
	}
	
	@Override
	public Session openSession() {
		return repository.getSession();
	}
	
	@Override
	public void changeStatus(ValueObject object, EntityStatus targetStatus) throws ValidationException {
		validator.validateChangeStatus(object, targetStatus);
		repository.changeStatus(object, targetStatus);
	}
	
	@Override
	public void changeStatus(ValueObject object, EntityStatus targetStatus,
			 Session session, ValueObjectFunctionContext functionContext) throws ValidationException {
		validator.validateChangeStatus(object, targetStatus);
		repository.changeStatus(object, targetStatus, session, functionContext);
	}
	
	@Override
	public void transform(Transformer transformer, ValueObject sourceObject, ValueObject targetObject) {
		objectTransformer.transform(transformer, targetObject, targetObject);
	}
	
	@Override
	public void transform(Transformer transformer, ValueObject targetObject, EntityField sourceObjectField) {
		objectTransformer.transform(transformer, targetObject, sourceObjectField);
	}
	
	@Override
	public ValueObject transform(Transformer transformer, ValueObject sourceObject) {
		Assert.notNull(transformer, "transformer is null");
		Assert.notNull(sourceObject, "sourceObject is null");
		
		final ValueObject targetObject = createInstance(transformer.getTargetEntity());
		objectTransformer.transform(transformer, sourceObject, targetObject);
		return targetObject;
	}
	
	@Override
	public List<? extends SystemEntity> findUsage(Entity entity) {
		if (repository.exist(entity, null)) {
			return Collections.singletonList(VALUE_ENTITY);
		}
		return Collections.emptyList();
	}

	@Override
	public List<? extends SystemEntity> findUsage(EntityField entityField) {
		return Collections.emptyList();
	}

	@Override
	public List<? extends SystemEntity> findUsage(EntityFieldGroup fieldGroup) {
		return Collections.emptyList();
	}

	@Override
	public List<? extends SystemEntity> findUsage(EntityStatus entityStatus) {
		final Entity entity = entityStatus.getEntity();
		final Filter filter = filterService.createStatusFilter(entity, entityStatus);
		if (repository.exist(entity, filter)) {
			return Collections.singletonList(VALUE_ENTITY);
		}
		return Collections.emptyList();
	}
	
	@Override
	public List<? extends SystemEntity> findUsage(EntityFunction entityFunction) {
		return Collections.emptyList();
	}

	@Override
	public List<? extends SystemEntity> findUsage(NestedEntity nestedEntity) {
		return Collections.emptyList();
	}
	
	@Override
	public List<? extends Entity> findUsage(ValueObject object) {
		final List<Entity> result = new ArrayList<>();
		final Entity entity = repository.getEntity(object);
		for (Entity otherEntity : entityService.findAllObjects()) {
			if (otherEntity.isGeneric() || entity.equals(otherEntity)) {
				continue;
			}
			for (EntityField otherRefField : otherEntity.getReferenceFields(entity)) {
				final Filter filter = filterService.createFieldFilter(otherEntity, otherRefField, object);
				if (repository.exist(otherEntity, filter)) {
					result.add(otherEntity);
					break;
				}
			}
		}
		return result;
	}
	
	private List<ValueObject> loadFullTextChunk(Cursor cursor) {
		final List<Tupel<Long, Long>> fullTextResult = cursor.getFullTextResult();
		final List<ValueObject> result = new ArrayList<>(CHUNK_SIZE);
		final int startIdx = cursor.getStartIndex();
		
		Entity entity = null;
		for (int i = startIdx; i < Math.min(startIdx + CHUNK_SIZE, fullTextResult.size()); i++) {
			final Tupel<Long, Long> fullTextResultRecord = fullTextResult.get(i);
			if (entity == null || !entity.getId().equals(fullTextResultRecord.x)) {
				entity = repository.getEntity(fullTextResultRecord.x);
			}
			final ValueObject object = repository.get(entity, fullTextResultRecord.y);
			Assert.state(object != null, "value object is not available. id:" + fullTextResultRecord.y);
			result.add(object);
		}
		return result;
	}
	
	private void clearEmptyFileObjects(ValueObject object) {
		setFileObjects(object, false);
	}
	
	private void collectFileObjects(ValueObject object, Entity entity, List<FileObject> fileObjects) {
		for (EntityField fileField : entity.getAllFieldsByType(FieldType.FILE)) {
			final FileObject fileObject = (FileObject) getValue(object, fileField);
			if (fileObject != null && fileObject.getContent() != null && !fileObject.isNew()) {
				fileObjects.add(fileObject);
			}
		}
	}
	
	private void setFileObjects(ValueObject object, boolean createObject) {
		Assert.notNull(object, "object is null");
		
		final Entity entity = repository.getEntity(object);
		setFileFields(object, entity, createObject);
		if (entity.hasAllNesteds()) {
			for (NestedEntity nested : entity.getAllNesteds()) {
				if (nested.getNestedEntity().hasAllFields() && hasNestedObjects(object, nested)) {
					for (ValueObject nestedObject : getNestedObjects(object, nested)) {
						setFileFields(nestedObject, nested.getNestedEntity(), createObject);
					}
				}
			}
		}
	}
	
	private void setFileFields(ValueObject object, Entity entity, boolean createObject) {
		for (EntityField fileField : entity.getAllFieldsByType(FieldType.FILE)) {
			if (!createObject) {
				final FileObject fileObject = (FileObject) getValue(object, fileField);
				if (fileObject != null && fileObject.getContent() == null) {
					setValue(object, fileField, null);
				}
			}
			else if (isEmpty(object, fileField)) {
				setValue(object, fileField, new FileObject());
			}
		}
	}

}
