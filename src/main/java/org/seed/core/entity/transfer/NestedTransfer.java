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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.seed.C;
import org.seed.core.data.SystemObject;
import org.seed.core.entity.NestedEntity;
import org.seed.core.util.Assert;

public class NestedTransfer implements SystemObject {
	
	private NestedEntity nested;
	
	private final List<TransferElement> elements = new ArrayList<>();
	
	public NestedTransfer() {}

	NestedTransfer(NestedEntity nested) {
		this.nested = nested;
	}

	public NestedEntity getNested() {
		return nested;
	}

	public void setNested(NestedEntity nested) {
		this.nested = nested;
	}

	public List<TransferElement> getElements() {
		return elements;
	}
	
	public boolean containsElement(TransferElement element) {
		Assert.notNull(element, C.ELEMENT);
		
		return elements.contains(element);
	}
	
	public void addElement(TransferElement element) {
		Assert.notNull(element, C.ELEMENT);
		
		elements.add(element);
	}
	
	public void removeElements() {
		elements.clear();
	}

	@Override
	public Long getId() {
		return nested != null ? nested.getId() : null;
	}

	@Override
	public int getVersion() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isNew() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEqual(Object other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Date getCreatedOn() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCreatedBy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Date getModifiedOn() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getModifiedBy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Date getLastModified() {
		throw new UnsupportedOperationException();
	}
	
}
