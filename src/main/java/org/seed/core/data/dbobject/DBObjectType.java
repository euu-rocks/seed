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
package org.seed.core.data.dbobject;

public enum DBObjectType {
	
	VIEW	  (true),
	PROCEDURE (true),
	FUNCTION  (true),
	TRIGGER   (false),
	SEQUENCE  (false);
	
	private final boolean editable;
	
	private DBObjectType(boolean editable) {
		this.editable = editable;
	}

	public boolean isEditable() {
		return editable;
	}
	
}
