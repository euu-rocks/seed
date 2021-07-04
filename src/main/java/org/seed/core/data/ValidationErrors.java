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
package org.seed.core.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ValidationErrors implements Serializable {

	private static final long serialVersionUID = 5038795516523393867L;
	
	private final Set<ValidationError> errors;
	
	public ValidationErrors() {
		errors = new LinkedHashSet<>();
	}
	
	public ValidationErrors(ValidationError error) {
		errors = Collections.singleton(error);
	}
	
	Set<ValidationError> getErrors() {
		return errors;
	}
	
	public boolean isEmpty() {
		return errors.isEmpty();
	}

	public ValidationErrors addError(String error, String ...parameters) {
		errors.add(new ValidationError(error, parameters));
		return this;
	}
	
	public ValidationErrors addEmptyName() {
		return addEmptyField("label.name");
	}
	
	public ValidationErrors addEmptyField(String name) {
		return addError("val.empty.field", name);
	}
	
	public ValidationErrors addEmptyCriterionField(String name) {
		return addError("val.empty.criterionfield", name);
	}
	
	public ValidationErrors addIllegalField(String name, String value) {
		return addError("val.illegal.field", name, value);
	}
	
	public ValidationErrors addOverlongName(int maxlength) {
		return addError("val.toolong.name", String.valueOf(maxlength));
	}
	
	public ValidationErrors addOverlongField(String name, int maxlength) {
		return addError("val.toolong.fieldvalue", name, String.valueOf(maxlength));
	}
	
	public ValidationErrors addOverlongObjectName(String objectName, int maxlength) {
		return addOverlongObjectField("label.name", objectName, maxlength);
	}
	
	public ValidationErrors addOverlongObjectField(String fieldName, String objectName, int maxlength) {
		return addError("val.toolong.objectfieldvalue", fieldName, objectName, String.valueOf(maxlength));
	}
	
	public ValidationErrors addZeroField(String name) {
		return addError("val.zero.field", name);
	}
	
	public ValidationErrors addNotContains(String term) {
		return addError("val.query.noterm", term);
	}
	
}
