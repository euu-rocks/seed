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
import java.util.Set;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FilterValidator extends AbstractSystemEntityValidator<Filter> {
	
	@Autowired
	private List<FilterDependent<? extends SystemEntity>> filterDependents;
	
	@Override
	public void validateCreate(Filter filter) throws ValidationException {
		Assert.notNull(filter, C.FILTER);
		
		if (isEmpty(filter.getEntity())) {
			throw new ValidationException(ValidationError.emptyField("label.entity"));
		}
	}
	
	@Override
	public void validateSave(Filter filter) throws ValidationException {
		Assert.notNull(filter, C.FILTER);
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(filter.getName())) {
			errors.add(ValidationError.emptyName());
		}
		else if (!isNameLengthAllowed(filter.getName())) {
			errors.add(ValidationError.overlongName(getMaxNameLength()));
		}
		
		// HQL
		if (((FilterMetadata) filter).isHqlInput()) {
			if (isEmpty(filter.getHqlQuery())) {
				errors.add(ValidationError.emptyField("label.hqlinput"));
			}
		}
		// criterias
		else if (filter.hasCriteria()) {
			validateCriterias(filter, errors);
		}
		
		validate(errors);
	}
	
	private void validateCriterias(Filter filter, Set<ValidationError> errors) {
		for (FilterCriterion criterion : filter.getCriteria()) {
			if (isEmpty(criterion.getElement())) {
				errors.add(emptyCriterionField("label.field"));
			}
			if (isEmpty(criterion.getOperator())) {
				errors.add(emptyCriterionField("label.oparator"));
			}
			if (criterion.needsValue() && !criterion.hasValue()) {
				errors.add(emptyCriterionField("label.value"));
			}
			else if (criterion.getStringValue() != null &&
					 criterion.getStringValue().length() > getMaxStringLength()) {
				errors.add(ValidationError.overlongObjectField("label.value", "label.criteria", 
															   getMaxStringLength()));
			}
		}
	}
	
	@Override
	public void validateDelete(Filter filter) throws ValidationException {
		Assert.notNull(filter, C.FILTER);
		final Set<ValidationError> errors = createErrorList();
		
		for (FilterDependent<? extends SystemEntity> dependent : filterDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(filter)) {
				if (C.FORM.equals(getEntityType(systemEntity))) {
					errors.add(new ValidationError("val.inuse.filterform", systemEntity.getName()));
				}
				else {
					unhandledEntity(systemEntity);
				}
			}
		}
		
		validate(errors);
	}
	
	private static ValidationError emptyCriterionField(String name) {
		return new ValidationError("val.empty.criterionfield", name);
	}
	
}
