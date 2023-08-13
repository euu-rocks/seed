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

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.C;
import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.util.Assert;
import org.seed.core.util.NameUtils;

@Entity
@Table(name = "sys_rest")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RestMetadata extends AbstractApplicationEntity
	implements Rest {
	
	@OneToMany(mappedBy = "rest",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<RestFunction> functions;
	
	@OneToMany(mappedBy = "rest",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<RestPermission> permissions;
	
	private String mapping;
	
	@Override
	@XmlAttribute
	public String getMapping() {
		return mapping;
	}

	public void setMapping(String mapping) {
		this.mapping = mapping;
	}
	
	public String getNameMapping() {
		return getMapping(getInternalName());
	}
	
	@Override
	public boolean hasFunctions() {
		return notEmpty(getFunctions());
	}
	
	@Override
	@XmlElement(name="function")
	@XmlElementWrapper(name="functions")
	public List<RestFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(List<RestFunction> functions) {
		this.functions = functions;
	}

	@Override
	public boolean hasPermissions() {
		return notEmpty(getPermissions());
	}

	@Override
	@XmlElement(name="permission")
	@XmlElementWrapper(name="permissions")
	public List<RestPermission> getPermissions() {
		return permissions;
	}
	
	public void setPermissions(List<RestPermission> permissions) {
		this.permissions = permissions;
	}
	
	@Override
	public void addFunction(RestFunction function) {
		Assert.notNull(function, C.FUNCTION);
		
		if (functions == null) {
			functions = new ArrayList<>();
		}
		function.setRest(this);
		functions.add(function);
	}
	
	@Override
	public void removeFunction(RestFunction function) {
		Assert.notNull(function, C.FUNCTION);
		
		getFunctions().remove(function);
	}
	
	@Override
	public RestFunction getFunctionByMapping(String mapping) {
		Assert.notNull("mapping", mapping);
		final String mappingStr = '/' + mapping;
		
		RestFunction function = null;
		function = firstMatch(getFunctions(), func -> mappingStr.equals(func.getMapping()));
		if (function == null) {
			function = firstMatch(getFunctions(), 
								  func -> func.getInternalName().equalsIgnoreCase(mapping));
		}
		return function;
	}
	
	@Override
	public RestFunction getFunctionByUid(String uid) {
		return getObjectByUid(getFunctions(), uid);
	}
	
	@Override
	public RestPermission getPermissionByUid(String uid) {
		return getObjectByUid(getPermissions(), uid);
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
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
		return isEqualFunctions(otherRest) && 
			   isEqualPermissions(otherRest);
	}
	
	private boolean isEqualFunctions(Rest otherRest) {
		return !(anyMatch(functions, function -> !function.isEqual(otherRest.getFunctionByUid(function.getUid()))) || 
				 anyMatch(otherRest.getFunctions(), function -> getFunctionByUid(function.getUid()) == null));
	}
	
	private boolean isEqualPermissions(Rest otherRest) {
		return !(anyMatch(permissions, permission -> !permission.isEqual(otherRest.getPermissionByUid(permission.getUid()))) || 
				 anyMatch(otherRest.getPermissions(), permission -> getPermissionByUid(permission.getUid()) == null));
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getFunctions());
		removeNewObjects(getPermissions());
	}
	
	@Override
	public void initUid() {
		super.initUid();
		initUids(getFunctions());
		initUids(getPermissions());
	}
	
	void createLists() {
		permissions = new ArrayList<>();
	}
	
	static String getMapping(String name) {
		return name != null 
				? '/' + NameUtils.getInternalName(name).toLowerCase() 
				: null;
	}

}
