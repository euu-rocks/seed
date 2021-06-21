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

import java.util.Collections;
import java.util.Set;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.EntityField;
import org.seed.core.form.Form;
import org.seed.core.form.LabelProvider;
import org.seed.core.form.layout.BorderLayoutProperties.LayoutAreaProperties;
import org.seed.core.util.Assert;

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
	
	public void validateText(String text) throws ValidationException {
		validateText(text, null);
	}
	
	public void validateText(String text, String field) throws ValidationException {
		if (field == null) {
			field = "label.text";
		}
		if (isEmpty(text)) {
			validate(Collections.singleton(ValidationError.emptyField(field)));
		}
	}
	
	public void validateEntityField(EntityField field) throws ValidationException {
		if (isEmpty(field)) {
			validate(Collections.singleton(ValidationError.emptyField(LABEL_FIELD)));
		}
	}
	
	public void validateBinaryField(String width, String height) throws ValidationException {
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(width)) {
			errors.add(ValidationError.emptyField(LABEL_WIDTH));
		}
		if (isEmpty(height)) {
			errors.add(ValidationError.emptyField(LABEL_HEIGHT));
		}
		validate(errors);
	}
	
	public void validateNewGrid(Integer columns, Integer rows) throws ValidationException {
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(columns)) {
			errors.add(ValidationError.emptyField(LABEL_COLUMNS));
		}
		else if (isZeroOrBelow(columns)) {
			errors.add(ValidationError.zeroField(LABEL_COLUMNS));
		}
		if (isEmpty(rows)) {
			errors.add(ValidationError.emptyField(LABEL_ROWS));
		}
		else if (isZeroOrBelow(rows)) {
			errors.add(ValidationError.zeroField(LABEL_ROWS));
		}
		
		validate(errors);
	}
	
	public void validateProperties(LayoutElement element, LayoutElementAttributes properties) throws ValidationException {
		Assert.notNull(element, C.ELEMENT);
		Assert.notNull(properties, C.PROPERTIES);
		
		final Set<ValidationError> errors = createErrorList();
		if (!isEmpty(properties.getColumns()) && isZeroOrBelow(properties.getColumns())) {
			errors.add(ValidationError.zeroField(LABEL_COLUMNS));
		}
		if (!isEmpty(properties.getRows()) && isZeroOrBelow(properties.getRows())) {
			errors.add(ValidationError.zeroField(LABEL_ROWS));
		}
		if (!isEmpty(properties.getMaxlength()) && isZeroOrBelow(properties.getMaxlength())) {
			errors.add(ValidationError.zeroField("label.maxlength"));
		}
		if (element.is(LayoutElement.IMAGE)) {
			if (isEmpty(properties.getWidth())) {
				errors.add(ValidationError.emptyField(LABEL_WIDTH));
			}
			if (isEmpty(properties.getHeight())) {
				errors.add(ValidationError.emptyField(LABEL_HEIGHT));
			}
		}
		validate(errors);
	}
	
	public void validateBorderLayoutProperties(BorderLayoutProperties properties) throws ValidationException {
		Assert.notNull(properties, C.PROPERTIES);
		
		final Set<ValidationError> errors = createErrorList();
		for (LayoutAreaProperties areaProperties : properties.getLayoutAreaProperties()) {
			if (areaProperties.isVisible() && isZeroOrBelow(areaProperties.getMaxsize())) {
				errors.add(new ValidationError("val.zero.fieldinarea", "label.maxsize", 
											   labelProvider.getEnumLabel(areaProperties.getLayoutArea())));
			}
		}
		validate(errors);
	}
	
	public void validateBorderLayoutAreaProperties(LayoutAreaProperties properties) throws ValidationException {
		Assert.notNull(properties, C.PROPERTIES);
		
		if (isZeroOrBelow(properties.getMaxsize())) {
			validate(Collections.singleton(ValidationError.emptyField(LABEL_FIELD)));
		}
	}
	
	
	@Override
	public void validateDelete(Form object) throws ValidationException {
		throw new UnsupportedOperationException();
	}

}
