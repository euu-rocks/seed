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

import org.seed.C;
import org.seed.Seed;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.ModuleDependent;
import org.seed.core.application.module.TransferContext;
import org.seed.core.config.SchemaVersion;
import org.seed.core.config.UpdatableConfiguration;
import org.seed.core.data.AbstractSystemEntityService;
import org.seed.core.data.QueryParameter;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

public abstract class AbstractApplicationEntityService<T extends ApplicationEntity> extends AbstractSystemEntityService<T> 
	implements ApplicationEntityService<T>, ModuleDependent<T> {
	
	@Override
	public T findByUid(String uid) {
		Assert.notNull(uid, C.UID);
		
		return getRepository().findUnique(queryParam(C.UID, uid));
	}
	
	@Override
	public T findByUid(Session session, String uid) {
		Assert.notNull(session, C.SESSION);
		Assert.notNull(uid, C.UID);
		
		return getRepository().findUnique(session, queryParam(C.UID, uid));
	}
	
	@Override
	public List<T> findObjectsWithoutModule(Session session) {
		Assert.notNull(session, C.SESSION);
		
		return getRepository().find(session, queryParam(C.MODULE, QueryParameter.IS_NULL));
	}
	
	@Override
	public List<T> findUsage(Module module) {
		Assert.notNull(module, C.MODULE);
		
		return getRepository().find(queryParam(C.MODULE, module));
	}
	
	@Override
	public void createChangeLogs(TransferContext context, Session session) {
		// do nothing by default
	}
	
	@Override
	public void handleSchemaUpdate(TransferContext context, SchemaVersion schemaVersion) {
		// do nothing by default
	}
	
	protected abstract void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule); 
	
	protected abstract void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule); 
	
	@Override
	public void analyzeObjects(ImportAnalysis analysis, Module currentVersionModule) {
		Assert.notNull(analysis, C.ANALYSIS);
		
		analyzeNextVersionObjects(analysis, currentVersionModule);
		if (currentVersionModule != null) {
			analyzeCurrentVersionObjects(analysis, currentVersionModule);
		}
	}
	
	@Override
	public void saveObject(T object, Session session) throws ValidationException {
		Assert.notNull(object, C.OBJECT);
		
		((AbstractApplicationEntity) object).initUid();
		super.saveObject(object, session);
	}
	
	protected void updateConfiguration() {
		Seed.getBean(UpdatableConfiguration.class).updateConfiguration();
	}
	
}