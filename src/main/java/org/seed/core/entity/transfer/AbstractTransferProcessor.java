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

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.InternalException;
import org.seed.LabelProvider;
import org.seed.core.data.QueryCursor;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.ObjectAccess;

import org.springframework.util.ObjectUtils;

abstract class AbstractTransferProcessor implements TransferProcessor {
	
	private final ValueObjectService valueObjectService;
	
	private final Class<? extends ValueObject> objectClass;
	
	private final LabelProvider labelProvider;
	
	private final Transfer transfer;
	
	private QueryCursor<ValueObject> cursor;
	
	private List<ValueObject> chunk;
	
	private int index;
	
	private int chunkIndex = -1;
	
	protected AbstractTransferProcessor(ValueObjectService valueObjectService,
										Class<? extends ValueObject> objectClass,
										LabelProvider labelProvider,
										Transfer transfer) {
		Assert.notNull(valueObjectService, "valueObjectService");
		Assert.notNull(objectClass, C.OBJECTCLASS);
		Assert.notNull(transfer, "labelProvider");
		Assert.notNull(transfer, C.TRANSFER);
		
		this.valueObjectService = valueObjectService;
		this.objectClass = objectClass;
		this.labelProvider = labelProvider;
		this.transfer = transfer;
	}
	
	@Override
	public final TransferProcessor setCursor(QueryCursor<ValueObject> cursor) {
		Assert.notNull(cursor, C.CURSOR);
		
		this.cursor = cursor;
		return this;
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
				: MiscUtils.CHARSET;
	}
	
	protected String getLabel(String key, String ...params) {
		return labelProvider.getLabel(key, params);
	}
	
	protected String getEnumLabel(Enum<?> enm) {
		return labelProvider.getEnumLabel(enm);
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
	
	protected ValueObject importObject(String[] columns) throws ParseException {
		final ValueObject object = BeanUtils.instantiate(objectClass);
		int idx = 0;
		for (TransferElement element : transfer.getElements()) {
			final var column = columns[idx++];
			if (column != null) {
				final Object value;
				switch (element.getEntityField().getType()) {
					case AUTONUM:
					case TEXT:
					case TEXTLONG:
						value = column;
						break;
					
					case BOOLEAN:
						value = getTrueValue(element).equals(column);
						break;
						
					case DATE:
						value = parseDate(element, column);
						break;
						
					case DATETIME:
						value = parseDateTime(element, column);
						break;
						
					case DECIMAL:
						value = labelProvider.parseBigDecimal(column);
						break;
						
					case DOUBLE:
						value = Double.parseDouble(column);
						break;
						
					case INTEGER:
						value = Integer.parseInt(column);
						break;
						
					case LONG:
						value = Long.parseLong(column);
						break;
						
					default:
						throw new UnsupportedOperationException(element.getEntityField().getType().name());
				}
				if (element.getEntityField() != null) {
					ObjectAccess.callSetter(object, element.getEntityField().getInternalName(), value);
				}
				else {
					ObjectAccess.callSetter(object, element.getSystemField().property, value);
				}
			}
		}
		return object;
	}
	
	protected String[] exportObject(ValueObject object) {
		int idx = 0;
		final var result = new String[transfer.getElements().size()];
		for (TransferElement element : transfer.getElements()) {
			final var value = element.getEntityField() != null
								? ObjectAccess.callGetter(object, element.getEntityField().getInternalName())
								: ObjectAccess.callGetter(object, element.getSystemField().property);
			if (value == null) {
				idx++;
				continue;
			}
			switch (element.getEntityField().getType()) {
				case AUTONUM:
				case TEXT:
				case TEXTLONG:
					result[idx++] = (String) value;
					break;
					
				case BOOLEAN:
					result[idx++] = ((boolean) value) ? getTrueValue(element) : getFalseValue(element);
					break;
				
				case DATE:
					result[idx++] = formatDate(element, (Date) value);
					break;
					
				case DATETIME:
					result[idx++] = formatDateTime(element, (Date) value);
					break;
				
				case DECIMAL:
					result[idx++] = labelProvider.formatBigDecimal((BigDecimal) value);
					break;
					
				case DOUBLE:
				case INTEGER:
				case LONG:
					result[idx++] = value.toString();
					break;
				
				default:
					throw new UnsupportedOperationException(element.getEntityField().getType().name());
			}
		}
		return result;
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
	
	private String getTrueValue(TransferElement element) {
		return element.getValueTrue() != null ? element.getValueTrue() : "1";
	}
	
	private String getFalseValue(TransferElement element) {
		return element.getValueFalse() != null ? element.getValueFalse() : "0";
	}
	
	private String formatDate(TransferElement element, Date date) {
		if (element.getFormat() != null) {
			return formatDate(date, element.getFormat());
		}
		return labelProvider.formatDate(date);
	}
	
	private String formatDateTime(TransferElement element, Date date) {
		if (element.getFormat() != null) {
			return formatDate(date, element.getFormat());
		}
		return labelProvider.formatDateTime(date);
	}
	
	private String formatDate(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}
	
	private Date parseDate(TransferElement element, String text) throws ParseException {
		if (element.getFormat() != null) {
			return parseDate(text, element.getFormat());
		}
		return labelProvider.parseDate(text);
	}
	
	private Date parseDateTime(TransferElement element, String text) throws ParseException {
		if (element.getFormat() != null) {
			return parseDate(text, element.getFormat());
		}
		return labelProvider.parseDateTime(text);
	}
	
	private Date parseDate(String text, String format) throws ParseException {
		return new SimpleDateFormat(format).parse(text);
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
	
	private QueryCursor<ValueObject> getCursor() {
		if (cursor == null) {
			cursor = valueObjectService.createCursor(transfer.getEntity(), 100);
		}
		return cursor;
	}

}
