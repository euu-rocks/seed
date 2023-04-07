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
 * <code>ClientProvider</code> provides access to client-side (UI) functionalities.
 * 
 * @author seed-master
 *
 */
public interface ClientProvider {
	
	/**
	 * Shows an info message dialog
	 * @param message the message to show
	 */
	void showInfoMessage(String message);
	
	/**
	 * Shows an info message dialog
	 * @param message the message to show
	 * @param title the title of the dialog
	 */
	void showInfoMessage(String message, String title);
	
	/**
	 * Shows a warning message dialog
	 * @param message the message to show
	 */
	void showWarnMessage(String message);
	
	/**
	 * Shows a warning message dialog
	 * @param message the message to show
	 * @param title the title of the dialog
	 */
	void showWarnMessage(String message, String title);
	
	/**
	 * Shows an error message dialog
	 * @param message the message to show
	 */
	void showErrorMessage(String message);
	
	/**
	 * Shows an error message dialog
	 * @param message the message to show
	 * @param title the title of the dialog
	 */
	void showErrorMessage(String message, String title);
	
}
