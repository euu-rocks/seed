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

import org.seed.core.api.RestFunction.MethodType;

/**
 * A <code>RestFunctionContext</code> is the context in which a {@link RestFunction} is executed.
 * 
 * @author seed-master
 *
 */
public interface RestFunctionContext extends CallbackFunctionContext {
	
	/**
	 * Returns the HTTP request method type
	 * @return the HTTP request method type
	 */
	MethodType getMethodType();
	
	/**
	 * Returns the request body object
	 * @return the request body object or <code>null</code> if no body object exist
	 */
	Object getBody();
	
	/**
	 * Returns an array of parameter values parsed from path elements.
	 * Example: /a/b/123 -> ["a","b","123"]
	 * @return an array of parameter values parsed from path elements
	 */
	String[] getParameters();
	
}
