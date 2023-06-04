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
package org.seed.core.entity.transform;

import static org.seed.core.util.CollectionUtils.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.codegen.CodeManager;
import org.seed.core.codegen.CodeUtils;
import org.seed.core.data.Options;
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
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupDependent;
import org.seed.core.user.UserGroupService;
import org.seed.core.util.Assert;
import org.seed.core.util.MultiKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class TransformerServiceImpl extends AbstractApplicationEntityService<Transformer>
	implements TransformerService, EntityDependent<Transformer>, UserGroupDependent<Transformer> {
	
	@Autowired
	private EntityService entityService;
	
	@Autowired
	private TransformerRepository repository;
	
	@Autowired
	private TransformerValidator validator;
	
	@Autowired
	private UserGroupService userGroupService;
	
	@Autowired
	private CodeManager codeManager;
	
	@Override
	protected TransformerRepository getRepository() {
		return repository;
	}

	@Override
	protected TransformerValidator getValidator() {
		return validator;
	}
	
	@Override
	public Transformer createInstance(@Nullable Options options) {
		final var transformer = (TransformerMetadata) super.createInstance(options);
		transformer.createLists();
		return transformer;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public TransformerFunction createFunction(Transformer transformer) {
		Assert.notNull(transformer, C.TRANSFORMER);
		
		final var function = new TransformerFunction();
		transformer.addFunction(function);
		return function;
	}
	
	@Override
	public Transformer getTransformerByName(Entity sourceEntity, Entity targetEntity, 
											String name, Session session) {
		Assert.notNull(sourceEntity, C.SOURCEENTITY);
		Assert.notNull(targetEntity, C.TARGETENTITY);
		Assert.notNull(name, C.NAME);
		
		return repository.findUnique(session,
									 queryParam(C.SOURCEENTITY, sourceEntity),
				   					 queryParam(C.TARGETENTITY, targetEntity),
				   					 queryParam(C.NAME, name));
	}
	
	@Override
	public List<Transformer> findTransformers(Entity sourceEntity, Session session) {
		Assert.notNull(sourceEntity, C.SOURCEENTITY);
		
		return repository.find(session, queryParam(C.SOURCEENTITY, sourceEntity));
	}
	
	@Override
	public List<Transformer> findTransformers(Entity sourceEntity, Entity targetEntity) {
		Assert.notNull(sourceEntity, C.SOURCEENTITY);
		Assert.notNull(targetEntity, C.TARGETENTITY);
		
		return repository.find(queryParam(C.SOURCEENTITY, sourceEntity),
							   queryParam(C.TARGETENTITY, targetEntity));
	}
	
	@Override
	public List<Transformer> findUsage(Entity entity, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		if (entity.isGeneric()) {
			return Collections.emptyList();
		}
		return subList(getObjects(session),
					   trans -> entity.equals(trans.getSourceEntity()) ||
					   			entity.equals(trans.getTargetEntity()) ||	
					   			anyMatch(getNestedTransformers(trans),
					   					 nested -> entity.equals(nested.getSourceNested().getNestedEntity()) ||
					   					 		   entity.equals(nested.getTargetNested().getNestedEntity())));
	}
	
	public List<TransformerPermission> getAvailablePermissions(Transformer transformer, Session session) {
		Assert.notNull(transformer, C.TRANSFORMER);
		Assert.notNull(session, C.SESSION);
		
		return filterAndConvert(userGroupService.findNonSystemGroups(session), 
								not(transformer::containsPermission),
								group -> createPermission(transformer, group));
	}
	
	
	
	@Override
	public List<TransformerStatus> getAvailableStatus(Transformer transformer) {
		Assert.notNull(transformer, C.TRANSFORMER);
		
		return filterAndConvert(transformer.getSourceEntity().getStatusList(), 
								not(transformer::containsStatus),
								status -> createStatus(transformer, status));
	}
	
	@Override
	public List<TransformerElement> getMainObjectElements(Transformer transformer) {
		Assert.notNull(transformer, C.TRANSFORMER);
		
		return subList(transformer.getElements(), 
					   elem -> transformer.getSourceEntity().containsField(elem.getSourceField()));
	}
	
	@Override
	public List<NestedTransformer> getNestedTransformers(Transformer transformer) {
		Assert.notNull(transformer, C.TRANSFORMER);
		final var resultMap = new HashMap<MultiKey, NestedTransformer>();
		
		if (transformer.hasElements()) {
			final var sourceEntity = transformer.getSourceEntity();
			final var targetEntity = transformer.getTargetEntity();
			for (TransformerElement element : transformer.getElements()) {
				final var sourceNested = sourceEntity.getNestedByEntityField(element.getSourceField());
				final var targetNested = targetEntity.getNestedByEntityField(element.getTargetField());
				if (sourceNested != null && targetNested != null) {
					final var key = MultiKey.valueOf(sourceNested, targetNested);
					resultMap.computeIfAbsent(key, t -> new NestedTransformer(sourceNested, targetNested));
					resultMap.get(key).addElement(element);
				}
			}
		}
		return valueList(resultMap);
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void adjustElements(Transformer transformer, List<TransformerElement> elements, List<NestedTransformer> nesteds) {
		Assert.notNull(transformer, C.TRANSFORMER);
		Assert.notNull(elements, "elements");
		Assert.notNull(nesteds, "nesteds");
		
		filterAndForEach(elements, not(transformer::containsElement), transformer::addElement);
		nesteds.forEach(nested -> filterAndForEach(nested.getElements(), not(transformer::containsElement), transformer::addElement));
		if (transformer.hasElements()) {
			transformer.getElements().removeIf(element -> !(elements.contains(element) || 
											   anyMatch(nesteds, nested -> nested.containsElement(element))));
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void saveObject(Transformer transformer) throws ValidationException {
		Assert.notNull(transformer, C.TRANSFORMER);
		
		final boolean isNew = transformer.isNew();
		final var currentVersionTransformer = !isNew ? getObject(transformer.getId()) : null;
		final boolean renamed = !isNew && !currentVersionTransformer.getInternalName().equals(transformer.getInternalName());
		
		if (currentVersionTransformer != null) { // It's necessary to load function names now
			convertedList(currentVersionTransformer.getFunctions(), TransformerFunction::getName);
		}
		if (renamed && transformer.hasFunctions()) {
			renamePackages(transformer, currentVersionTransformer);
		}
		super.saveObject(transformer);
		
		if (!isNew && currentVersionTransformer.hasFunctions()) {
			for (TransformerFunction currentFunction : currentVersionTransformer.getFunctions()) {
				final var function = transformer.getFunctionByUid(currentFunction.getUid());
				if (function == null || !function.getName().equals(currentFunction.getName())) {
					removeFunctionClass(currentFunction);
				}
			}
		}
		if (transformer.hasFunctions()) {
			updateConfiguration();
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void deleteObject(Transformer transformer) throws ValidationException {
		super.deleteObject(transformer);
		removeFunctionClasses(transformer);
	}

	@Override
	public List<Transformer> findUsage(EntityField entityField, Session session) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), trans -> trans.containsField(entityField));
	}
	
	@Override
	public List<Transformer> findUsage(UserGroup userGroup, Session session) {
		Assert.notNull(userGroup, C.USERGROUP);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), trans -> trans.containsPermission(userGroup));
	}
	
	@Override
	public List<Transformer> findUsage(EntityStatus entityStatus, Session session) {
		Assert.notNull(entityStatus, C.STATUS);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), trans -> trans.containsStatus(entityStatus));
	}
	
	@Override
	public List<Transformer> findUsage(NestedEntity nestedEntity, Session session) {
		Assert.notNull(nestedEntity, "nested entity");
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), 
					   trans -> anyMatch(getNestedTransformers(trans), 
							   			 nested -> nestedEntity.equals(nested.getSourceNested()) ||
							   			 		   nestedEntity.equals(nested.getTargetNested())));
	}
	
	@Override
	public List<Transformer> findUsage(EntityFieldGroup fieldGroup) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Transformer> findUsage(EntityFunction entityFunction, Session session) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Transformer> findUsage(EntityRelation entityRelation, Session session) {
		return Collections.emptyList();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { EntityService.class };
	}
	
	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (analysis.getModule().getTransformers() != null) {
			for (Transformer transformer : analysis.getModule().getTransformers()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(transformer);
				}
				else {
					final var currentVersionTransformer = currentVersionModule.getTransformerByUid(transformer.getUid());
					if (currentVersionTransformer == null) {
						analysis.addChangeNew(transformer);
					}
					else if (!transformer.isEqual(currentVersionTransformer)) {
						analysis.addChangeModify(transformer);
					}
				}
			}
		}
	}
	
	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		filterAndForEach(currentVersionModule.getTransformers(), 
						trans -> analysis.getModule().getTransformerByUid(trans.getUid()) == null, 
						analysis::addChangeDelete);
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) throws ValidationException {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		
		if (context.getModule().getTransformers() != null) {
			for (Transformer transformer : context.getModule().getTransformers()) {
				initTransformer(transformer, context, session);
				getRepository().save(transformer, session);
			}
		}
	}
	
	private void renamePackages(Transformer transformer, Transformer currentVersionTransformer) {
		currentVersionTransformer.getFunctions().forEach(this::removeFunctionClass);
		filterAndForEach(transformer.getFunctions(), 
						 function -> function.getContent() != null, 
						 function -> function.setContent(CodeUtils.renamePackage(function.getContent(), function.getGeneratedPackage())));	
	}
	
	private void removeFunctionClasses(Transformer transformer) {
		if (transformer.hasFunctions()) {
			transformer.getFunctions().forEach(this::removeFunctionClass);
		}
	}
	
	private void removeFunctionClass(TransformerFunction function) {
		codeManager.removeClass(CodeUtils.getQualifiedName(function));
	}
	
	private void initTransformer(Transformer transformer, TransferContext context, Session session) {
		final var currentVersionTransformer = findByUid(session, transformer.getUid());
		final var sourceEntity = entityService.findByUid(session, transformer.getSourceEntityUid());
		final var targetEntity = entityService.findByUid(session, transformer.getTargetEntityUid());
		((TransformerMetadata) transformer).setModule(context.getModule());
		((TransformerMetadata) transformer).setSourceEntity(sourceEntity);
		((TransformerMetadata) transformer).setTargetEntity(targetEntity);
		if (currentVersionTransformer != null) {
			((TransformerMetadata) currentVersionTransformer).copySystemFieldsTo(transformer);
			session.detach(currentVersionTransformer);
		}
		if (transformer.hasElements()) {
			transformer.getElements().forEach(element -> 
				initTransformerElement(element, transformer, sourceEntity, targetEntity, currentVersionTransformer));
		}
		if (transformer.hasFunctions()) {
			transformer.getFunctions().forEach(function -> 
				initTransformerFunction(function, transformer, currentVersionTransformer));
		}
		if (transformer.hasStatus()) {
			transformer.getStatus().forEach(status -> 
				initTransformerStatus(status, transformer, currentVersionTransformer));
		}
		if (transformer.hasPermissions()) {
			transformer.getPermissions().forEach(permission -> 
				initTransformerPermission(permission, transformer, currentVersionTransformer, session));
		}
	}
	
	private void initTransformerElement(TransformerElement element, Transformer transformer,
										Entity sourceEntity, Entity targetEntity, 
										Transformer currentVersionTransformer) {
		element.setTransformer(transformer);
		element.setSourceField(sourceEntity.findFieldByUid(element.getSourceFieldUid()));
		element.setTargetField(targetEntity.findFieldByUid(element.getTargetFieldUid()));
		final var currentVersionElement = currentVersionTransformer != null 
											? currentVersionTransformer.getElementByUid(element.getUid()) 
											: null;
		if (currentVersionElement != null) {
			currentVersionElement.copySystemFieldsTo(element);
		}
	}
	
	private void initTransformerFunction(TransformerFunction function, Transformer transformer,
										 Transformer currentVersionTransformer) {
		function.setTransformer(transformer);
		final var currentVersionFunction = currentVersionTransformer != null
											? currentVersionTransformer.getFunctionByUid(function.getUid())
											: null;
		if (currentVersionFunction != null) {
			currentVersionFunction.copySystemFieldsTo(function);
		}
	}
	
	private void initTransformerPermission(TransformerPermission permission, Transformer transformer,
										   Transformer currentVersionTransformer, Session session) {
		permission.setTransformer(transformer);
		permission.setUserGroup(userGroupService.findByUid(session, permission.getUserGroupUid()));
		final var currentVersionPermission = currentVersionTransformer != null
												? currentVersionTransformer.getPermissionByUid(permission.getUid())
												: null;
		if (currentVersionPermission != null) {
			currentVersionPermission.copySystemFieldsTo(permission);
		}
	}
	
	private void initTransformerStatus(TransformerStatus status, Transformer transformer,
									   Transformer currentVersionTransformer) {
		status.setTransformer(transformer);
		status.setStatus(transformer.getSourceEntity().getStatusByUid(status.getStatusUid()));
		final var currentVersionStatus = currentVersionTransformer != null
											? currentVersionTransformer.getStatusByUid(status.getUid())
											: null;
		if (currentVersionStatus != null) {
			currentVersionStatus.copySystemFieldsTo(status);
		}
	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		if (currentVersionModule.getTransformers() != null) {
			for (Transformer transformer : currentVersionModule.getTransformers()) {
				if (module.getTransformerByUid(transformer.getUid()) == null) {
					session.delete(transformer);
					removeFunctionClasses(transformer);
				}
			}
		}
	}
	
	@Override
	public boolean autoMatchFields(Transformer transformer, List<TransformerElement> elements) {
		Assert.notNull(transformer, C.TRANSFORMER);
		Assert.notNull(elements, "elements");
		
		boolean matched = false;
		if (transformer.getSourceEntity().hasAllFields() &&
			transformer.getTargetEntity().hasAllFields()) {
			for (EntityField sourceField : transformer.getSourceEntity().getAllFields()) {
				for (EntityField targetField : transformer.getTargetEntity().getAllFields()) {
					if (sourceField.getType() == targetField.getType() &&
						sourceField.getName().equalsIgnoreCase(targetField.getName()) &&
						!containsElement(elements, sourceField, targetField)) {
						
						final var element = createElement(sourceField, targetField);
						element.setTransformer(transformer);
						elements.add(element);
						matched = true;
						break;
					}
				}
			}
		}
		return matched;
	}
	
	@Override
	public boolean autoMatchFields(NestedTransformer transformer) {
		Assert.notNull(transformer, C.TRANSFORMER);
		
		boolean matched = false;
		if (transformer.getSourceNested().getNestedEntity().hasAllFields() &&
			transformer.getTargetNested().getNestedEntity().hasAllFields()) {
			for (EntityField sourceField : transformer.getSourceNested().getFields(true)) {
				for (EntityField targetField : transformer.getTargetNested().getFields(true)) {
					if (sourceField.getType() == targetField.getType() &&
						sourceField.getName().equalsIgnoreCase(targetField.getName()) &&
						!transformer.containsElement(sourceField, targetField)) {
						
						transformer.addElement(createElement(sourceField, targetField));
						matched = true;
						break;
					}
				}
			}
		}
		return matched;
	}
	
	private static boolean containsElement(List<TransformerElement> elements, EntityField sourceField, EntityField targetField) {
		return anyMatch(elements, elem -> elem.getSourceField() != null && elem.getSourceField().equals(sourceField) &&
										  elem.getTargetField() != null && elem.getTargetField().equals(targetField));
	}
	
	private static TransformerElement createElement(EntityField sourceField, EntityField targetField) {
		final var element = new TransformerElement();
		element.setSourceField(sourceField);
		element.setTargetField(targetField);
		return element;
	}
	
	private static TransformerPermission createPermission(Transformer transformer, UserGroup group) {
		final var permission = new TransformerPermission();
		permission.setTransformer(transformer);
		permission.setUserGroup(group);
		return permission; 
	}
	
	private static TransformerStatus createStatus(Transformer transformer, EntityStatus status) {
		final var transformerStatus = new TransformerStatus();
		transformerStatus.setTransformer(transformer);
		transformerStatus.setStatus(status);
		return transformerStatus;
	}

}
