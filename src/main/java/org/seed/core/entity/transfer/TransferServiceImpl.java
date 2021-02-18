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
package org.seed.core.entity.transfer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.Session;

import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.codegen.CodeManager;
import org.seed.core.data.FileObject;
import org.seed.core.data.Options;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityDependent;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class TransferServiceImpl extends AbstractApplicationEntityService<Transfer>
	implements TransferService, EntityDependent {
	
	@Autowired
	private EntityService entityService;
	
	@Autowired
	private ValueObjectService valueObjectService;
	
	@Autowired
	private CodeManager codeManager;
	
	@Autowired
	private TransferRepository transferRepository;
	
	@Autowired
	private TransferValidator validator;
	
	@Override
	protected TransferRepository getRepository() {
		return transferRepository;
	}

	@Override
	protected TransferValidator getValidator() {
		return validator;
	}
	
	@Override
	public Transfer createInstance(@Nullable Options options) {
		final TransferMetadata instance = (TransferMetadata) super.createInstance(options);
		instance.createLists();
		return instance;
	}
	
	@Override
	public ImportOptions createImportOptions(Transfer transfer) {
		Assert.notNull(transfer, "transfer is null");
		
		final ImportOptions options = new ImportOptions();
		options.setAllOrNothing(true);
		options.setCreateIfNew(true);
		if (transfer.getIdentifierField() != null) {
			options.setModifyExisting(true);
		}
		options.setExecuteCallbacks(true);
		return options;
	}
	
	@Override
	public void initObject(Transfer transfer) throws ValidationException {
		Assert.notNull(transfer, "transfer is null");
		
		super.initObject(transfer);
		final TransferMetadata transferMeta = (TransferMetadata) transfer;
		switch (transfer.getFormat()) {
			case CSV:
				transferMeta.setQuoteAll(true);
				transferMeta.setEncoding(CharEncoding.UTF8);
				transferMeta.setNewline(Newline.systemDefault());
				break;
				
			default:
				throw new UnsupportedOperationException(transfer.getFormat().name());
		}
	}
	
	@Override
	public List<Transfer> findTransfers(Entity entity) {
		Assert.notNull(entity, "entity is null");
		
		return transferRepository.find(queryParam("entity", entity));
	}
	
	@Override
	public List<TransferElement> getAvailableElements(Transfer transfer) {
		Assert.notNull(transfer, "transfer is null");
		
		final Entity entity = transfer.getEntity();
		final List<TransferElement> result = new ArrayList<>();
		if (entity.hasAllFields()) {
			for (EntityField entityField : entity.getAllFields()) {
				boolean found = false;
				if (transfer.hasElements()) {
					for (TransferElement element : transfer.getElements()) {
						if (entityField.equals(element.getEntityField())) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					result.add(createElement(transfer, entityField));
				}
			}
		}
		return result;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public byte[] doExport(Transfer transfer) {
		Assert.notNull(transfer, "transfer is null");
		
		return getProcessor(transfer).doExport();
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public TransferResult doImport(Transfer transfer, ImportOptions options, 
								   FileObject importFile) throws ValidationException {
		Assert.notNull(transfer, "transfer is null");
		Assert.notNull(transfer, "options are null");
		Assert.notNull(importFile, "importFile is null");
		
		validator.validateImport(importFile);
		
		return getProcessor(transfer)
				.doImport(options, new ByteArrayInputStream(importFile.getContent()));
	}
	
	@Override
	public List<Transfer> findUsage(Entity entity) {
		return findTransfers(entity);
	}

	@Override
	public List<Transfer> findUsage(EntityField entityField) {
		Assert.notNull(entityField, "entityField is null");
		
		final List<Transfer> result = new ArrayList<>();
		for (Transfer transfer : findTransfers(entityField.getEntity())) {
			if (transfer.hasElements()) {
				for (TransferElement element : transfer.getElements()) {
					if (entityField.equals(element.getEntityField())) {
						result.add(transfer);
						break;
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public List<Transfer> findUsage(EntityFieldGroup fieldGroup) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Transfer> findUsage(NestedEntity nestedEntity) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Transfer> findUsage(EntityStatus entityStatus) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Transfer> findUsage(EntityFunction entityFunction) {
		return Collections.emptyList();
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void saveObject(Transfer transfer) throws ValidationException {
		super.saveObject(transfer);
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void deleteObject(Transfer transfer) throws ValidationException {
		super.deleteObject(transfer);
	}
	
	@Override
	public void analyzeObjects(ImportAnalysis analysis, Module currentVersionModule) {
		Assert.notNull(analysis, "analysis is null");
		
		if (analysis.getModule().getTransfers() != null) {
			for (Transfer transfer : analysis.getModule().getTransfers()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(transfer);
				}
				else {
					final Transfer currentVersionTransfer =
						currentVersionModule.getTransferByUid(transfer.getUid());
					if (currentVersionTransfer == null) {
						analysis.addChangeNew(transfer);
					}
					else if (!transfer.isEqual(currentVersionTransfer)) {
						analysis.addChangeModify(transfer);
					}
				}
			}
		}
		if (currentVersionModule != null && currentVersionModule.getTransfers() != null) {
			for (Transfer currentVersionTransfer : currentVersionModule.getTransfers()) {
				if (analysis.getModule().getTransferByUid(currentVersionTransfer.getUid()) == null) {
					analysis.addChangeDelete(currentVersionTransfer);
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<? extends ApplicationEntity>>[] getImportDependencies() {
		return (Class<? extends ApplicationEntityService<? extends ApplicationEntity>>[]) 
				new Class[] { EntityService.class };
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, "context is null");
		Assert.notNull(session, "session is null");
		try {
			if (context.getModule().getTransfers() != null) {
				for (Transfer transfer : context.getModule().getTransfers()) {
					final Transfer currentVersionTransfer = findByUid(session, transfer.getUid());
					final Entity entity = entityService.findByUid(session, transfer.getEntityUid());
					((TransferMetadata) transfer).setModule(context.getModule());
					((TransferMetadata) transfer).setEntity(entity);
					if (currentVersionTransfer != null) {
						((TransferMetadata) currentVersionTransfer).copySystemFieldsTo(transfer);
						session.detach(currentVersionTransfer);
					}
					if (transfer.hasElements()) {
						for (TransferElement element : transfer.getElements()) {
							element.setTransfer(transfer);
							element.setEntityField(entity.findFieldByUid(element.getFieldUid()));
							final TransferElement currentVersionElement =
								currentVersionTransfer != null 
									? currentVersionTransfer.getElementByUid(element.getUid()) 
									: null;
							if (currentVersionElement != null) {
								currentVersionElement.copySystemFieldsTo(element);
							}
						}
					}
					saveObject(transfer, session);
				}
			}
		}
		catch (ValidationException vex) {
			throw new RuntimeException(vex);
		}
	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, "module is null");
		Assert.notNull(currentVersionModule, "currentVersionModule is null");
		Assert.notNull(session, "session is null");
		
		if (currentVersionModule.getTransfers() != null) {
			for (Transfer currentVersionTransfer : currentVersionModule.getTransfers()) {
				if (module.getTransferByUid(currentVersionTransfer.getUid()) == null) {
					session.delete(currentVersionTransfer);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private TransferProcessor getProcessor(Transfer transfer) {
		final Class<?> objectClass = codeManager.getGeneratedClass(transfer.getEntity());
		final Class<? extends ValueObject> valueObjectClass = (Class<? extends ValueObject>) objectClass;
		switch (transfer.getFormat()) {
			case CSV:
				return new CSVProcessor(valueObjectService, valueObjectClass, transfer);
			
			default:
				throw new UnsupportedOperationException(transfer.getFormat().name());
		}
	}
	
	private static TransferElement createElement(Transfer transfer, EntityField entityField) {
		Assert.notNull(transfer, "transfer is null");
		Assert.notNull(entityField, "entityField is null");
		
		final TransferElement element = new TransferElement();
		element.setTransfer(transfer);
		element.setEntityField(entityField);
		if (entityField.isUnique() && transfer.getIdentifierField() == null) {
			element.setIdentifier(true);
		}
		return element;
	}

}
