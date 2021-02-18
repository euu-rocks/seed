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
package org.seed.core.entity.transfer;

import org.seed.core.data.ValidationError;

public class TransferError {
	
	public final TransferErrorType type;
	
	public final ValidationError validationError; 
	
	public final String message;
	
	public final String fieldName;
	
	public final String value;

	public TransferError(TransferErrorType type, String fieldName, String value) {
		validationError = null;
		message = null;
		this.type = type;
		this.fieldName = fieldName;
		this.value = value;
	}

	public TransferError(TransferErrorType type, ValidationError validationError) {
		message = null;
		fieldName = null;
		value = null;
		this.type = type;
		this.validationError = validationError;
	}

	public TransferError(TransferErrorType type, String message) {
		validationError = null;
		fieldName = null;
		value = null;
		this.type = type;
		this.message = message;
	}

	public TransferErrorType getType() {
		return type;
	}
	
}
