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
package org.seed.core.data.revision;

import org.seed.core.data.FieldType;

public enum RevisionField {
	
	REV		(FieldType.INTEGER, "revision_id"),
	REVTYPE	(FieldType.INTEGER, "revisiontype");
	
	public final FieldType type;
	
	public final String columName;
	
	private RevisionField(FieldType type, String columName) {
		this.type = type;
		this.columName = columName;
	}
	
}
