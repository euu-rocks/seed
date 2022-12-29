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

import java.util.List;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FilterValidator extends AbstractSystemEntityValidator<Filter> {
	
	@Autowired
	private FilterRepository repository;
	
	private List<FilterDependent<? extends SystemEntity>> filterDependents;
	
	@Override
	public void validateCreate(Filter filter) throws ValidationException {
		Assert.notNull(filter, C.FILTER);
		final ValidationErrors errors = createValidationErrors(filter);
		
		if (isEmpty(filter.getEntity())) {
			errors.addEmptyField("label.entity");
		}
		
		validate(errors);
	}
	
	@Override
	public void validateSave(Filter filter) throws ValidationException {
		Assert.notNull(filter, C.FILTER);
		final ValidationErrors errors = createValidationErrors(filter);
		
		if (isEmpty(filter.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(filter.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		
		// HQL
		if (((FilterMetadata) filter).isHqlInput()) {
			if (isEmpty(filter.getHqlQuery())) {
				errors.addEmptyField("label.hqlinput");
			}
		}
		// criterias
		else if (filter.hasCriteria()) {
			validateCriterias(filter, errors);
		}
		
		validate(errors);
	}
	
	private void validateCriterias(Filter filter, ValidationErrors errors) {
		for (FilterCriterion criterion : filter.getCriteria()) {
			if (isEmpty(criterion.getElement())) {
				errors.addEmptyCriterionField("label.field");
			}
			if (isEmpty(criterion.getOperator())) {
				errors.addEmptyCriterionField("label.operator");
			}
			if (criterion.needsValue() && !criterion.hasValue()) {
				errors.addEmptyCriterionField("label.value");
			}
			else if (criterion.getStringValue() != null &&
					 criterion.getStringValue().length() > getMaxStringLength()) {
				errors.addOverlongObjectField("label.value", "label.criteria", getMaxStringLength());
			}
		}
	}
	
	@Override
	public void validateDelete(Filter filter) throws ValidationException {
		Assert.notNull(filter, C.FILTER);
		final ValidationErrors errors = createValidationErrors(filter);
		
		try (Session session = repository.getSession()) {
			for (FilterDependent<? extends SystemEntity> dependent : getFilterDependents()) {
				for (SystemEntity systemEntity : dependent.findUsage(filter, session)) {
					if (C.FORM.equals(getEntityType(systemEntity))) {
						errors.addError("val.inuse.filterform", systemEntity.getName());
					}
					else {
						unhandledEntity(systemEntity);
					}
				}
			}
		}
		validate(errors);
	}
	
	private List<FilterDependent<? extends SystemEntity>> getFilterDependents() {
		if (filterDependents == null) {
			filterDependents = MiscUtils.castList(getBeans(FilterDependent.class));
		}
		return filterDependents;
	}
	
}
