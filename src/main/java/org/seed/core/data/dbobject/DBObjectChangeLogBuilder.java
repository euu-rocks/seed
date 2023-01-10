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
import org.seed.core.config.changelog.CreateSequenceChange;
import org.seed.core.config.changelog.CreateTriggerChange;
import org.seed.core.config.changelog.DropFunctionChange;
import org.seed.core.config.changelog.DropSequenceChange;
import org.seed.core.config.changelog.DropTriggerChange;
import org.seed.core.util.MiscUtils;
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
		
		// create or enable object
		if ((currentVersionObject == null || !currentVersionObject.isEnabled()) && 
			nextVersionObject.isEnabled()) {
			addCreateChange(nextVersionObject);
		}
		// drop or disable object
		else if (nextVersionObject == null || (!nextVersionObject.isEnabled() &&
											   currentVersionObject != null &&
											   currentVersionObject.isEnabled())) {
			addDropChange(currentVersionObject);
		}
		// content changed
		else if (nextVersionObject.isEnabled() &&
				 currentVersionObject != null &&
				 !ObjectUtils.nullSafeEquals(currentVersionObject.getContent(), 
					   						 nextVersionObject.getContent())) {
			addDropChange(currentVersionObject);
			addCreateChange(nextVersionObject);
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
			
			case SEQUENCE:
				addCreateSequenceChange(dbObject);
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
			
			case SEQUENCE:
				addDropSequenceChange(dbObject);
				break;
				
			default:
				throw new UnsupportedOperationException(dbObject.getType().name());
		}
	}
	
	private void addCreateViewChange(DBObject dbObject) {
		final var createViewChange = new CreateViewChange();
		createViewChange.setFullDefinition(Boolean.FALSE);
		createViewChange.setEncoding(MiscUtils.CHARSET.name());
		createViewChange.setViewName(dbObject.getObjectName());
		createViewChange.setSelectQuery(dbObject.getContent());
		addChange(createViewChange);
	}
	
	private void addDropViewChange(DBObject dbObject) {
		final var dropViewChange = new DropViewChange();
		dropViewChange.setViewName(dbObject.getObjectName());
		addChange(dropViewChange);
	}
	
	private void addCreateProcedureChange(DBObject dbObject) {
		final var createProcedureChange = new CreateProcedureChange();
		createProcedureChange.setEncoding(MiscUtils.CHARSET.name());
		createProcedureChange.setProcedureName(dbObject.getObjectName());
		createProcedureChange.setProcedureText(dbObject.getContent());
		addChange(createProcedureChange);
	}
	
	private void addDropProcedureChange(DBObject dbObject) {
		final var dropProcedureChange = new DropProcedureChange();
		dropProcedureChange.setProcedureName(dbObject.getObjectName());
		addChange(dropProcedureChange);
	}
	
	private void addCreateFunctionChange(DBObject dbObject) {
		final var createFunctionChange = new CreateFunctionChange();
		createFunctionChange.setFunctionText(getCompressedContent(dbObject));
		addChange(createFunctionChange);
	}
		
	private void addDropFunctionChange(DBObject dbObject) {
		final var dropFunctionChange = new DropFunctionChange();
		dropFunctionChange.setFunctionName(dbObject.getObjectName());
		addChange(dropFunctionChange);
	}
	
	private void addCreateTriggerChange(DBObject dbObject) {
		final var createTriggerChange = new CreateTriggerChange();
		createTriggerChange.setTriggerText(getCompressedContent(dbObject));
		addChange(createTriggerChange);
	}
		
	private void addDropTriggerChange(DBObject dbObject) {
		final var dropTriggerChange = new DropTriggerChange();
		dropTriggerChange.setTriggerName(dbObject.getObjectName());
		addChange(dropTriggerChange);
	}
	
	private void addCreateSequenceChange(DBObject dbObject) {
		final var createSequenceChange = new CreateSequenceChange();
		createSequenceChange.setSequenceText(getCompressedContent(dbObject));
		addChange(createSequenceChange);
	}
	
	private void addDropSequenceChange(DBObject dbObject) {
		final var dropSequenceChange = new DropSequenceChange();
		dropSequenceChange.setSequenceName(dbObject.getObjectName());
		addChange(dropSequenceChange);
	}
	
	private static String getCompressedContent(DBObject dbObject) {
		return StreamUtils.compress(dbObject.getContent());
	}
}
