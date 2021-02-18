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
package org.seed.core.user;

import java.util.Set;

import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class UserValidator extends AbstractSystemEntityValidator<User> {
	
	public void validatePassword(String password, String passwordRepeated) throws ValidationException {
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(password)) {
			errors.add(new ValidationError("val.empty.field", "label.password"));
		}
		if (isEmpty(passwordRepeated)) {
			errors.add(new ValidationError("val.empty.field", "label.passwordrepeated"));
		}
		if (errors.isEmpty() && !password.equals(passwordRepeated)) {
			errors.add(new ValidationError("val.ambiguous.password"));
		}
		
		validate(errors);
	}
	
	@Override
	public void validateSave(User user) throws ValidationException {
		Assert.notNull(user, "user is null");
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(user.getName())) {
			errors.add(new ValidationError("val.empty.field", "label.name"));
		}
		else if (user.getName().length() > getLimit("user.name.length")) {
			errors.add(new ValidationError("val.toolong.fieldvalue", "label.username", 
					   					   String.valueOf(getLimit("user.name.length"))));
		}
		if (isEmpty(user.getEmail())) {
			errors.add(new ValidationError("val.empty.field", "label.email"));
		}
		else if (user.getEmail().length() > getLimit("entity.stringfield.length")) {
			errors.add(new ValidationError("val.toolong.fieldvalue", "label.email", 
					   					   String.valueOf(getLimit("entity.stringfield.length"))));
		}
		if (user.getFirstname() != null &&
			user.getFirstname().length() > getLimit("entity.stringfield.length")) {
			errors.add(new ValidationError("val.toolong.fieldvalue", "label.firstname", 
					   					   String.valueOf(getLimit("entity.stringfield.length"))));
		}
		if (user.getLastname() != null &&
			user.getLastname().length() > getLimit("entity.stringfield.length")) {
			errors.add(new ValidationError("val.toolong.fieldvalue", "label.lastname", 
					   					   String.valueOf(getLimit("entity.stringfield.length"))));
		}
		
		validate(errors);
	}
	
}
