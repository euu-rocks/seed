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
import org.seed.core.util.Assert;

import org.springframework.stereotype.Component;

@Component
public class CustomCodeValidator extends AbstractSystemEntityValidator<CustomCode> {
	
	@Override
	public void validateSave(CustomCode code) throws ValidationException {
		Assert.notNull(code, "code");
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(code.getName())) {
			errors.add(ValidationError.emptyName());
		}
		else if (!isNameAllowed(code.getInternalName())) {
			errors.add(ValidationError.illegalField("label.name", code.getName()));
		}
		else if (code.getName().length() > 512) {
			errors.add(ValidationError.overlongName(512));
		}
		if (isEmpty(code.getContent())) {
			errors.add(ValidationError.emptyField("label.sourcecode"));
		}
		
		validate(errors);
	}
	
}
