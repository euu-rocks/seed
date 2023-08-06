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
package org.seed.core.entity;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractOrderedTransferableObject;
import org.seed.core.util.NameUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

@javax.persistence.Entity
@Table(name = "sys_entity_field_group")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EntityFieldGroup extends AbstractOrderedTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
	@JsonIgnore
	private EntityMetadata entity;
	
	private String name;
	
	@XmlTransient
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = (EntityMetadata) entity;
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@JsonIgnore
	public String getInternalName() {
		return name != null 
				? NameUtils.getInternalName(name).toLowerCase()
				: null;
	}

	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final EntityFieldGroup otherGroup = (EntityFieldGroup) other;
		return new EqualsBuilder()
			.append(getOrder(), otherGroup.getOrder())
			.append(name, otherGroup.name)
			.isEquals();
	}

}
