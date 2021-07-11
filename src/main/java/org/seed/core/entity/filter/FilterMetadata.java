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
package org.seed.core.entity.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.seed.C;
import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityMetadata;
import org.seed.core.util.Assert;
import org.seed.core.util.ReferenceJsonSerializer;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@javax.persistence.Entity
@Table(name = "sys_entity_filter")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FilterMetadata extends AbstractApplicationEntity implements Filter {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private EntityMetadata entity;
	
	@OneToMany(mappedBy = "filter",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	private List<FilterCriterion> criteria;
	
	private String hqlQuery;
	
	@Transient
	private String entityUid;
	
	@Transient
	private boolean hqlInput;
	
	@Override
	@XmlTransient
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = (EntityMetadata) entity;
	}
	
	@Override
	public String getHqlQuery() {
		return hqlQuery;
	}

	public void setHqlQuery(String hqlQuery) {
		this.hqlQuery = hqlQuery;
	}
	
	@XmlTransient
	public boolean isHqlInput() {
		return hqlInput;
	}

	public void setHqlInput(boolean hqlInput) {
		this.hqlInput = hqlInput;
	}
	
	@Override
	@XmlAttribute
	@JsonIgnore
	public String getEntityUid() {
		return entity != null ? entity.getUid() : entityUid;
	}

	public void setEntityUid(String entityUid) {
		this.entityUid = entityUid;
	}

	@Override
	@XmlElement(name="criterion")
	@XmlElementWrapper(name="criteria")
	public List<FilterCriterion> getCriteria() {
		return criteria;
	}

	public void setCriteria(List<FilterCriterion> criteria) {
		this.criteria = criteria;
	}
	
	@Override
	public boolean hasCriteria() {
		return !ObjectUtils.isEmpty(getCriteria());
	}
	
	@Override
	public FilterCriterion getCriterionByUid(String uid) {
		return getObjectByUid(getCriteria(), uid);
	}
	
	@Override
	public void addCriterion(FilterCriterion criterion) {
		Assert.notNull(criterion, C.CRITERION);
		
		criterion.setFilter(this);
		if (criteria == null) {
			criteria = new ArrayList<>();
		}
		criteria.add(criterion);
	}
	
	@Override
	public void removeCriterion(FilterCriterion criterion) {
		Assert.notNull(criterion, C.CRITERION);
		
		getCriteria().remove(criterion);
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !Filter.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final Filter otherFilter = (Filter) other;
		if (!new EqualsBuilder()
			.append(hqlQuery, otherFilter.getHqlQuery())
			.isEquals()) {
			return false;
		}
		return isEqualCriteria(otherFilter);
	}
	
	private boolean isEqualCriteria(Filter otherFilter) {
		if (hasCriteria()) {
			for (FilterCriterion criterion : getCriteria()) {
				if (!criterion.isEqual(otherFilter.getCriterionByUid(criterion.getUid()))) {
					return false;
				}
			}
		}
		if (otherFilter.hasCriteria()) {
			for (FilterCriterion otherCriterion : otherFilter.getCriteria()) {
				if (getCriterionByUid(otherCriterion.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getCriteria());
	}
	
	@Override
	public void initUid() {
		super.initUid();
		initUids(getCriteria());
	} 
	
}
