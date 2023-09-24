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

import static org.seed.core.util.CollectionUtils.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.Session;

import org.seed.C;
import org.seed.InternalException;
import org.seed.LabelProvider;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.codegen.CodeManager;
import org.seed.core.config.SystemLog;
import org.seed.core.data.FileObject;
import org.seed.core.data.Options;
import org.seed.core.data.QueryCursor;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityDependent;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupDependent;
import org.seed.core.user.UserGroupService;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class TransferServiceImpl extends AbstractApplicationEntityService<Transfer>
	implements TransferService, EntityDependent<Transfer>, UserGroupDependent<Transfer> {
	
	@Autowired
	private EntityService entityService;
	
	@Autowired
	private UserGroupService userGroupService;
	
	@Autowired
	private ValueObjectService valueObjectService;
	
	@Autowired
	private CodeManager codeManager;
	
	@Autowired
	private TransferRepository transferRepository;
	
	@Autowired
	private TransferValidator validator;
	
	@Autowired
	private LabelProvider labelProvider;
	
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
		final var instance = (TransferMetadata) super.createInstance(options);
		instance.createLists();
		return instance;
	}
	
	@Override
	public List<Transfer> getTransfers(User user, Session session) {
		Assert.notNull(user, C.USER);
		
		return subList(getObjects(session), transfer -> transfer.checkPermissions(user));
	}
	
	@Override
	public ImportOptions createImportOptions(Transfer transfer) {
		Assert.notNull(transfer, C.TRANSFER);
		
		final var options = new ImportOptions();
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
		Assert.notNull(transfer, C.TRANSFER);
		
		super.initObject(transfer);
		final var transferMeta = (TransferMetadata) transfer;
		
		switch (transfer.getFormat()) {
			case CSV:
				transferMeta.setEncoding(CharEncoding.UTF8);
				transferMeta.setNewline(Newline.systemDefault());
				break;
				
			case JSON:
				break; // do nothing
				
			default:
				throw new UnsupportedOperationException(transfer.getFormat().name());
		}
	}
	
	@Override
	public List<TransferElement> getAvailableElements(Transfer transfer, List<TransferElement> elements) {
		Assert.notNull(transfer, C.TRANSFER);
		Assert.notNull(elements, ELEMENTS);
		final var entity = transfer.getEntity();
		final var result = new ArrayList<TransferElement>();
		
		boolean identifierFound = false;
		for (EntityField entityField : entity.getAllFields()) {
			// skip non-transferable types
			if (entityField.getType().isBinary() || 
				entityField.getType().isFile() ||
				entityField.isReferenceField()) {
				continue;
			}
			if (!anyMatch(elements, elem -> entityField.equals(elem.getEntityField()))) {
				final var element = createElement(transfer, entityField);
				if (entityField.isUnique() && !identifierFound) {
					element.setIdentifier(true);
					identifierFound = true;
				}
				result.add(element);
			}
		}
		return result;
	}
	
	@Override
	public List<TransferElement> getAvailableNestedElements(NestedTransfer nestedTransfer, List<TransferElement> elements) {
		Assert.notNull(nestedTransfer, "NestedTransfer");
		Assert.notNull(elements, ELEMENTS);
		final var entity = nestedTransfer.getNested().getNestedEntity();
		final var result = new ArrayList<TransferElement>();
		
		for (EntityField entityField : entity.getAllFields()) {
			if (entityField.getType().isBinary() || 
				entityField.getType().isFile() ||
				entityField.isReferenceField()) {
				continue;
			}
			if (!anyMatch(elements, elem -> entityField.equals(elem.getEntityField()))) {
				final var element = new TransferElement();
				element.setEntityField(entityField);
				result.add(element);
			}
		}
		return result;
	}
	
	@Override
	public List<NestedTransfer> getAvailableNesteds(Transfer transfer, List<NestedTransfer> nesteds) {
		Assert.notNull(transfer, C.TRANSFER);
		Assert.notNull(nesteds, "nesteds");
		
		final var entity = transfer.getEntity();
		final var result = new ArrayList<NestedTransfer>();
		if (entity.hasNesteds()) {
			for (NestedEntity nested : entity.getNesteds()) {
				if (!anyMatch(nesteds, nst -> nested.equals(nst.getNested()))) {
					result.add(new NestedTransfer(nested));
				}
			}
		}
		return result;
	}
	
	@Override
	public List<TransferPermission> getAvailablePermissions(Transfer transfer, Session session) {
		Assert.notNull(transfer, C.TRANSFER);
		Assert.notNull(session, C.SESSION);
		
		return filterAndConvert(userGroupService.findNonSystemGroups(session), 
								not(transfer::containsPermission), 
								group -> createPermission(transfer, group));
	}
	
	@Override
	public List<TransferElement> getMainObjectElements(Transfer transfer) {
		Assert.notNull(transfer, C.TRANSFER);
		
		return subList(transfer.getElements(),
					   elem -> transfer.getEntity().containsField(elem.getEntityField()));
	}
	
	@Override
	public List<NestedTransfer> getNestedTransfers(Transfer transfer) {
		Assert.notNull(transfer, C.TRANSFER);
		final var resultMap = new HashMap<NestedEntity, NestedTransfer>();
		
		if (transfer.hasElements()) {
			for (TransferElement element : transfer.getElements()) {
				final var nested = transfer.getEntity().getNestedByEntityField(element.getEntityField());
				if (nested != null) {
					resultMap.computeIfAbsent(nested, t -> new NestedTransfer(nested));
					resultMap.get(nested).addElement(element);
				}
			}
		}
		return valueList(resultMap);
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void adjustElements(Transfer transfer, List<TransferElement> elements, List<NestedTransfer> nesteds) {
		Assert.notNull(transfer, C.TRANSFER);
		Assert.notNull(elements, ELEMENTS);
		Assert.notNull(nesteds, "nesteds");
		
		filterAndForEach(elements, not(transfer::containsElement), transfer::addElement);
		nesteds.forEach(nested -> filterAndForEach(nested.getElements(), not(transfer::containsElement), transfer::addElement));
		if (transfer.hasElements()) {
			transfer.getElements().removeIf(element -> !(elements.contains(element) || 
														 anyMatch(nesteds, nested -> nested.containsElement(element))));
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public byte[] doExport(Transfer transfer) {
		Assert.notNull(transfer, C.TRANSFER);
		
		return createProcessor(transfer).doExport();
	}
	
	@Override
	public byte[] doExport(Entity transferableEntity) {
		Assert.notNull(transferableEntity, C.ENTITY);
		Assert.state(transferableEntity.isTransferable(), "entity is not transferable");
		
		return doExport(createAutoTransfer(transferableEntity, false));
	}
	
	@Override
	public byte[] doExport(Entity entity, List<TransferElement> elements, 
						   QueryCursor<ValueObject> cursor) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(cursor, C.CURSOR);
		Assert.notNull(elements, ELEMENTS);
		
		return createProcessor(createTransfer(entity, elements))
				.setCursor(cursor)
				.doExport();
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public TransferResult doImport(Transfer transfer, ImportOptions options, 
								   FileObject importFile) throws ValidationException {
		Assert.notNull(transfer, C.TRANSFER);
		Assert.notNull(transfer, C.OPTIONS);
		Assert.notNull(importFile, "importFile");
		
		validator.validateImport(importFile);
		
		return createProcessor(transfer)
				.doImport(options, new ByteArrayInputStream(importFile.getContent()));
	}
	
	@Override
	public TransferResult doImport(Entity transferableEntity, byte[] content) throws ValidationException {
		Assert.notNull(transferableEntity, C.ENTITY);
		Assert.notNull(content, C.CONTENT);
		Assert.state(transferableEntity.isTransferable(), "entity is not transferable");
		
		final var transfer = createAutoTransfer(transferableEntity, true);
		return createProcessor(transfer)
				.doImport(transfer.getOptions(), new ByteArrayInputStream(content));
	}
	
	@Override
	public List<Transfer> findUsage(Entity entity, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		if (entity.isGeneric()) {
			return Collections.emptyList();
		}
		return subList(getObjects(session),
					   transfer -> entity.equals(transfer.getEntity()) ||
					   			   anyMatch(getNestedTransfers(transfer),
					   					   	nested -> entity.equals(nested.getNested().getNestedEntity())));
	}
	
	@Override
	public List<Transfer> findUsage(NestedEntity nestedEntity, Session session) {
		Assert.notNull(nestedEntity, "nested entity");
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session),
					   transfer -> anyMatch(getNestedTransfers(transfer),
		   					   				nested -> nestedEntity.equals(nested.getNested())));
	}

	@Override
	public List<Transfer> findUsage(EntityField entityField, Session session) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), transfer -> transfer.containsField(entityField));
	}
	
	@Override
	public List<Transfer> findUsage(UserGroup userGroup, Session session) {
		Assert.notNull(userGroup, C.USERGROUP);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), transfer -> transfer.containsPermission(userGroup));
	}
	
	@Override
	public List<Transfer> findUsage(EntityFieldGroup fieldGroup) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Transfer> findUsage(EntityRelation entityRelation, Session session) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Transfer> findUsage(EntityStatus entityStatus, Session session) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Transfer> findUsage(EntityFunction entityFunction, Session session) {
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
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (analysis.getModule().getTransfers() != null) {
			for (Transfer transfer : analysis.getModule().getTransfers()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(transfer);
				}
				else {
					final var currentVersionTransfer = currentVersionModule.getTransferByUid(transfer.getUid());
					if (currentVersionTransfer == null) {
						analysis.addChangeNew(transfer);
					}
					else if (!transfer.isEqual(currentVersionTransfer)) {
						analysis.addChangeModify(transfer);
					}
				}
			}
		}
	}
	
	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		filterAndForEach(currentVersionModule.getTransfers(), 
						 trans -> analysis.getModule().getTransferByUid(trans.getUid()) == null,
						 analysis::addChangeDelete);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { EntityService.class };
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) throws ValidationException {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		
		if (context.getModule().getTransfers() != null) {
			for (Transfer transfer : context.getModule().getTransfers()) {
				initTransfer(transfer, context, session);
				saveObject(transfer, session);
			}
		}
	}
	
	private void initTransfer(Transfer transfer, TransferContext context, Session session) {
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
				initTransferElement(element, entity, transfer, currentVersionTransfer);
			}
		}
		if (transfer.hasPermissions()) {
			initPermissions(session, transfer, currentVersionTransfer);
		}
	}
	
	private void initTransferElement(TransferElement element, Entity entity, 
									 Transfer transfer, Transfer currentVersionTransfer) {
		element.setTransfer(transfer);
		element.setEntityField(entity.findFieldByUid(element.getFieldUid()));
		final var currentVersionElement = currentVersionTransfer != null 
											? currentVersionTransfer.getElementByUid(element.getUid()) 
											: null;
		if (currentVersionElement != null) {
			currentVersionElement.copySystemFieldsTo(element);
		}
	}
	
	private void initPermissions(Session session, Transfer transfer, Transfer currentVersionTransfer) {
		for (TransferPermission permission : transfer.getPermissions()) {
			permission.setTransfer(transfer);
			permission.setUserGroup(userGroupService.findByUid(session, permission.getUserGroupUid()));
			if (currentVersionTransfer != null) {
				final TransferPermission currentVersionPermission =
						currentVersionTransfer.getPermissionByUid(permission.getUid());
				if (currentVersionPermission != null) {
					currentVersionPermission.copySystemFieldsTo(permission);
				}
			}
		}
	}
	
	@Override
	public void removeObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		filterAndForEach(currentVersionModule.getTransfers(), 
						 trans -> module.getTransferByUid(trans.getUid()) == null, 
						 trans -> session.saveOrUpdate(removeModule(trans)));
	}
	
	private TransferProcessor createProcessor(Transfer transfer) {
		final Class<? extends ValueObject> objectClass = MiscUtils.castClass(
				codeManager.getGeneratedClass(transfer.getEntity()));
		switch (transfer.getFormat()) {
			case CSV:
				return new CSVProcessor(this, valueObjectService, objectClass, labelProvider, transfer);
				
			case JSON:
				return new JsonProcessor(this, valueObjectService, objectClass, labelProvider, transfer);
				
			default:
				throw new UnsupportedOperationException(transfer.getFormat().name());
		}
	}
	
	private Transfer createTransfer(Entity entity, List<TransferElement> elements)  {
		final var transfer = (TransferMetadata) createInstance(null);
		transfer.setFormat(TransferFormat.CSV);
		transfer.setEntity(entity);
		transfer.setElements(elements);
		transfer.setHeader(true);
		try {
			initObject(transfer);
		} 
		catch (ValidationException vex) {
			SystemLog.logError(vex);
			throw new InternalException(vex);
		}
		return transfer;
	}
	
	private Transfer createAutoTransfer(Entity entity, boolean forImport) {
		ImportOptions options = null;
		if (forImport) {
			options = new ImportOptions();
			options.setCreateIfNew(true);
			options.setModifyExisting(true);
		}
		final var transfer = (TransferMetadata) createInstance(options);
		transfer.setFormat(TransferFormat.CSV);
		transfer.setEntity(entity);
		
		final var elements = new ArrayList<TransferElement>();
		final var elementUid = createElement(transfer, transfer.getEntity().getUidField());
		elementUid.setIdentifier(true);
		elements.add(elementUid);
		elements.addAll(getAvailableElements(transfer, transfer.getElements()));
		transfer.setElements(elements);
		try {
			initObject(transfer);
		} 
		catch (ValidationException vex) {
			SystemLog.logError(vex);
			throw new InternalException(vex);
		}
		return transfer;
	}
	
	private static TransferElement createElement(Transfer transfer, EntityField entityField) {
		final var element = new TransferElement();
		element.setTransfer(transfer);
		element.setEntityField(entityField);
		return element;
	}
	
	private static TransferPermission createPermission(Transfer transfer, UserGroup group) {
		final var permission = new TransferPermission();
		permission.setTransfer(transfer);
		permission.setUserGroup(group);
		permission.setAccess(TransferAccess.IMPORT);
		return permission;
	}
	
	private static final String ELEMENTS = "elements";

}
