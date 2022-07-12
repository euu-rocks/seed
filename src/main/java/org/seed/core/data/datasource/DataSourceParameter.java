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
package org.seed.core.data.datasource;

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

import org.seed.core.application.TransferableObject;
import org.seed.core.data.AbstractSystemEntity;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityMetadata;

@javax.persistence.Entity
@Table(name = "sys_datasource_param")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DataSourceParameter extends AbstractSystemEntity
	implements TransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datasource_id")
	private DataSourceMetadata dataSource;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refentity_id")
	private EntityMetadata referenceEntity;
	
	private String uid;
	
	private DataSourceParameterType type;
	
	@Transient
	private String referenceEntityUid;
	
	@Transient
	private Object value;
	
	@XmlTransient
	public IDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(IDataSource dataSource) {
		this.dataSource = (DataSourceMetadata) dataSource;
	}
	
	@XmlTransient
	public Entity getReferenceEntity() {
		return referenceEntity;
	}

	public void setReferenceEntity(Entity referenceEntity) {
		this.referenceEntity = (EntityMetadata) referenceEntity;
	}
	
	@XmlAttribute
	public String getReferenceEntityUid() {
		return referenceEntity != null ? referenceEntity.getUid() : referenceEntityUid;
	}

	public void setReferenceEntityUid(String referenceEntityUid) {
		this.referenceEntityUid = referenceEntityUid;
	}

	@Override
	@XmlAttribute
	public String getUid() {
		return uid;
	}
	
	@Override
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	@XmlAttribute
	public DataSourceParameterType getType() {
		return type;
	}

	public void setType(DataSourceParameterType type) {
		this.type = type;
	}
	
	@XmlTransient
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final DataSourceParameter otherParameter = (DataSourceParameter) other;
		return new EqualsBuilder()
			.append(getName(), otherParameter.getName())
			.append(referenceEntity, otherParameter.referenceEntity)
			.append(type, otherParameter.type)
			.isEquals();
	}

}
