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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractOrderedTransferableObject;
import org.seed.core.util.Assert;
import org.seed.core.util.NameUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

@javax.persistence.Entity
@Table(name = "sys_entity_relation")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EntityRelation extends AbstractOrderedTransferableObject  {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
	@JsonIgnore
	private EntityMetadata entity;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_entity_id")
	@JsonIgnore
	private EntityMetadata relatedEntity;
	
	private String name;
	
	@Transient
	@JsonIgnore
	private String relatedEntityUid;
	
	@XmlTransient
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = (EntityMetadata) entity;
	}
	
	@XmlTransient
	public Entity getRelatedEntity() {
		return relatedEntity;
	}

	public void setRelatedEntity(Entity relatedEntity) {
		this.relatedEntity = (EntityMetadata) relatedEntity;
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getRelatedEntityUid() {
		return relatedEntity != null ? relatedEntity.getUid() : relatedEntityUid;
	}

	public void setRelatedEntityUid(String relatedEntityUid) {
		this.relatedEntityUid = relatedEntityUid;
	}
	
	@JsonIgnore
	public String getInternalName() {
		return name != null ? NameUtils.getInternalName(name).toLowerCase() : null;
	}
	
	@JsonIgnore
	public String getJoinTableName() {
		final StringBuilder buf = new StringBuilder();
		if (entity.getTableName() != null) {
			buf.append(entity.getTableName());
		}
		else {
			buf.append(entity.getInternalName().toLowerCase());
		}
		buf.append('_');
		if (relatedEntity.getTableName() != null) {
			buf.append(relatedEntity.getTableName());
		}
		else {
			buf.append(relatedEntity.getInternalName().toLowerCase());
		}
		return buf.toString();
	}
	
	@JsonIgnore
	public String getJoinColumnName() {
		return entity.getInternalName().toLowerCase() + "_id";
	}
	
	@JsonIgnore
	public String getInverseJoinColumnName() {
		return relatedEntity.getInternalName().toLowerCase() + "_id";
	}
	
	public EntityRelation createNewInverseRelation(Entity relatedEntity) {
		Assert.notNull(relatedEntity, "related entity");
		
		final EntityRelation relation = new EntityRelation();
		relation.setEntity(entity);
		relation.setRelatedEntity(relatedEntity);
		return relation;
	}
	
	public boolean isRelated(Entity entity) {
		return relatedEntity.equals(entity);
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !EntityRelation.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final EntityRelation otherRelation = (EntityRelation) other;
		return new EqualsBuilder()
			.append(name, otherRelation.getName())
			.append(relatedEntityUid, otherRelation.getRelatedEntityUid())
			.isEquals();
	}
	
}
