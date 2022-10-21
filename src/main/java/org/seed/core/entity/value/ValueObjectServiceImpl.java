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

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.data.AbstractSystemEntity;
import org.seed.core.data.QueryCursor;
import org.seed.core.data.FieldType;
import org.seed.core.data.FileObject;
import org.seed.core.data.Sort;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityDependent;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityRelation;
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
import org.seed.core.util.Assert;
import org.seed.core.util.ExceptionUtils;
import org.seed.core.util.Tupel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class ValueObjectServiceImpl 
	implements ValueObjectService, EntityDependent<SystemEntity>, ValueObjectDependent<Entity> {
	
	// dummy object; only last part of package name is important ("value")
	private static final SystemEntity VALUE_ENTITY = new AbstractSystemEntity() {};
	
	private final Comparator<ValueObject> objectComparator =
			(ValueObject vo1, ValueObject vo2) -> getIdentifier(vo1).compareTo(getIdentifier(vo2));
	
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
	private List<ValueObjectChangeAware> changeAwareObjects;
	
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
		return entity.isGeneric()
				? anyMatch(entityService.findDescendants(entity), 
						   descendant -> repository.exist(descendant, null))
				: repository.exist(entity, null);
	}
	
	@Override
	public boolean existObjects(Entity entity, Session session) {
		return entity.isGeneric()
				? anyMatch(entityService.findDescendants(entity), 
						   descendant -> repository.exist(descendant, null))
				: repository.exist(session, entity, null);
	}
	
	@Override
	public String getIdentifier(ValueObject object) {
		return repository.getIdentifier(object);
	}
	
	@Override
	public String getIdentifier(ValueObject object, Session session) {
		return repository.getIdentifier(object, session);
	}
	
	@Override
	public void copyFields(ValueObject sourceObject, 
						   ValueObject targetObject,
						   List<EntityField> entityFields) {
		Assert.notNull(sourceObject, "sourceObject");
		Assert.notNull(targetObject, "targetObject");
		Assert.notNull(entityFields, "entity fields");
		
		entityFields.forEach(field -> setValue(targetObject, field, 
											   getValue(sourceObject, field)));
	}
	
	@Override
	public ValueObject createInstance(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
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
	public ValueObject createObject(Session session, Entity entity, Map<String,Object> valueMap) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(entity, C.ENTITY);
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			final ValueObject object = repository.createInstance(entity, session, null);
			if (setObjectValues(session, entity, object, valueMap)) {
				saveObject(object, session, null);
			}
			tx.commit();
			return object;
		}
		catch (Exception ex) {
			if (tx != null) {
				tx.rollback();
			}
			throw new InternalException(ex);
		}
	}
	
	// von FullTextSearchViewMode aufgerufen
	@Override
	public QueryCursor<FullTextResult> createFullTextSearchCursor(String fullTextQueryString) {
		Assert.notNull(fullTextQueryString, "fullTextQueryString");
		
		final List<Tupel<Long,Long>> fullTextSearchResult = fullTextSearch.query(fullTextQueryString, null);
		return new QueryCursor<>(fullTextQueryString, fullTextSearchResult, ValueObjectRepository.DEFAULT_CHUNK_SIZE);
	}
	
	// von ListFormViewModel aufgerufen
	@Override
	public QueryCursor<ValueObject> createFullTextSearchCursor(String fullTextQueryString, Entity entity) {
		Assert.notNull(fullTextQueryString, "fullTextQueryString");
		
		final List<Tupel<Long,Long>> fullTextSearchResult = fullTextSearch.query(fullTextQueryString, entity);
		return new QueryCursor<>(fullTextQueryString, fullTextSearchResult, ValueObjectRepository.DEFAULT_CHUNK_SIZE);
	}
	
	@Override
	public QueryCursor<ValueObject> createCursor(Entity entity, @Nullable Filter filter, Sort ...sort) {
		Assert.notNull(entity, C.ENTITY);
		
		return repository.createCursor(entity, filter, ValueObjectRepository.DEFAULT_CHUNK_SIZE, sort);
	}
	
	@Override
	public QueryCursor<ValueObject> createCursor(Session session, Entity entity, @Nullable Filter filter, int chunkSize, Sort ...sort) {
		Assert.notNull(entity, C.ENTITY);
		
		return repository.createCursor(session, entity, filter, chunkSize, sort);
	}
	
	@Override
	public QueryCursor<ValueObject> createCursor(Entity entity, int chunkSize) {
		Assert.notNull(entity, C.ENTITY);
		
		return repository.createCursor(entity, chunkSize);
	}
	
	@Override
	public QueryCursor<ValueObject> createCursor(ValueObject searchObject, Map<Long, Map<String, CriterionOperator>> criteriaMap, Sort ...sort) {
		Assert.notNull(searchObject, "searchObject");
		Assert.notNull(criteriaMap, "criteriaMap");
		
		return repository.createCursor(searchObject, criteriaMap, sort);
	}
	
	@Override
	@Async
	public void indexAllObjects() {
		for (Entity entity : entityService.findNonGenericEntities()) {
			if (entity.hasFullTextSearchFields()) {
				int idx = 0;
				int chunkIdx = 0;
				final QueryCursor<ValueObject> cursor = createCursor(entity, 500);
				while (idx < cursor.getTotalCount()) {
					cursor.setChunkIndex(chunkIdx++);
					fullTextSearch.indexChunk(entity, loadChunk(cursor));
					idx += cursor.getChunkSize();
				}
			}
		}
	}
	
	@Override
	public List<ValueObject> loadChunk(QueryCursor<ValueObject> cursor) {
		Assert.notNull(cursor, C.CURSOR);
		
		if (cursor.isFullTextSearch()) {
			return loadFullTextObjects(cursor);
		}
		try (Session session = repository.getSession()) {
			return loadChunk(session, cursor);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<ValueObject> loadChunk(Session session, QueryCursor<ValueObject> cursor) {
		final Query<ValueObject> query = cursor.getQueryText() != null
							? session.createQuery(cursor.getQueryText())
							: session.createQuery(cursor.getQuery());
		query.setFirstResult(cursor.getStartIndex());
		query.setMaxResults(cursor.getChunkSize());
		query.setCacheable(true);
		return query.getResultList();
	}
	
	@Override
	public List<FullTextResult> loadFullTextChunk(QueryCursor<FullTextResult> cursor) {
		Assert.notNull(cursor, C.CURSOR);
		
		final List<ValueObject> listObjects = loadFullTextObjects(cursor);
		final Map<Long, String> mapTexts = fullTextSearch.getTextMap(listObjects, cursor.getQueryText());
		return convertedList(listObjects, 
				obj -> new FullTextResult(obj, getIdentifier(obj), mapTexts.get(obj.getId())));
	}
	
	@Override
	public List<FileObject> getFileObjects(ValueObject object) {
		Assert.notNull(object, C.OBJECT);
		
		final List<FileObject> fileObjects = new ArrayList<>();
		final Entity entity = repository.getEntity(object);
		collectFileObjects(object, entity, fileObjects);
		if (entity.hasNesteds()) {
			for (NestedEntity nested : entity.getNesteds()) {
				if (nested.getNestedEntity().hasAllFields() && hasNestedObjects(object, nested)) {
					getNestedObjects(object, nested)
						.forEach(obj -> collectFileObjects(obj, nested.getNestedEntity(), fileObjects));
				}
			}
		}
		return fileObjects;
	}
	
	@Override
	public String callUserActionFunction(Session session, ValueObject object, EntityFunction function) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(function, C.FUNCTION);
		
		String message = null;
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
	
	@Override
	public void preallocateFileObjects(ValueObject object, Session session) {
		setFileObjects(object, session, true);
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
	public void addRelation(ValueObject object, EntityRelation relation, ValueObject relatedObject) {
		objectAccess.addRelatedObject(object, relation, relatedObject);
	}
	
	@Override
	public void removeRelation(ValueObject object, EntityRelation relation, ValueObject relatedObject) {
		objectAccess.removeRelatedObject(object, relation, relatedObject);
	}
	
	@Override
	public ValueObject getObject(Entity entity, Long objectId) {
		return repository.get(entity, objectId);
	}
	
	@Override
	public ValueObject getObject(Session session, Entity entity, Long id) {
		return repository.get(session, entity, id);
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
	public List<ValueObject> getAllObjects(Session session, Entity entity) {
		return repository.findAll(session, entity);
	}
	
	@Override
	public List<ValueObject> getAllObjects(Session session, Class<?> entityClass) {
		return repository.findAll(session, entityClass);
	}
	
	@Override
	public List<ValueObject> getAvailableRelationObjects(ValueObject object, EntityRelation relation) {
		final List<ValueObject> result = getAllObjects(relation.getRelatedEntity());
		if (objectAccess.hasRelatedObjects(object, relation)) {
			result.removeAll(objectAccess.getRelatedObjects(object, relation));
		}
		return result;
	}
	
	@Override
	public List<ValueObject> find(Session session, CriteriaQuery<ValueObject> query) {
		return repository.find(session, query);
	}
	
	@Override
	public List<ValueObject> find(Entity entity, Filter filter) {
		Assert.notNull(filter, C.FILTER);
		
		try (Session session = repository.getSession()) {
			filterService.initFilterCriteria(filter, session);
			return repository.find(session, entity, filter);
		}
	}
	
	@Override
	public List<ValueObject> find(Entity entity, Filter filter, Session session) {
		Assert.notNull(filter, C.FILTER);
		Assert.notNull(session, C.SESSION);
		
		filterService.initFilterCriteria(filter, session);
		return repository.find(session, entity, filter);
	}
	
	@Override
	public List<ValueObject> find(Session session, Entity entity, Filter filter) {
		Assert.notNull(filter, C.FILTER);
		Assert.notNull(session, C.SESSION);
		
		filterService.initFilterCriteria(filter, session);
		return repository.find(session, entity, filter);
	}
	
	@Override
	public ValueObject findByUid(Entity entity, String uid) {
		Assert.notNull(entity, C.ENTITY);
		
		return findUnique(entity, entity.getUidField(), uid);
	}
	
	@Override
	public ValueObject findByUid(Entity entity, String uid, Session session) {
		Assert.notNull(entity, C.ENTITY);
		
		return findUnique(entity, entity.getUidField(), uid, session);
	}
	
	@Override
	public ValueObject findUnique(Entity entity, EntityField entityField, Object value) {
		return findUnique(entity, entityField, value, null);
	}
	
	@Override
	public ValueObject findUnique(Entity entity, EntityField entityField, Object value, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(value, C.VALUE);
		Assert.notNull(entityField, C.ENTITYFIELD);
		Assert.state(entityField.isUnique(), "entityField is not unique");
		
		final Filter filter = filterService.createFieldFilter(entity, entityField, value);
		return session != null 
				? repository.findUnique(session, entity, filter)
				: repository.findUnique(entity, filter);
	}
	
	@Override
	public List<ValueObject> findByIds(Session session, Class<?> entityClass, Long ...ids) {
		return repository.findByIds(session, entityClass, ids);
	}
	
	@Override
	public List<ValueObject> findByIds(Session session, Class<?> entityClass, List<Long> idList) {
		return repository.findByIds(session, entityClass, idList);
	}
	
	@Override
	public void deleteObject(ValueObject object) throws ValidationException {
		Assert.notNull(object, C.OBJECT);
		
		
		try (Session session = repository.getSession()) {
			clearEmptyFileObjects(object, session);
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				deleteObject(object, session, null);
				tx.commit();
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
	public void deleteObject(ValueObject object, Session session, ValueObjectFunctionContext functionContext) 
			throws ValidationException {
		Assert.notNull(object, C.OBJECT);
		final Session localSession = functionContext != null 
				? functionContext.getSession() 
				: session;
		
		validator.validateDelete(localSession, object);
		repository.delete(object, session, functionContext);
		changeAwareObjects.forEach(changeAware -> changeAware.notifyDelete(object, localSession));
	}
	
	@Override
	public ValueObject updateObject(Session session, Entity entity, Long objectId, Map<String,Object> valueMap) throws ValidationException {
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			final ValueObject object = repository.get(session, entity, objectId);
			if (setObjectValues(session, entity, object, valueMap)) {
				saveObject(object, session, null);
			}
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
	
	@Override
	public void saveObject(ValueObject object) throws ValidationException {
		saveObject(object, null);
	}
	
	@Override
	public void saveObject(ValueObject object, List<FileObject> deletedFiles) throws ValidationException {
		Assert.notNull(object, C.OBJECT);
		
		final boolean isInsert = object.isNew();
		try (Session session = repository.getSession()) {
			clearEmptyFileObjects(object, session);
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				saveObject(object, session, null);
				
				if (deletedFiles != null) {
					for (FileObject file : deletedFiles) {
						session.delete(file);
					}
				}
				tx.commit();
			}
			catch (Exception ex) {
				if (isInsert) {
					// reset id because its assigned even if insert fails
					((AbstractValueObject) object).resetId();
				}
				if (tx != null) {
					tx.rollback();
				}
				if (ExceptionUtils.isUniqueConstraintViolation(ex)) {
					final Tupel<String, String> details = ExceptionUtils.getUniqueConstraintDetails(ex);
					throw new ValidationException(
							new ValidationError("val.ambiguous.unique", details.x, details.y));
				}
				else {
					throw ex;
				}
			}
		}
	}
	
	@Override
	public void saveObject(ValueObject object, Session session, ValueObjectFunctionContext functionContext) 
			throws ValidationException {
		Assert.notNull(object, C.OBJECT);
		final boolean isInsert = object.isNew();
		final Session localSession = functionContext != null 
										? functionContext.getSession() 
										: session;
		validator.validateSave(localSession, object);
		repository.save(object, session, functionContext);
		
		if (isInsert) {
			changeAwareObjects.forEach(changeAware -> changeAware.notifyCreate(object, localSession));
		}
		else {
			changeAwareObjects.forEach(changeAware -> changeAware.notifyChange(object, localSession));
		}
	}
	
	@Override
	public Session openSession() {
		return repository.getSession();
	}
	
	@Override
	public void changeStatus(ValueObject object, EntityStatus targetStatus) throws ValidationException {
		Assert.notNull(object, C.OBJECT);
		
		try (Session session = repository.getSession()) {
			clearEmptyFileObjects(object, session);
			validator.validateChangeStatus(object, targetStatus, session);
			repository.changeStatus(object, targetStatus, session);
		}
	}
	
	@Override
	public void changeStatus(ValueObject object, EntityStatus targetStatus,
			 Session session, ValueObjectFunctionContext functionContext) throws ValidationException {
		Assert.notNull(object, C.OBJECT);
		final Session localSession = session != null ? session : functionContext.getSession();
		
		clearEmptyFileObjects(object, localSession);
		validator.validateChangeStatus(object, targetStatus, localSession);
		repository.changeStatus(object, targetStatus, session, functionContext);
	}
	
	@Override
	public void transform(Transformer transformer, ValueObject sourceObject, ValueObject targetObject, Session session) {
		objectTransformer.transform(transformer, sourceObject, targetObject, session);
	}
	
	@Override
	public void transform(Transformer transformer, ValueObject targetObject, EntityField sourceObjectField, Session session) {
		objectTransformer.transform(transformer, targetObject, sourceObjectField, session);
	}
	
	@Override
	public ValueObject transform(Transformer transformer, ValueObject sourceObject) {
		try (Session session = repository.getSession()) {
			return transform(transformer, sourceObject, session);
		}
	}
	
	@Override
	public ValueObject transform(Transformer transformer, ValueObject sourceObject, Session session) {
		Assert.notNull(transformer, "transformer");
		Assert.notNull(sourceObject, "sourceObject");
		
		final ValueObject targetObject = createInstance(transformer.getTargetEntity());
		objectTransformer.transform(transformer, sourceObject, targetObject, session);
		return targetObject;
	}
	
	@Override
	public List<SystemEntity> findUsage(Entity entity) {
		if (!entity.isGeneric() && repository.exist(entity, null)) {
			return Collections.singletonList(VALUE_ENTITY);
		}
		return Collections.emptyList();
	}

	@Override
	public List<SystemEntity> findUsage(EntityField entityField) {
		return Collections.emptyList();
	}

	@Override
	public List<SystemEntity> findUsage(EntityFieldGroup fieldGroup) {
		return Collections.emptyList();
	}

	@Override
	public List<SystemEntity> findUsage(EntityStatus entityStatus) {
		final Entity entity = entityStatus.getEntity();
		final Filter filter = filterService.createStatusFilter(entity, entityStatus);
		if (repository.exist(entity, filter)) {
			return Collections.singletonList(VALUE_ENTITY);
		}
		return Collections.emptyList();
	}
	
	@Override
	public List<SystemEntity> findUsage(EntityFunction entityFunction) {
		return Collections.emptyList();
	}

	@Override
	public List<SystemEntity> findUsage(NestedEntity nestedEntity) {
		return Collections.emptyList();
	}
	
	@Override
	public List<SystemEntity> findUsage(EntityRelation entityRelation) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Entity> findUsage(Session session, ValueObject object) {
		Assert.notNull(session, C.SESSION);
		final List<Entity> result = new ArrayList<>();
		final Entity entity = repository.getEntity(session, object);
		for (Entity otherEntity : subList(entityService.findNonGenericEntities(session), 
										  other -> !entity.equals(other))) {
			for (EntityField otherRefField : otherEntity.getReferenceFields(entity)) {
				final Filter filter = filterService.createFieldFilter(otherEntity, otherRefField, object);
				if (repository.exist(session, otherEntity, filter)) {
					result.add(otherEntity);
					break;
				}
			}
		}
		return result;
	}
	
	@Override
	public void sortObjects(List<ValueObject> objectList) {
		Assert.notNull(objectList, "object list");
		
		objectList.sort(objectComparator);
	}
	
	private List<ValueObject> loadFullTextObjects(QueryCursor<?> cursor) {
		final List<ValueObject> result = new ArrayList<>(cursor.getChunkSize());
		Entity entity = null;
		for (int i = cursor.getStartIndex(); i < Math.min(cursor.getStartIndex() + cursor.getChunkSize(), cursor.getTotalCount()); i++) {
			final Tupel<Long, Long> fullTextResult = cursor.getFullTextResult(i);
			if (entity == null || !entity.getId().equals(fullTextResult.x)) {
				entity = repository.getEntity(fullTextResult.x);
			}
			final ValueObject object = repository.get(entity, fullTextResult.y);
			Assert.state(object != null, "value object is not available. id:" + fullTextResult.y);
			result.add(object);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private boolean setObjectValues(Session session, Entity entity, ValueObject object, Map<String, Object> valueMap) {
		boolean isModified = false;
		if (entity.hasAllFields()) {
			for (EntityField field : entity.getAllFields()) {
				if (setObjectFieldValue(session, field, object, valueMap)) {
					isModified = true;
				}
			}
		}
		if (entity.hasNesteds()) {
			for (NestedEntity nested : entity.getNesteds()) {
				// get nested maps
				final List<Map<String, Object>> listNestedMaps = 
						(List<Map<String, Object>>) valueMap.get(nested.getInternalName());
				if (!ObjectUtils.isEmpty(listNestedMaps) && 
					setNestedObjectValues(session, nested, object, listNestedMaps)) {
					isModified = true;
				}
			}
		}
		return isModified;
	}
	
	@SuppressWarnings("unchecked")
	private boolean setObjectFieldValue(Session session, EntityField field, ValueObject object, Map<String, Object> valueMap) {
		if (field.isJsonSerializable() && 
			!field.getType().isAutonum() &&
			valueMap.containsKey(field.getInternalName())) {
			
			Object value = valueMap.get(field.getInternalName());
			if (value != null && field.getType().isReference()) {
				Assert.state(value instanceof Map, "value of '" + field.getInternalName() + "' is not a map");
				final Map<String, Object> objectMap = (Map<String, Object>) value;
				final Integer referenceId = (Integer) objectMap.get(C.ID);
				Assert.stateAvailable(referenceId, "reference id of " + field.getInternalName());
				value = getObject(session, field.getReferenceEntity(), referenceId.longValue());
			}
			objectAccess.setValue(object, field, value);
			return true;
		}
		return false;
	}
	
	private boolean setNestedObjectValues(Session session, NestedEntity nested, ValueObject object, 
										  List<Map<String, Object>> listNestedMaps) {
		boolean isModified = false;
		// get existing nested value objects as map
		final Map<Long, ValueObject> nestedObjectMap = convertedMap(objectAccess.getNestedObjects(object, nested), 
																	ValueObject::getId, obj -> obj);
		// iterate over nested maps
		final Set<Long> nestedIds = new HashSet<>();
		for (Map<String, Object> nestedValueMap : listNestedMaps) {
			final Integer nestedId = (Integer) nestedValueMap.get(C.ID);
			// update existing nested object
			if (nestedId != null && nestedObjectMap != null && 
				nestedObjectMap.containsKey(nestedId.longValue())) {
				final ValueObject nestedObject = nestedObjectMap.get(nestedId.longValue());
				if (setObjectValues(session, nested.getNestedEntity(), nestedObject, nestedValueMap)) {
					isModified = true;
				}
				nestedIds.add(nestedId.longValue());
			}
			// create new nested object
			else {
				final ValueObject nestedObject = objectAccess.addNestedInstance(object, nested);
				setObjectValues(session, nested.getNestedEntity(), nestedObject, nestedValueMap);
				isModified = true;
			}
		}
		// remove nested objects 
		if (nestedObjectMap != null) {
			filterAndForEach(nestedObjectMap.entrySet(), 
							 entry -> !nestedIds.contains(entry.getKey()), 
							 entry -> objectAccess.removeNestedObject(object, nested, entry.getValue()));
		}
		return isModified;
	}
	
	private void clearEmptyFileObjects(ValueObject object, Session session) {
		setFileObjects(object, session, false);
	}
	
	private void collectFileObjects(ValueObject object, Entity entity, List<FileObject> fileObjects) {
		for (EntityField fileField : entity.getAllFieldsByType(FieldType.FILE)) {
			final FileObject fileObject = (FileObject) getValue(object, fileField);
			if (fileObject != null && fileObject.getContent() != null && !fileObject.isNew()) {
				fileObjects.add(fileObject);
			}
		}
	}
	
	private void setFileObjects(ValueObject object, Session session, boolean createObject) {
		final Entity entity = repository.getEntity(session, object);
		setFileFields(object, entity, createObject);
		filterAndForEach(entity.getNesteds(), 
						 nested -> nested.getNestedEntity().hasAllFields() && hasNestedObjects(object, nested), 
						 nested -> getNestedObjects(object, nested)
						 			.forEach(obj -> setFileFields(obj, nested.getNestedEntity(), createObject)));
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
