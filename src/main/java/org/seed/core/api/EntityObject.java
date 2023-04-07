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
 * <code>EntityObject</code> is the base interface of all entity objects.
 * 
 * @author seed-master
 *
 */
public interface EntityObject {
	
	/**
	 * Return the primary key of the entity object
	 * @return the primary key or null if the object has not yet been saved
	 */
	Long getId();
	
	/**
	 * Return the current status of the entity object, if any
	 * @return the current status or null if no status model exists
	 */
	Status getEntityStatus();
	
}
