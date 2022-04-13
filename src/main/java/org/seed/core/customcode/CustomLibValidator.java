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

import org.seed.Seed;
import org.seed.core.codegen.CodeUtils;
import org.seed.core.codegen.Compiler;
import org.seed.core.codegen.compile.CustomJarException;
import org.seed.core.codegen.compile.InMemoryCompiler;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class CustomLibValidator extends AbstractSystemEntityValidator<CustomLib>
	implements ApplicationContextAware {
	
	@Override
	public void validateSave(CustomLib lib) throws ValidationException {
		Assert.notNull(lib, "lib");
		final ValidationErrors errors = new ValidationErrors();
		if (isEmpty(lib.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(lib.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		else if (isEmpty(lib.getContent()) || isEmpty(lib.getFilename())) {
			errors.addError("val.empty.upload");
		}
		else if (lib.getFilename().length() > 255) {
			errors.addOverlongField("label.filename", 255);
		}
		else if (!CodeUtils.isJarFile(lib.getFilename())) {
			errors.addError("val.illegal.fileformat", "JAR");
		}
		else {
			final String jarError = testCustomLib(lib);
			if (jarError != null) {
				errors.addError("val.illegal.jar", jarError);
			}
		}
		validate(errors);
	}
	
	private String testCustomLib(CustomLib customLib) {
		try {
			((InMemoryCompiler) Seed.getBean(Compiler.class)).testCustomJar(customLib);
		}
		catch (CustomJarException cjex) {
			return cjex.getMessage();
		}
		return null;
	}
	
}
