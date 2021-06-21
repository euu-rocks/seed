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
package org.seed.core.form;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.NestedEntity;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FormValidator extends AbstractSystemEntityValidator<Form> {
	
	@Autowired
	private List<FormDependent<? extends SystemEntity>> formDependents;
	
	public void validateAddSubForm(NestedEntity nested) throws ValidationException {
		if (isEmpty(nested)) {
			validate(Collections.singleton(ValidationError.emptyField("label.nested")));
		}
	}
	
	@Override
	public void validateCreate(Form form) throws ValidationException {
		Assert.notNull(form, C.FORM);
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(form.getEntity())) {
			errors.add(ValidationError.emptyField("label.entity"));
		}
		
		validate(errors);
	}
	
	@Override
	public void validateSave(Form form) throws ValidationException {
		Assert.notNull(form, C.FORM);
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(form.getName())) {
			errors.add(ValidationError.emptyName());
		}
		else if (!isNameLengthAllowed(form.getName())) {
			errors.add(ValidationError.overlongName(getMaxNameLength()));
		}
		
		if (form.hasFields()) {
			for (FormField field : form.getFields()) {
				validateField(field, errors);
			}
		}
		if (form.hasActions()) {
			validateActions(form, errors);
		}
		if (form.hasTransformers()) {
			validateTransformers(form, errors);
		}
		if (form.hasPrintouts()) {
			validatePrintouts(form, errors);
		}
		if (form.hasSubForms()) {
			validateSubForms(form, errors);
		}
		
		validate(errors);
	}
	
	@Override
	public void validateDelete(Form form) throws ValidationException {
		Assert.notNull(form, C.FORM);
		final Set<ValidationError> errors = createErrorList();
		
		for (FormDependent<? extends SystemEntity> dependent : formDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(form)) {
				if (C.FORM.equals(getEntityType(systemEntity))) {
					errors.add(new ValidationError("val.inuse.formform", systemEntity.getName()));
				}
			}
		}
		
		validate(errors);
	}
	
	private void validateField(FormField field, Set<ValidationError> errors) {
		if (field.getLabel() != null && 
			field.getLabel().length() > getMaxStringLength()) {
			errors.add(errorOverlongFormField(LABEL_LABEL));
		}
		if (field.getStyle() != null && 
			field.getStyle().length() > getMaxStringLength()) {
			errors.add(errorOverlongFormField("label.style"));
		}
		if (field.getLabelStyle() != null && 
			field.getLabelStyle().length() > getMaxStringLength()) {
			errors.add(errorOverlongFormField("label.labelstyle"));
		}
		if (field.getWidth() != null && 
			field.getWidth().length() > getMaxStringLength()) {
			errors.add(errorOverlongFormField("label.width"));
		}
		if (field.getHeight() != null && 
			field.getHeight().length() > getMaxStringLength()) {
			errors.add(errorOverlongFormField("label.height"));
		}
		final String hflex = field.getHflex();
		if (hflex != null && !(hflex.equals("min") || hflex.equals("max")) && 
			!isPositiveInteger(hflex)) {
			errors.add(ValidationError.illegalField("hflex", hflex));
		}
	}
	
	private void validateActions(Form form, Set<ValidationError> errors) {
		for (FormAction action : form.getActions()) {
			if (action.isCustom() && isEmpty(action.getEntityFunction())) {
				errors.add(new ValidationError("val.empty.actionfunction"));
			}
			if (action.getLabel() != null &&
				action.getLabel().length() > getMaxStringLength()) {
				errors.add(ValidationError.overlongObjectField(LABEL_LABEL, "label.action", getMaxStringLength()));
			}
		}
	}
	
	private void validateTransformers(Form form, Set<ValidationError> errors) {
		for (FormTransformer transformer : form.getTransformers()) {
			if (transformer.getLabel() != null &&
				transformer.getLabel().length() > getMaxStringLength()) {
				errors.add(ValidationError.overlongObjectField(LABEL_LABEL, "label.transformer", getMaxStringLength()));
			}
			if (isEmpty(transformer.getTargetForm())) {
				errors.add(new ValidationError("val.empty.transformtarget", transformer.getName()));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validatePrintouts(Form form, Set<ValidationError> errors) {
		for (FormPrintout printout : form.getPrintouts()) {
			if (isEmpty(printout.getName())) {
				errors.add(new ValidationError("val.empty.printoutfield", "label.name"));
			}
			else if (!isNameLengthAllowed(printout.getName())) {
				errors.add(ValidationError.overlongObjectName("label.printout", getMaxNameLength()));
			}
			else if (!isNameUnique(printout.getName(), form.getPrintouts())) {
				errors.add(new ValidationError("val.ambiguous.fieldgroup", printout.getName()));
			}
			if (isEmpty(printout.getContent())) {
				errors.add(new ValidationError("val.empty.printoutfield", "label.file"));
			}
		}
	}
	
	private void validateSubForms(Form form, Set<ValidationError> errors) {
		for (SubForm subForm : form.getSubForms()) {
			if (subForm.hasFields()) {
				for (SubFormField subFormField : subForm.getFields()) {
					validateSubFormField(subFormField, errors);
				}
			}
			if (subForm.hasActions()) {
				for (SubFormAction action : subForm.getActions()) {
					validateSubFormAction(action, errors);
				}
			}
		}
	}
	
	private void validateSubFormField(SubFormField subFormField, Set<ValidationError> errors) {
		if (subFormField.getLabel() != null &&
			subFormField.getLabel().length() > getMaxStringLength()) {
			errors.add(errorOverlongSubFormField(LABEL_LABEL));
		}
		if (subFormField.getWidth() != null &&
			subFormField.getWidth().length() > getMaxStringLength()) {
			errors.add(errorOverlongSubFormField("label.width"));
		}
		if (subFormField.getHeight() != null &&
			subFormField.getHeight().length() > getMaxStringLength()) {
			errors.add(errorOverlongSubFormField("label.height"));
		}
		if (subFormField.getStyle() != null &&
			subFormField.getStyle().length() > getMaxStringLength()) {
			errors.add(errorOverlongSubFormField("label.style"));
		}
		if (subFormField.getLabelStyle() != null &&
			subFormField.getLabelStyle().length() > getMaxStringLength()) {
			errors.add(errorOverlongSubFormField("label.labelstyle"));
		}
		final String hflex = subFormField.getHflex();
		if (hflex != null && !(hflex.equals("min") || hflex.equals("max")) && 
			!isPositiveInteger(hflex)) {
			errors.add(ValidationError.illegalField("hflex", hflex));
		}
	}
	
	private void validateSubFormAction(SubFormAction action, Set<ValidationError> errors) {
		if (action.getLabel() != null &&
			action.getLabel().length() > getMaxStringLength()) {
			errors.add(ValidationError.overlongObjectField(LABEL_LABEL, "label.subformaction", getMaxStringLength()));
		}
	}
	
	private ValidationError errorOverlongFormField(String name) {
		return ValidationError.overlongObjectField(name, "label.field", getMaxStringLength());
	}
	
	private ValidationError errorOverlongSubFormField(String name) {
		return ValidationError.overlongObjectField(name, "label.subformfield", getMaxStringLength());
	}
	
	private static final String LABEL_LABEL = "label.label";
	
}