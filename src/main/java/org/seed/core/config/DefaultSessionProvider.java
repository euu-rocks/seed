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
package org.seed.core.config;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import org.seed.core.util.Assert;

import org.springframework.stereotype.Component;

@Component
public class DefaultSessionProvider implements SessionProvider {
	
	private SessionFactory sessionFactory;
	
	private Dialect dialect;
	
	void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public Session getSession() {
		return getSessionFactory().openSession();
	}

	@Override
	public Dialect getDialect() {
		if (dialect == null) {
			dialect = ((SessionFactoryImplementor) getSessionFactory()).getJdbcServices().getDialect();
		}
		return dialect;
	}

	private SessionFactory getSessionFactory() {
		Assert.stateAvailable(sessionFactory, "session factory");
		
		return sessionFactory;
	}
	
}
