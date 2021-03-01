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
package org.seed.core.entity.value;

import java.util.List;

import org.seed.core.codegen.CodeManager;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.NestedEntity;
import org.seed.core.util.ObjectAccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Component
public class ValueObjectAccess extends ObjectAccess {
	
	@Autowired
	private CodeManager codeManager;
	
	public Object getValue(ValueObject object, EntityField field) {
		Assert.notNull(object, "object is null");
		Assert.notNull(field, "field is null");
		
		return callGetter(object, field.getInternalName());
	}
	
	public void setValue(ValueObject object, EntityField field, Object value) {
		Assert.notNull(object, "object is null");
		Assert.notNull(field, "field is null");
		
		callSetter(object, field.getInternalName(), value);
	}
	
	public Object getReferenceValue(ValueObject object, EntityField referenceField) {
		Assert.notNull(object, "object is null");
		Assert.notNull(referenceField, "referenceField is null");
		Assert.state(referenceField.getType().isReference(), "field is not a reference field");
		
		final ValueObject refrenceObject = (ValueObject) getValue(object, referenceField); 
		return refrenceObject != null 
				? getValue(refrenceObject, referenceField.getReferenceEntityField())
				: null;
	}
	
	public boolean hasNestedObjects(ValueObject object, NestedEntity nested) {
		return !ObjectUtils.isEmpty(getNestedObjects(object, nested));
	}
	
	@SuppressWarnings("unchecked")
	public List<ValueObject> getNestedObjects(ValueObject object, NestedEntity nested) {
		Assert.notNull(object, "object is null");
		Assert.notNull(nested, "nested is null");
		
		return (List<ValueObject>) callGetter(object, nested.getInternalName());
	}
	
	public ValueObject addNestedInstance(ValueObject object, NestedEntity nested) {
		Assert.notNull(object, "object is null");
		Assert.notNull(nested, "nested is null");
		
		try {
			final Class<?> nestedClass = codeManager.getGeneratedClass(nested.getNestedEntity());
			final AbstractValueObject nestedObject = (AbstractValueObject) nestedClass.getDeclaredConstructor().newInstance();
			nestedObject.setTmpId(System.currentTimeMillis());
			callMethod(object, "add" + StringUtils.capitalize(nested.getInternalName()), nestedObject);
			return nestedObject;
		} 
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public void removeNestedObject(ValueObject object, NestedEntity nested, ValueObject nestedObject) {
		Assert.notNull(object, "object is null");
		Assert.notNull(nested, "nested is null");
		Assert.notNull(nestedObject, "nestedObject is null");
		
		callMethod(object, "remove" + StringUtils.capitalize(nested.getInternalName()), nestedObject);
	}
	
	static Object invokeGetIdentifierOrGetter(ValueObject object, String referenceFieldName) {
		try {
			return callMethod(object, "getIdentifier");
		}
		catch (Exception ex) {
			return callGetter(object, referenceFieldName);
		}
	}
	
}
