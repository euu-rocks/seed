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

public interface SystemEntityService<T extends SystemEntity> {
	
	boolean isEntityType(Class<?> clas);
	
	T createInstance(@Nullable Options options);
	
	T getObject(Long id);
	
	T getObject(Long id, Session session);
	
	T findByName(String name);
	
	T findByName(String name, Session session);
	
	void initObject(T object) throws ValidationException;
	
	void reloadObject(T object, Session session);
	
	boolean existObjects(Session session);
	
	List<T> getObjects();
	
	List<T> getObjects(Session session);
	
	void saveObject(T object) throws ValidationException;
	
	void deleteObject(T object) throws ValidationException;
	
}
