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
package org.seed.core.rest;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

import org.springframework.stereotype.Component;

@Component
public class RestValidator extends AbstractSystemEntityValidator<Rest> {
	
	private static final String LABEL_FUNCTION = "label.function";
	private static final String LABEL_FUNCTIONNAME = "label.functionname";
	private static final String LABEL_MAPPING = "label.mapping";

	@Override
	public void validateSave(Rest rest) throws ValidationException {
		Assert.notNull(rest, C.REST);
		final ValidationErrors errors = new ValidationErrors();
		
		// name
		if (isEmpty(rest.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(rest.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		else if (!isRestNameAllowed(rest.getInternalName()) && 
				 isEmpty(rest.getMapping())) {
			errors.addIllegalName(rest.getInternalName());
		}
		
		// mapping
		if (!isEmpty(rest.getMapping())) {
			if (!isNameLengthAllowed(rest.getMapping())) {
				errors.addOverlongField(LABEL_MAPPING, getMaxNameLength());
			}
			else if (!rest.getMapping().startsWith("/")) {
				errors.addNotStartsWith(LABEL_MAPPING, "/");
			}
			else if (!isRestNameAllowed(rest.getMapping().substring(1))) {
				errors.addIllegalField(LABEL_MAPPING, rest.getMapping());
			}
		}
		// functions
		if (rest.hasFunctions()) {
			validateFunctions(rest, errors);
		}
		
		validate(errors);
	}
	
	@SuppressWarnings("unchecked")
	private void validateFunctions(Rest rest, ValidationErrors errors) {
		for (RestFunction function : rest.getFunctions()) {
			// method
			if (isEmpty(function.getMethod())) {
				errors.addEmptyField("label.method");
			}
			
			// name
			if (isEmpty(function.getName())) {
				errors.addEmptyField(LABEL_FUNCTIONNAME);
			}
			else if (!isNameLengthAllowed(function.getName())) {
				errors.addOverlongObjectName(LABEL_FUNCTION, getMaxNameLength());
			}
			else if (!isNameAllowed(function.getInternalName())) {
				errors.addIllegalField(LABEL_FUNCTIONNAME, function.getName());
			}
			else if (!isNameUnique(function.getName(), rest.getFunctions())) {
				errors.addError("val.ambiguous.functionname", function.getName());
			}
			
			// mapping
			if (!isEmpty(function.getMapping())) {
				if (!isNameLengthAllowed(function.getMapping())) {
					errors.addOverlongObjectField(LABEL_MAPPING, LABEL_FUNCTION, getMaxNameLength());
				}
				else if (!function.getMapping().startsWith("/")) {
					errors.addNotStartsWith(LABEL_MAPPING, LABEL_FUNCTION, "/");
				}
				else if (!isUnique(function.getMapping(), "mapping", rest.getFunctions())) {
					errors.addError("val.ambiguous.functionmapping", function.getMapping());
				}
			}
		}
	}
	
	private static boolean isRestNameAllowed(String name) {
		return isNameAllowed(name) &&					// prevent illegal package name
				!(name.equalsIgnoreCase(C.ENTITY) 	 || // prevent ambiguous mappings
				  name.equalsIgnoreCase(C.FILTER) 	 ||
				  name.equalsIgnoreCase(C.TASK)   	 ||
				  name.equalsIgnoreCase(C.OBJECT))   ||
				  name.equalsIgnoreCase("transform");
				  
		
	}
	
}
