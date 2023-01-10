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

import java.util.Collections;
import java.util.Set;

public class ValidationException extends Exception {
	
	private static final long serialVersionUID = -8839158339815075607L;
	
	private final transient Set<ValidationError> errors;
	
	public ValidationException(ValidationError error) {
		this.errors = Collections.singleton(error);
	}
	
	public ValidationException(ValidationError error, Throwable cause) {
		super(cause);
		this.errors = Collections.singleton(error);
	}
	
	public ValidationException(ValidationErrors errors) {
		this.errors = errors.getErrors();
	}
	
	public Set<ValidationError> getErrors() {
		return errors;
	}
	
}
