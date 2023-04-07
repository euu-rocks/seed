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
 * An <code>ApplicationException</code> can be used to abort an operation with an error message.
 * ApplicationExceptions are treated specially in forms and displayed as a warning message 
 * below the triggering button.
 * 
 * This exception is not thrown by Seed itself. It is intended for use in custom code only.
 * 
 * @author seed-master
 *
 */
public class ApplicationException extends RuntimeException {

	private static final long serialVersionUID = -717383974887740304L;
	
	/** 
     * Constructs a new <code>ApplicationException</code> exception 
     * with the message and cause.
     * @param message the detail message.
     * @param cause the cause.
     */
	public ApplicationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/** 
     * Constructs a new <code>ApplicationException</code> exception 
     * with the message.
     * @param message the detail message.
     */
	public ApplicationException(String message) {
		super(message);
	}

}
