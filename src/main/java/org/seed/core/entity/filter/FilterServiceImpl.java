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
package org.seed.core.entity.filter;

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.TransferableObject;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.data.Options;
import org.seed.core.data.SystemField;
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
import org.seed.core.entity.value.ValueObjectDependent;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupDependent;
import org.seed.core.user.UserGroupService;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class FilterServiceImpl extends AbstractApplicationEntityService<Filter>
	implements FilterService, EntityDependent<Filter>, 
			   ValueObjectDependent<Filter>, UserGroupDependent<Filter> {
	
	@Autowired
	private EntityService entityService;
	
	@Autowired
	private UserGroupService userGroupService;
	
	@Autowired
	private FilterRepository filterRepository;
	
	@Autowired
	private FilterValidator validator;
	
	@Override
	protected FilterRepository getRepository() {
		return filterRepository;
	}

	public void setRepository(FilterRepository filterRepository) {
		this.filterRepository = filterRepository;
	}

	@Override
	protected FilterValidator getValidator() {
		return validator;
	}
	
	@Override
	public Filter createInstance(@Nullable Options options) {
		final var filter = (FilterMetadata) super.createInstance(options);
		filter.createLists();
		return filter;
	}
	
	@Override
	public void initObject(Filter filter) throws ValidationException {
		Assert.notNull(filter, C.FILTER);
		
		super.initObject(filter);
		final var filterMeta = (FilterMetadata) filter;
		if (filterMeta.isHqlInput()) {
			filterMeta.setHqlQuery("from " + filter.getEntity().getInternalName());
		}
	}
	
	@Override
	public Filter getFilterByName(Entity entity, String name, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(name, C.NAME);
		
		return filterRepository.findUnique(session,
										   queryParam(C.ENTITY, entity),
										   queryParam(C.NAME, name));
	}
	
	@Override
	public List<Filter> getFilters(Entity entity, User user, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(user, C.USER);
		
		return subList(findFilters(entity, session), filter -> filter.checkPermissions(user));
	}
	
	@Override
	public Filter createFieldFilter(Entity entity, EntityField entityField, Object value) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(entityField, C.ENTITYFIELD);
		Assert.notNull(value, C.VALUE);
		
		final var filter = new FilterMetadata();
		filter.setEntity(entity);
		final var criterion = new FilterCriterion();
		criterion.setEntityField(entityField);
		criterion.setOperator(CriterionOperator.EQUAL);
		if (entityField.isReferenceField()) {
			criterion.setValueObject((ValueObject) value);
		}
		else {
			criterion.setValue(value);
		}
		filter.addCriterion(criterion);
		return filter;
	}
	
	@Override
	public Filter createStatusFilter(Entity entity, EntityStatus status) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(status, C.STATUS);
		
		final var filter = new FilterMetadata();
		filter.setEntity(entity);
		final var criterion = new FilterCriterion();
		criterion.setSystemField(SystemField.ENTITYSTATUS);
		criterion.setOperator(CriterionOperator.EQUAL);
		criterion.setValue(status);
		filter.addCriterion(criterion);
		return filter;
	}
	
	@Override
	public List<FilterElement> getFilterElements(Filter filter, 
												 @Nullable NestedEntity nestedEntity) {
		Assert.notNull(filter, C.FILTER);
		
		final var elements = new ArrayList<FilterElement>();
		// main object
		if (nestedEntity == null) {
			createEntityElements(filter.getEntity(), elements);
		}
		// nested
		else {
			nestedEntity.getFields(true).forEach(field -> elements.add(createElement(field, null)));
 		}
		return elements;
	}
	
	@Override
	public List<Filter> findFilters(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		return filterRepository.find(queryParam(C.ENTITY, entity));
	}
	
	@Override
	public List<Filter> findFilters(Entity entity, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		return filterRepository.find(session, queryParam(C.ENTITY, entity));
	}
	
	@Override
	public List<FilterPermission> getAvailablePermissions(Filter filter, Session session) {
		Assert.notNull(filter, C.FILTER);
		Assert.notNull(session, C.SESSION);
		
		return filterAndConvert(userGroupService.findNonSystemGroups(session), 
								not(filter::containsPermission),
								group -> createPermission(filter, group));
	}
	
	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, 
											 Module currentVersionModule) {
		if (analysis.getModule().getFilters() != null) {
			for (Filter filter : analysis.getModule().getFilters()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(filter);
					continue;
				}
				
				// entity needs to be set to determine field types
				if (filter.getEntityUid() != null) {
					((FilterMetadata) filter).setEntity(analysis.getModule().getEntityByUid(filter.getEntityUid()));
				}
				final Filter currentVersionFilter = 
					currentVersionModule.getFilterByUid(filter.getUid());
				if (currentVersionFilter == null) {
					analysis.addChangeNew(filter);
				}
				else if (!filter.isEqual(currentVersionFilter)) {
					analysis.addChangeModify(filter);
				}
			}
		}
	}
	
	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, 
												Module currentVersionModule) {
		filterAndForEach(currentVersionModule.getFilters(), 
						 filter -> analysis.getModule().getFilterByUid(filter.getUid()) == null, 
						 analysis::addChangeDelete);
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) throws ValidationException {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);

		if (context.getModule().getFilters() != null) {
			for (Filter filter : context.getModule().getFilters()) {
				final Entity entity = entityService.findByUid(session, filter.getEntityUid());
				final Filter currentVersionFilter = findByUid(session, filter.getUid()); 
				((FilterMetadata) filter).setModule(context.getModule());
				((FilterMetadata) filter).setEntity(entity);
				if (currentVersionFilter != null) {
					((FilterMetadata) currentVersionFilter).copySystemFieldsTo(filter);
					session.detach(currentVersionFilter);
				}
				initFilter(entity, filter, currentVersionFilter, session);
				saveObject(filter, session);
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { EntityService.class };
	}
	
	@Override
	public void removeObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		filterAndForEach(currentVersionModule.getFilters(), 
						 filter -> module.getFilterByUid(filter.getUid()) == null, 
						 filter -> session.saveOrUpdate(removeModule(filter)));
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void saveObject(Filter filter) throws ValidationException {
		Assert.notNull(filter, C.FILTER);
		
		if (filter.hasCriteria()) {
			for (FilterCriterion criterion : filter.getCriteria()) {
				criterion.cleanup();
				final FilterElement element = criterion.getElement();
				if (element != null) {
					criterion.setSystemField(null);
					criterion.setEntityField(null);
					if (element.getEntityField() != null) {
						Assert.state(element.getSystemField() == null, "entity and system field");
						criterion.setEntityField(element.getEntityField());
					}
					else {
						Assert.state(element.getSystemField() != null, "either entity or system field");
						criterion.setSystemField(element.getSystemField());
					}
				}
			}
		}
		super.saveObject(filter);
	}
	
	@Override
	@Secured("ROLE_ADMIN_ENTITY")
	public void deleteObject(Filter filter) throws ValidationException {
		super.deleteObject(filter);
	}
	
	@Override
	public List<Filter> findUsage(Entity entity, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(session, C.SESSION);
		
		return entity.isGeneric()
				? Collections.emptyList()
				: subList(getObjects(session), 
						filter -> entity.equals(filter.getEntity()) || 
						  anyMatch(filter.getCriteria(), 
							criterion -> criterion.getEntityField() != null && 
										 entity.equals(criterion.getEntityField().getEntity())));
	}

	@Override
	public List<Filter> findUsage(Entity entity, EntityField entityField, Session session) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(entityField, C.ENTITYFIELD);
		Assert.notNull(session, C.SESSION);
		
		return entity.isGeneric()
				? Collections.emptyList()
				: subList(findFilters(entity, session), 
						  filter -> anyMatch(filter.getCriteria(), 
								   			 criterion -> entityField.equals(criterion.getEntityField())));
	}
	
	@Override
	public List<Filter> findUsage(UserGroup userGroup, Session session) {
		Assert.notNull(userGroup, C.USERGROUP);
		Assert.notNull(session, C.SESSION);
		
		return subList(getObjects(session), filter -> filter.containsPermission(userGroup));
	}
	
	@Override
	public List<Filter> findUsage(Session session, ValueObject object) {
		Assert.notNull(object, C.OBJECT);
		
		if (object instanceof TransferableObject) {
			final String objectUid = ((TransferableObject) object).getUid();
			Assert.stateAvailable(objectUid, "object uid");
			
			return subList(getObjects(session), filter -> containsReferenceUid(filter, objectUid));
		}
		return Collections.emptyList();
	}
	
	@Override
	public List<Filter> findUsage(EntityStatus entityStatus, Session session) {
		Assert.notNull(entityStatus, "entity status");
		Assert.notNull(session, C.SESSION);
		Assert.state(entityStatus.getUid() != null, "status is new");
		
		return subList(getObjects(session), filter -> containsReferenceUid(filter, entityStatus.getUid()));
	}
	
	@Override
	public List<Filter> findUsage(Entity entity, EntityFieldGroup fieldGroup) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Filter> findUsage(EntityFunction entityFunction, Session session) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Filter> findUsage(NestedEntity nestedEntity, Session session) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Filter> findUsage(Entity entity, EntityRelation entityRelation, Session session) {
		return Collections.emptyList();
	}
	
	@Override
	public void initFilterCriteria(Filter filter, Session session) {
		Assert.notNull(filter, C.FILTER);
		Assert.notNull(session, C.SESSION);
		
		if (filter.hasCriteria()) {
			for (FilterCriterion filterCriterion : filter.getCriteria()) {
				initFilterCriterionElement(filter, filterCriterion, true, session);
			}
		}
	}
	
	private void initFilter(Entity entity, Filter filter, Filter currentVersionFilter, Session session) {
		if (filter.hasCriteria()) {
			for (FilterCriterion criterion : filter.getCriteria()) {
				initFilterCriterion(entity, criterion, filter, currentVersionFilter, session);
			}
		}
		if (filter.hasPermissions()) {
			for (FilterPermission permission : filter.getPermissions()) {
				initFilterPermission(permission, filter, currentVersionFilter, session);
			}
		}
	}
	
	private void initFilterCriterion(Entity entity, FilterCriterion criterion, 
									 Filter filter, Filter currentVersionFilter,
									 Session session) {
		criterion.setFilter(filter);
		if (criterion.getEntityFieldUid() != null) {
			criterion.setEntityField(entity.findFieldByUid(criterion.getEntityFieldUid()));
		}
		final FilterCriterion currentVersionCriterion = 
			currentVersionFilter != null 
				? currentVersionFilter.getCriterionByUid(criterion.getUid()) 
				: null;
		if (currentVersionCriterion != null) {
			currentVersionCriterion.copySystemFieldsTo(criterion);
		}
		initFilterCriterionElement(filter, criterion, false, session);
	}
	
	private void initFilterCriterionElement(Filter filter, FilterCriterion criterion, 
											boolean setReference, Session session) {
		final FilterElement element = new FilterElement();
		criterion.setElement(element);
		
		// entity field
		if (criterion.getEntityField() != null) {
			element.setEntityField(criterion.getEntityField());
			// reference field
			if (setReference && criterion.getReferenceUid() != null) {
				criterion.setReference(getReferenceObject(criterion, session));
			}
		}
		
		// system field
		else if (criterion.getSystemField() != null) {
			element.setSystemField(criterion.getSystemField());
			// status field
			if (criterion.getSystemField() == SystemField.ENTITYSTATUS) {
				criterion.setReference(filter.getEntity().getStatusByUid(criterion.getReferenceUid()));
			}
		}
		else {
			Assert.stateIllegal("neither entity nor system field");
		}
	}
	
	private TransferableObject getReferenceObject(FilterCriterion criterion, Session session) {
		return (TransferableObject) 
				getBean(ValueObjectService.class)
					.findByUid(criterion.getEntityField().getReferenceEntity(), 
		 					   criterion.getReferenceUid(), session);
	}
	
	private void initFilterPermission(FilterPermission permission, Filter filter, 
									  Filter currentVersionFilter, Session session) {
		permission.setFilter(filter);
		permission.setUserGroup(userGroupService.findByUid(session, permission.getUserGroupUid()));
		final FilterPermission currentVersionPermission =
			currentVersionFilter != null
				? currentVersionFilter.getPermissionByUid(permission.getUid())
				: null;
		if (currentVersionPermission != null) {
			currentVersionPermission.copySystemFieldsTo(permission);
		}
	}
	
	private static boolean containsReferenceUid(Filter filter, String uid) {
		return anyMatch(filter.getCriteria(), criterion -> uid.equals(criterion.getReferenceUid()));
	}
	
	private static void createEntityElements(Entity entity, List<FilterElement> elements) {
		// entity fields
		filterAndForEach(entity.getAllFields(), 
						 field -> !field.isReferenceField() || // allow reference fields only for module entities
								  field.getReferenceEntity().isTransferable(), 
						 field -> elements.add(createElement(field, null)));
		// system fields
		filterAndForEach(SystemField.publicSystemFields(), 
						 field -> field != SystemField.ENTITYSTATUS || entity.hasStatus(), 
						 field -> elements.add(createElement(null, field)));
	}
	
	private static FilterElement createElement(EntityField entityField, SystemField systemField)  {
		final var element = new FilterElement();
		if (entityField != null) {
			element.setEntityField(entityField);
		}
		else if (systemField != null) {
			element.setSystemField(systemField);
		}
		else {
			Assert.stateIllegal("neither entity nor system field");
		}
		return element;
	}
	
	private static FilterPermission createPermission(Filter filter, UserGroup group) {
		final var permission = new FilterPermission();
		permission.setFilter(filter);
		permission.setUserGroup(group);
		return permission;
	}

}
