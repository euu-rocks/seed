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
package org.seed.core.entity.transfer;

import org.seed.core.data.Options;

public class ImportOptions implements Options {
	
	private boolean allOrNothing;
	
	private boolean createIfNew;
	
	private boolean modifyExisting;
	
	private boolean executeCallbacks;

	public boolean isAllOrNothing() {
		return allOrNothing;
	}

	public void setAllOrNothing(boolean allOrNothing) {
		this.allOrNothing = allOrNothing;
	}

	public boolean isCreateIfNew() {
		return createIfNew;
	}

	public void setCreateIfNew(boolean createIfNew) {
		this.createIfNew = createIfNew;
	}

	public boolean isModifyExisting() {
		return modifyExisting;
	}

	public void setModifyExisting(boolean modifyExisting) {
		this.modifyExisting = modifyExisting;
	}

	public boolean isExecuteCallbacks() {
		return executeCallbacks;
	}

	public void setExecuteCallbacks(boolean executeCallbacks) {
		this.executeCallbacks = executeCallbacks;
	}
	
	@Override
	public String toString() {
		return "allOrNothing:" + allOrNothing + ", createIfNew:" + createIfNew + "," + 
			   " modifyExisting:" + modifyExisting + ", executeCallbacks:" + executeCallbacks;  
	}
 
}
