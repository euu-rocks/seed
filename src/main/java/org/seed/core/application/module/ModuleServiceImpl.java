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

import static org.seed.core.util.CollectionUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.data.AbstractSystemEntityService;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;
import org.seed.core.util.UID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class ModuleServiceImpl extends AbstractSystemEntityService<Module>
	implements ModuleService, ModuleDependent<Module> {
	
	@Autowired
	private ModuleRepository repository;
	
	@Autowired
	private ModuleValidator validator;
	
	@Autowired
	private ModuleTransfer transfer;
	
	@Override
	protected ModuleRepository getRepository() {
		return repository;
	}

	@Override
	protected ModuleValidator getValidator() {
		return validator;
	}
	
	@Override
	public boolean isExternalDirEnabled() {
		return transfer.isExternalDirEnabled();
	}
	
	@Override
	public boolean existOtherModules(Module module, Session session) {
		Assert.notNull(module, C.MODULE);
		
		return anyMatch(repository.find(session), not(module::equals));
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public List<Module> getAvailableNesteds(Module module, Session session) {
		Assert.notNull(module, C.MODULE);
		
		return subList(repository.find(session), 
					   mod -> !module.equals(mod) && 
					   		  !module.containsModule(mod) &&
					   		  !mod.containsModule(module));
	}
	
	@Override
	public List<Module> findUsage(Module module, Session session) {
		Assert.notNull(module, C.MODULE);
		
		return subList(repository.find(session), 
					   mod -> mod.containsNestedModule(module));
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public Module readModule(InputStream inputStream) {
		try {
			return transfer.readModule(inputStream);
		} 
		catch (IOException ioex) {
			throw new InternalException(ioex);
		}	
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public Module readModuleFromDir(String moduleName) {
		try {
			return transfer.readModuleFromDir(moduleName);
		} 
		catch (IOException ioex) {
			throw new InternalException(ioex);
		}	
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public byte[] exportModule(Module module) {
		try {
			return transfer.exportModule(module);
		} 
		catch (IOException ioex) {
			throw new InternalException(ioex);
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public void exportModuleToDir(Module module) {
		try {
			transfer.exportModuleToDir(module);
		} 
		catch (IOException ioex) {
			throw new InternalException(ioex);
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public ImportAnalysis analyzeModule(Module module) throws ValidationException {
		Assert.notNull(module, C.MODULE);
		
		final var currentVersionModule = repository.findByUid(module.getUid());
		validator.validateImport(module, currentVersionModule);
		
		final var analysis = new ImportAnalysis(module);
		transfer.analyzeModule(module, analysis);
		return analysis;
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public void importModule(Module module) throws ValidationException {
		transfer.importModule(module);
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public ModuleParameter createParameter(Module module) {
		Assert.notNull(module, C.MODULE);
		
		final var parameter = new ModuleParameter();
		module.addParameter(parameter);
		return parameter;
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public NestedModule createNested(Module module) {
		Assert.notNull(module, C.MODULE);
		
		final var nested = new NestedModule();
		module.addNested(nested);
		return nested;
	}
	
	@Override
	@Secured("ROLE_ADMIN_MODULE")
	public void saveObject(Module module) throws ValidationException {
		Assert.notNull(module, C.MODULE);
		final var moduleMeta = (ModuleMetadata) module;
		
		try (Session session = repository.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				if (module.getUid() == null) {
					moduleMeta.setUid(UID.createUID());
				}
				filterAndForEach(module.getParameters(), 
								 param -> param.getUid() == null, 
								 param -> param.setUid(UID.createUID()));
				filterAndForEach(module.getNesteds(), 
						 		 nested -> nested.getUid() == null, 
						 		 nested -> nested.setUid(UID.createUID()));
				saveObject(module, session);
				
				if (moduleMeta.getChangedObjects() != null) {
					for (var changedObject : moduleMeta.getChangedObjects()) {
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
