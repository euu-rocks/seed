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

import org.seed.ui.zk.vm.AbstractFormViewModel;

import org.zkoss.zk.ui.Component;

/**
 * A <code>FormFunctionContext</code> is the context in which a {@link FormFunction} is executed.
 * 
 * @author seed-master
 *
 */
public interface FormFunctionContext extends CallbackFunctionContext {
	
	/**
	 * Returns the view model
	 * @return the view model
	 */
	AbstractFormViewModel getViewModel();
	
	/**
	 * Returns the component that raised the event
	 * @return the component that raised the event
	 */
	Component getComponent();
	
	/**
	 * Returns a custom function parameter
	 * @return a custom function parameter
	 */
	<T> T getParameter();
	
}
