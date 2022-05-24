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
package org.seed.core.entity.value.revision;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;

import org.seed.C;
import org.seed.core.codegen.CodeManager;
import org.seed.core.config.SessionProvider;
import org.seed.core.data.FieldType;
import org.seed.core.data.SystemField;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectAccess;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RevisionServiceImpl implements RevisionService {
	
	@Autowired
	private ValueObjectAccess valueObjectAccess;
	
	@Autowired
	private SessionProvider sessionProvider;
	
	@Autowired
	private CodeManager codeManager;
	
	@Override
	public List<Revision> getRevisions(Entity entity, Long id) {
		Assert.notNull(id, C.ID);
		checkAudited(entity);
		
		try (Session session = sessionProvider.getSession()) {
			return getRevisionNumbers(session, codeManager.getGeneratedClass(entity), id).stream()
				.map(num -> getRevision(session, num.intValue()))
				.collect(Collectors.toList());
		}
	}
	
	@Override
	public ValueObject getRevisionObject(Entity entity, Long id, Revision revision) {
		Assert.notNull(id, C.ID);
		Assert.notNull(revision, "revision");
		checkAudited(entity);
		
		try (Session session = sessionProvider.getSession()) {
			final ValueObject object = (ValueObject) createAuditReader(session)
					.find(codeManager.getGeneratedClass(entity), id, revision.getId());
			
			// load all referenced objects in this session to avoid lazy loading issues
			if (entity.hasStatus()) {
				loadStatus(object);
			}
			loadReferences(entity, object, FieldType.REFERENCE);
			loadReferences(entity, object, FieldType.FILE);
			if (entity.hasNesteds()) {
				loadNesteds(entity, object);
			}
			return object;
		}
	}
	
	private void loadStatus(ValueObject object) {
		final Object status = valueObjectAccess.getValue(object, SystemField.ENTITYSTATUS);
		if (status != null) {
			try {
				status.toString(); // to really load entity status
			}
			catch (ObjectNotFoundException onfex) {
				// set null if status not exists
				valueObjectAccess.setValue(object, SystemField.ENTITYSTATUS, null);
			}
		}
	}
	
	private void loadReferences(Entity entity, ValueObject object, FieldType fieldType) {
		for (EntityField referenceField : entity.getAllFieldsByType(fieldType)) {
			final Object reference = valueObjectAccess.getValue(object, referenceField);
			if (reference != null) {
				try {
					reference.toString(); // to really load reference object
				}
				catch (ObjectNotFoundException onfex) {
					// set null if reference not exists
					valueObjectAccess.setValue(object, referenceField, null);
				}
			}
		}
	}
	
	private void loadNesteds(Entity entity, ValueObject object) {
		for (NestedEntity nested : entity.getNesteds()) {
			final List<ValueObject> nestedObjects = valueObjectAccess.getNestedObjects(object, nested);
			if (nestedObjects != null) {
				try {
					nestedObjects.forEach(ValueObject::toString); // to really load nested object
				}
				catch (ObjectNotFoundException onfex) {
					nestedObjects.clear();
				}
			}
		}
	}
	
	private static void checkAudited(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		Assert.state(entity.isAudited(), "entity is not audited");
	}
	
	private static Revision getRevision(Session session, Integer id) {
		return session.get(RevisionEntity.class, id);
	}
	
	private static List<Number> getRevisionNumbers(Session session, Class<?> entityClass, Long id) {
		return createAuditReader(session).getRevisions(entityClass, id);
	}
	
	private static AuditReader createAuditReader(Session session) {
		return AuditReaderFactory.get(session);
	}

}
