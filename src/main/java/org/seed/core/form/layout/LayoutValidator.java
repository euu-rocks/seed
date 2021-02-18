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

import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.EntityField;
import org.seed.core.form.Form;
import org.seed.core.form.LabelProvider;
import org.seed.core.form.layout.BorderLayoutProperties.LayoutAreaProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class LayoutValidator extends AbstractSystemEntityValidator<Form> {
	
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
			validate(Collections.singleton(new ValidationError("val.empty.field", field)));
		}
	}
	
	public void validateEntityField(EntityField field) throws ValidationException {
		if (isEmpty(field)) {
			validate(Collections.singleton(new ValidationError("val.empty.field", "label.field")));
		}
	}
	
	public void validateBinaryField(String width, String height) throws ValidationException {
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(width)) {
			errors.add(new ValidationError("val.empty.field", "label.width"));
		}
		if (isEmpty(height)) {
			errors.add(new ValidationError("val.empty.field", "label.height"));
		}
		validate(errors);
	}
	
	public void validateNewGrid(Integer columns, Integer rows) throws ValidationException {
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(columns)) {
			errors.add(new ValidationError("val.empty.field", "label.columns"));
		}
		else if (isZeroOrBelow(columns)) {
			errors.add(new ValidationError("val.zero.field", "label.columns"));
		}
		if (isEmpty(rows)) {
			errors.add(new ValidationError("val.empty.field", "label.rows"));
		}
		else if (isZeroOrBelow(rows)) {
			errors.add(new ValidationError("val.zero.field", "label.rows"));
		}
		
		validate(errors);
	}
	
	public void validateProperties(LayoutElement element, LayoutElementProperties properties) throws ValidationException {
		Assert.notNull(element, "element is null");
		Assert.notNull(properties, "properties is null");
		
		final Set<ValidationError> errors = createErrorList();
		if (!isEmpty(properties.getColumns()) && isZeroOrBelow(properties.getColumns())) {
			errors.add(new ValidationError("val.zero.field", "label.columns"));
		}
		if (!isEmpty(properties.getRows()) && isZeroOrBelow(properties.getRows())) {
			errors.add(new ValidationError("val.zero.field", "label.rows"));
		}
		if (!isEmpty(properties.getMaxlength()) && isZeroOrBelow(properties.getMaxlength())) {
			errors.add(new ValidationError("val.zero.field", "label.maxlength"));
		}
		if (element.is(LayoutElement.IMAGE)) {
			if (isEmpty(properties.getWidth())) {
				errors.add(new ValidationError("val.empty.field", "label.width"));
			}
			if (isEmpty(properties.getHeight())) {
				errors.add(new ValidationError("val.empty.field", "label.height"));
			}
		}
		validate(errors);
	}
	
	public void validateBorderLayoutProperties(BorderLayoutProperties properties) throws ValidationException {
		Assert.notNull(properties, "properties is null");
		
		final Set<ValidationError> errors = createErrorList();
		for (LayoutAreaProperties areaProperties : properties.getLayoutAreaProperties()) {
			if (areaProperties.isVisible()) {
				if (isZeroOrBelow(areaProperties.getMaxsize())) {
					errors.add(new ValidationError("val.zero.fieldinarea", "label.maxsize", 
												   labelProvider.getEnumLabel(areaProperties.getLayoutArea())));
				}
			}
		}
		validate(errors);
	}
	
	public void validateBorderLayoutAreaProperties(LayoutAreaProperties properties) throws ValidationException {
		Assert.notNull(properties, "properties is null");
		
		if (isZeroOrBelow(properties.getMaxsize())) {
			validate(Collections.singleton(new ValidationError("val.empty.field", "label.field")));
		}
	}
	
	
	@Override
	public void validateDelete(Form object) throws ValidationException {
		throw new UnsupportedOperationException();
	}

}
