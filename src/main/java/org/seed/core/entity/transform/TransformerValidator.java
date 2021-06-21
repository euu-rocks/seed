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

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransformerValidator extends AbstractSystemEntityValidator<Transformer> {
	
	@Autowired
	private List<TransformerDependent<? extends SystemEntity>> transformerDependents;
	
	@Override
	public void validateCreate(Transformer transformer) throws ValidationException {
		Assert.notNull(transformer, C.TRANSFORMER);
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(transformer.getSourceEntity())) {
			errors.add(ValidationError.emptyField("label.sourceentity"));
		}
		if (isEmpty(transformer.getTargetEntity())) {
			errors.add(ValidationError.emptyField("label.targetentity"));
		}
		
		validate(errors);
	}
	
	@Override
	public void validateSave(Transformer transformer) throws ValidationException {
		Assert.notNull(transformer, C.TRANSFORMER);
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(transformer.getName())) {
			errors.add(ValidationError.emptyName());
		}
		else if (!isNameLengthAllowed(transformer.getName())) {
			errors.add(ValidationError.overlongName(getMaxNameLength()));
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
		Assert.notNull(transformer, C.TRANSFORMER);
		final Set<ValidationError> errors = createErrorList();
		
		for (TransformerDependent<? extends SystemEntity> dependent : transformerDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(transformer)) {
				if (C.FORM.equals(getEntityType(systemEntity))) {
					errors.add(new ValidationError("val.inuse.transformform", systemEntity.getName()));
				}
				else {
					unhandledEntity(systemEntity);
				}
			}
		}
		
		validate(errors);
	}

}
