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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.util.Assert;

import org.springframework.util.ObjectUtils;

@Entity
@Table(name = "sys_rest")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RestMetadata extends AbstractApplicationEntity
	implements Rest {
	
	static final String PACKAGE_NAME = "org.seed.generated.rest";
	
	@OneToMany(mappedBy = "rest",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<RestMapping> mappings;
	
	@OneToMany(mappedBy = "rest",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<RestPermission> permissions;
	
	private String mapping;

	public String getMapping() {
		return mapping;
	}

	public void setMapping(String mapping) {
		this.mapping = mapping;
	}

	public void setPermissions(List<RestPermission> permissions) {
		this.permissions = permissions;
	}

	@Override
	public boolean hasMappings() {
		return !ObjectUtils.isEmpty(getMappings());
	}
	
	@Override
	@XmlElement(name="restmapping")
	@XmlElementWrapper(name="restmappings")
	public List<RestMapping> getMappings() {
		return mappings;
	}

	public void setMappings(List<RestMapping> mappings) {
		this.mappings = mappings;
	}

	@Override
	public boolean hasPermissions() {
		return !ObjectUtils.isEmpty(getPermissions());
	}

	@Override
	@XmlElement(name="permission")
	@XmlElementWrapper(name="permissions")
	public List<RestPermission> getPermissions() {
		return permissions;
	}
	
	@Override
	public void addMapping(RestMapping mapping) {
		Assert.notNull(mapping, "mapping");
		
		if (mappings == null) {
			mappings = new ArrayList<>();
		}
		mapping.setRest(this);
		mappings.add(mapping);
	}
	
	@Override
	public RestMapping getMappingByUid(String uid) {
		return getObjectByUid(getMappings(), uid);
	}
	
	@Override
	public RestPermission getPermissionByUid(String uid) {
		return getObjectByUid(getPermissions(), uid);
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !Rest.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final Rest otherRest = (Rest) other;
		if (!new EqualsBuilder()
				.append(getName(), otherRest.getName())
				.isEquals()) {
			return false;
		}
		return isEqualsMappings(otherRest) && 
			   isEqualPermissions(otherRest);
	}
	
	private boolean isEqualsMappings(Rest otherRest) {
		if (hasMappings()) {
			for (RestMapping mapping : getMappings()) {
				if (!mapping.isEqual(otherRest.getMappingByUid(mapping.getUid()))) {
					return false;
				}
			}
		}
		if (otherRest.hasMappings()) {
			for (RestMapping otherMapping : otherRest.getMappings()) {
				if (getMappingByUid(otherMapping.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean isEqualPermissions(Rest otherRest) {
		if (hasPermissions()) {
			for (RestPermission permission : getPermissions()) {
				if (!permission.isEqual(otherRest.getPermissionByUid(permission.getUid()))) {
					return false;
				}
			}
		}
		if (otherRest.hasPermissions()) {
			for (RestPermission otherPermission : otherRest.getPermissions()) {
				if (getPermissionByUid(otherPermission.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getMappings());
		removeNewObjects(getPermissions());
	}
	
	@Override
	public void initUid() {
		super.initUid();
		initUids(getMappings());
		initUids(getPermissions());
	}
	
	void createLists() {
		permissions = new ArrayList<>();
	}

}
