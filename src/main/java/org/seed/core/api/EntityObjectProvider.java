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

public interface EntityObjectProvider {
	
	BatchOperation startBatchOperation();
	
	CriteriaBuilder getCriteriaBuilder();
	
	<T extends EntityObject> DBCursor<T> createCursor(Class<T> objectClass, int chunkSize);
	
	<T extends EntityObject> DBCursor<T> createCursor(Class<T> objectClass, @Nullable EntityFilter filter, int chunkSize);
	
	<T extends EntityObject> T createObject(Class<T> objectClass);
	
	<T extends EntityObject> long count(Class<T> objectClass);
	
	<T extends EntityObject> T getObject(Class<T> objectClass, Long id);
	
	<T extends EntityObject> String getIdentifier(T entityObject);
	
	<T extends EntityObject> List<T> findAll(Class<T> objectClass);
	
	<T extends EntityObject> List<T> find(EntityFilter filter);
	
	<T extends EntityObject> List<T> find(CriteriaQuery<T> query);
	
	<T extends EntityObject> EntityFilter getFilter(Class<T> objectClass, String filterName);
	
	<T extends EntityObject,U extends EntityObject> EntityTransformer getTransformer(Class<T> sourceClass, Class<U> targetClass, String transformerName);
	
	<T extends EntityObject> void save(T entityObject) throws ValidationException;
	
	<T extends EntityObject> void save(T entityObject, BatchOperation batchOperation) throws ValidationException;
	
	<T extends EntityObject> void delete(T entityObject) throws ValidationException;
	
	<T extends EntityObject> void delete(T entityObject, BatchOperation batchOperation) throws ValidationException;
	
	<T extends EntityObject> Status getStatus(T entityObject, Integer statusNumber);
	
	<T extends EntityObject> void changeStatus(T entityObject, Status targetStatus) throws ValidationException;
	
	<T extends EntityObject,U extends EntityObject> U transform(EntityTransformer transformer, T sourceObject);
	
	<T extends EntityObject,U extends EntityObject> void transform(EntityTransformer transformer, T sourceObject, U targetObject);
	
}
