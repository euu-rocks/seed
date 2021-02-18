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
package org.seed.core.customcode;

import java.util.Set;

import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class CustomCodeValidator extends AbstractSystemEntityValidator<CustomCode> {

	public void validateSave(CustomCode code) throws ValidationException {
		Assert.notNull(code, "code is null");
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(code.getName())) {
			errors.add(new ValidationError("val.empty.field", "label.name"));
		}
		else if (!isNameAllowed(code.getInternalName())) {
			errors.add(new ValidationError("val.illegal.field", "label.name", code.getName()));
		}
		if (isEmpty(code.getContent())) {
			errors.add(new ValidationError("val.empty.field", "label.sourcecode"));
		}
		
		validate(errors);
	}
	
}
