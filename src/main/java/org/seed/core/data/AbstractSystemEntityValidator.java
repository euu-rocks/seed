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

import java.util.List;

import org.seed.C;
import org.seed.core.config.Limits;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;
import org.seed.core.util.NameUtils;
import org.seed.core.util.ObjectAccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public abstract class AbstractSystemEntityValidator<T extends SystemEntity> 
	implements SystemEntityValidator<T>, ApplicationContextAware {
	
	@Autowired
	private Limits limits;
	
	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public void validateCreate(T object) throws ValidationException {
		// do nothing by default
	}
	
	@Override
	public void validateDelete(T object) throws ValidationException {
		// do nothing by default
	}
	
	@Override
	public void validateSave(T object) throws ValidationException {
		Assert.notNull(object, C.OBJECT);
		
		final ValidationErrors errors = new ValidationErrors();
		if (isEmpty(object.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(object.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		validate(errors);
	}
	
	protected <B> List<B> getBeans(Class<B> type) {
		return BeanUtils.getBeans(applicationContext, type);
	}
	
	protected int getLimit(String key) {
		Assert.notNull(key, C.KEY);
		
		return limits.getLimit(key);
	}
	
	protected int getMaxNameLength() {
		return getLimit(Limits.LIMIT_IDENTIFIER_LENGTH);
	}
	
	protected int getMaxStringLength() {
		return getLimit(Limits.LIMIT_TEXT_LENGTH);
	}
	
	protected boolean isNameLengthAllowed(String name) {
		Assert.notNull(name, C.NAME);
		
		return name.length() <= getMaxNameLength();
	}
	
	protected static void validate(ValidationErrors errors) throws ValidationException {
		Assert.notNull(errors, "errors");
		
		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
	}
	
	protected static boolean isEmpty(Object object) {
		return ObjectUtils.isEmpty(object);
	}
	
	protected static boolean isEmpty(String str) {
		return !StringUtils.hasText(str);
	}
	
	protected static boolean isZeroOrBelow(Number number) {
		return number == null || number.longValue() <= 0;
	}
	
	@SuppressWarnings("unchecked")
	protected static boolean isNameUnique(String name, 
										  List<? extends SystemObject> ...elementLists) {
		return isUnique(name, C.NAME, elementLists);
	}
	
	protected static boolean isNameAllowed(String name) {
		Assert.notNull(name, C.NAME);
		
		return !Character.isDigit(name.charAt(0)) && 
			   !NameUtils.isKeyword(name);
	}
	
	protected static boolean isPositiveInteger(String value) {
		Assert.notNull(value, C.VALUE);
		try {
			return !isZeroOrBelow(Integer.parseInt(value));
		}
		catch (NumberFormatException ex) {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected static boolean isUnique(Object value, String fieldName, 
									  List<? extends SystemObject> ...elementLists) {
		Assert.notNull(value, C.VALUE);
		Assert.notNull(fieldName, "fieldName");
		Assert.notNull(elementLists, "elementLists");
		
		int occurrences = 0;
		for (List<? extends SystemObject> elementList : elementLists) {
			if (elementList != null) {
				for (SystemObject element : elementList) {
					if (checkElement(value, fieldName, element) && 
						++occurrences > 1) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private static boolean checkElement(Object value, String fieldName, SystemObject element) {
		final Object elementValue = ObjectAccess.callGetter(element, fieldName);
		return (value instanceof String && elementValue instanceof String &&
				((String) value).equalsIgnoreCase((String) elementValue)) || 
				value.equals(elementValue);
	}
	
	protected static String getEntityType(SystemEntity entity) {
		Assert.notNull(entity, C.ENTITY);
		
		final String packageName = entity.getClass().getPackage().getName();
		return packageName.substring(packageName.lastIndexOf('.') + 1);
	}
	
	protected static void unhandledEntity(SystemEntity entity) {
		Assert.stateIllegal("unhandled entity: " + getEntityType(entity));
	}
	
}
