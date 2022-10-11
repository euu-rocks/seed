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
package org.seed.core.data;

import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;

public abstract class AbstractSystemEntityService<T extends SystemEntity> 
	implements SystemEntityService<T> {
	
	protected abstract SystemEntityRepository<T> getRepository();
	
	protected abstract SystemEntityValidator<T> getValidator();
	
	@Override
	public T createInstance(@Nullable Options options) { 
		try {
			final T instance = BeanUtils.instantiate(getRepository().getEntityTypeClass());
			((AbstractSystemEntity) instance).setOptions(options);
			return instance;
		} 
		catch (Exception ex) {
			throw new InternalException(ex);
		}
	}
	
	@Override
	public boolean isEntityType(Class<?> clas) {
		Assert.notNull(clas, "class");
		
		return clas.isAssignableFrom(getRepository().getEntityTypeClass());
	}
	
	@Override
	public T getObject(Long id) {
		return getRepository().get(id);
	}
	
	@Override
	public T getObject(Long id, Session session) {
		return getRepository().get(id, session);
	}
	
	@Override
	public T findByName(String name) {
		Assert.notNull(name, C.NAME);
		
		return getRepository().findUnique(queryParam(C.NAME, name));
	}
	
	@Override
	public T findByName(String name, Session session) {
		Assert.notNull(name, C.NAME);
		
		return getRepository().findUnique(session, queryParam(C.NAME, name));
	}
	
	@Override
	public void initObject(T object) throws ValidationException {
		if (getValidator() != null) {
			getValidator().validateCreate(object);
		}
	}
	
	@Override
	public void reloadObject(T object, Session session) {
		getRepository().reload(object, session);
	}
	
	@Override
	public boolean existObjects(Session session) {
		return getRepository().exist(session);
	}

	@Override
	public long countObjects() {
		return getRepository().count();
	}
	
	@Override
	public List<T> getObjects() {
		return getRepository().find();
	}
	
	@Override
	public List<T> getObjects(Session session) {
		return getRepository().find(session);
	}
	
	@Override
	public void saveObject(T object) throws ValidationException {
		saveObject(object, null);
	}
	
	// save without validation
	protected void saveObjectDirectly(T object) {
		getRepository().save(object);
	}

	protected void saveObject(T object, Session session) throws ValidationException {
		Assert.notNull(object, C.OBJECT);
		
		if (getValidator() != null) {
			getValidator().validateSave(object);
		}
		if (session != null) {
			getRepository().save(object, session);
		}
		else {
			getRepository().save(object);
		}
	}
	
	@Override
	public void deleteObject(T object) throws ValidationException {
		deleteObject(object, null);
	}

	protected void deleteObject(T object, Session session) throws ValidationException {
		Assert.notNull(object, C.OBJECT);
		
		if (getValidator() != null) {
			getValidator().validateDelete(object);
		}
		if (session != null) {
			getRepository().delete(object, session);
		}
		else {
			getRepository().delete(object);
		}
	}
	
	protected static QueryParameter queryParam(String name, Object value) {
		return AbstractSystemEntityRepository.queryParam(name, value);
	}
	
	protected static void handleException(Transaction tx, Exception ex) throws ValidationException {
		if (tx != null) {
			tx.rollback();
		}
		if (ex instanceof ValidationException) {
			throw (ValidationException) ex;
		}
		AbstractSystemEntityRepository.handleException(ex);
	}

}
