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
package org.seed.core.rest;

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

@javax.persistence.Entity
@Table(name = "sys_rest_permission")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RestPermission extends AbstractPermissionObject
	implements Permission<RestPermission.RestAccess> {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rest_id")
	private RestMetadata rest;
	
	@XmlTransient
	public Rest getRest() {
		return rest;
	}

	public void setRest(Rest rest) {
		this.rest = (RestMetadata) rest;
	}
	
	@Override
	public RestAccess getAccess() {
		return null;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final RestPermission otherPermission = (RestPermission) other;
		return new EqualsBuilder()
			.append(getUserGroupUid(), otherPermission.getUserGroupUid())
			.isEquals();
	}
	
	enum RestAccess {
		// dummy access
	}
	
}
