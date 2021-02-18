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
import java.util.List;
import java.util.Set;

import org.seed.core.config.Limits;
import org.seed.core.util.NameUtils;
import org.seed.core.util.ObjectAccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public abstract class AbstractSystemEntityValidator<T extends SystemEntity> 
	implements SystemEntityValidator<T> {
	
	@Autowired
	private Limits limits;
	
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
		Assert.notNull(object, "object is null");
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(object.getName())) {
			errors.add(new ValidationError("val.empty.field", "label.name"));
		}
		else if (!isNameLengthAllowed(object.getName())) {
			errors.add(new ValidationError("val.toolong.fieldvalue", "label.name",
										   String.valueOf(getMaxNameLength())));
		}
		validate(errors);
	}
	
	protected int getLimit(String key) {
		Assert.notNull(key, "key is null");
		
		return limits.getLimit(key);
	}
	
	protected int getMaxNameLength() {
		return getLimit("entity.identifier.length");
	}
	
	protected boolean isNameLengthAllowed(String name) {
		Assert.notNull(name, "name is null");
		
		return name.length() <= getMaxNameLength();
	}
	
	protected static Set<ValidationError> createErrorList() {
		return new LinkedHashSet<>();
	}
	
	protected static void validate(Set<ValidationError> errors) throws ValidationException {
		Assert.notNull(errors, "errors is null");
		
		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}
	}
	
	protected static boolean isEmpty(Object object) {
		if (object instanceof String) {
			return !StringUtils.hasText((String) object);
		}
		return object == null;
	}
	
	protected static boolean isZeroOrBelow(Number number) {
		return number == null || number.longValue() <= 0;
	}
	
	@SuppressWarnings("unchecked")
	protected static boolean isNameUnique(String name, 
										  List<? extends SystemObject> ...elementLists) {
		return isUnique(name, "name", elementLists);
	}
	
	protected static boolean isNameAllowed(String name) {
		Assert.notNull(name, "name is null");
		
		return !Character.isDigit(name.charAt(0)) && 
			   !NameUtils.isKeyword(name);
	}
	
	protected static boolean isPositiveInteger(String value) {
		Assert.notNull(value, "value is null");
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
		Assert.notNull(value, "value is null");
		Assert.notNull(fieldName, "fieldName is null");
		Assert.notNull(elementLists, "elementLists is null");
		int counter = 0;
		for (List<? extends SystemObject> elementList : elementLists) {
			if (!ObjectUtils.isEmpty(elementList)) {
				for (SystemObject element : elementList) {
					final Object elementValue = ObjectAccess.callGetter(element, fieldName);
					if ((value instanceof String && elementValue instanceof String &&
						((String) value).equalsIgnoreCase((String) elementValue)) || 
						value.equals(elementValue)) {
						if (++counter > 1) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	protected static String getEntityType(SystemEntity entity) {
		Assert.notNull(entity, "entity is null");
		
		final String packageName = entity.getClass().getPackage().getName();
		return packageName.substring(packageName.lastIndexOf('.') + 1);
	}
	
}
