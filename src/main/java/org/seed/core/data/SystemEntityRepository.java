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

import org.hibernate.Session;

public interface SystemEntityRepository<T extends SystemEntity> {
	
	Class<? extends T> getEntityTypeClass();
	
	T get(Long id);
	
	T get(Long id, Session session);
	
	void reload(T object, Session session);
	
	boolean exist(QueryParameter ...params);
	
	boolean exist(Session session, QueryParameter ...params);
	
	List<T> find(QueryParameter ...params);
	
	List<T> find(Session session, QueryParameter ...params);
	
	T findUnique(QueryParameter ...params);
	
	T findUnique(Session session, QueryParameter ...params);
	
	void save(T object);
	
	void save(T object, Session session);
	
	void delete(T object);
	
	void delete(T object, Session sessionTx);
	
}
