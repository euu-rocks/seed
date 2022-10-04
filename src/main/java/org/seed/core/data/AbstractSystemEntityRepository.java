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

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.config.SessionProvider;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractSystemEntityRepository<T extends SystemEntity> 
	implements SystemEntityRepository<T> {
	
	@Autowired
	private SessionProvider sessionProvider;
	
	private final Class<? extends T> entityTypeClass;
	
	protected AbstractSystemEntityRepository(Class<? extends T> entityTypeClass) {
		Assert.notNull(entityTypeClass, "entityTypeClass");
		
		this.entityTypeClass = entityTypeClass;
	}
	
	@Override
	public Class<? extends T> getEntityTypeClass() {
		return entityTypeClass;
	}

	@Override
	public T get(Long id) {
		try (Session session = getSession()) {
			return get(id, session);
		}
	}
	
	@Override
	public T get(Long id, Session session) {
		Assert.notNull(id, C.ID);
		Assert.notNull(session, C.SESSION);
		
		return session.get(entityTypeClass, id);
	}
	
	@Override
	public void reload(T object, Session session) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(session, C.SESSION);
		
		session.refresh(object);
	}
	
	@Override
	public long count() {
		try (Session session = getSession()) {
			final CriteriaBuilder builder = session.getCriteriaBuilder();
			final CriteriaQuery<Long> query = builder.createQuery(Long.class);
			query.select(builder.count(query.from(entityTypeClass)));
			return session.createQuery(query).getSingleResult();
		}
	}
	
	@Override
	public boolean exist(QueryParameter ...params) {
		try (Session session = getSession()) {
			return exist(session, params);
		}
	}
	
	@Override
	public boolean exist(Session session, QueryParameter ...params) {
		Assert.notNull(session, C.SESSION);
		
		return !createQuery(session, params)
				.setMaxResults(1)
				.getResultList().isEmpty();
	}
	
	@Override
	public List<T> find(QueryParameter ...params) {
		try (Session session = getSession()) {
			return find(session, params);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<T> find(Session session, QueryParameter ...params) {
		Assert.notNull(session, C.SESSION);
		
		return createQuery(session, params).getResultList();
	}
	
	@Override
	public T findUnique(QueryParameter ...params) {
		try (Session session = getSession()) {
			return findUnique(session, params);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T findUnique(Session session, QueryParameter ...params) {
		Assert.notNull(session, C.SESSION);
		
		return ((org.hibernate.query.Query<T>) createQuery(session, params))
												.setMaxResults(2)
												.uniqueResult();
	}
	
	@Override
	public void save(T object) {
		Assert.notNull(object, C.OBJECT);
		
		final boolean isInsert = object.isNew();
		try (Session session = getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				save(object, session);
				tx.commit();
			}
			catch (Exception ex) {
				if (tx != null) {
					tx.rollback();
				}
				if (isInsert) {
					// reset id because its assigned even if insert fails
					((AbstractSystemObject) object).resetId();
				}
				handleException(ex);
			}
		}
	}
	
	@Override
	public void save(T object, Session session) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(session, C.SESSION);
		
		session.saveOrUpdate(object);
	}
	
	@Override
	public void delete(T object) {
		Assert.notNull(object, C.OBJECT);
		
		try (Session session = getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				delete(object, session);
				tx.commit();
			}
			catch (Exception ex) {
				if (tx != null) {
					tx.rollback();
				}
				handleException(ex);
			}
		}
	}
	
	@Override
	public void delete(T object, Session session) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(session, C.SESSION);
		
		session.delete(object);
	}
	
	protected Session getSession() {
		return sessionProvider.getSession();
	}
	
	@SuppressWarnings({"unchecked"})
	private Query createQuery(Session session, QueryParameter ...params) {
		final CriteriaBuilder builder = session.getCriteriaBuilder();
		final CriteriaQuery<T> query = (CriteriaQuery<T>) builder.createQuery(entityTypeClass);
		final Root<T> table = (Root<T>) query.from(entityTypeClass);
		
		query.select(table);
		if (params != null) {
			if (params.length == 1) {
				query.where(createRestriction(builder, table, params[0]));
			}
			else if (params.length > 1) {
				final Predicate[] restrictions = new Predicate[params.length];
				for (int i = 0; i < params.length; i++) {
					restrictions[i] = createRestriction(builder, table, params[i]);
				}
				query.where(builder.and(restrictions));
			}
		}
		return session.createQuery(query)
					  .setCacheable(true);
	}
	
	private Predicate createRestriction(CriteriaBuilder builder, Root<T> table, QueryParameter param) {
		if (param.value == QueryParameter.IS_NULL) {
			return builder.isNull(table.get(param.name));
		}
		if (param.value == QueryParameter.NOT_NULL) {
			return builder.isNotNull(table.get(param.name));
		}
		return builder.equal(table.get(param.name), param.value);
	}
	
	protected static void handleException(Exception ex) {
		if (ex instanceof RuntimeException) {
			throw (RuntimeException) ex;
		}
		else {
			throw new InternalException(ex);
		}
	}
	
	protected static QueryParameter queryParam(String name, Object value) {
		return new QueryParameter(name, value);
	}
	
}
