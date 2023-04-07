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
 * <code>ParameterProvider</code> provides access to parameters.
 * 
 * @author seed-master
 *
 */
public interface ParameterProvider {
	
	/**
	 * Checks if there is a module parameter with the given name
	 * @param name the name of the parameter
	 * @return <code>true</code> if there is a parameter with the given name
	 */
	boolean hasModuleParameter(String name);
	
	/**
	 * Returns the value of a module parameter with the given name
	 * @param name the name of the parameter
	 * @return the value of the parameter or null if the parameter doesn't exist
	 */
	String getModuleParameter(String name);
	
	/**
	 * Returns the value of a module parameter with the given name or a default value if the parameter doesn't exist 
	 * @param name the name of the parameter
	 * @param defaultValue the default value
	 * @return the value of the parameter or the default value if the parameter doesn't exist 
	 */
	String getModuleParameter(String name, String defaultValue);
	
}
