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

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.config.SchemaManager;
import org.seed.core.config.changelog.ChangeLog;
import org.seed.core.data.AbstractSystemObject;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityDependent;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class DBObjectServiceImpl extends AbstractApplicationEntityService<DBObject>
	implements DBObjectService, EntityDependent<DBObject> {
	
	@Autowired
	private DBObjectRepository repository;
	
	@Autowired
	private DBObjectValidator validator;
	
	@Autowired
	private SchemaManager schemaManager;
	
	@Override
	protected DBObjectRepository getRepository() {
		return repository;
	}

	@Override
	protected DBObjectValidator getValidator() {
		return validator;
	}
	
	@Override
	public void initObject(DBObject dbObject) throws ValidationException {
		Assert.notNull(dbObject, C.DBOBJECT);
		super.initObject(dbObject);
		
		final String content;
		switch (dbObject.getType()) {
			case VIEW:
				content = "select ";
				break;
			
			case PROCEDURE:
				content = "create or replace procedure ";
				break;
				
			case FUNCTION:
				content = "create or replace function ";
				break;
			
			case TRIGGER:
				content = "create trigger ";
				break;
			
			case SEQUENCE:
				content = "create sequence ";
				break;	
				
			default:
				throw new UnsupportedOperationException(dbObject.getType().name());
		}
		((DBObjectMetadata) dbObject).setContent(content);
	}
	
	@Override
	public List<DBObject> findUsage(DBObject dbObject) {
		Assert.notNull(dbObject, C.DBOBJECT);
		
		return subList(repository.find(), object -> object.isEnabled() && object.contains(dbObject));
	}
	
	@Override
	public List<DBObject> findUsage(Entity entity, Session session) {
		return convertedList(schemaManager.findDependencies(session, entity.getEffectiveTableName(), null), 
							 DBObjectServiceImpl::createDummyObject);
	}

	@Override
	public List<DBObject> findUsage(EntityField entityField, Session session) {
		return convertedList(schemaManager.findDependencies(session, 
															entityField.getEntity().getEffectiveTableName(),
															entityField.getEffectiveColumnName()), 
							 DBObjectServiceImpl::createDummyObject);
	}
	
	@Override
	public List<DBObject> findUsage(EntityFieldGroup fieldGroup) {
		return Collections.emptyList();
	}

	@Override
	public List<DBObject> findUsage(EntityStatus entityStatus, Session session) {
		return Collections.emptyList();
	}

	@Override
	public List<DBObject> findUsage(EntityFunction entityFunction, Session session) {
		return Collections.emptyList();
	}

	@Override
	public List<DBObject> findUsage(NestedEntity nestedEntity, Session session) {
		return Collections.emptyList();
	}

	@Override
	public List<DBObject> findUsage(EntityRelation entityRelation, Session session) {
		return Collections.emptyList();
	}
	
	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
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
	}
	
	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		filterAndForEach(currentVersionModule.getDBObjects(), 
						 dbObject -> analysis.getModule().getDBObjectByUid(dbObject.getUid()) == null, 
						 analysis::addChangeDelete);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { EntityService.class };
	}

	@Override
	public void importObjects(TransferContext context, Session session) throws ValidationException {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		
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
				repository.save(dbObject, session); // don't validate
			}
		}
	}
	
	@Override
	public void createChangeLogs(TransferContext context, Session session) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		
		final var newObjects = new ArrayList<DBObject>(context.getNewDBObjects());
		newObjects.removeIf(not(DBObject::isEnabled));
		newObjects.sort((DBObject dbObject1, DBObject dbObject2) -> 
							Integer.compare(dbObject1.getOrder() != null ? dbObject1.getOrder() : 0, 
											dbObject2.getOrder() != null ? dbObject2.getOrder() : 0));
		for (DBObject dbObject : newObjects) {
			final ChangeLog changeLog = createChangeLog(null, dbObject);
			if (changeLog != null) {
				session.saveOrUpdate(changeLog);
			}
		}
		
		final var existingObjects = new ArrayList<DBObject>(context.getExistingDBObjects());
		existingObjects.sort((DBObject dbObject1, DBObject dbObject2) -> 
								Integer.compare(dbObject1.getOrder() != null ? dbObject1.getOrder() : 0, 
												dbObject2.getOrder() != null ? dbObject2.getOrder() : 0));
		for (DBObject dbObject : existingObjects) {
			final DBObject currentVersionObject = context.getCurrentVersionDBObject(dbObject.getUid());
			final ChangeLog changeLog = createChangeLog(currentVersionObject, dbObject);
			if (changeLog != null) {
				session.saveOrUpdate(changeLog);
			}
		}
	}
	
	@Override
	public void removeObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		filterAndForEach(currentVersionModule.getDBObjects(), 
						 dbObject -> module.getDBObjectByUid(dbObject.getUid()) == null,
						 dbObject -> session.saveOrUpdate(removeModule(dbObject)));
	}
	
	@Override
	@Secured("ROLE_ADMIN_DBOBJECT")
	public void deleteObject(DBObject dbObject) throws ValidationException {
		Assert.notNull(dbObject, C.DBOBJECT);
		
		try (Session session = repository.getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				deleteObject(dbObject, session);
				
				final ChangeLog changeLog = createChangeLog(dbObject, null);
				if (changeLog != null) {
					session.saveOrUpdate(changeLog);
				}
				tx.commit();
			}
			catch (Exception ex) {
				handleException(tx, ex);
			}
		}
		
		updateConfiguration();
	}
	
	@Override
	@Secured("ROLE_ADMIN_DBOBJECT")
	public void saveObject(DBObject dbObject) throws ValidationException {
		Assert.notNull(dbObject, C.DBOBJECT);
		
		final boolean isInsert = dbObject.isNew();
		final DBObject currentVersionObject = !isInsert ? getObject(dbObject.getId()) : null;
		try (Session session = repository.getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				saveObject(dbObject, session);
				
				final var changeLog = createChangeLog(currentVersionObject, dbObject);
				if (changeLog != null) {
					session.saveOrUpdate(changeLog);
				}
				tx.commit();
			}
			catch (Exception ex) {
				if (isInsert) {
					// reset id because its assigned even if insert fails
					((AbstractSystemObject) dbObject).resetId();
				}
				handleException(tx, ex);
			}
		}
		
		updateConfiguration();
	}
	
	private static ChangeLog createChangeLog(DBObject currentVersionObject, DBObject nextVersionObject) {
		return new DBObjectChangeLogBuilder()
						.setCurrentVersionObject(currentVersionObject)
						.setNextVersionObject(nextVersionObject)
						.build();
	}
	
	private static DBObject createDummyObject(String name) {
		final var object = new DBObjectMetadata();
		object.setName(name);
		return object;
	}

}
