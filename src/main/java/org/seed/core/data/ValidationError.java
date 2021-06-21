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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ValidationError implements Serializable {
	
	private static final long serialVersionUID = -1116515324696240537L;

	private final String error;
	
	private final String[] parameters;

	public ValidationError(String error, String ...parameters) {
		this.error = error;
		this.parameters = parameters;
	}

	public String getError() {
		return error;
	}

	public String[] getParameters() {
		return parameters;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(error)
				.append(parameters)
				.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		if (this == object) {
			return true;
		}
		final ValidationError otherError = (ValidationError) object;
		return new EqualsBuilder()
				.append(error, otherError.error)
				.append(parameters, otherError.parameters)
				.isEquals();
	}
	
	public static ValidationError emptyName() {
		return emptyField("label.name");
	}
	
	public static ValidationError emptyField(String name) {
		return new ValidationError("val.empty.field", name);
	}
	
	public static ValidationError illegalField(String name, String value) {
		return new ValidationError("val.illegal.field", name, value);
	}
	
	public static ValidationError overlongName(int maxlength) {
		return new ValidationError("val.toolong.name", String.valueOf(maxlength));
	}
	
	public static ValidationError overlongField(String name, int maxlength) {
		return new ValidationError("val.toolong.fieldvalue", name, String.valueOf(maxlength));
	}
	
	public static ValidationError overlongObjectName(String objectName, int maxlength) {
		return overlongObjectField("label.name", objectName, maxlength);
	}
	
	public static ValidationError overlongObjectField(String fieldName, String objectName, int maxlength) {
		return new ValidationError("val.toolong.objectfieldvalue", fieldName, objectName, String.valueOf(maxlength));
	}
	
	public static ValidationError zeroField(String name) {
		return new ValidationError("val.zero.field", name);
	}
	
}
