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
package org.seed.core.application;

import java.util.List;

import org.hibernate.Session;

import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.config.SchemaVersion;
import org.seed.core.data.SystemEntityService;
import org.seed.core.data.ValidationException;

public interface ApplicationEntityService<T extends ApplicationEntity> extends SystemEntityService<T> {
	
	T findByUid(Session session, String uid);
	
	List<T> findObjectsWithoutModule(Session session);
	
	void analyzeObjects(ImportAnalysis analysis, Module currentVersionModule);
	
	Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies();
	
	void handleSchemaUpdate(TransferContext context, SchemaVersion schemaVersion);
	
	void importObjects(TransferContext context, Session session) throws ValidationException;
	
	void createChangeLogs(TransferContext context, Session session);
	
	void deleteObjects(Module module, Module currentVersionModule, Session session);
	
	void saveObject(T object, Session session) throws ValidationException;
	
}
