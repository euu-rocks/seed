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

import java.util.regex.Pattern;

import org.seed.C;
import org.seed.core.config.Limits;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.springframework.stereotype.Component;

@Component
public class UserValidator extends AbstractSystemEntityValidator<User> {
	
	private static final Pattern PATTERN_EMAIL = Pattern.compile(
		"^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$");
	
	@Override
	public void validateSave(User user) throws ValidationException {
		Assert.notNull(user, C.USER);
		final ValidationErrors errors = createValidationErrors(user);
		
		if (isEmpty(user.getName())) {
			errors.addEmptyName();
		}
		else if (user.getName().length() > getLimit(Limits.LIMIT_USER_LENGTH)) {
			errors.addOverlongField("label.username", getLimit(Limits.LIMIT_USER_LENGTH));
		}
		else if (user.getInternalName().equalsIgnoreCase(MiscUtils.USERNAME_SYSTEM)) {
			errors.addIllegalName(user.getInternalName());
		}
		if (isEmpty(user.getEmail())) {
			errors.addEmptyField("label.email");
		}
		else if (user.getEmail().length() > getMaxStringLength()) {
			errors.addOverlongField("label.email", getMaxStringLength());
		}
		else if (!PATTERN_EMAIL.matcher(user.getEmail()).matches()) {
			errors.addError("val.illegal.email");
		}
		if (user.getFirstname() != null &&
			user.getFirstname().length() > getMaxStringLength()) {
			errors.addOverlongField("label.firstname", getMaxStringLength());
		}
		if (user.getLastname() != null &&
			user.getLastname().length() > getMaxStringLength()) {
			errors.addOverlongField("label.lastname", getMaxStringLength());
		}
		if (isEmpty(user.getUserGroups())) {
			errors.addError("val.missing.userrole");
		}
		
		validate(errors);
	}
	
	void validatePassword(User user, String password, String passwordRepeated) throws ValidationException {
		Assert.notNull(user, C.USER);
		final ValidationErrors errors = createValidationErrors(user);
		
		if (isEmpty(password)) {
			errors.addEmptyField("label.password");
		}
		if (isEmpty(passwordRepeated)) {
			errors.addEmptyField("label.passwordrepeated");
		}
		if (errors.isEmpty() && !password.equals(passwordRepeated)) {
			errors.addError("val.ambiguous.password");
		}
		
		validate(errors);
	}
	
}
