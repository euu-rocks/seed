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
package org.seed.core.entity.transform;

import java.util.List;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.codegen.CodeManager;
import org.seed.core.codegen.CodeUtils;
import org.seed.core.codegen.compile.CompilerException;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.transform.codegen.TransformerFunctionCodeProvider;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransformerValidator extends AbstractSystemEntityValidator<Transformer> {
	
	@Autowired
	private CodeManager codeManager;
	
	@Autowired
	private TransformerFunctionCodeProvider functionCodeProvider;
	
	@Autowired
	private TransformerRepository repository;
	
	private List<TransformerDependent<? extends SystemEntity>> transformerDependents;
	
	@Override
	public void validateCreate(Transformer transformer) throws ValidationException {
		Assert.notNull(transformer, C.TRANSFORMER);
		final var errors = createValidationErrors(transformer);
		
		if (isEmpty(transformer.getSourceEntity())) {
			errors.addEmptyField("label.sourceentity");
		}
		if (isEmpty(transformer.getTargetEntity())) {
			errors.addEmptyField("label.targetentity");
		}
		
		validate(errors);
	}
	
	@Override
	public void validateSave(Transformer transformer) throws ValidationException {
		Assert.notNull(transformer, C.TRANSFORMER);
		final var errors = createValidationErrors(transformer);
		
		if (isEmpty(transformer.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(transformer.getName())) {
			errors.addOverlongName(getMaxNameLength());
		}
		
		// elements
		if (transformer.hasElements()) {
			validateElements(transformer, errors);
		}
		
		// functions
		if (transformer.hasFunctions()) {
			validateFunctions(transformer, errors);
		}
		
		validate(errors);
	}
	
	@Override
	public void validateDelete(Transformer transformer) throws ValidationException {
		Assert.notNull(transformer, C.TRANSFORMER);
		final var errors = createValidationErrors(transformer);
		
		try (Session session = repository.openSession()) {
			for (TransformerDependent<? extends SystemEntity> dependent : getTransformerDependents()) {
				for (SystemEntity systemEntity : dependent.findUsage(transformer, session)) {
					if (C.FORM.equals(getEntityType(systemEntity))) {
						errors.addError("val.inuse.transformform", systemEntity.getName());
					}
					else {
						unhandledEntity(systemEntity);
					}
				}
			}
		}
		
		validate(errors);
	}
	
	private void validateElements(Transformer transformer, ValidationErrors errors) {
		for (TransformerElement element : transformer.getElements()) {
			if (isEmpty(element.getSourceField())) {
				errors.addError("val.empty.elementfield", "label.sourcefield");
			}
			if (isEmpty(element.getTargetField())) {
				errors.addError("val.empty.elementfield", "label.targetfield");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validateFunctions(Transformer transformer, ValidationErrors errors) {
		for (TransformerFunction function : transformer.getFunctions()) {
			// name
			if (isEmpty(function.getName())) {
				errors.addEmptyField(LABEL_FUNCTIONNAME);
			}
			else if (!isNameLengthAllowed(function.getName())) {
				errors.addOverlongObjectName(LABEL_FUNCTION, getMaxNameLength());
			}
			else if (!isNameAllowed(function.getInternalName())) {
				errors.addIllegalField(LABEL_FUNCTIONNAME, function.getName());
			}
			else if (!isNameUnique(function.getName(), transformer.getFunctions())) {
				errors.addError("val.ambiguous.functionname", function.getName());
			}
			
			// content
			if (isEmpty(function.getContent())) {
				errors.addError("val.empty.functioncode", function.getName());
			}
			else {
				final String className = CodeUtils.extractClassName(
						CodeUtils.extractQualifiedName(function.getContent()));
				if (!function.getGeneratedClass().equals(className)) {
					errors.addError("val.illegal.functionclassname", function.getName(), function.getGeneratedClass());
				}
				else {
					validateFunctionCode(function, errors);
				}
			}
		}
	}
	
	private void validateFunctionCode(TransformerFunction function, ValidationErrors errors) {
		try {
			codeManager.testCompile(functionCodeProvider.getFunctionSource(function));
		}
		catch (CompilerException cex) {
			errors.addError("val.illegal.functioncode", function.getName());
		}
	}
	
	private List<TransformerDependent<? extends SystemEntity>> getTransformerDependents() {
		if (transformerDependents == null) {
			transformerDependents = MiscUtils.castList(getBeans(TransformerDependent.class));
		}
		return transformerDependents;
	}
	
	private static final String LABEL_FUNCTION = "label.function";
	private static final String LABEL_FUNCTIONNAME = "label.functionname";

}
