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

import org.hibernate.Session;
import org.seed.C;
import org.seed.InternalException;
import org.seed.core.api.TransformationFunction;
import org.seed.core.codegen.CodeManager;
import org.seed.core.codegen.GeneratedCode;
import org.seed.core.config.SessionProvider;
import org.seed.core.data.FileObject;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.transform.NestedTransformer;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerElement;
import org.seed.core.entity.transform.TransformerFunction;
import org.seed.core.entity.transform.TransformerService;
import org.seed.core.entity.value.event.ValueObjectFunctionContext;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

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
	
	@Autowired
	private SessionProvider sessionFactoryProvider;
	
	public void transform(Transformer transformer, ValueObject sourceObject, ValueObject targetObject) {
		Assert.notNull(transformer, C.TRANSFORMER);
		Assert.notNull(sourceObject, "sourceObject");
		Assert.notNull(targetObject, "targetObject");
		Assert.state(sourceObject.getEntityId().equals(transformer.getSourceEntity().getId()), "illegal source object");
		Assert.state(targetObject.getEntityId().equals(transformer.getTargetEntity().getId()), "illegal target object");
		
		callFunctions(transformer, sourceObject, targetObject, true);
		transformElements(transformerService.getMainObjectElements(transformer), sourceObject, targetObject);
		for (NestedTransformer nestedTransformer : transformerService.getNestedTransformers(transformer)) {
			for (ValueObject sourceNested : objectAccess.getNestedObjects(sourceObject, nestedTransformer.getSourceNested())) {
				final ValueObject targetNested = objectAccess.addNestedInstance(targetObject, nestedTransformer.getTargetNested());
				transformElements(nestedTransformer.getElements(), sourceNested, targetNested);
			}
		}
		callFunctions(transformer, sourceObject, targetObject, false);
	}
	
	public void transform(Transformer transformer, ValueObject targetObject, EntityField sourceObjectField) {
		Assert.notNull(transformer, C.TRANSFORMER);
		Assert.notNull(targetObject, "targetObject");
		Assert.notNull(sourceObjectField, "sourceObjectField");
		Assert.state(targetObject.getEntityId().equals(transformer.getTargetEntity().getId()), "illegal target object");

		final ValueObject sourceObject = (ValueObject) objectAccess.getValue(targetObject, sourceObjectField);
		callFunctions(transformer, sourceObject, targetObject, true);
		if (transformer.hasElements()) {
			for (TransformerElement element : transformer.getElements()) {
				final Object value = sourceObject == null ? null : 
					objectAccess.getValue(sourceObject, element.getSourceField());
				objectAccess.setValue(targetObject, element.getTargetField(), value);
			}
		}
		callFunctions(transformer, sourceObject, targetObject, false);
	}
	
	private void transformElements(List<TransformerElement> elements, 
								   ValueObject sourceObject, ValueObject targetObject) {
		if (elements != null) {
			for (TransformerElement element : elements) {
				Object value = objectAccess.getValue(sourceObject, element.getSourceField());
				if (value instanceof FileObject) {
					value = ((FileObject) value).copy();
				}
				objectAccess.setValue(targetObject, element.getTargetField(), value);
			}
		}
	}
	
	private void callFunctions(Transformer transformer, 
							   ValueObject sourceObject, ValueObject targetObject,
							   boolean beforeTransformation) {
		if (transformer.hasFunctions()) {
			try (Session session = sessionFactoryProvider.getSession()) {
				final ValueObjectFunctionContext functionContext = new ValueObjectFunctionContext(session, transformer.getModule());
				for (TransformerFunction function : transformer.getFunctions()) {
					if ((beforeTransformation && function.isActiveBeforeTransformation()) || 
						(!beforeTransformation && function.isActiveAfterTransformation())) {
						callFunction(function, sourceObject, targetObject, functionContext);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void callFunction(TransformerFunction function,
							  ValueObject sourceObject, ValueObject targetObject,
							  ValueObjectFunctionContext functionContext) {
		final Class<GeneratedCode> functionClass = codeManager.getGeneratedClass(function);
		if (functionClass == null) {
			throw new IllegalStateException("function class not available: " + function.getGeneratedPackage() + '.' + function.getGeneratedClass());
		}
		try {
			final TransformationFunction<ValueObject, ValueObject> transformationFunction = 
					(TransformationFunction<ValueObject, ValueObject>) MiscUtils.instantiate(functionClass);
			transformationFunction.transform(sourceObject, targetObject, functionContext);
		}
		catch (Exception ex) {
			throw new InternalException(ex);
		}
	}
	
}
