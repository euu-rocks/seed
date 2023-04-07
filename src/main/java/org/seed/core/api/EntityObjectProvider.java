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
package org.seed.core.api;

import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.seed.core.data.ValidationException;

/**
 * <code>EntityObjectProvider</code> provides functionalities to access and manipulate {@link EntityObject} instances.
 * 
 * @author seed-master
 *
 */
public interface EntityObjectProvider {
	
	/**
	 * Starts a new {@link BatchOperation}. 
	 * Use the returned <code>BatchOperation</code> in save and delete functions
	 * to execute them as bulk operations.
	 * @return
	 */
	BatchOperation startBatchOperation();
	
	/**
	 * Creates a new JPA <code>CriteriaBuilder</code>
	 * @return the new <code>CriteriaBuilder</code>
	 */
	CriteriaBuilder getCriteriaBuilder();
	
	/**
	 * Creates a new {@link EntityObject} instance
	 * @param <T> the type of the entity object
	 * @param objectClass the class of the entity object
	 * @return the new entity object instance
	 */
	<T extends EntityObject> T createObject(Class<T> objectClass);
	
	/**
	 * Returns the total count of all existing entity objects of the given class
	 * @param <T> the type of the entity object
	 * @param objectClass the class of the entity object
	 * @return the total count of all existing entity objects of the given class
	 */
	<T extends EntityObject> long count(Class<T> objectClass);
	
	/**
	 * Returns the {@link EntityObject} for the given class and id
	 * @param <T> the type of the entity object
	 * @param objectClass the class of the entity object
	 * @param id the id of the entity object
	 * @return the entity object or <code>null</code> if it doesn't exist
	 */
	<T extends EntityObject> T getObject(Class<T> objectClass, Long id);
	
	/**
	 * Creates a new {@link DBCursor}
	 * @param <T> the type of the entity object
	 * @param objectClass the class of the entity object
	 * @param chunkSize the chunk size to use
	 * @return the new cursor
	 */
	<T extends EntityObject> DBCursor<T> getCursor(Class<T> objectClass, int chunkSize);
	
	/**
	 * Creates a new {@link DBCursor} based on an {@link EntityFilter}
	 * @param <T> the type of the entity object
	 * @param objectClass the class of the entity object
	 * @param filter the filter to use
	 * @param chunkSize the chunk size to use
	 * @return the new cursor
	 */
	<T extends EntityObject> DBCursor<T> getCursor(Class<T> objectClass, @Nullable EntityFilter filter, int chunkSize);
	
	/**
	 * Returns the identifier of a given {@link EntityObject}
	 * @param <T> the type of the entity object
	 * @param entityObject objectClass the class of the entity object
	 * @return the identifier
	 */
	<T extends EntityObject> String getIdentifier(T entityObject);
	
	/**
	 * Returns a list of all entity objects of the given class
	 * @param <T> the type of the entity object
	 * @param objectClass the class of the entity object
	 * @return a list of all entity objects of the given class
	 */
	<T extends EntityObject> List<T> findAll(Class<T> objectClass);
	
	/**
	 * Returns a list of entity objects for the given class and primary keys
	 * @param <T> the type of the entity object
	 * @param objectClass the class of the entity object
	 * @param ids an array of entity object ids
	 * @return a list of entity objects for the given class and primary keys
	 */
	<T extends EntityObject> List<T> findByIds(Class<T> objectClass, Long ...ids);
	
	/**
	 * Returns a list of entity objects for the given class and primary keys
	 * @param <T> the type of the entity object
	 * @param objectClass the class of the entity object
	 * @param idList a list of entity object ids
	 * @return a list of entity objects for the given class and primary keys
	 */
	<T extends EntityObject> List<T> findByIds(Class<T> objectClass, List<Long> idList);
	
	/**
	 * Returns a list of entity objects based on an {@link EntityFilter} 
	 * @param <T> the type of the entity object
	 * @param filter the filter to use
	 * @return a list of entity objects based on an entity filter
	 */
	<T extends EntityObject> List<T> find(EntityFilter filter);
	
	/**
	 * Returns a list of entity objects based on a JPA <code>CriteriaQuery</code>
	 * @param <T> the type of the entity object
	 * @param query the query to use
	 * @return a list of entity objects based on a <code>CriteriaQuery</code>
	 */
	<T extends EntityObject> List<T> find(CriteriaQuery<T> query);
	
	/**
	 * Returns a predefined {@link EntityFilter} for the given class and name
	 * @param <T> the type of the entity object
	 * @param objectClass the class of the entity object
	 * @param filterName the name of the filter
	 * @return the filter or <code>null</code> if it doesn't exist
	 */
	<T extends EntityObject> EntityFilter getFilter(Class<T> objectClass, String filterName);
	
	/**
	 * Returns a predefined {@link EntityTransformer} for the given classes and name
	 * @param <T> the type of the source entity object
	 * @param <U> the type of the target entity object
	 * @param sourceClass the class of the source entity object
	 * @param targetClass the class of the target entity object
	 * @param transformerName the name of the transformation
	 * @return the transformer or <code>null</code> if it doesn't exist
	 */
	<T extends EntityObject, U extends EntityObject> EntityTransformer getTransformer(Class<T> sourceClass, Class<U> targetClass, String transformerName);
	
	<T extends EntityObject> void save(T entityObject) throws ValidationException;
	
	<T extends EntityObject> void save(T entityObject, BatchOperation batchOperation) throws ValidationException;
	
	<T extends EntityObject> void delete(T entityObject) throws ValidationException;
	
	<T extends EntityObject> void delete(T entityObject, BatchOperation batchOperation) throws ValidationException;
	
	<T extends EntityObject> Status getStatus(T entityObject, Integer statusNumber);
	
	<T extends EntityObject> void changeStatus(T entityObject, Integer statusNumber) throws ValidationException;
	
	<T extends EntityObject> void changeStatus(T entityObject, Status targetStatus) throws ValidationException;
	
	<T extends EntityObject, U extends EntityObject> U transform(EntityTransformer transformer, T sourceObject);
	
	<T extends EntityObject, U extends EntityObject> void transform(EntityTransformer transformer, T sourceObject, U targetObject);
	
}
