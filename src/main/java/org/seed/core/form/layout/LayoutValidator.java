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
package org.seed.core.form.layout;

import org.seed.LabelProvider;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.EntityField;
import org.seed.core.form.Form;
import org.seed.core.form.layout.BorderLayoutProperties.LayoutAreaProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LayoutValidator extends AbstractSystemEntityValidator<Form> {
	
	private static final String LABEL_COLUMNS = "label.columns";
	private static final String LABEL_FIELD   = "label.field";
	private static final String LABEL_ROWS    = "label.rows";
	private static final String LABEL_WIDTH   = "label.width";
	private static final String LABEL_HEIGHT  = "label.height";
	
	@Autowired
	private LabelProvider labelProvider;
	
	public void validateText(Form form, String text) throws ValidationException {
		validateText(form, text, null);
	}
	
	public void validateText(Form form, String text, String fieldName) throws ValidationException {
		if (fieldName == null) {
			fieldName = "label.text";
		}
		if (isEmpty(text)) {
			validate(createValidationErrors(form).addEmptyField(fieldName));
		}
	}
	
	public void validateEntityField(Form form, EntityField field) throws ValidationException {
		if (isEmpty(field)) {
			validate(createValidationErrors(form).addEmptyField(LABEL_FIELD));
		}
	}
	
	public void validateBinaryField(Form form, String width, String height) throws ValidationException {
		final ValidationErrors errors = createValidationErrors(form);
		
		if (isEmpty(width)) {
			errors.addEmptyField(LABEL_WIDTH);
		}
		if (isEmpty(height)) {
			errors.addEmptyField(LABEL_HEIGHT);
		}
		validate(errors);
	}
	
	public void validateNewGrid(Form form, Integer columns, Integer rows) throws ValidationException {
		final ValidationErrors errors = createValidationErrors(form);
		
		if (isEmpty(columns)) {
			errors.addEmptyField(LABEL_COLUMNS);
		}
		else if (isZeroOrBelow(columns)) {
			errors.addZeroField(LABEL_COLUMNS);
		}
		if (isEmpty(rows)) {
			errors.addEmptyField(LABEL_ROWS);
		}
		else if (isZeroOrBelow(rows)) {
			errors.addZeroField(LABEL_ROWS);
		}
		
		validate(errors);
	}
	
	public void validateProperties(Form form, LayoutElement element, 
								   LayoutElementAttributes properties) throws ValidationException {
		final ValidationErrors errors = createValidationErrors(form);
		
		if (!isEmpty(properties.getColumns()) && isZeroOrBelow(properties.getColumns())) {
			errors.addZeroField(LABEL_COLUMNS);
		}
		if (!isEmpty(properties.getRows()) && isZeroOrBelow(properties.getRows())) {
			errors.addZeroField(LABEL_ROWS);
		}
		if (!isEmpty(properties.getMaxlength()) && isZeroOrBelow(properties.getMaxlength())) {
			errors.addZeroField("label.maxlength");
		}
		if (element.is(LayoutElement.IMAGE)) {
			if (isEmpty(properties.getWidth())) {
				errors.addEmptyField(LABEL_WIDTH);
			}
			if (isEmpty(properties.getHeight())) {
				errors.addEmptyField(LABEL_HEIGHT);
			}
		}
		validate(errors);
	}
	
	public void validateBorderLayoutProperties(Form form, BorderLayoutProperties properties) throws ValidationException {
		final ValidationErrors errors = createValidationErrors(form);
		for (LayoutAreaProperties areaProperties : properties.getLayoutAreaProperties()) {
			if (areaProperties.isVisible() && isZeroOrBelow(areaProperties.getMaxsize())) {
				errors.addError("val.zero.fieldinarea", "label.maxsize", 
								labelProvider.getEnumLabel(areaProperties.getLayoutArea()));
			}
		}
		validate(errors);
	}
	
	public void validateBorderLayoutAreaProperties(Form form, LayoutAreaProperties properties) throws ValidationException {
		final ValidationErrors errors = createValidationErrors(form);
		if (properties.isVisible() && isZeroOrBelow(properties.getMaxsize())) {
			errors.addError("val.zero.fieldinarea", "label.maxsize", 
							labelProvider.getEnumLabel(properties.getLayoutArea()));
		}
		validate(errors);
	}
	
	
	@Override
	public void validateDelete(Form object) throws ValidationException {
		throw new UnsupportedOperationException();
	}

}
