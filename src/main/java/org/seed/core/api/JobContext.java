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
 * A <code>JobContext</code> is the context in which a {@link Job} is executed.
 * 
 * @author seed-master
 *
 */
public interface JobContext extends CallbackFunctionContext {
	
	/**
	 * Checks if there is a job parameter with the given name
	 * @param name the name of the job parameter
	 * @return  <code>true</code> if there is a parameter with the given name
	 */
	boolean hasJobParameter(String name);
	
	/**
	 * Returns the job parameter with the given name
	 * @param name the name of the job parameter
	 * @return the value of the parameter or <code>null</code> if the parameter doesn't exist
	 */
	String getJobParameter(String name);
	
	/**
	 * Returns the job parameter with the given name as <code>Integer</code>
	 * @param name the name of the job parameter
	 * @return the value of the parameter as <code>Integer</code> or <code>null</code> if the parameter doesn't exist
	 * @throws IllegalStateException if the parameter is not an integer value
	 */
	Integer getJobParameterAsInt(String name);
	
	/**
	 * Returns the job parameter with the given name or a default value if the parameter doesn't exist
	 * @param name the name of the job parameter
	 * @param defaultValue the default value that is used if the parameter doesn't exist
	 * @return the value of the parameter or the default value if the parameter doesn't exist
	 */
	String getJobParameter(String name, String defaultValue);
	
	/**
	 * Returns the job parameter with the given name as <code>Integer</code> or a default value if the parameter doesn't exist
	 * @param name the name of the job parameter
	 * @param defaultValue the default value that is used if the parameter doesn't exist
	 * @return the value of the parameter as <code>Integer</code> or the default value if the parameter doesn't exist
	 * @throws IllegalStateException if the parameter is not an integer value
	 */
	Integer getJobParameterAsInt(String name, Integer defaultValue);
	
	/**
	 * Writes a message (info) to the job run log
	 * @param message the message to write to the job run log
	 */
	void log(String message);
	
	/**
	 * Writes an info message to the job run log
	 * @param message the message to write to the job run log
	 */
	void logInfo(String message);
	
	/**
	 * Writes an warning message to the job run log
	 * @param message the message to write to the job run log
	 */
	void logWarn(String message);
	
	/**
	 * Writes an error message to the job run log
	 * @param message the message to write to the job run log
	 */
	void logError(String message);
	
}
