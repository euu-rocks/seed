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

import static org.seed.core.util.CollectionUtils.convertedList;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	
	private final TransferService transferService;
	
	private final Class<? extends ValueObject> objectClass;
	
	private final LabelProvider labelProvider;
	
	private final Transfer transfer;
	
	private QueryCursor<ValueObject> cursor;
	
	private List<ValueObject> chunk;
	
	private int index;
	
	private int chunkIndex = -1;
	
	protected AbstractTransferProcessor(TransferService transferService,
										ValueObjectService valueObjectService,
										Class<? extends ValueObject> objectClass,
										LabelProvider labelProvider,
										Transfer transfer) {
		Assert.notNull(valueObjectService, "valueObjectService");
		Assert.notNull(objectClass, C.OBJECTCLASS);
		Assert.notNull(transfer, "labelProvider");
		Assert.notNull(transfer, C.TRANSFER);
		
		this.transferService = transferService;
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
	
	protected String getElementName(TransferElement element) {
		if (element.getName() != null) {
			return element.getName();
		}
		else if (element.getEntityField() != null) {
			return element.getEntityField().getName();
		}
		else if (element.getSystemField() != null) {
			return getEnumLabel(element.getSystemField());
		}
		else {
			throw new IllegalStateException("no entity or system field");
		}
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
	
	@SuppressWarnings("unchecked")
	protected ValueObject importObject(Map<String, Object> map) throws ParseException {
		final ValueObject object = BeanUtils.instantiate(objectClass);
		importObjectFields(object, transferService.getMainObjectElements(transfer), map);
		for (NestedTransfer nested : transferService.getNestedTransfers(transfer)) {
			final var nestedList = (List<Map<String, Object>>) map.get(nested.getNested().getInternalName());
			if (nestedList == null) {
				continue;
			}
			for (Map<String, Object> nestedMap : nestedList) {
				final ValueObject nestedObj = valueObjectService.addNestedInstance(object, nested.getNested());
				importObjectFields(nestedObj, nested.getElements(), nestedMap);
			}
		}
		return object;
	}
	
	private void importObjectFields(ValueObject object, List<TransferElement> elements, Map<String, Object> map) throws ParseException {
		for (TransferElement element : elements) {
			var value = map.get(getElementName(element));
			if (value == null) {
				continue;
			}
			switch (element.getEntityField().getType()) {
				case AUTONUM:
				case BOOLEAN:
				case DOUBLE:
				case INTEGER:
				case LONG:
				case TEXT:
				case TEXTLONG:
					break; // do nothing
					
				case DATE:
					value = parseDate(element, (String) value); 
					break;
					
				case DATETIME:
					value = parseDateTime(element, (String) value);
					break;
					
				case DECIMAL:
					value = labelProvider.parseBigDecimal((String) value);
					break;
					
				default:
					throw new UnsupportedOperationException(element.getEntityField().getType().name());
			}
			ObjectAccess.callSetter(object, element.getEntityField().getInternalName(), value);
		}
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
	
	protected Map<String, Object> exportObjectMap(ValueObject object) {
		final var objMap = new HashMap<String, Object>();
		exportObjectFields(object, transferService.getMainObjectElements(transfer), objMap);
		for (NestedTransfer nested : transferService.getNestedTransfers(transfer)) {
			final var nestedList = new ArrayList<Map<String, Object>>();
			objMap.put(nested.getNested().getInternalName(), nestedList);
			for (ValueObject nestedObj : valueObjectService.getNestedObjects(object, nested.getNested())) {
				final var nestedMap = new HashMap<String, Object>();
				nestedList.add(nestedMap);
				exportObjectFields(nestedObj, nested.getElements(), nestedMap);
			}
		}
		return objMap;
	}
	
	private void exportObjectFields(ValueObject object, List<TransferElement> elements, Map<String, Object> objMap) {
		for (TransferElement element : elements) {
			final var value = ObjectAccess.callGetter(object, element.getEntityField().getInternalName());
			if (value != null) {
				objMap.put(getElementName(element), formatMapValue(element, value));
			}
		}
	}
	
	private Object formatMapValue(TransferElement element, Object value) {
		switch (element.getEntityField().getType()) {
			case AUTONUM:
			case BOOLEAN:
			case DOUBLE:
			case INTEGER:
			case LONG:
			case TEXT:
			case TEXTLONG:
				return value; // do nothing
				
			case DATE:
				return formatDate(element, (Date) value);
			
			case DATETIME:
				return formatDateTime(element, (Date) value);
				
			case DECIMAL:
				return labelProvider.formatBigDecimal((BigDecimal) value);
			
			default:
				throw new UnsupportedOperationException(element.getEntityField().getType().name());
		}
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
	
	protected void saveObjects(List<ValueObject> objects, ImportOptions options, TransferResult result) {
		final var keys = new HashSet<Object>();
		final var identifierField = transfer.getIdentifierField();
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
			if (!(ex instanceof ValidationException ||      // These exceptions are used to
				  ex instanceof MissingKeyException ||      // abort "all or nothing" imports and
				  ex instanceof DuplicateKeyException)) {   // rollback overall transaction
				// only throw unexpected exceptions
				throw new InternalException(ex);
			}
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
	private int updateExistingObject(ValueObject object, EntityField identifierField, ImportOptions options, 
									 TransferResult result, Set<Object> keys, Session session) 
		throws MissingKeyException, DuplicateKeyException, ValidationException {
		
		final var key = valueObjectService.getValue(object, identifierField);
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
		final var existingObject = session != null 
				? valueObjectService.findUnique(identifierField.getEntity(), identifierField, key, session)
				: valueObjectService.findUnique(identifierField.getEntity(), identifierField, key);
		
		if (existingObject != null) {
			copyFields(object, existingObject);
			saveObject(existingObject, options, result, session);
			return 1;
		}
		else {
			// no existing object
			return 0;
		}
	}
	
	private void copyFields(ValueObject sourceObject, ValueObject targetObject) {
		var fieldList = convertedList(transferService.getMainObjectElements(transfer), elem -> elem.getEntityField());
		valueObjectService.copyFields(sourceObject, targetObject, fieldList);
		for (NestedTransfer nested : transferService.getNestedTransfers(transfer)) {
			fieldList = convertedList(nested.getElements(), elem -> elem.getEntityField());
			if (valueObjectService.hasNestedObjects(targetObject, nested.getNested())) {
				valueObjectService.getNestedObjects(targetObject, nested.getNested()).clear();
			}
			if (valueObjectService.hasNestedObjects(sourceObject, nested.getNested())) {
				for (ValueObject nestedObj : valueObjectService.getNestedObjects(sourceObject, nested.getNested())) {
					var nestedInstance = valueObjectService.addNestedInstance(targetObject, nested.getNested());
					valueObjectService.copyFields(nestedObj, nestedInstance, fieldList);
				}
			}
		}
	}
	
	private void saveObject(ValueObject object, ImportOptions options, TransferResult result, Session session) 
		throws ValidationException {
		
		final boolean wasNew = object.isNew();
		try {
			if (session != null) {
				valueObjectService.saveObject(object, session, null);
			}
			else {
				valueObjectService.saveObject(object);
			}
			if (wasNew) {
				result.registerCreatedObject();
			}
			else {
				result.registerUpdatedObject();
			}
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
