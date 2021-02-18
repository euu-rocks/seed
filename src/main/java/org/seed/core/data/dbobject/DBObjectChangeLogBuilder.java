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

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.seed.core.config.AbstractChangeLogBuilder;
import org.seed.core.config.ChangeLog;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import liquibase.change.core.CreateViewChange;
import liquibase.change.core.DropViewChange;

class DBObjectChangeLogBuilder extends AbstractChangeLogBuilder {
	
	private DBObject currentVersionObject;
	
	private DBObject nextVersionObject;

	DBObjectChangeLogBuilder setCurrentVersionObject(DBObject currentVersionObject) {
		this.currentVersionObject = currentVersionObject;
		return this;
	}

	DBObjectChangeLogBuilder setNextVersionObject(DBObject nextVersionObject) {
		this.nextVersionObject = nextVersionObject;
		return this;
	}

	@Override
	public List<ChangeLog> build() {
		// create object
		if (currentVersionObject == null) {
			switch (nextVersionObject.getType()) {
				case VIEW:
					addCreateViewChangeSet(nextVersionObject);
					break;
			}
		}
		// drop object
		else if (nextVersionObject == null) {
			switch (currentVersionObject.getType()) {
				case VIEW:
					addDropViewChangeSet(currentVersionObject);
					break;
			}
		}
		else {
			final boolean nameChanged = !ObjectUtils.nullSafeEquals(currentVersionObject.getInternalName(), 
					   												nextVersionObject.getInternalName());
			final boolean contentChanged = !ObjectUtils.nullSafeEquals(currentVersionObject.getContent(), 
																	   nextVersionObject.getContent());
			switch (nextVersionObject.getType()) {
				case VIEW:
					if (nameChanged || contentChanged) {
						addDropViewChangeSet(currentVersionObject);
						addCreateViewChangeSet(nextVersionObject);
					}
					break;
			}
		}
		return super.build();
	}
	
	private void addCreateViewChangeSet(DBObject dbObject) {
		Assert.notNull(dbObject, "dbObject is null");
		
		final CreateViewChange createViewChange = new CreateViewChange();
		createViewChange.setFullDefinition(Boolean.FALSE);
		createViewChange.setEncoding(StandardCharsets.UTF_8.name());
		createViewChange.setViewName(dbObject.getInternalName());
		createViewChange.setSelectQuery(dbObject.getContent());
		createChangeSet().addChange(createViewChange);
	}
	
	private void addDropViewChangeSet(DBObject dbObject) {
		Assert.notNull(dbObject, "dbObject is null");
		
		final DropViewChange dropViewChange = new DropViewChange();
		dropViewChange.setViewName(dbObject.getInternalName());
		createChangeSet().addChange(dropViewChange);
	}
	
}
