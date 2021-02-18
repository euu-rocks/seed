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

import javax.annotation.Nullable;

import org.hibernate.Session;

import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class FilterServiceImpl extends AbstractApplicationEntityService<Filter>
	implements FilterService, EntityDependent {
	
	@Autowired
	private EntityService entityService;
	
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
	public void initObject(Filter filter) throws ValidationException {
		Assert.notNull(filter, "filter is null");
		
		super.initObject(filter);
		final FilterMetadata filterMeta = (FilterMetadata) filter;
		if (filterMeta.isHqlInput()) {
			filterMeta.setHqlQuery("from " + filter.getEntity().getInternalName());
		}
	}
	
	@Override
	public Filter getFilterByName(Entity entity, String name) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(name, "name is null");
		
		final List<Filter> list = filterRepository.find(queryParam("entity", entity),
														queryParam("name", name));
		return !list.isEmpty() ? list.get(0) : null;
	}
	
	@Override
	public Filter createFieldFilter(Entity entity, EntityField entityField, Object value) {
		Assert.notNull(entity, "entity is null");
		Assert.notNull(entityField, "entityField is null");
		Assert.notNull(value, "value is null");
		
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
		Assert.notNull(entity, "entity is null");
		Assert.notNull(status, "status is null");
		
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
		Assert.notNull(filter, "filter is null");
		
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
		Assert.notNull(entity, "entity is null");
		
		return filterRepository.find(queryParam("entity", entity));
	}
	
	@Override
	public void analyzeObjects(ImportAnalysis analysis, Module currentVersionModule) {
		Assert.notNull(analysis, "analysis is null");
		
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
		if (currentVersionModule != null && currentVersionModule.getFilters() != null) {
			for (Filter currentVersionFilter : currentVersionModule.getFilters()) {
				if (analysis.getModule().getFilterByUid(currentVersionFilter.getUid()) == null) {
					analysis.addChangeDelete(currentVersionFilter);
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
					if (filter.hasCriteria()) {
						for (FilterCriterion criterion : filter.getCriteria()) {
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
					}
					saveObject(filter, session);
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
		Assert.notNull(filter, "filter is null");
		
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
		Assert.notNull(entityField, "entityField is null");
		
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
