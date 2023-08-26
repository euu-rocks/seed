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

import static org.seed.core.util.CollectionUtils.*;

import java.util.List;

import org.hibernate.Session;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.api.CallbackEventType;
import org.seed.core.api.TransformationFunction;
import org.seed.core.codegen.CodeManager;
import org.seed.core.config.SystemLog;
import org.seed.core.data.FileObject;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.transform.NestedTransformer;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerElement;
import org.seed.core.entity.transform.TransformerFunction;
import org.seed.core.entity.transform.TransformerService;
import org.seed.core.entity.value.event.ValueObjectFunctionContext;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValueObjectTransformer {
	
	@Autowired
	private CodeManager codeManager;
	
	@Autowired
	private TransformerService transformerService;
	
	@Autowired
	private ValueObjectAccess objectAccess;
	
	public void transform(Transformer transformer, ValueObject sourceObject, ValueObject targetObject, Session session) {
		Assert.notNull(transformer, C.TRANSFORMER);
		Assert.notNull(sourceObject, "source object");
		Assert.notNull(targetObject, "target object");
		Assert.notNull(session, C.SESSION);
		Assert.state(sourceObject.getEntityId().equals(transformer.getSourceEntity().getId()), "illegal source object");
		Assert.state(targetObject.getEntityId().equals(transformer.getTargetEntity().getId()), "illegal target object");
		
		callFunctions(transformer, sourceObject, targetObject, session, true);
		transformElements(transformerService.getMainObjectElements(transformer), sourceObject, targetObject);
		for (NestedTransformer nestedTransformer : transformerService.getNestedTransformers(transformer)) {
			if (objectAccess.hasNestedObjects(sourceObject, nestedTransformer.getSourceNested())) {
				for (ValueObject sourceNested : objectAccess.getNestedObjects(sourceObject, nestedTransformer.getSourceNested())) {
					final var targetNested = objectAccess.addNestedInstance(targetObject, nestedTransformer.getTargetNested());
					transformElements(nestedTransformer.getElements(), sourceNested, targetNested);
				}
			}
		}
		callFunctions(transformer, sourceObject, targetObject, session, false);
	}
	
	public void transform(Transformer transformer, ValueObject targetObject, EntityField sourceObjectField, Session session) {
		Assert.notNull(transformer, C.TRANSFORMER);
		Assert.notNull(targetObject, "target object");
		Assert.notNull(sourceObjectField, "source object field");
		Assert.state(targetObject.getEntityId().equals(transformer.getTargetEntity().getId()), "illegal target object");

		final var sourceObject = (ValueObject) objectAccess.getValue(targetObject, sourceObjectField);
		callFunctions(transformer, sourceObject, targetObject, session, true);
		if (transformer.hasElements()) {
			for (TransformerElement element : transformer.getElements()) {
				final var value = sourceObject != null 
									? objectAccess.getValue(sourceObject, element.getSourceField())
									: null;
				objectAccess.setValue(targetObject, element.getTargetField(), value);
			}
		}
		callFunctions(transformer, sourceObject, targetObject, session, false);
	}
	
	private void transformElements(List<TransformerElement> elements, 
								   ValueObject sourceObject, ValueObject targetObject) {
		if (elements != null) {
			for (TransformerElement element : elements) {
				var value = objectAccess.getValue(sourceObject, element.getSourceField());
				if (value instanceof FileObject) {
					value = ((FileObject) value).copy();
				}
				objectAccess.setValue(targetObject, element.getTargetField(), value);
			}
		}
	}
	
	private void callFunctions(Transformer transformer, 
							   ValueObject sourceObject, ValueObject targetObject,
							   Session session, boolean beforeTransformation) {
		if (transformer.hasFunctions()) {
			final var functionContext = new ValueObjectFunctionContext(session, transformer.getModule());
			functionContext.setEventType(beforeTransformation 
											? CallbackEventType.BEFORETRANSFORMATION 
											: CallbackEventType.AFTERTRANSFORMATION);
			filterAndForEach(transformer.getFunctions(), 
							 function -> (beforeTransformation && function.isActiveBeforeTransformation()) || 
							 			 (!beforeTransformation && function.isActiveAfterTransformation()), 
							 function -> callFunction(function, sourceObject, targetObject, functionContext));
		}
	}
	
	@SuppressWarnings("unchecked")
	private void callFunction(TransformerFunction function,
							  ValueObject sourceObject, ValueObject targetObject,
							  ValueObjectFunctionContext functionContext) {
		final var functionClass = codeManager.getGeneratedClass(function);
		Assert.stateAvailable(functionClass, "function class " + function.getGeneratedClass());
		
		try {
			final var functionInstance = (TransformationFunction<ValueObject, ValueObject>) 
					BeanUtils.instantiate(functionClass);
			functionInstance.transform(sourceObject, targetObject, functionContext);
		}
		catch (Exception ex) {
			SystemLog.logError(ex);
			throw new InternalException(ex);
		}
	}
	
}
