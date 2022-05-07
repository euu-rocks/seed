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
package org.seed.core.entity.doc;

abstract class AbstractPlantUMLBuilder {
	
	protected static final String COLON = " : ";
	
	final String build() {
		final StringBuilder buf = new StringBuilder();
		buildHeader(buf);
		build(buf);
		buildFooter(buf);
		return buf.toString();
	}
	
	protected abstract void build(StringBuilder buf);
	
	protected void buildHeader(StringBuilder buf) {
		buf.append("@startuml\n\n");
	}
	
	protected void buildFooter(StringBuilder buf) {
		buf.append("@enduml\n");
	}
	
}
