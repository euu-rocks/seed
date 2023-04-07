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
package org.seed.core.api;

/**
 * A <code>Status</code> represents the status of an {@link EntityObject}.
 * 
 * @author seed-master
 *
 */
public interface Status {
	
	/**
	 * Returns the name of the status
	 * @return the name of the status
	 */
	String getName();
	
	/**
	 * Returns the description of the status
	 * @return the description of the status
	 */
	String getDescription();
	
	/**
	 * Returns the number of the status
	 * @return the number of the status
	 */
	Integer getStatusNumber();
	
	/**
	 * Returns the name and number of the status
	 * @return the name and number of the status
	 */
	String getNumberAndName();
}
