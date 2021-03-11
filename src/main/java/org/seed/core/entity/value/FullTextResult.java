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
package org.seed.core.entity.value;

import java.util.Date;

import org.seed.core.data.SystemObject;

public class FullTextResult implements SystemObject {
	
	private final ValueObject object;
	
	private final String text;
	
	private String name;

	FullTextResult(ValueObject object, String text) {
		this.object = object;
		this.text = text;
	}

	public ValueObject getObject() {
		return object;
	}

	public String getText() {
		return text;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Long getId() {
		return object.getId();
	}
	
	public Long getEntityId() {
		return object.getEntityId();
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
