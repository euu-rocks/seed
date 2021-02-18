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

import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.SystemEntity;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.NestedEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class FormValidator extends AbstractSystemEntityValidator<Form> {
	
	@Autowired
	private List<FormDependent> formDependents;
	
	public void validateAddSubForm(NestedEntity nested) throws ValidationException {
		if (isEmpty(nested)) {
			validate(Collections.singleton(new ValidationError("val.empty.field", "label.nested")));
		}
	}
	
	public void validateCreate(Form form) throws ValidationException {
		Assert.notNull(form, "form is null");
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(form.getEntity())) {
			errors.add(new ValidationError("val.empty.field", "label.entity"));
		}
		
		validate(errors);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void validateSave(Form form) throws ValidationException {
		Assert.notNull(form, "form is null");
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(form.getName())) {
			errors.add(new ValidationError("val.empty.field", "label.name"));
		}
		else if (!isNameLengthAllowed(form.getName())) {
			errors.add(new ValidationError("val.toolong.name", String.valueOf(getMaxNameLength())));
		}
		
		if (form.hasFields()) {
			for (FormField field : form.getFields()) {
				if (field.getLabel() != null && 
					field.getLabel().length() > getLimit("entity.stringfield.length")) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.label", "label.field", 
												   String.valueOf(getLimit("entity.stringfield.length"))));
				}
				if (field.getStyle() != null && 
					field.getStyle().length() > getLimit("entity.stringfield.length")) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.style", "label.field", 
							   					   String.valueOf(getLimit("entity.stringfield.length"))));
				}
				if (field.getLabelStyle() != null && 
					field.getLabelStyle().length() > getLimit("entity.stringfield.length")) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.labelstyle", "label.field", 
		   					   					   String.valueOf(getLimit("entity.stringfield.length"))));
				}
				if (field.getWidth() != null && 
					field.getWidth().length() > getLimit("entity.stringfield.length")) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.width", "label.field", 
		   					   					   String.valueOf(getLimit("entity.stringfield.length"))));
				}
				if (field.getHeight() != null && 
					field.getHeight().length() > getLimit("entity.stringfield.length")) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.height", "label.field", 
		   					   					   String.valueOf(getLimit("entity.stringfield.length"))));
				}
				final String hflex = field.getHflex();
				if (hflex != null && !(hflex.equals("min") || hflex.equals("max")) && 
					!isPositiveInteger(hflex)) {
					errors.add(new ValidationError("val.illegal.field", "hflex", hflex));
				}
			}
		}
		if (form.hasActions()) {
			for (FormAction action : form.getActions()) {
				if (action.isCustom() && isEmpty(action.getEntityFunction())) {
					errors.add(new ValidationError("val.empty.actionfunction"));
				}
				if (action.getLabel() != null &&
					action.getLabel().length() > getLimit("entity.stringfield.length")) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.label", "label.action", 
		   					   					   String.valueOf(getLimit("entity.stringfield.length"))));
				}
			}
		}
		if (form.hasTransformers()) {
			for (FormTransformer transformer : form.getTransformers()) {
				if (transformer.getLabel() != null &&
					transformer.getLabel().length() > getLimit("entity.stringfield.length")) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.label", "label.transformer", 
		   					   					   String.valueOf(getLimit("entity.stringfield.length"))));
				}
				if (isEmpty(transformer.getTargetForm())) {
					errors.add(new ValidationError("val.empty.transformtarget", transformer.getName()));
				}
			}
		}
		if (form.hasPrintouts()) {
			for (FormPrintout printout : form.getPrintouts()) {
				if (isEmpty(printout.getName())) {
					errors.add(new ValidationError("val.empty.printoutfield", "label.name"));
				}
				else if (!isNameLengthAllowed(printout.getName())) {
					errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.name", "label.printout", 
												   String.valueOf(getMaxNameLength())));
				}
				else if (!isNameUnique(printout.getName(), form.getPrintouts())) {
					errors.add(new ValidationError("val.ambiguous.fieldgroup", printout.getName()));
				}
				if (isEmpty(printout.getContent())) {
					errors.add(new ValidationError("val.empty.printoutfield", "label.file"));
				}
			}
		}
		if (form.hasSubForms()) {
			for (SubForm subForm : form.getSubForms()) {
				if (subForm.hasFields()) {
					for (SubFormField subFormField : subForm.getFields()) {
						if (subFormField.getLabel() != null &&
							subFormField.getLabel().length() > getLimit("entity.stringfield.length")) {
							errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.label", "label.subformfield", 
				   					   					   String.valueOf(getLimit("entity.stringfield.length"))));
						}
						if (subFormField.getWidth() != null &&
							subFormField.getWidth().length() > getLimit("entity.stringfield.length")) {
							errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.width", "label.subformfield", 
				   					   					   String.valueOf(getLimit("entity.stringfield.length"))));
						}
						if (subFormField.getHeight() != null &&
							subFormField.getHeight().length() > getLimit("entity.stringfield.length")) {
							errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.height", "label.subformfield", 
				   					   					   String.valueOf(getLimit("entity.stringfield.length"))));
						}
						if (subFormField.getStyle() != null &&
							subFormField.getStyle().length() > getLimit("entity.stringfield.length")) {
							errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.style", "label.subformfield", 
				   					   					   String.valueOf(getLimit("entity.stringfield.length"))));
						}
						if (subFormField.getLabelStyle() != null &&
							subFormField.getLabelStyle().length() > getLimit("entity.stringfield.length")) {
							errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.labelstyle", "label.subformfield", 
				   					   					   String.valueOf(getLimit("entity.stringfield.length"))));
						}
						final String hflex = subFormField.getHflex();
						if (hflex != null && !(hflex.equals("min") || hflex.equals("max")) && 
							!isPositiveInteger(hflex)) {
							errors.add(new ValidationError("val.illegal.field", "hflex", hflex));
						}
					}
				}
				if (subForm.hasActions()) {
					for (SubFormAction action : subForm.getActions()) {
						if (action.getLabel() != null &&
							action.getLabel().length() > getLimit("entity.stringfield.length")) {
							errors.add(new ValidationError("val.toolong.objectfieldvalue", "label.label", "label.subformaction", 
														   String.valueOf(getLimit("entity.stringfield.length"))));
						}
					}
				}
			}
		}
		
		validate(errors);
	}
	
	@Override
	public void validateDelete(Form form) throws ValidationException {
		Assert.notNull(form, "form is null");
		final Set<ValidationError> errors = createErrorList();
		
		for (FormDependent dependent : formDependents) {
			for (SystemEntity systemEntity : dependent.findUsage(form)) {
				switch (getEntityType(systemEntity)) {
					case "form":
						errors.add(new ValidationError("val.inuse.formform", systemEntity.getName()));
						break;
				}
			}
		}
		
		validate(errors);
	}
	
}