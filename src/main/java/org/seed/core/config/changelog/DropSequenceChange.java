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

public class DropSequenceChange extends AbstractCustomChange {
	
	private String sequenceName;
	
	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	@Override
	public String getConfirmationMessage() {
		return "Sequence droped";
	}

	@Override
	public ValidationErrors validate(Database database) {
		final ValidationErrors errors = new ValidationErrors();
		if (!StringUtils.hasText(sequenceName)) {
			errors.addError("sequence name is empty");
		}
		return errors;
	}

	@Override
	protected String getSql() {
		return "drop sequence " + sequenceName;
	}

	@Override
	protected String getParameterName() {
		return "sequenceName";
	}

	@Override
	protected String getParameterValue() {
		return sequenceName;
	}

}
