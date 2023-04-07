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
 * A <code>StoredProcedureCall</code> represents a call of a stored procedure.
 * If there are any outputs, register output type classes first via <code>awaitOutput</code>
 
 * @author seed-master
 *
 */
public interface StoredProcedureCall {
	
	/**
	 * Sets the class of the expected output parameter for the given index.
	 * This should be set before executing the procedure.
	 * @param paramIndex the index of the output parameter
	 * @param outputClass the class of the output parameter
	 */
	void awaitOutput(int paramIndex, Class<?> outputClass);
	
	/**
	 * Sets the class of the expected output parameter for the given parameter name.
	 * This should be set before executing the procedure
	 * @param paramName the name of the output parameter
	 * @param outputClass the class of the output parameter
	 */
	void awaitOutput(String paramName, Class<?> outputClass);
	
	/**
	 * Sets the value of an input parameter at a specific index.
	 * @param paramIndex the index of the input parameter
	 * @param paramValue the value of the input parameter
	 */
	void setParameter(int paramIndex, Object paramValue);
	
	/**
	 * Sets the value of an input parameter for the given parameter name.
	 * @param paramName the name of the input parameter
	 * @param paramValue the value of the input parameter
	 */
	void setParameter(String paramName, Object paramValue);
	
	/**
	 * Exceutes the stored procedure
	 */
	void execute();
	
	/**
	 * Returns the output parameter value at a specific index.
	 * You must first register the output type via <code>awaitOutput</code>
	 * @param <T> the type of the output parameter
	 * @param paramIndex the index of the output parameter
	 * @return the output parameter value
	 */
	<T> T getOutput(int paramIndex);
	
	/**
	 * Returns the output parameter value for the given parameter name.
	 * You must first register the output type via <code>awaitOutput</code>
	 * @param <T> the type of the output parameter
	 * @param paramName the name of the output parameter
	 * @return the output parameter value
	 */
	<T> T getOutput(String paramName);

}
