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

import org.seed.core.config.changelog.AbstractChangeLogBuilder;
import org.seed.core.config.changelog.ChangeLog;
import org.seed.core.config.changelog.CreateFunctionChange;
import org.seed.core.config.changelog.CreateTriggerChange;
import org.seed.core.config.changelog.DropFunctionChange;
import org.seed.core.config.changelog.DropTriggerChange;
import org.seed.core.util.StreamUtils;

import org.springframework.util.ObjectUtils;

import liquibase.change.core.CreateProcedureChange;
import liquibase.change.core.CreateViewChange;
import liquibase.change.core.DropProcedureChange;
import liquibase.change.core.DropViewChange;

class DBObjectChangeLogBuilder extends AbstractChangeLogBuilder<DBObject> {
	
	@Override
	public ChangeLog build() {
		checkValid();
		
		// create object
		if (currentVersionObject == null) {
			addCreateChange(nextVersionObject);
		}
		// drop object
		else if (nextVersionObject == null) {
			addDropChange(currentVersionObject);
		}
		else {
			// content changed
			if (!ObjectUtils.nullSafeEquals(currentVersionObject.getContent(), 
					   						nextVersionObject.getContent())) {
				addDropChange(currentVersionObject);
				addCreateChange(nextVersionObject);
			}
		}
		return super.build();
	}
	
	private void addCreateChange(DBObject dbObject) {
		switch (dbObject.getType()) {
			case VIEW:
				addCreateViewChange(dbObject);
				break;
				
			case PROCEDURE:
				addCreateProcedureChange(dbObject);
				break;
				
			case FUNCTION:
				addCreateFunctionChange(dbObject);
				break;
				
			case TRIGGER:
				addCreateTriggerChange(dbObject);
				break;
				
			default:
				throw new UnsupportedOperationException(dbObject.getType().name());
		}
	}
	
	private void addDropChange(DBObject dbObject) {
		switch (dbObject.getType()) {
			case VIEW:
				addDropViewChange(dbObject);
				break;
				
			case PROCEDURE:
				addDropProcedureChange(dbObject);
				break;
				
			case FUNCTION:
				addDropFunctionChange(dbObject);
				break;
				
			case TRIGGER:
				addDropTriggerChange(dbObject);
				break;
				
			default:
				throw new UnsupportedOperationException(dbObject.getType().name());
		}
	}
	
	private void addCreateViewChange(DBObject dbObject) {
		final CreateViewChange createViewChange = new CreateViewChange();
		createViewChange.setFullDefinition(Boolean.FALSE);
		createViewChange.setEncoding(StreamUtils.CHARSET.name());
		createViewChange.setViewName(dbObject.getInternalName());
		createViewChange.setSelectQuery(dbObject.getContent());
		addChange(createViewChange);
	}
	
	private void addDropViewChange(DBObject dbObject) {
		final DropViewChange dropViewChange = new DropViewChange();
		dropViewChange.setViewName(dbObject.getInternalName());
		addChange(dropViewChange);
	}
	
	private void addCreateProcedureChange(DBObject dbObject) {
		final CreateProcedureChange createProcedureChange = new CreateProcedureChange();
		createProcedureChange.setEncoding(StreamUtils.CHARSET.name());
		createProcedureChange.setProcedureName(dbObject.getInternalName());
		createProcedureChange.setProcedureText(dbObject.getContent());
		addChange(createProcedureChange);
	}
	
	private void addDropProcedureChange(DBObject dbObject) {
		final DropProcedureChange dropProcedureChange = new DropProcedureChange();
		dropProcedureChange.setProcedureName(dbObject.getInternalName());
		addChange(dropProcedureChange);
	}
	
	private void addCreateFunctionChange(DBObject dbObject) {
		final CreateFunctionChange createFunctionChange = new CreateFunctionChange();
		createFunctionChange.setFunctionText(StreamUtils.compress(dbObject.getContent()));
		addChange(createFunctionChange);
	}
		
	private void addDropFunctionChange(DBObject dbObject) {
		final DropFunctionChange dropFunctionChange = new DropFunctionChange();
		dropFunctionChange.setFunctionName(dbObject.getInternalName());
		addChange(dropFunctionChange);
	}
	
	private void addCreateTriggerChange(DBObject dbObject) {
		final CreateTriggerChange createTriggerChange = new CreateTriggerChange();
		createTriggerChange.setTriggerText(StreamUtils.compress(dbObject.getContent()));
		addChange(createTriggerChange);
	}
		
	private void addDropTriggerChange(DBObject dbObject) {
		final DropTriggerChange dropTriggerChange = new DropTriggerChange();
		dropTriggerChange.setTriggerName(dbObject.getInternalName());
		addChange(dropTriggerChange);
	}
	
}
