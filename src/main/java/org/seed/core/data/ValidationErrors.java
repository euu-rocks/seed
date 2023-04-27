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

import java.util.LinkedHashSet;
import java.util.Set;

public final class ValidationErrors {

	private static final String LABEL_NAME = "label.name";
	
	private final SystemEntity systemEntity;
	
	private Set<ValidationError> errors;
	
	public ValidationErrors() {
		this.systemEntity = null;
	}
	
	public ValidationErrors(SystemEntity systemEntity) {
		this.systemEntity = systemEntity;
	}

	public SystemEntity getSystemEntity() {
		return systemEntity;
	}

	Set<ValidationError> getErrors() {
		return errors;
	}
	
	public boolean isEmpty() {
		return errors == null;
	}

	public ValidationErrors addError(String error, String ...parameters) {
		if (errors == null) {
			errors = new LinkedHashSet<>();
		}
		errors.add(new ValidationError(systemEntity, error, parameters));
		return this;
	}
	
	public ValidationErrors addEmptyName() {
		return addEmptyField(LABEL_NAME);
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
	
	public ValidationErrors addIllegalFieldValue(String name) {
		return addError("val.illegal.fieldvalue", name);
	}
	
	public ValidationErrors addIllegalName(String value) {
		return addIllegalField(LABEL_NAME, value);
	}
	
	public ValidationErrors addOverlongName(int maxlength) {
		return addError("val.toolong.name", String.valueOf(maxlength));
	}
	
	public ValidationErrors addOverlongField(String name, int maxlength) {
		return addError("val.toolong.fieldvalue", name, String.valueOf(maxlength));
	}
	
	public ValidationErrors addOverlongObjectName(String objectName, int maxlength) {
		return addOverlongObjectField(LABEL_NAME, objectName, maxlength);
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
	
	public ValidationErrors addNotStartsWith(String name, String term) {
		return addError("val.notstartswith.field", name, term);
	}
	
	public ValidationErrors addNotStartsWith(String name, String objectName, String term) {
		return addError("val.notstartswith.objectfield", name, objectName, term);
	}
	
}
