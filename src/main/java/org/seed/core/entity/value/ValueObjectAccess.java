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

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.codegen.CodeManager;
import org.seed.core.data.SystemField;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.NestedEntity;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.ObjectAccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Component
public class ValueObjectAccess extends ObjectAccess {
	
	@Autowired
	private CodeManager codeManager;
	
	public Object getValue(ValueObject object, EntityField field) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(field, C.FIELD);
		
		return callGetter(object, field.getInternalName());
	}
	
	public void setValue(ValueObject object, EntityField field, Object value) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(field, C.FIELD);
		
		callSetter(object, field.getInternalName(), value);
	}
	
	public void setValue(ValueObject object, SystemField systemField, Object value) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(systemField, "systemField");
		
		callSetter(object, systemField.property, value);
	}
	
	public boolean hasNestedObjects(ValueObject object, NestedEntity nested) {
		return !ObjectUtils.isEmpty(getNestedObjects(object, nested));
	}
	
	@SuppressWarnings("unchecked")
	public List<ValueObject> getNestedObjects(ValueObject object, NestedEntity nested) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(nested, C.NESTED);
		
		return (List<ValueObject>) callGetter(object, nested.getInternalName());
	}
	
	public ValueObject addNestedInstance(ValueObject object, NestedEntity nested) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(nested, C.NESTED);
		
		try {
			final Class<?> nestedClass = codeManager.getGeneratedClass(nested.getNestedEntity());
			final AbstractValueObject nestedObject = (AbstractValueObject) MiscUtils.instantiate(nestedClass);
			nestedObject.setTmpId(System.currentTimeMillis());
			callMethod(object, "add" + StringUtils.capitalize(nested.getInternalName()), nestedObject);
			return nestedObject;
		} 
		catch (Exception ex) {
			throw new InternalException(ex);
		}
	}
	
	public void removeNestedObject(ValueObject object, NestedEntity nested, ValueObject nestedObject) {
		Assert.notNull(object, C.OBJECT);
		Assert.notNull(nested, C.NESTED);
		Assert.notNull(nestedObject, "nestedObject");
		
		callMethod(object, "remove" + StringUtils.capitalize(nested.getInternalName()), nestedObject);
	}
	
}
