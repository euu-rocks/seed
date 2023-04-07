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

import javax.annotation.Nullable;

/**
 * <code>FunctionContext</code> is the base interface of all function contexts.
 * It provides basic functionality for setting and reading context properties.
 * 
 * @author seed-master
 *
 */
public interface FunctionContext {
	
	/**
	 * Checks if there is a context property with the given name
	 * @param name the name of the context property
	 * @return <code>true</code> if there is a property with the given name
	 */
	boolean hasProperty(String name);
	
	/**
	 * Returns the value of a context property with the given name or null if the property doesn't exist
	 * @param name the name of the context property
	 * @return the value of a context property with the given name or null if the property doesn't exist
	 */
	<T> T getProperty(String name);
	
	/**
	 * Sets the value of a context property. By setting it to null, the property can be removed.
	 * @param name the name of the context property
	 * @param value the value of a context property. Can also be null
	 */
	void setProperty(String name, @Nullable Object value);
	
	/**
	 * Returns the success message or null if none
	 * @return the success message or null if none
	 */
	String getSuccessMessage();
	
	/**
	 * Sets the success message
	 * @param successMessage the success message
	 */
	void setSuccessMessage(String successMessage);
	
}
