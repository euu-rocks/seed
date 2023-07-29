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
package org.seed.core.config.changelog;

import org.seed.core.util.StreamUtils;

import org.springframework.util.StringUtils;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;

public class CreateTriggerChange extends AbstractCustomChange {
	
	private String triggerText;
	
	public String getTriggerText() {
		return triggerText;
	}

	public void setTriggerText(String triggerText) {
		this.triggerText = triggerText;
	}

	@Override
	public String getConfirmationMessage() {
		return "Trigger created";
	}

	@Override
	public ValidationErrors validate(Database database) {
		final ValidationErrors errors = new ValidationErrors();
		if (!StringUtils.hasText(triggerText)) {
			errors.addError("triggerText is empty");
		}
		return errors;
	}
	
	@Override
	protected String getSql() {
		return StreamUtils.decompress(triggerText);
	}

	@Override
	protected String getParameterName() {
		return "triggerText";
	}

	@Override
	protected String getParameterValue() {
		return triggerText;
	}

}