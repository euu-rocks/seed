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

import org.seed.core.data.Cursor;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public abstract class AbstractTransferProcessor implements TransferProcessor {
	
	private final ValueObjectService valueObjectService;
	
	private final Class<? extends ValueObject> objectClass;
	
	private final Transfer transfer;
	
	private Cursor cursor;
	
	private List<ValueObject> chunk;
	
	private int index;
	
	private int chunkIndex = -1;
	
	public AbstractTransferProcessor(ValueObjectService valueObjectService,
									 Class<? extends ValueObject> objectClass,
									 Transfer transfer) {
		Assert.notNull(valueObjectService, "valueObjectService is null");
		Assert.notNull(objectClass, "objectClass is null");
		Assert.notNull(transfer, "transfer is null");
		
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
		final int chunkIndex = index / getCursor().getChunkSize();
		if (this.chunkIndex != chunkIndex) {
			this.chunkIndex = chunkIndex;
			getCursor().setChunkIndex(chunkIndex);
			chunk = valueObjectService.loadChunk(getCursor());
		}
		return chunk.get(index++ % getCursor().getChunkSize());
	}
	
	protected void saveObjects(List<ValueObject> objects, ImportOptions options, TransferResult result) {
		final Set<Object> keys = new HashSet<>();
		final Entity entity = transfer.getEntity();
		final EntityField identifierField = transfer.getIdentifierField();
		if (options.isModifyExisting()) {
			Assert.state(identifierField != null, "no identifier field");
		}
		Session session = null;
		Transaction tx = null;
		Exception ex = null;
		try {
			if (options.isAllOrNothing()) {
				session = valueObjectService.openSession();
				tx = session.beginTransaction();
			}
			for (ValueObject object : objects) {
				// update existing object
				if (options.isModifyExisting()) {
					final Object key = valueObjectService.getValue(object, identifierField);
					// check key
					if (ObjectUtils.isEmpty(key)) {
						result.addMissingKeyError(identifierField.getName());
						if (options.isAllOrNothing()) {
							ex = new RuntimeException("missingkey");
							break;
						}
						continue;
					}
					// check duplicate
					if (keys.contains(key)) {
						result.addDuplicateError(identifierField.getName(), key.toString());
						if (options.isAllOrNothing()) {
							ex = new RuntimeException("duplicate");
							break;
						}
						continue;
					}
					keys.add(key);
					
					final ValueObject existingObject = session != null 
							? valueObjectService.findUnique(session, entity, identifierField, key)
							: valueObjectService.findUnique(entity, identifierField, key);
					// if object exist
					if (existingObject != null) {
						valueObjectService.copyFields(object, existingObject, transfer.getElementFields());
						try {
							saveObject(existingObject, session);
							result.incUpdatedObjects();
							result.incSuccessfulTransfers();
						}
						catch (ValidationException vex) {
							result.addError(vex);
							if (options.isAllOrNothing()) {
								ex = vex;
								break;
							}
						}
						catch (PersistenceException pex) {
							result.addError(pex);
							if (options.isAllOrNothing()) {
								ex = pex;
								break;
							}
						}
						continue;
					}
				}
				
				// create new objects
				if (options.isCreateIfNew()) {
					try {
						saveObject(object, session);
						result.incCreatedObjects();
						result.incSuccessfulTransfers();
					}
					catch (ValidationException vex) {
						result.addError(vex);
						if (options.isAllOrNothing()) {
							ex = vex;
							break;
						}
					}
					catch (PersistenceException pex) {
						result.addError(pex);
						if (options.isAllOrNothing()) {
							ex = pex;
							break;
						}
					}
				}
			}
		}
		catch (Exception e) {
			ex = e;
			throw e;
		}
		finally {
			if (tx != null) {
				try {
					if (ex != null) {
						tx.rollback();
					}
					else {
						tx.commit();
					}
				}
				catch (PersistenceException pex) {
					result.addError(pex);
				}
				catch (Exception e) {}
			}
			if (session != null) {
				try {
					session.close();
				}
				catch (Exception e) {}
			}
			if (options.isAllOrNothing() && result.hasErrors()) {
				result.resetModifiedObjects();
			}
		}
	}
	
	private Cursor getCursor() {
		if (cursor == null) {
			cursor = valueObjectService.createCursor(transfer.getEntity(), null);
		}
		return cursor;
	}
	
	private void saveObject(ValueObject object, Session session) throws ValidationException {
		if (session != null) {
			valueObjectService.saveObject(object, session, null);
		}
		else {
			valueObjectService.saveObject(object);
		}
	}
	
}
