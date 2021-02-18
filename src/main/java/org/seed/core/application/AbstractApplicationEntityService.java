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
package org.seed.core.application;

import java.util.List;

import org.hibernate.Session;

import org.seed.core.application.module.Module;
import org.seed.core.application.module.ModuleDependent;
import org.seed.core.application.module.TransferContext;
import org.seed.core.data.AbstractSystemEntityService;
import org.seed.core.data.QueryParameter;
import org.seed.core.data.ValidationException;

import org.springframework.util.Assert;

public abstract class AbstractApplicationEntityService<T extends ApplicationEntity> extends AbstractSystemEntityService<T> 
	implements ApplicationEntityService<T>, ModuleDependent {
	
	@Override
	public T findByUid(String uid) {
		Assert.notNull(uid, "uid is null");
		
		return getRepository().findUnique(queryParam("uid", uid));
	}
	
	@Override
	public T findByUid(Session session, String uid) {
		Assert.notNull(session, "session is null");
		Assert.notNull(uid, "uid is null");
		
		return getRepository().findUnique(session, queryParam("uid", uid));
	}
	
	@Override
	public List<T> findObjectsWithoutModule() {
		return getRepository().find(queryParam("module", QueryParameter.IS_NULL));
	}
	
	@Override
	public List<T> findUsage(Module module) {
		Assert.notNull(module, "module is null");
		
		return getRepository().find(queryParam("module", module));
	}
	
	@Override
	public void createChangeLogs(TransferContext context, Session session) {
		// do nothing by default
	}
	
	@Override
	public void saveObject(T object, Session session) throws ValidationException {
		Assert.notNull(object, "object is null");
		
		((AbstractApplicationEntity) object).initUids();
		super.saveObject(object, session);
	}
	
}