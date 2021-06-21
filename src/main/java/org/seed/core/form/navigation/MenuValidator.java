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
package org.seed.core.form.navigation;

import java.util.Set;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationError;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

import org.springframework.stereotype.Component;

@Component
public class MenuValidator extends AbstractSystemEntityValidator<Menu> {
	
	@Override
	public void validateSave(Menu menu) throws ValidationException {
		Assert.notNull(menu, C.MENU);
		final Set<ValidationError> errors = createErrorList();
		
		if (isEmpty(menu.getName())) {
			errors.add(ValidationError.emptyName());
		}
		else if (!isNameLengthAllowed(menu.getName())) {
			errors.add(ValidationError.overlongName(getMaxNameLength()));
		}
		if (menu.getIcon() != null &&
		    menu.getIcon().length() > getMaxStringLength()) {
			errors.add(ValidationError.overlongField("label.icon", getMaxStringLength()));
		}
		if (menu.hasSubMenus()) {
			for (Menu subMenu : menu.getSubMenus()) {
				if (isEmpty(subMenu.getName())) {
					errors.add(new ValidationError("val.empty.menufield", "label.name"));
				}
				else if (!isNameLengthAllowed(subMenu.getName())) {
					errors.add(new ValidationError("val.toolong.name", String.valueOf(getMaxNameLength())));
				}
				if (isEmpty(subMenu.getForm())) {
					errors.add(new ValidationError("val.empty.menufield", "label.form"));
				}
			}
		}
		validate(errors);
	}
	
}
