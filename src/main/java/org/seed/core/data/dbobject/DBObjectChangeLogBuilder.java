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

import org.seed.core.config.AbstractChangeLogBuilder;
import org.seed.core.config.ChangeLog;
import org.seed.core.util.Assert;

import org.springframework.util.ObjectUtils;

import liquibase.change.core.CreateViewChange;
import liquibase.change.core.DropViewChange;

class DBObjectChangeLogBuilder extends AbstractChangeLogBuilder<DBObject> {
	
	@Override
	public ChangeLog build() {
		// create object
		if (currentVersionObject == null) {
			if (nextVersionObject.getType() == DBObjectType.VIEW) {
				addCreateViewChangeSet(nextVersionObject);
			}
		}
		// drop object
		else if (nextVersionObject == null) {
			if (currentVersionObject.getType() == DBObjectType.VIEW) {
				addDropViewChangeSet(currentVersionObject);
			}
		}
		else {
			final boolean nameChanged = !ObjectUtils.nullSafeEquals(currentVersionObject.getInternalName(), 
					   												nextVersionObject.getInternalName());
			final boolean contentChanged = !ObjectUtils.nullSafeEquals(currentVersionObject.getContent(), 
																	   nextVersionObject.getContent());
			if (nextVersionObject.getType() == DBObjectType.VIEW && 
				(nameChanged || contentChanged)) {
				addDropViewChangeSet(currentVersionObject);
				addCreateViewChangeSet(nextVersionObject);
			}
		}
		return super.build();
	}
	
	private void addCreateViewChangeSet(DBObject dbObject) {
		Assert.notNull(dbObject, "dbObject");
		
		final CreateViewChange createViewChange = new CreateViewChange();
		createViewChange.setFullDefinition(Boolean.FALSE);
		createViewChange.setEncoding(StandardCharsets.UTF_8.name());
		createViewChange.setViewName(dbObject.getInternalName());
		createViewChange.setSelectQuery(dbObject.getContent());
		addChange(createViewChange);
	}
	
	private void addDropViewChangeSet(DBObject dbObject) {
		Assert.notNull(dbObject, "dbObject");
		
		final DropViewChange dropViewChange = new DropViewChange();
		dropViewChange.setViewName(dbObject.getInternalName());
		addChange(dropViewChange);
	}
	
}
