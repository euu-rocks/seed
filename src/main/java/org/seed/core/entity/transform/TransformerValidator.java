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
public class TransformerValidator extends AbstractSystemEntityValidator<Transformer> {
	
	@Autowired
	private List<TransformerDependent> transformerDependents;
	
	public void validateCreate(Transformer transformer) throws ValidationException {
		Assert.notNull(transformer, "transformer is null");
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(transformer.getSourceEntity())) {
			errors.add(new ValidationError("val.empty.field", "label.sourceentity"));
		}
		if (isEmpty(transformer.getTargetEntity())) {
			errors.add(new ValidationError("val.empty.field", "label.targetentity"));
		}
		
		validate(errors);
	}
	
	@Override
	public void validateSave(Transformer transformer) throws ValidationException {
		Assert.notNull(transformer, "transformer is null");
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(transformer.getName())) {
			errors.add(new ValidationError("val.empty.field", "label.name"));
		}
		else if (!isNameLengthAllowed(transformer.getName())) {
			errors.add(new ValidationError("val.toolong.name", String.valueOf(getMaxNameLength())));
		}
		
		// elements
		if (transformer.hasElements()) {
			for (TransformerElement element : transformer.getElements()) {
				if (isEmpty(element.getSourceField())) {
					errors.add(new ValidationError("val.empty.elementfield", "label.sourcefield"));
				}
				if (isEmpty(element.getTargetField())) {
					errors.add(new ValidationError("val.empty.elementfield", "label.targetfield"));
				}
			}
		}
		
		validate(errors);
	}
	
	@Override
	public void validateDelete(Transformer transformer) throws ValidationException {
		Assert.notNull(transformer, "transformer is null");
		final Set<ValidationError> errors = createErrorList();
		
		for (TransformerDependent dependent : transformerDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(transformer)) {
				switch (getEntityType(systemEntity)) {
				case "form":
					errors.add(new ValidationError("val.inuse.transformform", systemEntity.getName()));
					break;
				default:
					throw new IllegalStateException("unhandled entity: " + getEntityType(systemEntity));
				}
			}
		}
		
		validate(errors);
	}

}
