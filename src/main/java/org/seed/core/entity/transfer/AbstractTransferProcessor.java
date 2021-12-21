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

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.data.Cursor;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.util.Assert;

import org.springframework.util.ObjectUtils;

public abstract class AbstractTransferProcessor implements TransferProcessor {
	
	private final ValueObjectService valueObjectService;
	
	private final Class<? extends ValueObject> objectClass;
	
	private final Transfer transfer;
	
	private Cursor<ValueObject> cursor;
	
	private List<ValueObject> chunk;
	
	private int index;
	
	private int chunkIndex = -1;
	
	public AbstractTransferProcessor(ValueObjectService valueObjectService,
									 Class<? extends ValueObject> objectClass,
									 Transfer transfer) {
		Assert.notNull(valueObjectService, "valueObjectService");
		Assert.notNull(objectClass, C.OBJECTCLASS);
		Assert.notNull(transfer, C.TRANSFER);
		
		this.valueObjectService = valueObjectService;
		this.objectClass = objectClass;
		this.transfer = transfer;
	}
	
	protected Transfer getTransfer() {
		return transfer;
	}
	
	protected Class<? extends ValueObject> getObjectClass() {
		return objectClass;
	}

	protected boolean hasNextObject() {
		return index < getCursor().getTotalCount();
	}
	
	protected Charset getCharset() {
		return transfer != null && transfer.getEncoding() != null 
				? Charset.forName(transfer.getEncoding().charset)
				: Charset.defaultCharset();
	}
	
	protected ValueObject getNextObject() {
		final int newChunkIndex = index / getCursor().getChunkSize();
		if (chunkIndex != newChunkIndex) {
			chunkIndex = newChunkIndex;
			getCursor().setChunkIndex(newChunkIndex);
			chunk = valueObjectService.loadChunk(getCursor());
		}
		return chunk.get(index++ % getCursor().getChunkSize());
	}
	
	protected void saveObjects(List<ValueObject> objects, ImportOptions options, 
							   TransferResult result) {
		final Set<Object> keys = new HashSet<>();
		final EntityField identifierField = transfer.getIdentifierField();
		if (options.isModifyExisting()) {
			Assert.stateAvailable(identifierField, "identifier field");
		}
		Session session = null;
		Transaction tx = null;
		try {
			if (options.isAllOrNothing()) {
				session = valueObjectService.openSession();
				tx = session.beginTransaction();
			}
			for (ValueObject object : objects) {
				processObject(object, options, identifierField, result, keys, session);
			}
			if (tx != null) {
				tx.commit();
			}
		}
		catch (Exception ex) {
			if (tx != null) {
				tx.rollback();
			}
			throw new InternalException(ex);
		}
		finally {
			if (session != null) {
				session.close();
			}
			if (options.isAllOrNothing() && result.hasErrors()) {
				result.resetModifiedObjects();
			}
		}
	}
	
	private void processObject(ValueObject object, ImportOptions options, EntityField identifierField,
				TransferResult result, Set<Object> keys, Session session) 
		throws MissingKeyException, DuplicateKeyException, ValidationException {
		
		int updateResult = 0;
		if (options.isModifyExisting()) {
			updateResult = updateExistingObject(object, identifierField, options, 
												result, keys, session);
		}
		if (updateResult == 0 && options.isCreateIfNew()) {
			saveObject(object, options, result, session);
		}
	}
	
	// -1 = error, 0 = no existing object, 1 = existing object updated
	private int updateExistingObject(ValueObject object, EntityField identifierField,
			 ImportOptions options, TransferResult result,
			 Set<Object> keys, Session session) 
		throws MissingKeyException, DuplicateKeyException, ValidationException {
		
		final Object key = valueObjectService.getValue(object, identifierField);
		// check key
		if (ObjectUtils.isEmpty(key)) {
			result.addMissingKeyError(identifierField.getName());
			if (options.isAllOrNothing()) {
				throw new MissingKeyException(identifierField.getName());
			}
			return -1;
		}
		// check duplicate
		if (keys.contains(key)) {
			result.addDuplicateError(identifierField.getName(), key.toString());
			if (options.isAllOrNothing()) {
				throw new DuplicateKeyException(key.toString());
			}
			return -1;
		}
		keys.add(key);

		// load existing object
		final ValueObject existingObject = session != null 
				? valueObjectService.findUnique(identifierField.getEntity(), identifierField, key, session)
				: valueObjectService.findUnique(identifierField.getEntity(), identifierField, key);
		if (existingObject == null) {
			// no object exits
			return 0;
		}

		valueObjectService.copyFields(object, existingObject, transfer.getElementFields());
		saveObject(existingObject, options, result, session);
		return 1;
	}
	
	private void saveObject(ValueObject object, ImportOptions options, TransferResult result, Session session) 
		throws ValidationException {
		try {
			if (session != null) {
				valueObjectService.saveObject(object, session, null);
			}
			else {
				valueObjectService.saveObject(object);
			}
			result.registerCreatedObject();
			result.registerSuccessfulTransfer();
		}
		catch (ValidationException vex) {
			result.addError(vex);
			if (options.isAllOrNothing()) {
				throw vex;
			}
		}
		catch (PersistenceException pex) {
			result.addError(pex);
			if (options.isAllOrNothing()) {
				throw pex;
			}
		}
	}
	
	private Cursor<ValueObject> getCursor() {
		if (cursor == null) {
			cursor = valueObjectService.createCursor(transfer.getEntity(), 500);
		}
		return cursor;
	}

}
