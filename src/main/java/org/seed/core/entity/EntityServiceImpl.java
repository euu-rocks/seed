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
package org.seed.core.entity;

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.codegen.CodeChangeAware;
import org.seed.core.codegen.CodeManager;
import org.seed.core.codegen.CodeManagerImpl;
import org.seed.core.codegen.CodeUtils;
import org.seed.core.codegen.SourceCode;
import org.seed.core.config.Limits;
import org.seed.core.config.SchemaManager;
import org.seed.core.config.SchemaVersion;
import org.seed.core.config.changelog.ChangeLog;
import org.seed.core.config.changelog.ReferenceChangeLog;
import org.seed.core.data.AbstractSystemObject;
import org.seed.core.data.FieldType;
import org.seed.core.data.Options;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.autonum.AutonumberService;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupDependent;
import org.seed.core.user.UserGroupService;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.NameUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class EntityServiceImpl extends AbstractApplicationEntityService<Entity> 
	implements EntityService, EntityDependent<Entity>, UserGroupDependent<Entity>, 
		CodeChangeAware { 
	
	@Autowired
	private EntityValidator entityValidator;

	@Autowired
	private EntityRepository entityRepository;
	
	@Autowired
	private AutonumberService autonumService;
	
	@Autowired
	private UserGroupService userGroupService;
	
	@Autowired
	private SchemaManager schemaManager; 
	
	@Autowired
	private CodeManager codeManager;
	
	@Autowired
	private Limits limits;
	
	private List<EntityChangeAware> changeAwareObjects;
	
	@Override
	protected EntityRepository getRepository() {
		return entityRepository;
	}

	public void setEntityRepository(EntityRepository entityRepository) {
		this.entityRepository = entityRepository;
	}

	@Override
	protected EntityValidator getValidator() {
		return entityValidator;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public Entity createInstance(@Nullable Options options) {
		final var instance = (EntityMetadata) super.createInstance(options);
		instance.createLists();
		return instance;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public EntityField createField(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final var field  = new EntityField();
		entity.addField(field);
		return field;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public EntityFieldGroup createFieldGroup(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final var group = new EntityFieldGroup();
		entity.addFieldGroup(group);
		return group;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public NestedEntity createNested(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final var nested = new NestedEntity();
		entity.addNested(nested);
		return nested;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public EntityFunction createFunction(Entity entity, boolean isCallback) {
		Assert.notNull(entity, C.ENTITY);
		
		final var function = new EntityFunction();
		if (isCallback) {
			function.setCallback(true);
			function.setActive(true);
		}
		entity.addFunction(function);
		return function;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public EntityRelation createRelation(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final var relation = new EntityRelation();
		entity.addRelation(relation);
		return relation;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public EntityStatus createStatus(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final var status = new EntityStatus();
		if (!entity.hasStatus()) {
			status.setInitial(true);
		}
		entity.addStatus(status);
		return status;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public EntityFieldConstraint createFieldConstraint(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final var constraint = new EntityFieldConstraint();
		entity.addFieldConstraint(constraint);
		return constraint;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public EntityStatusTransition createStatusTransition(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final var transition = new EntityStatusTransition();
		transition.createLists();
		entity.addStatusTransition(transition);
		return transition;
	}
	
	@Override
	public boolean existGenericEntities(Session session) {
		return existGeneric(true, session);
	}
	
	@Override
	public boolean existNonGenericEntities(Session session) {
		return existGeneric(false, session);
	}
	
	@Override
	public List<Entity> findGenericEntities(Session session) {
		return findGeneric(true, session);
	}
	
	public List<Entity> findNonGenericEntities() {
		try (Session session = entityRepository.getSession()) {
			return findGeneric(false, session);
		}
	}
	
	@Override
	public List<Entity> findNonGenericEntities(Session session) {
		return findGeneric(false, session);
	}
	
	@Override
	public List<Entity> findParentEntities(Entity entity, Session session) {
		return entityRepository.findParentEntities(entity, session);
	}
	
	@Override
	public List<Entity> findDescendants(Entity genericEntity, Session session) {
		Assert.notNull(genericEntity, "genericEntity");
		Assert.notNull(session, C.SESSION);
		Assert.state(genericEntity.isGeneric(), "entity is not generic");
		
		return entityRepository.find(session, queryParam("genericEntity", genericEntity));
	}
	
	@Override
	public List<Entity> findUsage(Entity entity, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		var result = subList(getObjects(session),
							 obj -> !entity.equals(obj) && 
							 		(entity.equals(obj.getGenericEntity()) ||
							 		 notEmpty(obj.getReferenceFields(entity)) ||
							 		obj.isNestedEntity(entity) || 
							 		obj.isRelatedEntity(entity)));
		if (result.isEmpty()) {
			result = convertedList(schemaManager.findReferences(session, entity.getEffectiveTableName()), 
								   EntityServiceImpl::createDummyEntity);
		}
		return result;
	}
	
	@Override
	public List<Entity> findUsage(EntityFieldGroup fieldGroup) {
		Assert.notNull(fieldGroup, C.FIELDGROUP);
		final Entity entity = fieldGroup.getEntity();
		
		return (anyMatch(entity.getFields(), field -> fieldGroup.equals(field.getFieldGroup())) ||
				anyMatch(entity.getFieldConstraints(), constr -> fieldGroup.equals(constr.getFieldGroup())))
				? Collections.singletonList(entity)
				: Collections.emptyList();
	}
	
	@Override
	public List<Entity> findUsage(EntityFunction entityFunction, Session session) {
		Assert.notNull(entityFunction, C.FUNCTION);
		Assert.notNull(session, C.SESSION);
		final Entity entity = entityFunction.getEntity();
		
		return anyMatch(entity.getStatusTransitions(), trans -> 
				   anyMatch(trans.getFunctions(), func -> entityFunction.equals(func.getFunction()))) 
					? Collections.singletonList(entity)
					: Collections.emptyList();
	}
	
	@Override
	public List<Entity> findUsage(EntityStatus entityStatus, Session session) {
		Assert.notNull(entityStatus, C.STATUS);
		Assert.notNull(session, C.SESSION);
		final Entity entity = entityStatus.getEntity();
		
		return anyMatch(entity.getFieldConstraints(), constr -> entityStatus.equals(constr.getStatus()))
				? Collections.singletonList(entity)
				: Collections.emptyList();
	}
	
	@Override
	public List<Entity> findUsage(NestedEntity nestedEntity, Session session) {
		Assert.notNull(nestedEntity, C.NESTEDENTITY);
		Assert.notNull(session, C.SESSION);
		final Entity entity = nestedEntity.getParentEntity();
		
		return anyMatch(entity.getFieldConstraints(), constr -> constr.isFieldEntity(nestedEntity.getNestedEntity()))
				? Collections.singletonList(entity)
				: Collections.emptyList();
	}
	
	@Override
	public List<Entity> findUsage(UserGroup userGroup, Session session) {
		Assert.notNull(userGroup, C.USERGROUP);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), 
					   entity -> entity.containsPermission(userGroup) ||
					   			 anyMatch(entity.getFieldConstraints(), 
					   					  constr -> userGroup.equals(constr.getUserGroup())) ||
					   			 anyMatch(entity.getStatusTransitions(), 
					   					  tran -> tran.containsPermission(userGroup)));
	}
	
	@Override
	public List<Entity> findUsage(EntityField entityField, Session session) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		Assert.notNull(session, C.SESSION);
		
		return entityField.getType().isReference()
				? subList(getObjects(session), entity -> anyMatch(entity.getNesteds(), 
																  nested -> nested.getReferenceField().equals(entityField)))
				: Collections.emptyList();
	}
	
	@Override
	public List<Entity> findUsage(EntityRelation entityRelation, Session session) {
		return Collections.emptyList();
	}
	
	@Override
	public FieldType[] getAvailableFieldTypes(Entity entity, @Nullable EntityField field) {
		Assert.notNull(entity, C.ENTITY);
		
		if (field == null) {
			return MiscUtils.toArray();
		}
		else if (field.isNew()) {
			if (entity.isGeneric()) {
				return FieldType.nonAutonumTypes();
			}
			else if (entity.isTransferable()) {
				return FieldType.transferableTypes();
			}
			else {
				return FieldType.values();
			}
		}
		else {
			switch (field.getType()) {
				case BOOLEAN:
					return MiscUtils.toArray(field.getType(), FieldType.TEXT, FieldType.TEXTLONG);
					
				case DATE:
					return MiscUtils.toArray(field.getType(), FieldType.DATETIME);
					
				case DATETIME:
					return MiscUtils.toArray(field.getType(), FieldType.DATE);
					
				case DECIMAL:
					return MiscUtils.toArray(field.getType(), FieldType.LONG, FieldType.DOUBLE);
					
				case DOUBLE:
					return MiscUtils.toArray(field.getType(), FieldType.LONG, FieldType.DECIMAL);
				
				case INTEGER:
					return MiscUtils.toArray(field.getType(), FieldType.LONG, FieldType.DOUBLE, FieldType.DECIMAL);	
				
				case LONG:	
					return MiscUtils.toArray(field.getType(), FieldType.DOUBLE, FieldType.DECIMAL);	
					
				case TEXT:
					return MiscUtils.toArray(field.getType(), FieldType.TEXTLONG);
					
				case AUTONUM:
				case BINARY:
				case FILE:
				case TEXTLONG:
				case REFERENCE:
					return MiscUtils.toArray(field.getType());
					
				default:
					throw new UnsupportedOperationException(field.getType().name());
			}
		}
	}
	
	@Override
	public List<Entity> getAvailableNestedEntities(Entity entity, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		return subList(entityRepository.find(session), 
				nested -> !nested.equals(entity) &&
						  !nested.isGeneric() &&
						  notEmpty(nested.getReferenceFields(entity)));
	}
	
	@Override
	public List<EntityPermission> getAvailablePermissions(Entity entity, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		return filterAndConvert(userGroupService.findNonSystemGroups(session), 
								not(entity::containsPermission), 
								group -> createPermission(entity, group));
	}
	
	@Override
	public List<EntityStatusTransitionPermission> getAvailableStatusTransitionPermissions(EntityStatusTransition transition, Session session) {
		Assert.notNull(transition, C.TRANSITION);
		Assert.notNull(session, C.SESSION);
		
		return filterAndConvert(userGroupService.findNonSystemGroups(session), 
								not(transition::containsPermission), 
								group -> createTransitionPermission(transition, group));
	}
	
	@Override
	public List<EntityStatus> getAvailableStatusList(Entity entity, EntityStatus currentStatus, User user) {
		Assert.notNull(entity, C.ENTITY);
		Assert.state(entity.hasStatus(), "entity has no status");
		Assert.notNull(currentStatus, "current status");
		Assert.notNull(user, C.USER);
		
		final var result = filterAndConvert(entity.getStatusTransitions(), 
								 trans -> trans.getSourceStatus().equals(currentStatus) && trans.isAuthorized(user), 
								 EntityStatusTransition::getTargetStatus);
		result.add(0, currentStatus);
		return result;
	}
	
	@Override
	public List<EntityStatusTransitionFunction> getAvailableStatusTransitionFunctions(Entity entity, EntityStatusTransition transition) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(transition, C.TRANSITION);
		
		return filterAndConvert(entity.getCallbackFunctions(), 
								function -> function.isActiveOnStatusTransition() && !transition.containsEntityFunction(function), 
								function -> createTransitionFunction(transition, function));
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void removeField(Entity entity, EntityField field) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(field, C.FIELD);
		
		if (!field.isNew()) {
			entityValidator.validateRemoveField(field);
		}
		entity.removeField(field);
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void removeFieldGroup(Entity entity, EntityFieldGroup fieldGroup) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(fieldGroup, C.FIELDGROUP);
		
		if (!fieldGroup.isNew()) {
			entityValidator.validateRemoveFieldGroup(fieldGroup);
		}
		filterAndForEach(entity.getFields(), 
						 field -> fieldGroup.equals(field.getFieldGroup()), 
						 field -> field.setFieldGroup(null));
		entity.removeFieldGroup(fieldGroup);
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void removeFunction(Entity entity, EntityFunction function) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(function, C.FUNCTION);
		
		if (!function.isNew() && function.isCallback()) {
			entityValidator.validateRemoveFunction(function);
		}
		entity.removeFunction(function);
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void removeStatus(Entity entity, EntityStatus status) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(status, C.STATUS);
		
		if (!status.isNew()) {
			entityValidator.validateRemoveStatus(status);
		}
		entity.removeStatus(status);
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void removeStatusTransition(Entity entity, EntityStatusTransition statusTransition) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(statusTransition, C.STATUSTRANSITION);
		
		entity.removeStatusTransition(statusTransition);
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void removeNested(Entity entity, NestedEntity nested) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(nested, C.NESTED);
		
		if (!nested.isNew()) {
			entityValidator.validateRemoveNested(nested);
		}
		entity.removeNested(nested);
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void removeRelation(Entity entity, EntityRelation relation) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(relation, C.RELATION);
		
		if (!relation.isNew()) {
			entityValidator.validateRemoveRelation(relation);
		}
		entity.removeRelation(relation);
	}
	
	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (analysis.getModule().getEntities() != null) {
			for (Entity entity : analysis.getModule().getEntities()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(entity);
				}
				else {
					final Entity currentVerionEntity = 
						currentVersionModule.getEntityByUid(entity.getUid());
					if (currentVerionEntity == null) {
						analysis.addChangeNew(entity);
					}
					else if (!entity.isEqual(currentVerionEntity)) {
						analysis.addChangeModify(entity);
					}
				}
			}
		}
	}
	
	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		filterAndForEach(currentVersionModule.getEntities(), 
						 entity -> analysis.getModule().getEntityByUid(entity.getUid()) == null, 
						 analysis::addChangeDelete);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { UserGroupService.class };
	}
	
	@Override
	public void initNestedEntity(Entity entity) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		entityValidator.validateCreateNested(entity);
		
		// add reference field to parent
		final Entity parent = ((EntityMetadata) entity).getParentEntity();
		final EntityField referenceField = new EntityField();
		referenceField.setName(parent.getName());
		referenceField.setType(FieldType.REFERENCE);
		referenceField.setReferenceEntity(parent);
		referenceField.setMandatory(true);
		entity.addField(referenceField);
		
		// set audited if parent is audited
		if (parent.isAudited()) {
			((EntityMetadata) entity).setAudited(true);
		}
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) throws ValidationException {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		final var entities = context.getModule().getEntities();
		
		if (entities != null) {
			importEntities(context, session);
			// init references to other entities
			for (Entity entity : entities) {
				final Entity genericEntity = entity.getGenericEntityUid() != null
						? findByUid(session, entity.getGenericEntityUid())
						: null;
				((EntityMetadata) entity).setGenericEntity(genericEntity);
				initEntityReferences(entity, session);
				initRelationEntities(session, entity);
				initConstraintFields(entity);
			}
			// validate and save
			for (Entity entity : entities) {
				saveObject(entity, session);
			}
		}
	}
	
	private void importEntities(TransferContext context, Session session) {
		for (Entity entity : context.getModule().getEntities()) {
			final Entity currentVersionEntity = findByUid(session, entity.getUid());
			((EntityMetadata) entity).setModule(context.getModule());
			
			// entity exists
			if (currentVersionEntity != null) {
				context.addExistingEntity(entity, currentVersionEntity);
				((EntityMetadata) currentVersionEntity).copySystemFieldsTo(entity);
				session.detach(currentVersionEntity);
			}
			// new entity
			else { 
				context.addNewEntity(entity);
			}
			initEntity(entity, currentVersionEntity, session);
			entityRepository.save(entity, session);
		}
	}
	
	@Override
	public void createChangeLogs(TransferContext context, Session session) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		
		final var referenceChangeLog = new ReferenceChangeLog();
		final var changeLogs = new ArrayList<ChangeLog>();
		for (Entity entity : context.getNewEntities()) {
			if (!entity.isGeneric()) {
				final ChangeLog changeLog = createChangeLog(null, entity, session, referenceChangeLog);
				if (changeLog != null) {
					changeLogs.add(changeLog);
				}
			}
		}
		for (Entity entity : context.getExistingEntities()) {
			if (!entity.isGeneric()) {
				final Entity currentVersionEntity = context.getCurrentVersionEntity(entity.getUid());
				final ChangeLog changeLog = createChangeLog(currentVersionEntity, entity, session, referenceChangeLog);
				if (changeLog != null) {
					changeLogs.add(changeLog);
				}
			}
		}
		changeLogs.sort(changeLogComparator);
		if (!referenceChangeLog.isEmpty()) {
			changeLogs.add(referenceChangeLog.build());
		}
		changeLogs.forEach(session::saveOrUpdate);
	}
	
	@Override
	public void removeObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		filterAndForEach(currentVersionModule.getEntities(), 
						 entity -> module.getEntityByUid(entity.getUid()) == null, 
						 entity -> session.saveOrUpdate(removeModule(entity)));
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void saveObject(Entity entity) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		
		cleanup(entity);
		final boolean isInsert = entity.isNew();
		final Entity currentVersionEntity = !isInsert ? getObject(entity.getId()) : null;
		final boolean renamed = !isInsert && !currentVersionEntity.getInternalName().equals(entity.getInternalName());
		
		try (Session session = entityRepository.getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				if (!isInsert) {
					final EntityField deletedAutonum = getDeletedAutonumField(currentVersionEntity, entity);
					if (deletedAutonum != null) {
						autonumService.deleteAutonumber(deletedAutonum, session);
					}
				}
				if (currentVersionEntity != null) { // It's necessary to load function names now
					convertedList(currentVersionEntity.getCallbackFunctions(), EntityFunction::getName);
				}
				if (renamed && notEmpty(entity.getCallbackFunctions())) {
					renamePackages(entity, currentVersionEntity);
				}
				
				saveObject(entity, session);
				
				final ChangeLog changeLog = createChangeLog(currentVersionEntity, entity, session);
				if (changeLog != null) {
					session.saveOrUpdate(changeLog);
				}
				tx.commit();
				removeEntityClasses(entity, currentVersionEntity, isInsert, renamed);
			}
			catch (Exception ex) {
				if (isInsert) {
					// reset id because its assigned even if insert fails
					((AbstractSystemObject) entity).resetId();
				}
				handleException(tx, ex);
			}
		}
		
		updateConfiguration();
	}
	
	@Override
	public void saveObject(Entity entity, Session session) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		final boolean isInsert = entity.isNew();
		final Entity parentEntity = ((EntityMetadata) entity).getParentEntity();
		
		beforeSaveObject(entity, session, isInsert);
		
		super.saveObject(entity, session);
		
		// add nested to parent
		if (parentEntity != null) {
			final var nested = new NestedEntity();
			nested.setName(entity.getName());
			nested.setNestedEntity(entity);
			nested.setReferenceField(entity.getReferenceFields(parentEntity).get(0));
			parentEntity.addNested(nested);
			super.saveObject(parentEntity, session);
		}
		
		afterSaveObject(entity, parentEntity, session, isInsert);
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void deleteObject(Entity entity) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		
		entityValidator.validateDelete(entity);
		try (Session session = entityRepository.getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				final EntityField autonumField = entity.findAutonumField();
				if (autonumField != null) {
					autonumService.deleteAutonumber(autonumField, session);
				}
				deleteObject(entity, session);
				
				final ChangeLog changeLog = createChangeLog(entity, null, session);
				if (changeLog != null) {
					session.saveOrUpdate(changeLog);
				}
				tx.commit();
			}
			catch (Exception ex) {
				handleException(tx, ex);
			}
		}
		removeEntityClass(entity);
		if (entity.hasFunctions()) {
			entity.getCallbackFunctions().forEach(this::removeEntityFunctionClass);
		}
		updateConfiguration();
	}
	
	@Override
	public void deleteObject(Entity entity, Session session) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		getChangeAwareObjects().forEach(aware -> aware.notifyDelete(entity, session));
		
		super.deleteObject(entity, session);
	}
	
	@Override
	public void handleSchemaUpdate(TransferContext context, SchemaVersion schemaVersion) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(schemaVersion, "schema version");
		
		if (notEmpty(context.getModule().getEntities()) && 
			schemaVersion == SchemaVersion.V_0_9_33) {
				// mask table and column names that equals SQL keywords
				new SchemaUpdateHandler0933().process(context.getModule());
		}
	}
	
	@Override
	public boolean processCodeChange(SourceCode sourceCode, Session session) {
		Assert.notNull(sourceCode, "sourceCode");
		Assert.notNull(session, C.SESSION);
		
		return sourceCode.getPackageName().startsWith(CodeManagerImpl.GENERATED_ENTITY_PACKAGE) &&
			   !sourceCode.getPackageName().equals(CodeManagerImpl.GENERATED_ENTITY_PACKAGE) &&
			   processCallbackFunctionChange(sourceCode, session);
	}
	
	private void renamePackages(Entity entity, Entity currentVersionEntity) {
		currentVersionEntity.getCallbackFunctions().forEach(this::removeEntityFunctionClass);
		filterAndForEach(entity.getCallbackFunctions(), 
						 function -> function.getContent() != null, 
						 function -> function.setContent(CodeUtils.renamePackage(function.getContent(), function.getGeneratedPackage())));
	}
	
	private void beforeSaveObject(Entity entity, Session session, boolean isInsert) {
		if (!isInsert && !entity.isGeneric()) {
			getChangeAwareObjects().forEach(aware -> aware.notifyBeforeChange(entity, session));
		}
	}
	
	private void afterSaveObject(Entity entity, Entity parentEntity, Session session, boolean isInsert) {
		final var descendants = entity.isGeneric() ? findDescendants(entity, session) : null;
		for (EntityChangeAware changeAware : getChangeAwareObjects()) {
			if (isInsert) {
				changeAware.notifyCreate(entity, session);
				// notify nested parent
				if (parentEntity != null) {
					changeAware.notifyChange(parentEntity, session);
				}
			}
			else if (descendants != null) {
				descendants.forEach(desc -> changeAware.notifyChange(desc, session));
			}
			else {
				changeAware.notifyChange(entity, session);
			}
		}
	}
	
	private boolean processCallbackFunctionChange(SourceCode sourceCode, Session session) {
		final String entityName = sourceCode.getPackageName().substring(CodeManagerImpl.GENERATED_ENTITY_PACKAGE.length() + 1);
		for (Entity entity : getObjects(session)) {
			if (entity.getName().equalsIgnoreCase(entityName)) {
				final EntityFunction function = firstMatch(entity.getFunctions(), 
						func -> func.isCallback() && 
								func.getName().equalsIgnoreCase(sourceCode.getClassName()) &&
								!func.getContent().equals(sourceCode.getContent()));
				if (function != null) {
					function.setContent(sourceCode.getContent());
					session.saveOrUpdate(function);
					return true;
				}
				break;
			}
		}
		return false;
	}
	
	private void removeEntityClass(Entity entity) {
		codeManager.removeClass(CodeUtils.getQualifiedName(entity));
	}
	
	private void removeEntityFunctionClass(EntityFunction function) {
		codeManager.removeClass(CodeUtils.getQualifiedName(function));
	}
	
	private void removeEntityClasses(Entity entity, Entity currentVersionEntity, boolean isInsert, boolean renamed) {
		if (renamed) {
			removeEntityClass(currentVersionEntity);
		}
		if (!isInsert && notEmpty(currentVersionEntity.getCallbackFunctions())) {
			for (EntityFunction currentFunction : currentVersionEntity.getCallbackFunctions()) {
				final EntityFunction function = entity.getFunctionByUid(currentFunction.getUid());
				if (function == null || !function.getName().equals(currentFunction.getName())) {
					removeEntityFunctionClass(currentFunction);
				}
			}
		}
	}
	
	private List<Entity> findGeneric(boolean generic, Session session) {
		return entityRepository.find(session, queryParam("isGeneric", generic));
	}
	
	private boolean existGeneric(boolean generic, Session session) {
		return entityRepository.exist(session, queryParam("isGeneric", generic));
	}
	
	private EntityField getDeletedAutonumField(Entity currentVersion, Entity entity) {
		final var autonumField = currentVersion.findAutonumField();
		if (autonumField != null && !autonumField.equals(entity.findAutonumField())) {
			return autonumField;
		}
		return null;
	}
	
	private void initEntity(Entity entity, Entity currentVersionEntity, Session session) {
		if (entity.hasFieldGroups()) {
			initFieldGroup(entity, currentVersionEntity);
		}
		if (entity.hasAllFields()) {
			initFields(entity, currentVersionEntity);
		}
		if (entity.hasFunctions()) {
			initFunctions(entity, currentVersionEntity);
		}
		if (entity.hasPermissions()) {
			initPermissions(session, entity, currentVersionEntity);
		}
		if (entity.hasStatus()) {
			initStatus(entity, currentVersionEntity);
			if (entity.hasStatusTransitions()) {
				initStatusTransitions(session, entity, currentVersionEntity);
			}
		}
		if (entity.hasFieldConstraints()) {
			initFieldConstraints(session, entity, currentVersionEntity);
		}
		if (entity.hasNesteds()) {
			initNesteds(entity, currentVersionEntity);
		}
		if (entity.hasAllRelations()) {
			initRelations(entity, currentVersionEntity);
		}
	}
	
	private void initEntityReferences(Entity entity, Session session) {
		if (entity.hasAllFields()) {
			initReferenceFields(session, entity);
		}
		if (entity.hasNesteds()) {
			initNestedEntities(session, entity);
		}
		if (entity.hasAllRelations()) {
			initRelatedEntities(session, entity);
		}
	}
	
	private void initConstraintFields(Entity entity) {
		filterAndForEach(entity.getFieldConstraints(), 
						 constraint -> constraint.getField() == null && constraint.getFieldGroup() == null, 
						 constraint -> constraint.setField(entity.findFieldByUid(constraint.getFieldUid())));
	}
	
	private void initRelationEntities(Session session, Entity entity) {
		if (entity.hasRelations()) {
			entity.getRelations().forEach(rel -> rel.setRelatedEntity(findByUid(session, rel.getRelatedEntityUid())));
		}
	}
	
	private void initFields(Entity entity, Entity currentVersionEntity) {
		for (EntityField field : entity.getAllFields()) {
			field.setEntity(entity);
			if (field.getFieldGroupUid() != null) {
				field.setFieldGroup(entity.getFieldGroupByUid(field.getFieldGroupUid()));
			}
			if (currentVersionEntity != null) {
				final EntityField currentVersionField = 
					currentVersionEntity.getFieldByUid(field.getUid());
				if (currentVersionField != null) {
					currentVersionField.copySystemFieldsTo(field);
				}
			}
		}
	}
	
	private void initFieldGroup(Entity entity, Entity currentVersionEntity) {
		for (EntityFieldGroup group : entity.getAllFieldGroups()) {
			group.setEntity(entity);
			if (currentVersionEntity != null) {
				final EntityFieldGroup currentVersionGroup =
					currentVersionEntity.getFieldGroupByUid(group.getUid());
				if (currentVersionGroup != null) {
					currentVersionGroup.copySystemFieldsTo(group);
				}
			}
		}
	}
	
	private void initReferenceFields(Session session, Entity entity) {
		for (EntityField field : entity.getAllFields()) {
			if (field.getType().isReference()) {
				final Entity reference = findByUid(session, field.getReferenceEntityUid());
				field.setReferenceEntity(reference);
			}
		}
	}
	
	private void initFunctions(Entity entity, Entity currentVersionEntity) {
		for (EntityFunction function : entity.getFunctions()) {
			function.setEntity(entity);
			if (currentVersionEntity != null) {
				final EntityFunction currentVersionFunction = 
					currentVersionEntity.getFunctionByUid(function.getUid());
				if (currentVersionFunction != null) {
					currentVersionFunction.copySystemFieldsTo(function);
				}
			}
		}
	}
	
	private void initPermissions(Session session, Entity entity, Entity currentVersionEntity) {
		for (EntityPermission permission : entity.getPermissions()) {
			permission.setEntity(entity);
			permission.setUserGroup(userGroupService.findByUid(session, permission.getUserGroupUid()));
			if (currentVersionEntity != null) {
				final EntityPermission currentVersionPermission =
					currentVersionEntity.getPermissionByUid(permission.getUid());
				if (currentVersionPermission != null) {
					currentVersionPermission.copySystemFieldsTo(permission);
				}
			}
		}
	}
	
	private void initNesteds(Entity entity, Entity currentVersionEntity) {
		for (NestedEntity nested : entity.getNesteds()) {
			nested.setParentEntity(entity);
			if (currentVersionEntity != null) {
				final NestedEntity currentVersionNested = 
						currentVersionEntity.getNestedByUid(nested.getUid());
				if (currentVersionNested != null) {
					currentVersionNested.copySystemFieldsTo(nested);
				}
			}
		}
	}
	
	private void initRelations(Entity entity, Entity currentVersionEntity) {
		for (EntityRelation relation : entity.getRelations()) {
			relation.setEntity(entity);
			if (currentVersionEntity != null) {
				final EntityRelation currentVersionRelation =
					currentVersionEntity.getRelationByUid(relation.getUid());
				if (currentVersionRelation != null) {
					currentVersionRelation.copySystemFieldsTo(relation);
				}
			}
		}
	}
	
	private void initNestedEntities(Session session, Entity entity) {
		for (NestedEntity nested : entity.getNesteds()) {
			final Entity nestedEntity = findByUid(session, nested.getNestedEntityUid());
			Assert.stateAvailable(nestedEntity, "nested entity");
			nested.setNestedEntity(nestedEntity);
			nested.setReferenceField(nestedEntity.getFieldByUid(nested.getReferenceFieldUid()));
		}
	}
	
	private void initRelatedEntities(Session session, Entity entity) {
		for (EntityRelation relation : entity.getAllRelations()) {
			final Entity relatedEntity = findByUid(session, relation.getRelatedEntityUid());
			Assert.stateAvailable(relatedEntity, "related entity");
			relation.setRelatedEntity(relatedEntity);
		}
	}
	
	private void initFieldConstraints(Session session, Entity entity, Entity currentVersionEntity) {
		for (EntityFieldConstraint constraint : entity.getFieldConstraints()) {
			constraint.setEntity(entity);
			if (constraint.getFieldUid() != null) {
				constraint.setField(entity.getFieldByUid(constraint.getFieldUid()));
			}
			else if (constraint.getFieldGroupUid() != null) {
				constraint.setFieldGroup(entity.getFieldGroupByUid(constraint.getFieldGroupUid()));
			}
			if (constraint.getStatusUid() != null) {
				constraint.setStatus(entity.getStatusByUid(constraint.getStatusUid()));
			}
			if (constraint.getUserGroupUid() != null) {
				constraint.setUserGroup(userGroupService.findByUid(session, constraint.getUserGroupUid()));
			}
			if (currentVersionEntity != null) {
				final EntityFieldConstraint currentVersionConstraint = 
						currentVersionEntity.getFieldConstraintByUid(constraint.getUid());
				if (currentVersionConstraint != null) {
					currentVersionConstraint.copySystemFieldsTo(constraint);
				}
			}
		}
	}
	
	private void initStatus(Entity entity, Entity currentVersionEntity) {
		for (EntityStatus status : entity.getStatusList()) {
			status.setEntity(entity);
			if (currentVersionEntity != null) {
				final EntityStatus currentVersionStatus = 
					currentVersionEntity.getStatusByUid(status.getUid());
				if (currentVersionStatus != null) {
					currentVersionStatus.copySystemFieldsTo(status);
				}
			}
		}
	}
	
	private void initStatusTransitions(Session session, Entity entity, Entity currentVersionEntity) {
		for (EntityStatusTransition transition : entity.getStatusTransitions()) {
			transition.setEntity(entity);
			transition.setSourceStatus(entity.getStatusByUid(transition.getSourceStatusUid()));
			transition.setTargetStatus(entity.getStatusByUid(transition.getTargetStatusUid()));
			
			EntityStatusTransition currentVersionTransition = null;
			if (currentVersionEntity != null) {
				currentVersionTransition = currentVersionEntity.getStatusTransitionByUid(transition.getUid());
				if (currentVersionTransition != null) {
					currentVersionTransition.copySystemFieldsTo(transition);
				}
			}
			
			// functions
			initStatusTransitionFunctions(entity, transition, currentVersionTransition);
			
			// permissions
			initStatusTransitionPermission(transition, currentVersionTransition, session);
		}
	}
	
	private void initStatusTransitionFunctions(Entity entity, 
											   EntityStatusTransition transition, 
											   EntityStatusTransition currentVersionTransition) {
		if (transition.hasFunctions()) {
			for (EntityStatusTransitionFunction function : transition.getFunctions()) {
				function.setStatusTransition(transition);
				function.setFunction(entity.getFunctionByUid(function.getFunctionUid()));
				if (currentVersionTransition != null) {
					final EntityStatusTransitionFunction currentVersionFunction =
							currentVersionTransition.getFunctionByUid(function.getUid());
					if (currentVersionFunction != null) {
						currentVersionFunction.copySystemFieldsTo(function);
					}
				}
			}
		}
	}

	private void initStatusTransitionPermission(EntityStatusTransition transition,
												EntityStatusTransition currentVersionTransition,
												Session session) {
		if (transition.hasPermissions()) {
			for (EntityStatusTransitionPermission permission : transition.getPermissions()) {
				permission.setStatusTransition(transition);
				permission.setUserGroup(userGroupService.findByUid(session, permission.getUserGroupUid()));
				if (currentVersionTransition != null) {
					final EntityStatusTransitionPermission currentVersionPermission =
						currentVersionTransition.getPermissionByUid(permission.getUid());
					if (currentVersionPermission != null) {
						currentVersionPermission.copySystemFieldsTo(permission);
					}
				}
			}
		}
	}
	
	// clean up inconsistencies
	private void cleanup(Entity entity) {
		// fields
		if (entity.hasFields()) {
			entity.getFields().forEach(this::cleanupField);
		}
		// function without content is inactive
		filterAndForEach(entity.getCallbackFunctions(), 
						 function -> function.getContent() == null,
						 function -> function.setActive(false));
	}
	
	private void cleanupField(EntityField field) {
		if (field.isCalculated()) {
			field.setAutonumPattern(null);
			field.setAutonumStart(null);
			field.setValidationPattern(null);
		}
		else {
			field.setFormula(null);
		}
		if (field.getType() == null) {
			return;
		}
		
		// boolean and calculated fields cannot have options
		if (field.getType().isBoolean() || field.isCalculated()) {
			field.setMandatory(false);
			field.setIndexed(false);
			field.setUnique(false);
		}
		// only text fields can have length
		if (!field.getType().isText()) {
			field.setLength(null);
		}
		// only some fields can be fulltext indexed
		if (!(field.isTextField() || field.getType().isAutonum() || field.getType().isReference())) {
			field.setFullTextSearch(false);
		}
		// only reference fields can have reference entity or field
		if (!field.getType().isReference()) {
			field.setReferenceEntity(null);
		}
		// only autonum fields can have pattern and start
		if (!field.getType().isAutonum()) {
			field.setAutonumPattern(null);
			field.setAutonumStart(null);
		}
		// only some field types support validation
		if (!field.getType().supportsValidation()) {
			field.setValidationPattern(null);
		}
		// only some field types support min / max values
		if (!field.getType().supportsMinMaxValues()) {
			field.setMinDate(null);
			field.setMinDateTime(null);
			field.setMinDecimal(null);
			field.setMinDouble(null);
			field.setMinInt(null);
			field.setMinLong(null);
			field.setMaxDate(null);
			field.setMaxDateTime(null);
			field.setMaxDecimal(null);
			field.setMaxDouble(null);
			field.setMaxInt(null);
			field.setMaxLong(null);
		}
	}
	
	private ChangeLog createChangeLog(Entity currentVersionEntity, Entity nextVersionEntity, Session session) {
		return createChangeLog(currentVersionEntity, nextVersionEntity, session, null);
	}
	
	private ChangeLog createChangeLog(Entity currentVersionEntity, Entity nextVersionEntity,
									  Session session, @Nullable ReferenceChangeLog referenceChangeLog) {
		final var builder = new EntityChangeLogBuilder(schemaManager.getDatabaseInfo(), limits);
		builder.setCurrentVersionObject(currentVersionEntity);
		builder.setNextVersionObject(nextVersionEntity);
		builder.setReferenceChangeLog(referenceChangeLog);
		// generic
		if (currentVersionEntity != null && currentVersionEntity.isGeneric()) {
			builder.setDescendants(findDescendants(currentVersionEntity, session));
		}
		if (nextVersionEntity != null) {
			builder.setInverseRelateds(findInverseRelatedEntities(nextVersionEntity, session));
		}
		return builder.build();
	}
	
	private List<Entity> findInverseRelatedEntities(Entity entity, Session session) {
		return subList(getObjects(session), otherEntity -> otherEntity.isRelatedEntity(entity));
	}
	
	private List<EntityChangeAware> getChangeAwareObjects() {
		if (changeAwareObjects == null) {
			changeAwareObjects = getBeans(EntityChangeAware.class);
		}
		return changeAwareObjects;
	}
	
	private static Entity createDummyEntity(String name) {
		final var entity = new EntityMetadata();
		entity.setName(name);
		return entity;
	}
	
	private static EntityPermission createPermission(Entity entity, UserGroup group) {
		final EntityPermission permission = new EntityPermission();
		permission.setEntity(entity);
		permission.setUserGroup(group);
		permission.setAccess(EntityAccess.DELETE);
		return permission;
	}
	
	private static EntityStatusTransitionFunction createTransitionFunction(EntityStatusTransition transition, EntityFunction function) {
		final EntityStatusTransitionFunction transitionFunction = new EntityStatusTransitionFunction();
		transitionFunction.setStatusTransition(transition);
		transitionFunction.setFunction(function);
		return transitionFunction;
	}
	
	private static EntityStatusTransitionPermission createTransitionPermission(EntityStatusTransition transition, UserGroup group) {
		final EntityStatusTransitionPermission permission = new EntityStatusTransitionPermission();
		permission.setStatusTransition(transition);
		permission.setUserGroup(group);
		return permission;
	}
	
	private static final Comparator<ChangeLog> changeLogComparator = new Comparator<>() {
		@Override
		public int compare(ChangeLog changeLog1, ChangeLog changeLog2) {
			return Integer.compare(getPriority(changeLog1), getPriority(changeLog2));
		}
		
		private int getPriority(ChangeLog changeLog) {
			return changeLog.getChangeSet().contains("createTable") ? 1 : 2;
		}
	};
	
	// mask table and column names that equals SQL keywords
	private class SchemaUpdateHandler0933 {
		
		void process(Module module) {
			module.getEntities().forEach(this::process);
		}
		
		private void process(Entity entity) {
			if (entity.getTableName() != null) {
				if (NameUtils.isSqlKeyword(entity.getTableName())) {
					((EntityMetadata) entity).setTableName(entity.getTableName() + '_');
				}
			}
			else if (NameUtils.isSqlKeyword(entity.getInternalName())) {
				((EntityMetadata) entity).setTableName(entity.getInternalName() + '_');
			}
			if (entity.hasFields()) {
				processFields(entity);
			}
		}
		
		private void processFields(Entity entity) {
			for (EntityField field : entity.getFields()) {
				if (field.getColumnName() != null) {
					if (NameUtils.isSqlKeyword(field.getColumnName())) {
						field.setColumnName(field.getColumnName() + '_');
					}
				}
				else if (NameUtils.isSqlKeyword(field.getInternalName())) {
					field.setColumnName(field.getInternalName() + '_');
				}
			}
		}
		
	}

}
