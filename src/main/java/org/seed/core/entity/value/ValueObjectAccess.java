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

import static org.seed.core.util.CollectionUtils.notEmpty;

import java.util.List;
import java.util.Set;

import org.seed.C;
import org.seed.core.codegen.CodeManager;
import org.seed.core.data.SystemField;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.NestedEntity;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ValueObjectAccess {
	
	private static final String PRE_ADD 	= "add";
	private static final String PRE_REMOVE	= "remove";
	
	@Autowired
	private CodeManager codeManager;
	
	public Object getValue(ValueObject object, EntityField entityField) {
		Assert.notNull(entityField, C.FIELD);
		
		return BeanUtils.callGetter(object, entityField.getInternalName());
	}
	
	public Object getValue(ValueObject object, SystemField systemField) {
		Assert.notNull(systemField, C.FIELD);
		
		return BeanUtils.callGetter(object, systemField.property);
	}
	
	public void setValue(ValueObject object, EntityField field, Object value) {
		Assert.notNull(field, C.FIELD);
		
		BeanUtils.callSetter(object, field.getInternalName(), value);
	}
	
	public void setValue(ValueObject object, SystemField systemField, Object value) {
		Assert.notNull(systemField, "system field");
		
		BeanUtils.callSetter(object, systemField.property, value);
	}
	
	public boolean hasNestedObjects(ValueObject object, NestedEntity nested) {
		return notEmpty(getNestedObjects(object, nested));
	}
	
	public List<ValueObject> getNestedObjects(ValueObject object, NestedEntity nested) {
		Assert.notNull(nested, C.NESTED);
		
		return MiscUtils.castList(BeanUtils.callGetter(object, nested.getInternalName()));
	}
	
	public void setNestedObjects(ValueObject object, NestedEntity nested, List<ValueObject> nestedList) {
		Assert.notNull(nested, C.NESTED);
		
		BeanUtils.callSetter(object, nested.getInternalName(), nestedList);
	}
	
	public ValueObject addNestedInstance(ValueObject object, NestedEntity nested) {
		Assert.notNull(nested, C.NESTED);
		
		final var nestedClass = codeManager.getGeneratedClass(nested.getNestedEntity());
		final var nestedObject = (AbstractValueObject) BeanUtils.instantiate(nestedClass);
		nestedObject.setTmpId(System.currentTimeMillis());
		BeanUtils.callMethod(object, PRE_ADD.concat(StringUtils.capitalize(nested.getInternalName())), nestedObject);
		return nestedObject;
	}
	
	public void removeNestedObject(ValueObject object, NestedEntity nested, ValueObject nestedObject) {
		Assert.notNull(nested, C.NESTED);
		Assert.notNull(nestedObject, "nested object");
		
		BeanUtils.callMethod(object, PRE_REMOVE.concat(StringUtils.capitalize(nested.getInternalName())), nestedObject);
	}
	
	public boolean hasRelatedObjects(ValueObject object, EntityRelation relation) {
		return notEmpty(getRelatedObjects(object, relation));
	}
	
	public Set<ValueObject> getRelatedObjects(ValueObject object, EntityRelation relation) {
		Assert.notNull(relation, C.RELATION);
		
		return MiscUtils.castSet(BeanUtils.callGetter(object, relation.getInternalName()));
	}
	
	public void setRelatedObjects(ValueObject object, EntityRelation relation, Set<ValueObject> relatedList) {
		Assert.notNull(relation, C.RELATION);
		
		BeanUtils.callSetter(object, relation.getInternalName(), relatedList);
	}
	
	public void addRelatedObject(ValueObject object, EntityRelation relation, ValueObject relatedObject) {
		Assert.notNull(relation, C.RELATION);
		Assert.notNull(relatedObject, "related object");
		
		BeanUtils.callMethod(object, PRE_ADD.concat(StringUtils.capitalize(relation.getInternalName())), relatedObject);
	}
	
	public void removeRelatedObject(ValueObject object, EntityRelation relation, ValueObject relatedObject) {
		Assert.notNull(relation, C.RELATION);
		Assert.notNull(relatedObject, "related object");
		
		BeanUtils.callMethod(object, PRE_REMOVE.concat(StringUtils.capitalize(relation.getInternalName())), relatedObject);
	}
	
}
