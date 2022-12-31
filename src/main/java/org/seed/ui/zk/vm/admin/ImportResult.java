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
package org.seed.ui.zk.vm.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.seed.core.data.ValidationError;

class ImportResult {
	
	final boolean success;
	
	final List<ValidationError> errors;

	ImportResult(boolean success) {
		this(success, null);
	}
	
	ImportResult(boolean success, Collection<ValidationError> errors) {
		this.success = success;
		this.errors = errors != null ? new ArrayList<>(errors) : null;
	}
	
}
