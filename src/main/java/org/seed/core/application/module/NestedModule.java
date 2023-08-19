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
package org.seed.core.application.module;

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

import org.seed.C;
import org.seed.core.application.AbstractOrderedTransferableObject;
import org.seed.core.data.Options;
import org.seed.core.data.SystemEntity;
import org.seed.core.util.Assert;
import org.seed.core.util.ReferenceJsonSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "sys_module_nested")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NestedModule extends AbstractOrderedTransferableObject
	implements SystemEntity {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_module_id")
	@JsonIgnore
	private ModuleMetadata parentModule;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nested_module_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private ModuleMetadata nested;
	
	@Transient
	private String nestedModuleUid;
	
	@XmlTransient
	public Module getParentModule() {
		return parentModule;
	}

	public void setParentModule(Module parentModule) {
		this.parentModule = (ModuleMetadata) parentModule;
	}
	
	@XmlTransient
	@JsonIgnore
	public Module getNestedModule() {
		return nested;
	}

	public void setNestedModule(Module nestedModule) {
		this.nested = (ModuleMetadata) nestedModule;
	}
	
	@XmlAttribute
	public String getNestedModuleUid() {
		return nested != null ? nested.getUid() : nestedModuleUid;
	}

	public void setNestedModuleUid(String nestedModuleUid) {
		this.nestedModuleUid = nestedModuleUid;
	}
	
	public boolean containsModule(Module module) {
		Assert.notNull(module, C.MODULE);
		
		return nested != null && nested.containsModule(module);
	}
	
	@JsonIgnore
	public String getFileName() {
		Assert.stateAvailable(nested, "nested module");
		
		return nested.getInternalName()
					 .concat(ModuleTransfer.MODULE_FILE_EXTENSION);
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final NestedModule otherNested = (NestedModule) other;
		return new EqualsBuilder()
				.append(getOrder(), otherNested.getOrder())
				.append(nestedModuleUid, otherNested.getNestedModuleUid())
				.isEquals();
	}
	
	@Override
	@JsonIgnore
	public <T extends Options> T getOptions() {
		return null;
	}

	@Override
	@JsonIgnore
	@XmlTransient
	public String getName() {
		return nested != null 
				? nested.getName() 
				: null;
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	@JsonIgnore
	public String getInternalName() {
		return nested != null 
				? nested.getInternalName() 
				: null;
	}

	@Override
	public void removeNewObjects() {
		// do nothing
	}
	
}
