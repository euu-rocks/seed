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
package org.seed.core.data;

/**
 * A SystemEntity represents a SystemObject that has a name and therefore a 'meaning'. 
 * It also provides additional Options for creation and an internal name,
 * which only consists of valid characters and contains no keywords.
 * 
 * @author seed-master
 * 
 */
public interface SystemEntity extends SystemObject {
	
	<T extends Options> T getOptions();
	
	String getName();
	
	void setName(String name);
	
	String getInternalName();
	
	void removeNewObjects();
	
}
