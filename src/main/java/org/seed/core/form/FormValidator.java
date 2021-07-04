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

import java.util.List;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationErrors;
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
			validate(new ValidationErrors().addEmptyField("label.nested"));
		}
	}
	
	@Override
	public void validateCreate(Form form) throws ValidationException {
		Assert.notNull(form, C.FORM);
		final ValidationErrors errors = new ValidationErrors();
		
		if (isEmpty(form.getEntity())) {
			errors.addEmptyField("label.entity");
		}
		
		validate(errors);
	}
	
	@Override
	public void validateSave(Form form) throws ValidationException {
		Assert.notNull(form, C.FORM);
		final ValidationErrors errors = new ValidationErrors();
		
		if (isEmpty(form.getName())) {
			errors.addEmptyName();
		}
		else if (!isNameLengthAllowed(form.getName())) {
			errors.addOverlongName(getMaxNameLength());
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
		final ValidationErrors errors = new ValidationErrors();
		
		for (FormDependent<? extends SystemEntity> dependent : formDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(form)) {
				if (C.FORM.equals(getEntityType(systemEntity))) {
					errors.addError("val.inuse.formform", systemEntity.getName());
				}
			}
		}
		
		validate(errors);
	}
	
	private void validateField(FormField field, ValidationErrors errors) {
		if (field.getLabel() != null && 
			field.getLabel().length() > getMaxStringLength()) {
			errors.addOverlongObjectField(LABEL, FIELD, getMaxStringLength());
		}
		if (field.getStyle() != null && 
			field.getStyle().length() > getMaxStringLength()) {
			errors.addOverlongObjectField("label.style", FIELD, getMaxStringLength());
		}
		if (field.getLabelStyle() != null && 
			field.getLabelStyle().length() > getMaxStringLength()) {
			errors.addOverlongObjectField("label.labelstyle", FIELD, getMaxStringLength());
		}
		if (field.getWidth() != null && 
			field.getWidth().length() > getMaxStringLength()) {
			errors.addOverlongObjectField("label.width", FIELD, getMaxStringLength());
		}
		if (field.getHeight() != null && 
			field.getHeight().length() > getMaxStringLength()) {
			errors.addOverlongObjectField("label.height", FIELD, getMaxStringLength());
		}
		final String hflex = field.getHflex();
		if (hflex != null && !(hflex.equals("min") || hflex.equals("max")) && 
			!isPositiveInteger(hflex)) {
			errors.addIllegalField("hflex", hflex);
		}
	}
	
	private void validateActions(Form form, ValidationErrors errors) {
		for (FormAction action : form.getActions()) {
			if (action.isCustom() && isEmpty(action.getEntityFunction())) {
				errors.addError("val.empty.actionfunction");
			}
			if (action.getLabel() != null &&
				action.getLabel().length() > getMaxStringLength()) {
				errors.addOverlongObjectField(LABEL, "label.action", getMaxStringLength());
			}
		}
	}
	
	private void validateTransformers(Form form, ValidationErrors errors) {
		for (FormTransformer transformer : form.getTransformers()) {
			if (transformer.getLabel() != null &&
				transformer.getLabel().length() > getMaxStringLength()) {
				errors.addOverlongObjectField(LABEL, "label.transformer", getMaxStringLength());
			}
			if (isEmpty(transformer.getTargetForm())) {
				errors.addError("val.empty.transformtarget", transformer.getName());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void validatePrintouts(Form form, ValidationErrors errors) {
		for (FormPrintout printout : form.getPrintouts()) {
			if (isEmpty(printout.getName())) {
				errors.addError("val.empty.printoutfield", "label.name");
			}
			else if (!isNameLengthAllowed(printout.getName())) {
				errors.addOverlongObjectName("label.printout", getMaxNameLength());
			}
			else if (!isNameUnique(printout.getName(), form.getPrintouts())) {
				errors.addError("val.ambiguous.fieldgroup", printout.getName());
			}
			if (isEmpty(printout.getContent())) {
				errors.addError("val.empty.printoutfield", "label.file");
			}
		}
	}
	
	private void validateSubForms(Form form, ValidationErrors errors) {
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
	
	private void validateSubFormField(SubFormField subFormField, ValidationErrors errors) {
		if (subFormField.getLabel() != null &&
			subFormField.getLabel().length() > getMaxStringLength()) {
			errors.addOverlongObjectField(LABEL, SUBFORMFIELD, getMaxStringLength());
		}
		if (subFormField.getWidth() != null &&
			subFormField.getWidth().length() > getMaxStringLength()) {
			errors.addOverlongObjectField("label.width", SUBFORMFIELD, getMaxStringLength());
		}
		if (subFormField.getHeight() != null &&
			subFormField.getHeight().length() > getMaxStringLength()) {
			errors.addOverlongObjectField("label.height", SUBFORMFIELD, getMaxStringLength());
		}
		if (subFormField.getStyle() != null &&
			subFormField.getStyle().length() > getMaxStringLength()) {
			errors.addOverlongObjectField("label.style", SUBFORMFIELD, getMaxStringLength());
		}
		if (subFormField.getLabelStyle() != null &&
			subFormField.getLabelStyle().length() > getMaxStringLength()) {
			errors.addOverlongObjectField("label.labelstyle", SUBFORMFIELD, getMaxStringLength());
		}
		final String hflex = subFormField.getHflex();
		if (hflex != null && !(hflex.equals("min") || hflex.equals("max")) && 
			!isPositiveInteger(hflex)) {
			errors.addIllegalField("hflex", hflex);
		}
	}
	
	private void validateSubFormAction(SubFormAction action, ValidationErrors errors) {
		if (action.getLabel() != null &&
			action.getLabel().length() > getMaxStringLength()) {
			errors.addOverlongObjectField(LABEL, "label.subformaction", getMaxStringLength());
		}
	}
	
	private static final String LABEL = "label.label";
	private static final String FIELD = "label.field";
	private static final String SUBFORMFIELD = "label.subformfield";
	
}