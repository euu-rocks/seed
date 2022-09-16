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
import org.seed.InternalException;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.codegen.CodeChangeAware;
import org.seed.core.codegen.CodeManagerImpl;
import org.seed.core.codegen.SourceCode;
import org.seed.core.config.Limits;
import org.seed.core.config.SchemaManager;
import org.seed.core.config.SchemaVersion;
import org.seed.core.config.UpdatableConfiguration;
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
import org.seed.core.util.BeanUtils;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.NameUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class EntityServiceImpl extends AbstractApplicationEntityService<Entity> 
	implements EntityService, EntityDependent<Entity>, UserGroupDependent<Entity>, 
		CodeChangeAware, ApplicationContextAware { 
	
	@Autowired
	private EntityValidator entityValidator;

	@Autowired
	private EntityRepository entityRepository;
	
	@Autowired
	private AutonumberService autonumService;
	
	@Autowired
	private UserGroupService userGroupService;
	
	@Autowired
	private UpdatableConfiguration configuration;
	
	@Autowired
	private SchemaManager schemaManager; 
	
	@Autowired
	private Limits limits;
	
	private ApplicationContext applicationContext;
	
	private List<EntityChangeAware> changeAwareObjects;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	@Override
	protected EntityRepository getRepository() {
		return entityRepository;
	}

	@Override
	protected EntityValidator getValidator() {
		return entityValidator;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public Entity createInstance(@Nullable Options options) {
		final EntityMetadata instance = (EntityMetadata) super.createInstance(options);
		instance.createLists();
		return instance;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public EntityField createField(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final EntityField field  = new EntityField();
		entity.addField(field);
		return field;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public EntityFieldGroup createFieldGroup(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final EntityFieldGroup group = new EntityFieldGroup();
		entity.addFieldGroup(group);
		return group;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public NestedEntity createNested(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final NestedEntity nested = new NestedEntity();
		entity.addNested(nested);
		return nested;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public EntityFunction createFunction(Entity entity, boolean isCallback) {
		Assert.notNull(entity, C.ENTITY);
		
		final EntityFunction function = new EntityFunction();
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
		
		final EntityRelation relation = new EntityRelation();
		entity.addRelation(relation);
		return relation;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public EntityStatus createStatus(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final EntityStatus status = new EntityStatus();
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
		
		final EntityFieldConstraint constraint = new EntityFieldConstraint();
		entity.addFieldConstraint(constraint);
		return constraint;
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public EntityStatusTransition createStatusTransition(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final EntityStatusTransition transition = new EntityStatusTransition();
		transition.createLists();
		entity.addStatusTransition(transition);
		return transition;
	}
	
	@Override
	public boolean existGenericEntities() {
		return existGeneric(true);
	}
	
	@Override
	public boolean existNonGenericEntities() {
		return existGeneric(false);
	}
	
	@Override
	public List<Entity> findGenericEntities() {
		return findGeneric(true);
	}
	
	@Override
	public List<Entity> findNonGenericEntities() {
		return findGeneric(false);
	}
	
	@Override
	public List<Entity> findTransferableEntities() {
		return entityRepository.find(queryParam("isTransferable", true));
	}
	
	@Override
	public List<Entity> findDescendants(Entity genericEntity) {
		Assert.notNull(genericEntity, "genericEntity");
		Assert.state(genericEntity.isGeneric(), "entity is not generic");
		
		return entityRepository.find(queryParam("genericEntity", genericEntity));
	}
	
	@Override
	public List<Entity> findUsage(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		return subList(getObjects(), obj -> !entity.equals(obj) && 
											(entity.equals(obj.getGenericEntity()) ||
											!obj.getReferenceFields(entity).isEmpty() ||
											obj.isNestedEntity(entity) || 
											obj.isRelatedEntity(entity)));
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
	public List<Entity> findUsage(UserGroup userGroup) {
		Assert.notNull(userGroup, C.USERNAME);
		
		return subList(getObjects(), entity -> entity.containsPermission(userGroup) ||
					   anyMatch(entity.getStatusTransitions(), tran -> tran.containsPermission(userGroup)));
	}
	
	@Override
	public List<Entity> findUsage(EntityField entityField) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		if (!entityField.getType().isReference()) {
			return Collections.emptyList();
		}
		return subList(getObjects(), entity -> anyMatch(entity.getNesteds(), nested -> nested.getReferenceField().equals(entityField)));
	}
	
	@Override
	public List<Entity> findUsage(EntityStatus entityStatus) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Entity> findUsage(EntityFunction entityFunction) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Entity> findUsage(NestedEntity nestedEntity) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Entity> findUsage(EntityRelation entityRelation) {
		return Collections.emptyList();
	}
	
	@Override
	public FieldType[] getAvailableFieldTypes(Entity entity, EntityField field, boolean existObjects) {
		Assert.notNull(entity, C.ENTITY);
		
		if (entity.isGeneric()) {
			return FieldType.nonAutonumTypes();
		}
		else if (existObjects) {
			if (field != null && !field.isNew() && field.getType() != null) {
				return getAvailableFieldTypesIfObjectsExist(entity, field);
			}
			else if (entity.isTransferable()) {
				return FieldType.transferableTypes();
			}
			else {
				return FieldType.nonAutonumTypes();
			}
		}
		else if (entity.isTransferable()) {
			return FieldType.transferableTypes();
		}
		else {
			return FieldType.values();
		}
	}
	
	@Override
	public List<Entity> getAvailableNestedEntities(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		try (Session session = entityRepository.getSession()) {
			return subList(entityRepository.find(session), 
					nested -> !nested.equals(entity) &&
							  !nested.isGeneric() &&
							  !nested.getReferenceFields(entity).isEmpty());
		}
	}
	
	@Override
	public List<EntityPermission> getAvailablePermissions(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		return filterAndConvert(userGroupService.findNonSystemGroups(), 
								group -> !entity.containsPermission(group), 
								group -> createPermission(entity, group));
	}
	
	@Override
	public List<EntityStatusTransitionPermission> getAvailableStatusTransitionPermissions(EntityStatusTransition transition) {
		Assert.notNull(transition, C.TRANSITION);
		
		return filterAndConvert(userGroupService.findNonSystemGroups(), 
								group -> !transition.containsPermission(group), 
								group -> createTransitionPermission(transition, group));
	}
	
	@Override
	public List<EntityStatus> getAvailableStatusList(Entity entity, EntityStatus currentStatus, User user) {
		Assert.notNull(entity, C.ENTITY);
		Assert.state(entity.hasStatus(), "entity has no status");
		Assert.notNull(currentStatus, "current status");
		Assert.notNull(user, C.USER);
		
		final List<EntityStatus> result =
				filterAndConvert(entity.getStatusTransitions(), 
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
								EntityFunction::isActiveOnStatusTransition, 
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
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		try {
			if (context.getModule().getEntities() != null) {
				importEntities(context, session);
				// init references to other entities
				for (Entity entity : context.getModule().getEntities()) {
					final Entity genericEntity = entity.getGenericEntityUid() != null
							? findByUid(session, entity.getGenericEntityUid())
							: null;
					((EntityMetadata) entity).setGenericEntity(genericEntity);
					initEntityReferences(entity, session);
					initRelationEntities(session, entity);
					initConstraintFields(entity);
					// validate and save
					saveObject(entity, session);
				}
			}
		}
		catch (ValidationException vex) {
			throw new InternalException(vex);
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
		
		final ReferenceChangeLog referenceChangeLog = new ReferenceChangeLog();
		final List<ChangeLog> changeLogs = new ArrayList<>();
		for (Entity entity : context.getNewEntities()) {
			if (!entity.isGeneric()) {
				final ChangeLog changeLog = createChangeLog(null, entity, referenceChangeLog);
				if (changeLog != null) {
					changeLogs.add(changeLog);
				}
			}
		}
		for (Entity entity : context.getExistingEntities()) {
			if (!entity.isGeneric()) {
				final Entity currentVersionEntity = context.getCurrentVersionEntity(entity.getUid());
				final ChangeLog changeLog = createChangeLog(currentVersionEntity, entity, referenceChangeLog);
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
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		if (currentVersionModule.getEntities() != null) {
			for (Entity currentVersionEntity : currentVersionModule.getEntities()) {
				if (module.getEntityByUid(currentVersionEntity.getUid()) == null) {
					final EntityField autonumField = currentVersionEntity.findAutonumField(); 
					// delete autonumber
					if (autonumField != null) {
						autonumService.deleteAutonumber(autonumField, session);
					}
					session.delete(currentVersionEntity);
				}
			}
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void saveObject(Entity entity) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		
		cleanup(entity);
		final boolean isInsert = entity.isNew();
		final Entity currentVersionEntity = !isInsert ? getObject(entity.getId()) : null;
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
				saveObject(entity, session);
				
				final ChangeLog changeLog = createChangeLog(currentVersionEntity, entity);
				if (changeLog != null) {
					session.saveOrUpdate(changeLog);
				}
				tx.commit();
			}
			catch (Exception ex) {
				if (isInsert) {
					// reset id because its assigned even if insert fails
					((AbstractSystemObject) entity).resetId();
				}
				handleException(tx, ex);
			}
		}
		
		configuration.updateConfiguration();
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
			final NestedEntity nested = new NestedEntity();
			nested.setName(entity.getName());
			nested.setNestedEntity(entity);
			nested.setReferenceField(entity.getReferenceFields(parentEntity).get(0));
			parentEntity.addNested(nested);
			super.saveObject(parentEntity, session);
		}
		
		afterSaveObject(entity, parentEntity, session, isInsert);
	}
	
	private void beforeSaveObject(Entity entity, Session session, boolean isInsert) {
		if (!isInsert && !entity.isGeneric()) {
			getChangeAwareObjects().forEach(aware -> aware.notifyBeforeChange(entity, session));
		}
	}
	
	private void afterSaveObject(Entity entity, Entity parentEntity, Session session, boolean isInsert) {
		final List<Entity> descendants = entity.isGeneric() ? findDescendants(entity) : null;
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
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void deleteObject(Entity entity) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		
		try (Session session = entityRepository.getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				final EntityField autonumField = entity.findAutonumField();
				if (autonumField != null) {
					autonumService.deleteAutonumber(autonumField, session);
				}
				deleteObject(entity, session);
				
				final ChangeLog changeLog = createChangeLog(entity, null);
				if (changeLog != null) {
					session.saveOrUpdate(changeLog);
				}
				tx.commit();
			}
			catch (Exception ex) {
				handleException(tx, ex);
			}
		}
		
		configuration.updateConfiguration();
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
		
		if (notEmpty(context.getModule().getEntities())) {
			if (schemaVersion == SchemaVersion.V_0_9_33) {
				// mask table and column names that equals SQL keywords
				new SchemaUpdateHandler0933().process(context.getModule());
			}
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
	
	private boolean processCallbackFunctionChange(SourceCode sourceCode, Session session) {
		final String entityName = sourceCode.getPackageName().substring(CodeManagerImpl.GENERATED_ENTITY_PACKAGE.length() + 1);
		for (Entity entity : getObjects()) {
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
	
	private List<Entity> findGeneric(boolean generic) {
		return entityRepository.find(queryParam("isGeneric", generic));
	}
	
	private boolean existGeneric(boolean generic) {
		return entityRepository.exist(queryParam("isGeneric", generic));
	}
	
	private EntityField getDeletedAutonumField(Entity currentVersion, Entity entity) {
		final EntityField autonumField = currentVersion.findAutonumField();
		if (autonumField != null && !autonumField.equals(entity.findAutonumField())) {
			return autonumField;
		}
		return null;
	}
	
	private static FieldType[] getAvailableFieldTypesIfObjectsExist(Entity entity, EntityField field) {
		switch (field.getType()) {
			case BOOLEAN:
				if (entity.isTransferable()) {
					return MiscUtils.toArray(field.getType(),
							 				 FieldType.INTEGER,
							 				 FieldType.LONG,
							 				 FieldType.TEXT);
				}
				return MiscUtils.toArray(field.getType(),
										 FieldType.INTEGER,
										 FieldType.LONG,
										 FieldType.TEXT,
										 FieldType.TEXTLONG);
			case INTEGER:
				if (entity.isTransferable()) {
					return MiscUtils.toArray(field.getType(),
											 FieldType.LONG,
											 FieldType.TEXT);
				}
				return MiscUtils.toArray(field.getType(),
										 FieldType.LONG,
										 FieldType.TEXT,
										 FieldType.TEXTLONG);
			case BINARY:
			case FILE:
			case TEXTLONG:
			case REFERENCE:
				return MiscUtils.toArray(field.getType());
				
			default:
				if (entity.isTransferable()) {
					return MiscUtils.toArray(field.getType(),
											 FieldType.TEXT);
				}
				return MiscUtils.toArray(field.getType(),
									 	 FieldType.TEXT,
									 	 FieldType.TEXTLONG);
		}
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
		if (field.getType() != null) {
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
		}
		if (!field.isCalculated()) {
			field.setFormula(null);
		}
	}
	
	private ChangeLog createChangeLog(Entity currentVersionEntity, Entity nextVersionEntity) {
		return createChangeLog(currentVersionEntity, nextVersionEntity, null);
	}
	
	private ChangeLog createChangeLog(Entity currentVersionEntity, Entity nextVersionEntity,
									  @Nullable ReferenceChangeLog referenceChangeLog) {
		final EntityChangeLogBuilder builder = 
				new EntityChangeLogBuilder(schemaManager.getDatabaseInfo(), limits);
		builder.setCurrentVersionObject(currentVersionEntity);
		builder.setNextVersionObject(nextVersionEntity);
		builder.setReferenceChangeLog(referenceChangeLog);
		// generic
		if (currentVersionEntity != null && currentVersionEntity.isGeneric()) {
			builder.setDescendants(findDescendants(currentVersionEntity));
		}
		if (nextVersionEntity != null) {
			builder.setInverseRelateds(findInverseRelatedEntities(nextVersionEntity));
		}
		return builder.build();
	}
	
	private List<Entity> findInverseRelatedEntities(Entity entity) {
		
		return subList(getObjects(), otherEntity -> otherEntity.isRelatedEntity(entity));
	}
	
	private List<EntityChangeAware> getChangeAwareObjects() {
		if (changeAwareObjects == null) {
			changeAwareObjects = BeanUtils.getBeans(applicationContext, EntityChangeAware.class);
		}
		return changeAwareObjects;
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
