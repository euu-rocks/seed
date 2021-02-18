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
package org.seed.core.data.dbobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.config.ChangeLog;
import org.seed.core.config.SessionFactoryProvider;
import org.seed.core.config.UpdatableConfiguration;
import org.seed.core.data.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class DBObjectServiceImpl extends AbstractApplicationEntityService<DBObject>
	implements DBObjectService {
	
	@Autowired
	private SessionFactoryProvider sessionFactoryProvider;
	
	@Autowired
	private UpdatableConfiguration configuration;
	
	@Autowired
	private DBObjectRepository repository;
	
	@Autowired
	private DBObjectValidator validator;
	
	@Override
	protected DBObjectRepository getRepository() {
		return repository;
	}

	@Override
	protected DBObjectValidator getValidator() {
		return validator;
	}
	
	@Override
	public void analyzeObjects(ImportAnalysis analysis, Module currentVersionModule) {
		Assert.notNull(analysis, "analysis is null");
		
		if (analysis.getModule().getDBObjects() != null) {
			for (DBObject dbObject : analysis.getModule().getDBObjects()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(dbObject);
				}
				else {
					final DBObject currentVersionObject = 
						currentVersionModule.getDBObjectByUid(dbObject.getUid());
					if (currentVersionObject == null) {
						analysis.addChangeNew(dbObject);
					}
					else if (!dbObject.isEqual(currentVersionObject)) {
						analysis.addChangeModify(dbObject);
					}
				}
			}
		}
		if (currentVersionModule!= null && currentVersionModule.getDBObjects() != null) {
			for (DBObject currentVersionObject : currentVersionModule.getDBObjects()) {
				if (analysis.getModule().getEntityByUid(currentVersionObject.getUid()) == null) {
					analysis.addChangeDelete(currentVersionObject);
				}
			}
		}
	}
	
	@Override
	public Class<? extends ApplicationEntityService<? extends ApplicationEntity>>[] getImportDependencies() {
		return null; // independent
	}

	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, "context is null");
		Assert.notNull(session, "session is null");
		
		if (context.getModule().getDBObjects() != null) {
			for (DBObject dbObject : context.getModule().getDBObjects()) {
				final DBObject currentVersionObject = findByUid(session, dbObject.getUid());
				((DBObjectMetadata) dbObject).setModule(context.getModule());
				// db object already exist
				if (currentVersionObject != null) {
					context.addExistingDBObject(dbObject, currentVersionObject);
					((DBObjectMetadata) currentVersionObject).copySystemFieldsTo(dbObject);
					session.detach(currentVersionObject);
				}
				else { // new db Object
					context.addNewDBObject(dbObject);
				}
				repository.save(dbObject, session);
			}
		}
	}
	
	@Override
	public void createChangeLogs(TransferContext context, Session session) {
		Assert.notNull(context, "context ist null");
		Assert.notNull(session, "session ist null");
		
		final List<DBObject> newObjects = new ArrayList<>(context.getNewDBObjects());
		Collections.sort(newObjects, dbObjectComparator);
		for (DBObject dbObject : newObjects) {
			for (ChangeLog changeLog : new DBObjectChangeLogBuilder()
											.setNextVersionObject(dbObject)
											.build()) {
				session.saveOrUpdate(changeLog);
			}
		}
		
		final List<DBObject> existingObjects = new ArrayList<>(context.getExistingDBObjects());
		Collections.sort(existingObjects, dbObjectComparator);
		for (DBObject dbObject : existingObjects) {
			final DBObject currentVersionObject = context.getCurrentVersionDBObject(dbObject.getUid());
			for (ChangeLog changeLog : new DBObjectChangeLogBuilder()
											.setCurrentVersionObject(currentVersionObject)
											.setNextVersionObject(dbObject)
											.build()) {
				session.saveOrUpdate(changeLog);
			}
		}
	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, "module is null");
		Assert.notNull(currentVersionModule, "currentVersionModule is null");
		Assert.notNull(session, "session is null");
		
		if (currentVersionModule.getDBObjects() != null) {
			for (DBObject currentVersionObject : currentVersionModule.getDBObjects()) {
				if (module.getDBObjectByUid(currentVersionObject.getUid()) == null) {
					session.delete(currentVersionObject);
				}
			}
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_DBOBJECT")
	public void deleteObject(DBObject dbObject) throws ValidationException {
		Assert.notNull(dbObject, "dbObject is null");
		
		try (Session session = sessionFactoryProvider.getSessionFactory().openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				deleteObject(dbObject, session);
				
				for (ChangeLog changeLog : new DBObjectChangeLogBuilder()
												.setCurrentVersionObject(dbObject)
												.build()) {
					session.saveOrUpdate(changeLog);
				}
				tx.commit();
			}
			catch (Exception ex) {
				handleException(tx, ex);
			}
		}
		
		configuration.updateConfiguration();
	}
	
	@Override
	@Secured("ROLE_ADMIN_DBOBJECT")
	public void saveObject(DBObject dbObject) throws ValidationException {
		Assert.notNull(dbObject, "dbObject is null");
		
		final boolean isInsert = dbObject.isNew();
		final DBObject currentVersionObject = !isInsert ? getObject(dbObject.getId()) : null;
		try (Session session = sessionFactoryProvider.getSessionFactory().openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				saveObject(dbObject, session);
				
				for (ChangeLog changeLog : new DBObjectChangeLogBuilder()
												.setCurrentVersionObject(currentVersionObject)
												.setNextVersionObject(dbObject)
												.build()) {
					session.saveOrUpdate(changeLog);
				}
				tx.commit();
			}
			catch (Exception ex) {
				handleException(tx, ex);
			}
		}
		
		configuration.updateConfiguration();
	}
	
	private static final Comparator<DBObject> dbObjectComparator = new Comparator<>() {
		@Override
		public int compare(DBObject dbObject1, DBObject dbObject2) {
			final int o1 = dbObject1.getOrder() != null ? dbObject1.getOrder() : 0;
			final int o2 = dbObject2.getOrder() != null ? dbObject2.getOrder() : 0;
			return Integer.compare(o1, o2);
		}
	
	};

}
