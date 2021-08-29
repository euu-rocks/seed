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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.hibernate.Session;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
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
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
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
	implements FilterService, EntityDependent<Filter>, UserGroupDependent<Filter> {
	
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

	@Override
	protected FilterValidator getValidator() {
		return validator;
	}
	
	@Override
	public Filter createInstance(@Nullable Options options) {
		final FilterMetadata filter = (FilterMetadata) super.createInstance(options);
		filter.createLists();
		return filter;
	}
	
	@Override
	public void initObject(Filter filter) throws ValidationException {
		Assert.notNull(filter, C.FILTER);
		
		super.initObject(filter);
		final FilterMetadata filterMeta = (FilterMetadata) filter;
		if (filterMeta.isHqlInput()) {
			filterMeta.setHqlQuery("from " + filter.getEntity().getInternalName());
		}
	}
	
	@Override
	public Filter getFilterByName(Entity entity, String name) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(name, C.NAME);
		
		final List<Filter> list = filterRepository.find(queryParam(C.ENTITY, entity),
														queryParam(C.NAME, name));
		return !list.isEmpty() ? list.get(0) : null;
	}
	
	@Override
	public List<Filter> getFilters(Entity entity, User user) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(user, C.USER);
		
		return findFilters(entity).stream()
							   	  .filter(f -> f.checkPermissions(user))
							   	  .collect(Collectors.toList());
	}
	
	@Override
	public Filter createFieldFilter(Entity entity, EntityField entityField, Object value) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(entityField, C.ENTITYFIELD);
		Assert.notNull(value, C.VALUE);
		
		final FilterMetadata filter = new FilterMetadata();
		filter.setEntity(entity);
		final FilterCriterion criterion = new FilterCriterion();
		criterion.setEntityField(entityField);
		criterion.setOperator(CriterionOperator.EQUAL);
		criterion.setValue(value);
		filter.addCriterion(criterion);
		return filter;
	}
	
	@Override
	public Filter createStatusFilter(Entity entity, EntityStatus status) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(status, C.STATUS);
		
		final FilterMetadata filter = new FilterMetadata();
		filter.setEntity(entity);
		final FilterCriterion criterion = new FilterCriterion();
		criterion.setSystemField(SystemField.ENTITYSTATUS);
		criterion.setOperator(CriterionOperator.EQUAL);
		criterion.setValue(status);
		filter.addCriterion(criterion);
		return filter;
	}
	
	@Override
	public List<FilterElement> getFilterElements(Filter filter, @Nullable NestedEntity nestedEntity) {
		Assert.notNull(filter, C.FILTER);
		
		final List<FilterElement> elements = new ArrayList<>();
		// main object
		if (nestedEntity == null) {
			final Entity entity = filter.getEntity();
			// entity fields
			if (entity.hasAllFields()) {
				for (EntityField entityField : entity.getAllFields()) {
					elements.add(createElement(entityField, null));
				}
			}
			// system fields
			for (SystemField systemField : SystemField.values()) {
				if (systemField != SystemField.ENTITYSTATUS || entity.hasStatus()) {
					elements.add(createElement(null, systemField));
				}
			}
		}
		// nested
		else {
			for (EntityField nestedField : nestedEntity.getFields(true)) {
				elements.add(createElement(nestedField, null));
			}
 		}
		return elements;
	}
	
	@Override
	public List<Filter> findFilters(Entity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		return filterRepository.find(queryParam(C.ENTITY, entity));
	}
	
	@Override
	public List<FilterPermission> getAvailablePermissions(Filter filter) {
		Assert.notNull(filter, C.FILTER);
		
		final List<FilterPermission> result = new ArrayList<>();
		for (UserGroup group : userGroupService.getObjects()) {
			boolean found = false;
			if (filter.hasPermissions()) {
				for (FilterPermission permission : filter.getPermissions()) {
					if (permission.getUserGroup().equals(group)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				final FilterPermission permission = new FilterPermission();
				permission.setFilter(filter);
				permission.setUserGroup(group);
				result.add(permission);
			}
		}
		return result;
	}
	
	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (analysis.getModule().getFilters() != null) {
			for (Filter filter : analysis.getModule().getFilters()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(filter);
				}
				else {
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
	}
	
	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (currentVersionModule.getFilters() != null) {
			for (Filter currentVersionFilter : currentVersionModule.getFilters()) {
				if (analysis.getModule().getFilterByUid(currentVersionFilter.getUid()) == null) {
					analysis.addChangeDelete(currentVersionFilter);
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { EntityService.class };
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		try {
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
		catch (ValidationException vex) {
			throw new InternalException(vex);
		}
	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		if (currentVersionModule.getFilters() != null) {
			for (Filter currentVersionFilter : currentVersionModule.getFilters()) {
				if (module.getFilterByUid(currentVersionFilter.getUid()) == null) {
					session.delete(currentVersionFilter);
				}
			}
		}
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
	public List<Filter> findUsage(Entity entity) {
		return findFilters(entity);
	}

	@Override
	public List<Filter> findUsage(EntityField entityField) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		
		final List<Filter> result = new ArrayList<>();
		for (Filter filter : findFilters(entityField.getEntity())) {
			if (filter.hasCriteria()) {
				for (FilterCriterion criterion : filter.getCriteria()) {
					if (entityField.equals(criterion.getEntityField())) {
						result.add(filter);
						break;
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public List<Filter> findUsage(UserGroup userGroup) {
		Assert.notNull(userGroup, C.USERGROUP);
		
		final List<Filter> result = new ArrayList<>();
		for (Filter filter : getObjects()) {
			if (filter.hasPermissions()) {
				for (FilterPermission permission : filter.getPermissions()) {
					if (userGroup.equals(permission.getUserGroup())) {
						result.add(filter);
						break;
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public List<Filter> findUsage(EntityFieldGroup fieldGroup) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Filter> findUsage(EntityStatus entityStatus) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Filter> findUsage(EntityFunction entityFunction) {
		return Collections.emptyList();
	}
	
	@Override
	public List<Filter> findUsage(NestedEntity nestedEntity) {
		return Collections.emptyList();
	}
	
	private void initFilter(Entity entity, Filter filter, Filter currentVersionFilter, Session session) {
		if (filter.hasCriteria()) {
			for (FilterCriterion criterion : filter.getCriteria()) {
				initFilterCriterion(entity, criterion, filter, currentVersionFilter);
			}
		}
		if (filter.hasPermissions()) {
			for (FilterPermission permission : filter.getPermissions()) {
				initFilterPermission(permission, filter, currentVersionFilter, session);
			}
		}
	}
	
	private void initFilterCriterion(Entity entity, FilterCriterion criterion, Filter filter, Filter currentVersionFilter) {
		criterion.setFilter(filter);
		criterion.setEntityField(entity.findFieldByUid(criterion.getEntityFieldUid()));
		final FilterCriterion currentVersionCriterion = 
			currentVersionFilter != null 
				? currentVersionFilter.getCriterionByUid(criterion.getUid()) 
				: null;
		if (currentVersionCriterion != null) {
			currentVersionCriterion.copySystemFieldsTo(criterion);
		}
	}
	
	private void initFilterPermission(FilterPermission permission, Filter filter, Filter currentVersionFilter, Session session) {
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
	
	private static FilterElement createElement(EntityField entityField, SystemField systemField)  {
		final FilterElement element = new FilterElement();
		if (entityField != null) {
			Assert.state(systemField == null, "entity and system field");
			element.setEntityField(entityField);
		}
		else {
			Assert.state(systemField != null, "either entity or system field");
			element.setSystemField(systemField);
		}
		return element;
	}

}
