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

import javax.persistence.Transient;

import org.seed.core.data.AbstractSystemObject;
import org.seed.core.entity.EntityStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractValueObject extends AbstractSystemObject implements ValueObject {
	
	@Transient
	@JsonIgnore
	private Long tmpId; // temporary id, if object is new
	
	public Long getTmpId() {
		return tmpId;
	}

	void setTmpId(Long tmpId) {
		this.tmpId = tmpId;
	}

	@Override
	public EntityStatus getEntityStatus() {
		return null;
	}
	
	public void setEntityStatus(EntityStatus entityStatus) {
		throw new IllegalStateException("entity has no status");
	}
	
}
