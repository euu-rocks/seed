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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;

import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;

public class TransferResult {
	
	private final ImportOptions options;
	
	private List<TransferError> errors;
	
	private int successfulTransfers;
	
	private int failedTransfers;
	
	private int createdObjects;
	
	private int updatedObjects;
	
	TransferResult(ImportOptions options) {
		this.options = options;
	}

	public ImportOptions getOptions() {
		return options;
	}
	
	public boolean hasErrors() {
		return errors != null;
	}
	
	public List<TransferError> getErrors() {
		return errors;
	}

	public int getSuccessfulTransfers() {
		return successfulTransfers;
	}
	
	public int getFailedTransfers() {
		return failedTransfers;
	}
	
	public int getCreatedObjects() {
		return createdObjects;
	}

	public int getUpdatedObjects() {
		return updatedObjects;
	}
	
	void registerCreatedObject() {
		this.createdObjects++;
	}
	
	void registerSuccessfulTransfer() {
		successfulTransfers++;
	}
	
	void registerUpdatedObject() {
		this.updatedObjects++;
	}

	void addDuplicateError(String fieldName, String value) {
		addError(new TransferError(TransferErrorType.DUPLICATE, fieldName, value));
	}
	
	void addMissingKeyError(String fieldName) {
		addError(new TransferError(TransferErrorType.MISSINGKEY, fieldName, null));
	}
	
	void addError(ParseException pex, int line) {
		addError(new TransferError(TransferErrorType.UNPARSABLE, pex.getMessage() + " at line " + line));
	}
	
	void addError(PersistenceException pex) {
		if (pex.getCause() instanceof ConstraintViolationException) {
			final String message = pex.getCause().getCause().getMessage();
			if (message.contains("unique constraint")) {
				addError(new TransferError(TransferErrorType.UNIQUEKEY, 
								extractText(message, "Detail: Key ("),
								extractText(message, "=(")));
			}
			else {
				addError(new TransferError(TransferErrorType.DATABASE, message));
			}
		}
	}
	
	void addError(ValidationException vex) {
		for (ValidationError error : vex.getErrors()) {
			addError(new TransferError(TransferErrorType.INVALID, error));
		}
	}
	
	void resetModifiedObjects() {
		createdObjects = 0;
		updatedObjects = 0;
		successfulTransfers = 0;
	}
	
	private void addError(TransferError error) {
		if (errors == null) {
			errors = new ArrayList<>();
		}
		errors.add(error);
		failedTransfers++;
	}
	
	private static String extractText(String text, String key) {
		int idx = text.indexOf(key);
		if (idx < 0) {
			return text;
		}
		idx += key.length();
		return text.substring(idx, text.indexOf(')', idx));
	}
	
}
