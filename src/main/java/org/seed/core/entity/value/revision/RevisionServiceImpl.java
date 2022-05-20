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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;

import org.seed.C;
import org.seed.core.codegen.CodeManager;
import org.seed.core.config.SessionProvider;
import org.seed.core.entity.Entity;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RevisionServiceImpl implements RevisionService {
	
	@Autowired
	private SessionProvider sessionProvider;
	
	@Autowired
	private CodeManager codeManager;
	
	@Override
	public List<Revision> getRevisions(Entity entity, Long id) {
		try (Session session = sessionProvider.getSession()) {
			return getRevisionNumbers(session, getEntityClass(entity), id).stream()
				.map(num -> getRevision(session, num.intValue()))
				.collect(Collectors.toList());
		}
	}
	
	@Override
	public ValueObject getRevisionObject(Entity entity, Long id, Revision revision) {
		Assert.notNull(id, C.ID);
		Assert.notNull(revision, "revision");
		try (Session session = sessionProvider.getSession()) {
			return (ValueObject) createAuditReader(session)
									.find(getEntityClass(entity), id, revision.getId());
		}
	}
	
	@Override
	public ValueObject getRevisionObject(Entity entity, Long id, Date timestamp) {
		Assert.notNull(id, C.ID);
		Assert.notNull(timestamp, "timestamp");
		try (Session session = sessionProvider.getSession()) {
			return (ValueObject) createAuditReader(session)
									.find(getEntityClass(entity), id, timestamp);
		}
	}
	
	private Revision getRevision(Session session, Integer id) {
		return session.get(RevisionEntity.class, id);
	}
	
	private Class<?> getEntityClass(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		return codeManager.getGeneratedClass(entity);
	}
	
	private List<Number> getRevisionNumbers(Session session, Class<?> entityClass, Long id) {
		return createAuditReader(session).getRevisions(entityClass, id);
	}
	
	private static AuditReader createAuditReader(Session session) {
		return AuditReaderFactory.get(session);
	}

}
