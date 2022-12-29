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
package org.seed.core.application.setting;

import java.time.DateTimeException;
import java.time.ZoneId;

import org.seed.core.data.AbstractSystemEntityValidator;
import org.seed.core.data.ValidationErrors;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;

import org.springframework.stereotype.Component;

@Component
public class ApplicationSettingValidator extends AbstractSystemEntityValidator<ApplicationSetting> {
	
	@Override
	public void validateSave(ApplicationSetting setting) throws ValidationException {
		Assert.notNull(setting, "setting");
		
		if (setting.getSetting() == Setting.APPLICATION_TIMEZONE && setting.getValue() != null) {
			final ValidationErrors errors = createValidationErrors(setting);
			try {
				ZoneId.of(setting.getValue());
			}
			catch (DateTimeException dtex) {
				errors.addError("val.illegal.timezone");
			}
			validate(errors);
		}
	}
	
}
