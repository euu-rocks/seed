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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.seed.C;
import org.seed.InternalException;
import org.seed.Seed;
import org.seed.core.api.EntityFilter;
import org.seed.core.api.EntityObject;
import org.seed.core.api.EntityObjectProvider;
import org.seed.core.api.EntityTransformer;
import org.seed.core.api.Status;
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
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

class ValueObjectProvider implements EntityObjectProvider {
	
	private final EntityService entityService;
	
	private final FilterService filterService;
	
	private final TransformerService transformerService;
	
	private final ValueObjectService valueObjectService;

	private final ValueObjectFunctionContext functionContext;
	
	ValueObjectProvider(ValueObjectFunctionContext functionContext) {
		Assert.notNull(functionContext, C.CONTEXT);
		
		this.functionContext = functionContext;
		valueObjectService = Seed.getBean(ValueObjectService.class);
		entityService = Seed.getBean(EntityService.class);
		filterService = Seed.getBean(FilterService.class);
		transformerService = Seed.getBean(TransformerService.class);
	}
	
	@Override
	public <T extends EntityObject> Status getStatus(T entityObject, Integer statusNumber) {
		Assert.notNull(entityObject, C.ENTITYOBJECT);
		Assert.notNull(statusNumber, "statusNumber");
		
		final Entity entity = entityService.getObject(((ValueObject) entityObject).getEntityId());
		final Status status = entity.getStatusByNumber(statusNumber); 
		Assert.state(status != null, "status " + statusNumber + " not available for " + entity.getName());
		return status;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends EntityObject> T createObject(Class<T> objectClass) {
		Assert.notNull(objectClass, C.OBJECTCLASS);
		
		final Entity entity = getEntity((Class<ValueObject>) objectClass);
		return (T) valueObjectService.createInstance(entity, null, functionContext);
	}
	
	@Override
	public <T extends EntityObject> long count(Class<T> objectClass) {
		Assert.notNull(objectClass, C.OBJECTCLASS);
		
		return valueObjectService.count(functionContext.getSession(), objectClass);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends EntityObject> T getObject(Class<T> objectClass, Long id) {
		Assert.notNull(objectClass, C.OBJECTCLASS);
		Assert.notNull(id, "id is null");
		
		return (T) valueObjectService.getObject(functionContext.getSession(), objectClass, id);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends EntityObject> List<T> findAll(Class<T> objectClass) {
		Assert.notNull(objectClass, C.OBJECTCLASS);
		
		return (List<T>) valueObjectService.getAllObjects(functionContext.getSession(), objectClass);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends EntityObject> List<T> find(EntityFilter entityFilter) {
		Assert.notNull(entityFilter, "entityFilter");
		
		final Filter filter = (Filter) entityFilter;
		return (List<T>) valueObjectService.find(filter.getEntity(), filter);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends EntityObject> List<T> find(CriteriaQuery<T> query) {
		Assert.notNull(query, "query");
		
		return MiscUtils.cast(valueObjectService.find(functionContext.getSession(), (CriteriaQuery<ValueObject>) query));
	}
	
	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return functionContext.getSession().getCriteriaBuilder();
	}
	
	@Override
	public void changeStatus(EntityObject entityObject, Status targetStatus) throws ValidationException {
		Assert.notNull(entityObject, C.ENTITYOBJECT);
		Assert.notNull(targetStatus, "targetStatus");
		
		valueObjectService.changeStatus((ValueObject) entityObject, (EntityStatus) targetStatus, null, functionContext);
	}
	
	@Override
	public <T extends EntityObject,U extends EntityObject> void transform(EntityTransformer transformer, T sourceObject, U targetObject) {
		Assert.notNull(transformer, C.TRANSFORMER);
		Assert.notNull(sourceObject, "sourceObject");
		Assert.notNull(targetObject, "targetObject");
		
		valueObjectService.transform((Transformer) transformer, (ValueObject) sourceObject, (ValueObject) targetObject);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends EntityObject,U extends EntityObject> U transform(EntityTransformer transformer, T sourceObject) {
		Assert.notNull(transformer, C.TRANSFORMER);
		Assert.notNull(sourceObject, "sourceObject");
		
		return (U) valueObjectService.transform((Transformer) transformer, (ValueObject) sourceObject);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends EntityObject> EntityFilter getFilter(Class<T> objectClass, String filterName) {
		Assert.notNull(objectClass, C.OBJECTCLASS);
		Assert.notNull(filterName, "filterName");
		
		final Entity entity = getEntity((Class<ValueObject>) objectClass);
		return filterService.getFilterByName(entity, filterName);
	}
	
	@Override
	public <T extends EntityObject,U extends EntityObject> EntityTransformer getTransformer(Class<T> sourceClass, Class<U> targetClass, String transformerName) {
		try {
			final T sourceObject = MiscUtils.instantiate(sourceClass);
			final U targetObject = MiscUtils.instantiate(targetClass);
			return transformerService
					.getTransformerByName(entityService.getObject(((ValueObject) sourceObject).getEntityId()),
										  entityService.getObject(((ValueObject) targetObject).getEntityId()), 
										  transformerName);
		}
		catch (Exception ex) {
			throw new InternalException(ex);
		}
	}
	
	@Override
	public <T extends EntityObject> void save(T entityObject) throws ValidationException {
		Assert.notNull(entityObject, C.ENTITYOBJECT);
		
		valueObjectService.saveObject((ValueObject) entityObject, null, functionContext);
	}
	
	@Override
	public <T extends EntityObject> void delete(T entityObject) throws ValidationException {
		Assert.notNull(entityObject, C.ENTITYOBJECT);
		
		valueObjectService.deleteObject((ValueObject) entityObject, null, functionContext);
	}
	
	private Entity getEntity(Class<ValueObject> clas) {
		try {
			final ValueObject object = MiscUtils.instantiate(clas);
			return entityService.getObject(object.getEntityId());
		} 
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | 
			   InvocationTargetException | NoSuchMethodException | SecurityException ex) {
			throw new InternalException(ex);
		}
	}

}
