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

import org.seed.core.codegen.CodeUtils;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

import org.springframework.stereotype.Component;

@Component
public class CustomLibValidator extends AbstractSystemEntityValidator<CustomLib> {
	
	@Override
	public void validateSave(CustomLib lib) throws ValidationException {
		Assert.notNull(lib, "lib");
		final ValidationErrors errors = createValidationErrors(lib);
		
		if (isEmpty(lib.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(lib.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		else if (isEmpty(lib.getContent()) || isEmpty(lib.getFilename())) {
			errors.addError("val.empty.upload");
		}
		else if (lib.getFilename().length() > getMaxStringLength()) {
			errors.addOverlongField("label.filename", getMaxStringLength());
		}
		else if (!CodeUtils.isJarFile(lib.getFilename())) {
			errors.addError("val.illegal.fileformat", "JAR");
		}
		validate(errors);
	}
	
}
