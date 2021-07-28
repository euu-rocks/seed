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
package org.seed.core.entity.autonum;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.data.AbstractSystemEntity;
import org.seed.core.entity.EntityField;

@javax.persistence.Entity
@Table(name = "sys_entity_autonum")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Autonumber extends AbstractSystemEntity {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
	private EntityField field;
	
	private Long value;

	public EntityField getField() {
		return field;
	}

	public void setField(EntityField field) {
		this.field = field;
	}

	public String getPattern() {
		return getName();
	}

	public void setPattern(String pattern) {
		setName(pattern);
	}

	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}

}
