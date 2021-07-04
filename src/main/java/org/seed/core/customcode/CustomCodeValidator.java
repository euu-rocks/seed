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

import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

import org.springframework.stereotype.Component;

@Component
public class CustomCodeValidator extends AbstractSystemEntityValidator<CustomCode> {
	
	@Override
	public void validateSave(CustomCode code) throws ValidationException {
		Assert.notNull(code, "code");
		final ValidationErrors errors = new ValidationErrors();
		
		if (isEmpty(code.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameAllowed(code.getInternalName())) {
			errors.addIllegalField("label.name", code.getName());
		}
		else if (code.getName().length() > 512) {
			errors.addOverlongName(512);
		}
		if (isEmpty(code.getContent())) {
			errors.addEmptyField("label.sourcecode");
		}
		
		validate(errors);
	}
	
}
