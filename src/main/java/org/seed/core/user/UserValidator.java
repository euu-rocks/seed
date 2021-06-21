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

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

import org.springframework.stereotype.Component;

@Component
public class UserValidator extends AbstractSystemEntityValidator<User> {
	
	@Override
	public void validateSave(User user) throws ValidationException {
		Assert.notNull(user, C.USER);
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(user.getName())) {
			errors.add(ValidationError.emptyField("label.name"));
		}
		else if (user.getName().length() > getLimit("user.name.length")) {
			errors.add(ValidationError.overlongField("label.username", getLimit("user.name.length")));
		}
		if (isEmpty(user.getEmail())) {
			errors.add(ValidationError.emptyField("label.email"));
		}
		else if (user.getEmail().length() > getMaxStringLength()) {
			errors.add(ValidationError.overlongField("label.email", getMaxStringLength()));
		}
		if (user.getFirstname() != null &&
			user.getFirstname().length() > getMaxStringLength()) {
			errors.add(ValidationError.overlongField("label.firstname", getMaxStringLength()));
		}
		if (user.getLastname() != null &&
			user.getLastname().length() > getMaxStringLength()) {
			errors.add(ValidationError.overlongField("label.lastname", getMaxStringLength()));
		}
		
		validate(errors);
	}
	
	void validatePassword(String password, String passwordRepeated) throws ValidationException {
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(password)) {
			errors.add(ValidationError.emptyField("label.password"));
		}
		if (isEmpty(passwordRepeated)) {
			errors.add(ValidationError.emptyField("label.passwordrepeated"));
		}
		if (errors.isEmpty() && !password.equals(passwordRepeated)) {
			errors.add(new ValidationError("val.ambiguous.password"));
		}
		
		validate(errors);
	}
	
}
