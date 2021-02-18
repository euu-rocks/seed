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
package org.seed.core.entity.value.event;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.seed.core.api.EntityFilter;
import org.seed.core.api.EntityObject;
import org.seed.core.api.EntityObjectProvider;
import org.seed.core.api.EntityTransformer;
import org.seed.core.api.Status;
import org.seed.core.config.ApplicationContextProvider;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterService;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerService;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;

import org.springframework.util.Assert;

class ValueObjectProvider implements EntityObjectProvider {
	
	private final EntityService entityService;
	
	private final FilterService filterService;
	
	private final TransformerService transformerService;
	
	private final ValueObjectService valueObjectService;

	private final ValueObjectFunctionContext functionContext;
	
	ValueObjectProvider(ValueObjectFunctionContext functionContext) {
		Assert.notNull(functionContext, "context is null");
		
		this.functionContext = functionContext;
		valueObjectService = ApplicationContextProvider.getBean(ValueObjectService.class);
		entityService = ApplicationContextProvider.getBean(EntityService.class);
		filterService = ApplicationContextProvider.getBean(FilterService.class);
		transformerService = ApplicationContextProvider.getBean(TransformerService.class);
	}
	
	@Override
	public <T extends EntityObject> Status getStatus(T entityObject, Integer statusNumber) {
		Assert.notNull(entityObject, "entityObject is null");
		Assert.notNull(statusNumber, "statusNumber is null");
		
		final Entity entity = entityService.getObject(((ValueObject) entityObject).getEntityId());
		final Status status = entity.getStatusByNumber(statusNumber); 
		Assert.state(status != null, "status " + statusNumber + " not available for " + entity.getName());
		return status;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends EntityObject> T createObject(Class<T> objectClass) {
		Assert.notNull(objectClass, "objectClass is null");
		
		try {
			final Entity entity = entityService.getObject(((ValueObject) objectClass.getDeclaredConstructor().newInstance()).getEntityId());
			return (T) valueObjectService.createInstance(entity, null, functionContext);
		} 
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public <T extends EntityObject> long count(Class<T> objectClass) {
		Assert.notNull(objectClass, "objectClass is null");
		
		return valueObjectService.count(functionContext.getSession(), objectClass);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends EntityObject> T getObject(Class<T> objectClass, Long id) {
		Assert.notNull(objectClass, "objectClass is null");
		Assert.notNull(id, "id is null");
		
		return (T) valueObjectService.getObject(functionContext.getSession(), objectClass, id);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends EntityObject> List<T> findAll(Class<T> objectClass) {
		Assert.notNull(objectClass, "objectClass is null");
		
		return (List<T>) valueObjectService.getAllObjects(functionContext.getSession(), objectClass);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends EntityObject> List<T> find(EntityFilter<T> entityFilter) {
		Assert.notNull(entityFilter, "entityFilter is null");
		
		final Filter filter = (Filter) entityFilter;
		return (List<T>) valueObjectService.find(filter.getEntity(), filter);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends EntityObject> List<T> find(CriteriaQuery<T> query) {
		Assert.notNull(query, "query is null");
		
		return (List<T>) valueObjectService.find(functionContext.getSession(), query);
	}
	
	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return functionContext.getSession().getCriteriaBuilder();
	}
	
	@Override
	public void changeStatus(EntityObject entityObject, Status targetStatus) throws ValidationException {
		Assert.notNull(entityObject, "entityObject is null");
		Assert.notNull(targetStatus, "targetStatus is null");
		
		valueObjectService.changeStatus((ValueObject) entityObject, (EntityStatus) targetStatus, null, functionContext);
	}
	
	@Override
	public <T extends EntityObject,U extends EntityObject> void transform(EntityTransformer<T,U> transformer, T sourceObject, U targetObject) {
		Assert.notNull(transformer, "transformer is null");
		Assert.notNull(sourceObject, "sourceObject is null");
		Assert.notNull(targetObject, "targetObject is null");
		
		valueObjectService.transform((Transformer)transformer, (ValueObject) sourceObject, (ValueObject) targetObject);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends EntityObject,U extends EntityObject> U transform(EntityTransformer<T,U> transformer, T sourceObject) {
		Assert.notNull(transformer, "transformer is null");
		Assert.notNull(sourceObject, "sourceObject is null");
		
		return (U) valueObjectService.transform((Transformer) transformer, (ValueObject) sourceObject);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends EntityObject> EntityFilter<T> getFilter(Class<T> objectClass, String filterName) {
		Assert.notNull(objectClass, "objectClass is null");
		Assert.notNull(filterName, "filterName is null");
		
		try {
			final ValueObject object = (ValueObject) objectClass.getDeclaredConstructor().newInstance();
			final Entity entity = entityService.getObject(object.getEntityId());
			return (EntityFilter<T>) filterService.getFilterByName(entity, filterName);
		} 
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends EntityObject,U extends EntityObject> EntityTransformer<T,U> getTransformer(Class<T> sourceClass, Class<U> targetClass, String transformerName) {
		try {
			final ValueObject sourceObject = (ValueObject) sourceClass.getDeclaredConstructor().newInstance();
			final ValueObject targetObject = (ValueObject) targetClass.getDeclaredConstructor().newInstance();
			return (EntityTransformer<T, U>) transformerService
					.getTransformerByName(entityService.getObject(sourceObject.getEntityId()),
										  entityService.getObject(targetObject.getEntityId()), 
										  transformerName);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public <T extends EntityObject> void save(T entityObject) throws ValidationException {
		Assert.notNull(entityObject, "entityObject is null");
		
		valueObjectService.saveObject((ValueObject) entityObject, null, functionContext);
	}
	
	@Override
	public <T extends EntityObject> void delete(T entityObject) throws ValidationException {
		Assert.notNull(entityObject, "entityObject is null");
		
		valueObjectService.deleteObject((ValueObject) entityObject, null, functionContext);
	}

}
