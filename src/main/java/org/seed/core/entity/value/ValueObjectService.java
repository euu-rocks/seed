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

import org.seed.core.data.Cursor;
import org.seed.core.data.FileObject;
import org.seed.core.data.Sort;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.filter.CriterionOperator;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.value.event.ValueObjectFunctionContext;

public interface ValueObjectService {
	
	void copyFields(ValueObject sourceObject, ValueObject targetObject, List<EntityField> entityFields);
	
	ValueObject createInstance(Entity entity);
	
	ValueObject createInstance(Entity entity, Session session, ValueObjectFunctionContext functionContext);
	
	ValueObject createObject(Entity entity, Map<String,Object> valueMap);
	
	Cursor<ValueObject> createCursor(Entity entity, int chuckSize);
	
	Cursor<ValueObject> createCursor(Entity entity, @Nullable Filter filter, Sort ...sort);
	
	Cursor<ValueObject> createCursor(ValueObject searchObject, Map<Long, Map<String, CriterionOperator>> criteriaMap, Sort ...sort);
	
	Cursor<ValueObject> createFullTextSearchCursor(String fullTextQueryString, Entity entity);
	
	Cursor<FullTextResult> createFullTextSearchCursor(String fullTextQueryString);
	
	List<ValueObject> loadChunk(Cursor<ValueObject> cursor);
	
	List<FullTextResult> loadFullTextChunk(Cursor<FullTextResult> cursor);
	
	void indexAllObjects();
	
	boolean notifyChange(ValueObject object);
	
	void changeStatus(ValueObject object, EntityStatus targetStatus) throws ValidationException;
	
	void changeStatus(ValueObject object, EntityStatus targetStatus,
			 Session session, ValueObjectFunctionContext functionContext) throws ValidationException;
	
	void transform(Transformer transformer, ValueObject targetObject, EntityField sourceObjectField);
	
	ValueObject transform(Transformer transformer, ValueObject sourceObject);
	
	void transform(Transformer transformer, ValueObject sourceObject, ValueObject targetObject);
	
	boolean isEmpty(ValueObject object, EntityField field);
	
	Object getValue(ValueObject object, EntityField field);
	
	void setValue(ValueObject object, EntityField field, Object value);
	
	boolean hasNestedObjects(ValueObject object, NestedEntity nested);
	
	List<ValueObject> getNestedObjects(ValueObject object, NestedEntity nested);
	
	ValueObject addNestedInstance(ValueObject object, NestedEntity nested);
	
	void removeNestedObject(ValueObject object, NestedEntity nested, ValueObject nestedObject);
	
	List<FileObject> getFileObjects(ValueObject object);
	
	void preallocateFileObjects(ValueObject object);
	
	String getIdentifier(ValueObject object);
	
	boolean existObjects(Entity entity);
	
	long count(Session session, Class<?> entityClass);
	
	ValueObject getObject(Entity entity, Long objectId);
	
	ValueObject getObject(Session session, Entity entity, Long id);
	
	ValueObject getObject(Session session, Class<?> entityClass, Long id);
	
	List<ValueObject> getAllObjects(Entity entity);
	
	List<ValueObject> getAllObjects(Session session, Class<?> entityClass);
	
	List<ValueObject> find(Entity entity, Filter filter);
	
	List<ValueObject> find(Session session, CriteriaQuery<ValueObject> query);
	
	ValueObject findByUid(Entity entity, String uid);
	
	ValueObject findUnique(Entity entity, EntityField entityField, Object value);
	
	ValueObject findUnique(Entity entity, EntityField entityField, Object value, Session session);
	
	void reloadObject(ValueObject object);
	
	void deleteObject(ValueObject object) throws ValidationException;
	
	void deleteObject(ValueObject object, Session session, ValueObjectFunctionContext functionContext) 
			throws ValidationException;
	
	ValueObject updateObject(Entity entity, Long objectId, Map<String,Object> valueMap) throws ValidationException;
	
	void saveObject(ValueObject object) throws ValidationException;
	
	void saveObject(ValueObject object, List<FileObject> deletedFiles) throws ValidationException;
	
	void saveObject(ValueObject object, Session session, ValueObjectFunctionContext functionContext) 
			throws ValidationException;
	
	String callUserActionFunction(ValueObject object, EntityFunction function);
	
	Session openSession();
	
}
