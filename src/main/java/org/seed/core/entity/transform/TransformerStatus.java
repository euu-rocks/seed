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
package org.seed.core.entity.transform;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractTransferableObject;
import org.seed.core.entity.EntityStatus;
import org.seed.core.util.ReferenceJsonSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "sys_entity_transform_status")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TransformerStatus extends AbstractTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transformer_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private TransformerMetadata transformer;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityStatus status;
	
	@Transient
	@JsonIgnore
	private String statusUid;
	
	@XmlTransient
	public Transformer getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = (TransformerMetadata) transformer;
	}
	
	@XmlTransient
	public EntityStatus getStatus() {
		return status;
	}

	public void setStatus(EntityStatus status) {
		this.status = status;
	}
	
	@XmlAttribute
	public String getStatusUid() {
		return status != null ? status.getUid() : statusUid;
	}

	public void setStatusUid(String statusUid) {
		this.statusUid = statusUid;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == this) {
			return true;
		}
		if (!isInstance(other)) {
			return false;
		}
		final TransformerStatus otherStatus = (TransformerStatus) other;
		return new EqualsBuilder()
				.append(getStatusUid(), otherStatus.getStatusUid())
				.isEquals();
	}
	
}
