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

import org.seed.core.api.Status;
import org.seed.core.application.TransferableObject;
import org.seed.core.data.AbstractSystemEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

@javax.persistence.Entity
@Table(name = "sys_entity_status")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EntityStatus extends AbstractSystemEntity 
	implements Status, TransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
	@JsonIgnore
	private EntityMetadata entity;
	
	private String uid;
	
	private Integer statusnumber;
	
	private String description;
	
	private boolean isInitial;
	
	@Override
	@XmlAttribute
	public String getUid() {
		return uid;
	}
	
	@Override
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	@XmlTransient
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = (EntityMetadata) entity;
	}
	
	@Override
	@XmlAttribute
	public Integer getStatusNumber() {
		return statusnumber;
	}

	public void setStatusNumber(Integer statusNumber) {
		this.statusnumber = statusNumber;
	}
	
	@Override
	@XmlAttribute
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@XmlAttribute
	public boolean isInitial() {
		return isInitial;
	}

	public void setInitial(boolean isInitial) {
		this.isInitial = isInitial;
	}
	
	@Override
	@JsonIgnore
	public String getNumberAndName() {
		final StringBuilder buf = new StringBuilder();
		if (statusnumber != null) 
			buf.append(statusnumber);
		if (getName() != null) {
			if (buf.length() > 0) {
				buf.append(' ');
			}
			buf.append(getName());
		}
		return buf.toString();
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !EntityStatus.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final EntityStatus otherStatus = (EntityStatus) other;
		return new EqualsBuilder()
			.append(getName(), otherStatus.getName())
			.append(statusnumber, otherStatus.statusnumber)
			.append(description, otherStatus.description)
			.append(isInitial, otherStatus.isInitial)
			.isEquals();
	}
	
}
