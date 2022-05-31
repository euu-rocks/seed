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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.TransferableObject;
import org.seed.core.data.AbstractSystemEntity;

@Entity
@Table(name = "sys_module_param")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ModuleParameter extends AbstractSystemEntity
	implements TransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
	private ModuleMetadata module;
	
	private String uid;
	
	private String value;
	
	@XmlTransient
	public Module getModule() {
		return module;
	}

	public void setModule(Module module) {
		this.module = (ModuleMetadata) module;
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
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !ModuleParameter.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final ModuleParameter otherParameter = (ModuleParameter) other;
		return new EqualsBuilder()
				.append(getName(), otherParameter.getName())
				.append(getValue(), otherParameter.getValue())
				.isEquals();
	}
	
}
