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
import org.seed.core.codegen.SourceCode;
import org.seed.core.config.Limits;
import org.seed.core.config.UpdatableConfiguration;
import org.seed.core.config.changelog.ChangeLog;
import org.seed.core.data.FieldType;
import org.seed.core.data.Options;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.autonum.AutonumberService;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupDependent;
import org.seed.core.user.UserGroupService;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class EntityServiceImpl extends AbstractApplicationEntityService<Entity> 
	implements EntityService, EntityDependent<Entity>, UserGroupDependent<Entity>, CodeChangeAware { 
	
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
	private Limits limits;
	
	@Autowired
	private List<EntityChangeAware> changeAwareObjects;
	
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
		return entityRepository.exist(queryParam("isGeneric", true));
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
	public List<Entity> findUsage(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final List<Entity> result = new ArrayList<>();
		for (Entity otherEntity : getObjects()) {
			if (entity.equals(otherEntity)) {
				continue;
			}
			if (entity.equals(otherEntity.getGenericEntity())) {
				result.add(otherEntity);
			}
			else if (!otherEntity.getReferenceFields(entity).isEmpty()) {
				result.add(otherEntity);
			}
			else if (otherEntity.hasNesteds()) {
				for (NestedEntity nested : otherEntity.getNesteds()) {
					if (nested.getNestedEntity().equals(entity)) {
						result.add(otherEntity);
						break;
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public List<Entity> findUsage(EntityFieldGroup fieldGroup) {
		Assert.notNull(fieldGroup, C.FIELDGROUP);
		
		final List<Entity> result = new ArrayList<>();
		for (Entity entity : getObjects()) {
			if (entity.equals(fieldGroup.getEntity())) {
				continue;
			}
			if (entity.containsFieldGroup(fieldGroup)) {
				result.add(entity);
			}
		}
		return result;
	}
	
	@Override
	public List<Entity> findUsage(UserGroup userGroup) {
		Assert.notNull(userGroup, C.USERNAME);
		
		final List<Entity> result = new ArrayList<>();
		for (Entity entity : getObjects()) {
			if ((entity.hasPermissions() && checkPermissions(entity, userGroup)) ||
				(entity.hasStatusTransitions() && checkStatusTransitions(entity, userGroup))) {
				result.add(entity);
			}
		}
		return result;
	}
	
	private boolean checkPermissions(Entity entity, UserGroup userGroup) {
		if (entity.hasPermissions()) {
			for (EntityPermission permission : entity.getPermissions()) {
				if (userGroup.equals(permission.getUserGroup())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean checkStatusTransitions(Entity entity, UserGroup userGroup) {
		if (entity.hasStatusTransitions()) {
			for (EntityStatusTransition transition : entity.getStatusTransitions()) {
				if (transition.hasPermissions()) {
					for (EntityStatusTransitionPermission permission : transition.getPermissions()) {
						if (userGroup.equals(permission.getUserGroup())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public List<Entity> findUsage(EntityField entityField) {
		return Collections.emptyList();
		
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
	public FieldType[] getAvailableFieldTypes(Entity entity, EntityField field, boolean existObjects) {
		Assert.notNull(entity, C.ENTITY);
		
		if (!existObjects) {
			return FieldType.values();
		}
		if (field != null && field.getType() != null && !field.isNew()) {
			switch (field.getType()) {
				case BOOLEAN:
					return new FieldType[] { FieldType.BOOLEAN,
											 FieldType.INTEGER,
											 FieldType.LONG,
											 FieldType.TEXT,
											 FieldType.TEXTLONG };
				case INTEGER:
					return new FieldType[] { FieldType.INTEGER,
											 FieldType.LONG,
											 FieldType.TEXT,
											 FieldType.TEXTLONG };
				case BINARY:
				case FILE:
				case TEXTLONG:
				case REFERENCE:
					return new FieldType[] { field.getType() };
					
				default:
					return new FieldType[] { field.getType(),
										 	 FieldType.TEXT,
										 	 FieldType.TEXTLONG };
			}
		}
		return FieldType.valuesWithoutAutonum();
	}
	
	@Override
	public List<Entity> getAvailableNestedEntities(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final List<Entity> result = new ArrayList<>();
		try (Session session = entityRepository.getSession()) {
			for (Entity nested : entityRepository.find(session)) {
				if (!nested.equals(entity) &&
					!nested.getReferenceFields(entity).isEmpty()) {
					result.add(nested);
				}
			}
		}
		return result;
	}
	
	@Override
	public List<EntityPermission> getAvailablePermissions(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final List<EntityPermission> result = new ArrayList<>();
		for (UserGroup group : userGroupService.getObjects()) {
			boolean found = false;
			if (entity.hasPermissions()) {
				for (EntityPermission permission : entity.getPermissions()) {
					if (permission.getUserGroup().equals(group)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				final EntityPermission permission = new EntityPermission();
				permission.setEntity(entity);
				permission.setUserGroup(group);
				permission.setAccess(EntityAccess.READ);
				result.add(permission);
			}
		}
		return result;
	}
	
	@Override
	public List<EntityStatusTransitionPermission> getAvailableStatusTransitionPermissions(EntityStatusTransition transition) {
		Assert.notNull(transition, C.TRANSITION);
		
		final List<EntityStatusTransitionPermission> result = new ArrayList<>();
		for (UserGroup group : userGroupService.getObjects()) {
			boolean found = false;
			if (transition.hasPermissions()) {
				for (EntityStatusTransitionPermission permission : transition.getPermissions()) {
					if (permission.getUserGroup().equals(group)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				final EntityStatusTransitionPermission permission = new EntityStatusTransitionPermission();
				permission.setStatusTransition(transition);
				permission.setUserGroup(group);
				result.add(permission);
			}
		}
		return result;
	}
	
	@Override
	public List<EntityStatus> getAvailableStatusList(Entity entity, EntityStatus currentStatus, User user) {
		Assert.notNull(entity, C.ENTITY);
		Assert.state(entity.hasStatus(), "entity has no status");
		Assert.notNull(currentStatus, "currentStatus");
		Assert.notNull(user, C.USER);
		
		final List<EntityStatus> result = new ArrayList<>();
		if (entity.hasStatus()) {
			result.add(currentStatus);
			if (entity.hasStatusTransitions()) {
				for (EntityStatusTransition transition : entity.getStatusTransitions()) {
					if (transition.getSourceStatus().equals(currentStatus) &&
						transition.isAuthorized(user)) {
						result.add(transition.getTargetStatus());
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public List<EntityStatusTransitionFunction> getAvailableStatusTransitionFunctions(Entity entity, EntityStatusTransition transition) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(transition, C.TRANSITION);
		
		final List<EntityStatusTransitionFunction> result = new ArrayList<>();
		if (entity.hasFunctions()) {
			for (EntityFunction function : entity.getCallbackFunctions()) {
				if (!function.isActiveOnStatusTransition()) {
					continue;
				}
				if (!transition.containsEntityFunction(function)) {
					final EntityStatusTransitionFunction transitionFunction = new EntityStatusTransitionFunction();
					transitionFunction.setStatusTransition(transition);
					transitionFunction.setFunction(function);
					result.add(transitionFunction);
				}
			}
		}
		return result;
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
		if (entity.hasFields()) {
			for (EntityField field : entity.getFields()) {
				if (fieldGroup.equals(field.getFieldGroup())) {
					field.setFieldGroup(null);
				}
			}
		}
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
		if (currentVersionModule.getEntities() != null) {
			for (Entity currentVersionEntity : currentVersionModule.getEntities()) {
				if (analysis.getModule().getEntityByUid(currentVersionEntity.getUid()) == null) {
					analysis.addChangeDelete(currentVersionEntity);
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { UserGroupService.class };
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		try {
			if (context.getModule().getEntities() != null) {
				importEntities(context, session);
				// set references to other entities
				for (Entity entity : context.getModule().getEntities()) {
					initEntityReferences(entity, session);
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
			
			// entity already exists
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
			session.saveOrUpdate(entity);
		}
	}
	
	@Override
	public void createChangeLogs(TransferContext context, Session session) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		
		final List<ChangeLog> changeLogs = new ArrayList<>();
		for (Entity entity : context.getNewEntities()) {
			final ChangeLog changeLog = createChangeLog(null, entity);
			if (changeLog != null) {
				changeLogs.add(changeLog);
			}
		}
		for (Entity entity : context.getExistingEntities()) {
			final Entity currentVersionEntity = context.getCurrentVersionEntity(entity.getUid());
			final ChangeLog changeLog = createChangeLog(currentVersionEntity, entity);
			if (changeLog != null) {
				changeLogs.add(changeLog);
			}
		}
		changeLogs.sort(changeLogComparator);
		for (ChangeLog changeLog : changeLogs) {
			session.saveOrUpdate(changeLog);
		}
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
				
				if (!entity.isGeneric()) {
					final ChangeLog changeLog = createChangeLog(currentVersionEntity, entity);
					if (changeLog != null) {
						session.saveOrUpdate(changeLog);
					}
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
	public void saveObject(Entity entity, Session session) throws ValidationException {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		final boolean isInsert = entity.isNew();
		super.saveObject(entity, session);
		// notify listeners
		for (EntityChangeAware changeAware : changeAwareObjects) {
			if (isInsert) {
				changeAware.notifyCreate(entity, session);
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
				
				if (!entity.isGeneric()) {
					final ChangeLog changeLog = createChangeLog(entity, null);
					if (changeLog != null) {
						session.saveOrUpdate(changeLog);
					}
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
		
		for (EntityChangeAware changeAware : changeAwareObjects) {
			changeAware.notifyDelete(entity, session);
		}
		
		super.deleteObject(entity, session);
	}
	
	@Override
	public boolean processCodeChange(SourceCode sourceCode, Session session) {
		Assert.notNull(sourceCode, "sourceCode");
		Assert.notNull(session, C.SESSION);
		
		return sourceCode.getPackageName().startsWith(EntityMetadata.PACKAGE_NAME) &&
			   !sourceCode.getPackageName().equals(EntityMetadata.PACKAGE_NAME) &&
			   processCallbackFunctionChange(sourceCode, session);
	}
	
	private boolean processCallbackFunctionChange(SourceCode sourceCode, Session session) {
		final String entityName = sourceCode.getPackageName().substring(EntityMetadata.PACKAGE_NAME.length() + 1);
		for (Entity entity : getObjects()) {
			if (entity.getName().equalsIgnoreCase(entityName)) {
				for (EntityFunction function : entity.getFunctions()) {
					if (function.isCallback() && 
						function.getName().equalsIgnoreCase(sourceCode.getClassName()) &&
						!function.getContent().equals(sourceCode.getContent())) {
						
						function.setContent(sourceCode.getContent());
						session.saveOrUpdate(function);
						return true;
					}
				}
				break;
			}
		}
		return false;
	}
	
	private List<Entity> findGeneric(boolean generic) {
		return entityRepository.find(queryParam("isGeneric", generic));
	}
	
	private EntityField getDeletedAutonumField(Entity currentVersion, Entity entity) {
		final EntityField autonumField = currentVersion.findAutonumField();
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
		if (entity.hasAllNesteds()) {
			initNesteds(entity, currentVersionEntity);
		}
	}
	
	private void initEntityReferences(Entity entity, Session session) {
		if (entity.hasAllFields() || entity.hasAllNesteds()) {
			if (entity.hasAllFields()) {
				initReferenceFields(session, entity);
			}
			if (entity.hasAllNesteds()) {
				initNestedEntities(session, entity);
			}
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
		for (NestedEntity nested : entity.getAllNesteds()) {
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
	
	private void initNestedEntities(Session session, Entity entity) {
		for (NestedEntity nested : entity.getAllNesteds()) {
			final Entity nestedEntity = findByUid(session, nested.getNestedEntityUid());
			nested.setNestedEntity(nestedEntity);
			nested.setReferenceField(nestedEntity.getFieldByUid(nested.getReferenceFieldUid()));
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
			if (currentVersionEntity != null) {
				final EntityStatusTransition currentVersionTransition = 
					currentVersionEntity.getStatusTransitionByUid(transition.getUid());
				if (currentVersionTransition != null) {
					currentVersionTransition.copySystemFieldsTo(transition);
				}
				
				// functions
				initStatusTransitionFunctions(currentVersionEntity, transition, currentVersionTransition);
				
				// permissions
				initStatusTransitionPermission(transition, currentVersionTransition, session);
			}
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
			for (EntityField field : entity.getFields()) {
				cleanupField(field);
			}
		}
		// functions
		if (entity.hasFunctions()) {
			for (EntityFunction function : entity.getCallbackFunctions()) {
				// function without content is inactive
				if (function.getContent() == null) {
					function.setActive(false);
				}
			}
		}
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
		return new EntityChangeLogBuilder(limits, configuration)
						.setCurrentVersionObject(currentVersionEntity)
						.setNextVersionObject(nextVersionEntity)
						.build();
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

}
