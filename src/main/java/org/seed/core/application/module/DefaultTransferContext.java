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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.seed.C;
import org.seed.core.data.dbobject.DBObject;
import org.seed.core.entity.Entity;
import org.seed.core.util.Assert;

class DefaultTransferContext implements TransferContext {
	
	private final Set<Entity> newEntities = new HashSet<>();
	
	private final Set<Entity> existingEntities = new HashSet<>();
	
	private final Set<DBObject> newDBObjects = new HashSet<>();
	
	private final Set<DBObject> existingDBObjects = new HashSet<>();
	
	private final Map<String, Entity> currentVersionEntityMap = new HashMap<>();
	
	private final Map<String, DBObject> currentVersionDBObjectMap = new HashMap<>();
	
	private final Module module;

	DefaultTransferContext(Module module) {
		Assert.notNull(module, C.MODULE);
		
		this.module = module;
	}
	
	@Override
	public Module getModule() {
		return module;
	}
	
	@Override
	public Collection<Entity> getNewEntities() {
		return newEntities;
	}
	
	@Override
	public Collection<Entity> getExistingEntities() {
		return existingEntities;
	}

	@Override
	public Entity getCurrentVersionEntity(String uid) {
		Assert.notNull(uid, C.UID);
		
		return currentVersionEntityMap.get(uid);
	}
	
	@Override
	public void addNewEntity(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		newEntities.add(entity);
	}
	
	@Override
	public void addExistingEntity(Entity entity, Entity currentVersionEntity) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(currentVersionEntity, "currentVersionEntity");
		
		existingEntities.add(entity);
		currentVersionEntityMap.put(entity.getUid(), currentVersionEntity);
	}
	
	@Override
	public Collection<DBObject> getNewDBObjects() {
		return newDBObjects;
	}
	
	@Override
	public Collection<DBObject> getExistingDBObjects() {
		return existingDBObjects;
	}
	
	@Override
	public DBObject getCurrentVersionDBObject(String uid) {
		Assert.notNull(uid, C.UID);
		
		return currentVersionDBObjectMap.get(uid);
	}
	
	@Override
	public void addNewDBObject(DBObject dbObject) {
		Assert.notNull(dbObject, C.DBOBJECT);
		
		newDBObjects.add(dbObject);
	}
	
	@Override
	public void addExistingDBObject(DBObject dbObject, DBObject currentVersionDBObject) {
		Assert.notNull(dbObject, C.DBOBJECT);
		Assert.notNull(currentVersionDBObject, "currentVersionDBObject");
		
		existingDBObjects.add(dbObject);
		currentVersionDBObjectMap.put(dbObject.getUid(), currentVersionDBObject);
	}
	
}
