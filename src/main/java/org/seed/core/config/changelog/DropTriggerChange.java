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

import org.springframework.util.StringUtils;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;

public class DropTriggerChange extends AbstractCustomChange {
	
	private String triggerName;
	
	public String getTriggerName() {
		return triggerName;
	}

	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	@Override
	public String getConfirmationMessage() {
		return "Trigger droped";
	}

	@Override
	public ValidationErrors validate(Database database) {
		final ValidationErrors errors = new ValidationErrors();
		if (!StringUtils.hasText(triggerName)) {
			errors.addError("triggerName is empty");
		}
		return errors;
	}
	
	@Override
	protected String getSql() {
		return "drop trigger " + triggerName;
	}

	@Override
	protected String getParameterName() {
		return "triggerName";
	}

	@Override
	protected String getParameterValue() {
		return triggerName;
	}

}
