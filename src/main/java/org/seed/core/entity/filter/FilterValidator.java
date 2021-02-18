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

import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class FilterValidator extends AbstractSystemEntityValidator<Filter> {
	
	@Autowired
	private List<FilterDependent> filterDependents;
	
	@Override
	public void validateCreate(Filter filter) throws ValidationException {
		Assert.notNull(filter, "filter is null");
		
		if (isEmpty(filter.getEntity())) {
			throw new ValidationException(new ValidationError("val.empty.field", "label.entity"));
		}
	}
	
	@Override
	public void validateSave(Filter filter) throws ValidationException {
		Assert.notNull(filter, "filter is null");
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(filter.getName())) {
			errors.add(new ValidationError("val.empty.field", "label.name"));
		}
		else if (!isNameLengthAllowed(filter.getName())) {
			errors.add(new ValidationError("val.toolong.name", String.valueOf(getMaxNameLength())));
		}
		
		// HQL
		if (((FilterMetadata) filter).isHqlInput()) {
			if (isEmpty(filter.getHqlQuery())) {
				errors.add(new ValidationError("val.empty.field", "label.hqlinput"));
			}
		}
		
		// criterias
		else if (filter.hasCriteria()) {
			for (FilterCriterion criterion : filter.getCriteria()) {
				if (isEmpty(criterion.getElement())) {
					errors.add(new ValidationError("val.empty.criterionfield", "label.field"));
				}
				if (isEmpty(criterion.getOperator())) {
					errors.add(new ValidationError("val.empty.criterionfield", "label.oparator"));
				}
				if (criterion.needsValue() && !criterion.hasValue()) {
					errors.add(new ValidationError("val.empty.criterionfield", "label.value"));
				}
				else if (criterion.getStringValue() != null &&
						 criterion.getStringValue().length() > getLimit("entity.stringfield.length")) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.value", "label.criteria", 
												   String.valueOf(getLimit("entity.stringfield.length"))));
				}
			}
		}
		
		validate(errors);
	}
	
	@Override
	public void validateDelete(Filter filter) throws ValidationException {
		Assert.notNull(filter, "filter is null");
		final Set<ValidationError> errors = createErrorList();
		
		for (FilterDependent dependent : filterDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(filter)) {
				switch (getEntityType(systemEntity)) {
				case "form":
					errors.add(new ValidationError("val.inuse.filterform", systemEntity.getName()));
					break;
				default:
					throw new IllegalStateException("unhandled entity: " + getEntityType(systemEntity));
				}
			}
		}
		
		validate(errors);
	}
	
}
