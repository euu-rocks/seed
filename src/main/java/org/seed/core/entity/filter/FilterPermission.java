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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractPermissionObject;
import org.seed.core.application.Permission;

@Entity
@Table(name = "sys_entity_filter_perm")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FilterPermission extends AbstractPermissionObject
	implements Permission<FilterPermission.FilterAccess> {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_id")
	private FilterMetadata filter;
	
	@XmlTransient
	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = (FilterMetadata) filter;
	}

	@Override
	public FilterAccess getAccess() {
		return null;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !FilterPermission.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final FilterPermission otherPermission = (FilterPermission) other;
		return new EqualsBuilder()
			.append(getUserGroupUid(), otherPermission.getUserGroupUid())
			.isEquals();
	}
	
	enum FilterAccess {
		// dummy access
	}
	
}
