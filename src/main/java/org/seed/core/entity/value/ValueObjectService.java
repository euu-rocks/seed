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

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.Session;

import org.seed.core.data.QueryCursor;
import org.seed.core.data.FileObject;
import org.seed.core.data.Sort;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.EntityUsage;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.filter.CriterionOperator;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.value.event.ValueObjectFunctionContext;
import org.seed.core.user.User;

public interface ValueObjectService extends EntityUsage {
	
	void copyFields(ValueObject sourceObject, ValueObject targetObject, List<EntityField> entityFields);
	
	ValueObject createInstance(Entity entity);
	
	ValueObject createInstance(Entity entity, Session session, ValueObjectFunctionContext functionContext);
	
	ValueObject createObject(Session session, Entity entity, Map<String,Object> valueMap) throws ValidationException;
	
	QueryCursor<ValueObject> createCursor(Entity entity, int chunkSize);
	
	QueryCursor<ValueObject> createCursor(Session session, Entity entity, @Nullable Filter filter, int chunkSize, Sort ...sort);
	
	QueryCursor<ValueObject> createCursor(Session session, ValueObject searchObject, Map<Long, Map<String, CriterionOperator>> criteriaMap, Sort ...sort);
	
	QueryCursor<ValueObject> createFullTextSearchCursor(String fullTextQueryString, Entity entity);
	
	QueryCursor<FullTextResult> createFullTextSearchCursor(String fullTextQueryString);
	
	boolean existObjects(Entity entity, Session session);
	
	List<ValueObject> loadChunk(QueryCursor<ValueObject> cursor);
	
	List<ValueObject> loadChunk(Session session, QueryCursor<ValueObject> cursor);
	
	List<FullTextResult> loadFullTextChunk(QueryCursor<FullTextResult> cursor);
	
	void indexAllObjects();
	
	boolean notifyChange(ValueObject object);
	
	void changeStatus(ValueObject object, EntityStatus targetStatus, Session session) throws ValidationException;
	
	void changeStatus(ValueObject object, EntityStatus targetStatus,
			 Session session, ValueObjectFunctionContext functionContext) throws ValidationException;
	
	void transform(Transformer transformer, ValueObject targetObject, EntityField sourceObjectField, Session session);
	
	ValueObject transform(Transformer transformer, ValueObject sourceObject, Session session);
	
	void transform(Transformer transformer, ValueObject sourceObject, ValueObject targetObject, Session session);
	
	boolean isEmpty(ValueObject object, EntityField field);
	
	<T> T getValue(ValueObject object, EntityField field);
	
	void setValue(ValueObject object, EntityField field, Object value);
	
	boolean hasNestedObjects(ValueObject object, NestedEntity nested);
	
	List<ValueObject> getNestedObjects(ValueObject object, NestedEntity nested);
	
	ValueObject addNestedInstance(ValueObject object, NestedEntity nested);
	
	void removeNestedObject(ValueObject object, NestedEntity nested, ValueObject nestedObject);
	
	void addRelation(ValueObject object, EntityRelation relation, ValueObject relatedObject);
	
	void removeRelation(ValueObject object, EntityRelation relation, ValueObject relatedObject);
	
	List<FileObject> getFileObjects(ValueObject object, Session session);
	
	void preallocateFileObjects(ValueObject object, Session session);
	
	String getIdentifier(ValueObject object);
	
	String getIdentifier(ValueObject object, Session session);
	
	long count(Session session, Class<ValueObject> entityClass);
	
	long count(Entity entity, Session session);
	
	ValueObject getObject(Session session, Entity entity, Long id);
	
	ValueObject getObject(Session session, Class<ValueObject> entityClass, Long id);
	
	List<ValueObject> getAllObjects(Session session, Entity entity);
	
	List<ValueObject> getAllObjects(Session session, Class<ValueObject> entityClass);
	
	List<ValueObject> getAvailableRelationObjects(Session session, ValueObject object, EntityRelation relation);
	
	List<ValueObject> find(Session session, Entity entity, Filter filter);
	
	List<ValueObject> find(Session session, CriteriaQuery<ValueObject> query);
	
	ValueObject findByUid(Entity entity, String uid, Session session);
	
	ValueObject findUnique(Entity entity, EntityField entityField, Object value);
	
	ValueObject findUnique(Entity entity, EntityField entityField, Object value, Session session);
	
	List<ValueObject> findByIds(Session session, Class<ValueObject> entityClass, Long ...ids);
	
	List<ValueObject> findByIds(Session session, Class<ValueObject> entityClass, List<Long> idList);
	
	void deleteObject(ValueObject object) throws ValidationException;
	
	void deleteObject(ValueObject object, Session session, ValueObjectFunctionContext functionContext) 
			throws ValidationException;
	
	ValueObject updateObject(Session session, Entity entity, Long objectId, Map<String,Object> valueMap) throws ValidationException;
	
	void saveObject(ValueObject object) throws ValidationException;
	
	void saveObject(ValueObject object, List<FileObject> deletedFiles) throws ValidationException;
	
	void saveObject(ValueObject object, Session session, ValueObjectFunctionContext functionContext) 
			throws ValidationException;
	
	ValueObject saveFieldContent(ValueObject object, EntityField field, Object value, Session session) throws ValidationException;
	
	String callUserActionFunction(Session session, ValueObject object, EntityFunction function);
	
	Session openSession();
	
	void sortObjects(List<ValueObject> objectList);
	
	ValueObject removeInvisibleFields(ValueObject object, Entity entity, User user);
	
	List<String> validateObject(Session session, Entity entity, @Nullable Long objectId, Map<String,Object> valueMap);
}
