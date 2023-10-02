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
package org.seed.core.entity;

import java.util.List;

import org.hibernate.Session;

import org.seed.core.data.SystemEntity;

public interface EntityDependent<T extends SystemEntity> {
	
	List<T> findUsage(Entity entity, Session session);
	
	List<T> findUsage(Entity entity, EntityField entityField, Session session);
	
	List<T> findUsage(Entity entity, EntityFieldGroup fieldGroup);
	
	List<T> findUsage(Entity entity, EntityRelation entityRelation, Session session);
	
	List<T> findUsage(EntityStatus entityStatus, Session session);
	
	List<T> findUsage(EntityFunction entityFunction, Session session);
	
	List<T> findUsage(NestedEntity nestedEntity, Session session);
	
}
