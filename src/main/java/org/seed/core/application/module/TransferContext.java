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
package org.seed.core.application.module;

import java.util.Collection;

import org.seed.core.data.dbobject.DBObject;
import org.seed.core.entity.Entity;

public interface TransferContext {
	
	Module getModule();
	
	void addNewEntity(Entity entity);
	
	void addExistingEntity(Entity entity, Entity currentVersionEntity);
	
	void addNewDBObject(DBObject dbObject);
	
	void addExistingDBObject(DBObject dbObject, DBObject currentVersionDBObject);
	
	Entity getCurrentVersionEntity(String uid);
	
	DBObject getCurrentVersionDBObject(String uid);
	
	Collection<Entity> getNewEntities();
	
	Collection<Entity> getExistingEntities();
	
	Collection<DBObject> getNewDBObjects();
	
	Collection<DBObject> getExistingDBObjects();
	
}
