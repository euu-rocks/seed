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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.data.AbstractSystemEntityService;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationException;
import org.seed.core.util.UID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class ModuleServiceImpl extends AbstractSystemEntityService<Module>
	implements ModuleService {
	
	@Autowired
	private ModuleRepository repository;
	
	@Autowired
	private ModuleValidator validator;
	
	@Autowired
	private ModuleTransfer transfer;
	
	@Autowired
	private List<ModuleDependent> moduleDependents;
	
	@Override
	protected ModuleRepository getRepository() {
		return repository;
	}

	@Override
	protected ModuleValidator getValidator() {
		return validator;
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public Module readModule(InputStream inputStream) {
		return transfer.readModule(inputStream);
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public byte[] exportModule(Module module) {
		return transfer.exportModule(module);
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public ImportAnalysis analyzeModule(Module module) throws ValidationException {
		Assert.notNull(module, "module is null");
		
		final Module currentVersionModule = repository.findByUid(module.getUid());
		validator.validateImport(module, currentVersionModule);
		return transfer.analyzeModule(module);
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public void importModule(Module module) {
		transfer.importModule(module);
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public ModuleParameter createParameter(Module module) {
		Assert.notNull(module, "module is null");
		
		final ModuleParameter parameter = new ModuleParameter();
		module.addParameter(parameter);
		return parameter;
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public void deleteObject(Module module) throws ValidationException {
		Assert.notNull(module, "module is null");
		
		final List<SystemEntity> moduleObjects = new ArrayList<>();
		for (ModuleDependent dependent : moduleDependents) {
			moduleObjects.addAll(dependent.findUsage(module));
		}
		
		try (Session session = repository.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				for (SystemEntity entity : moduleObjects) {
					((AbstractApplicationEntity) entity).setModule(null);
					session.update(entity);
				}
				
				deleteObject(module, session);
				tx.commit();
			}
			catch (Exception ex) {
				handleException(tx, ex);
			}
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public void saveObject(Module module) throws ValidationException {
		Assert.notNull(module, "module is null");
		
		try (Session session = repository.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				if (module.getUid() == null) {
					((ModuleMetadata) module).setUid(UID.createUID());
				}
				if (module.getParameters() != null) {
					for (ModuleParameter param : module.getParameters()) {
						if (param.getUid() == null) {
							param.setUid(UID.createUID());
						}
					}
				}
				saveObject(module, session);
				
				if (((ModuleMetadata) module).getChangedObjects() != null) {
					for (ApplicationEntity changedObject : ((ModuleMetadata)module).getChangedObjects()) {
						if (changedObject.getModule() == null) {
							((AbstractApplicationEntity) changedObject).setModule(module);
						}
						else {
							((AbstractApplicationEntity) changedObject).setModule(null);
						}
						session.update(changedObject);
					}
				}
				tx.commit();
			}
			catch (Exception ex) {
				handleException(tx, ex);
			}
		}
	}
	
}
