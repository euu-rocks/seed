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
package org.seed.core.data;

import java.util.List;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.seed.core.util.NameUtils;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

@MappedSuperclass
public abstract class AbstractSystemEntity extends AbstractSystemObject 
	implements SystemEntity {
	
	private String name;
	
	@Transient
	@JsonIgnore
	private Options options;
	
	@Override
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	@JsonIgnore
	public final String getInternalName() {
		return NameUtils.getInternalName(getName());
	}
	
	@Override
	@XmlTransient
	@SuppressWarnings("unchecked")
	public <T extends Options> T getOptions() {
		return (T) options;
	}

	void setOptions(Options options) {
		this.options = options;
	}

	@Override
	public void removeNewObjects() {
		// do nothing by default
	}
	
	protected static <T extends SystemEntity> T getObjectByName(List<T> list, String name, boolean ignoreCase) {
		Assert.notNull(name, "name is null");
		
		if (list != null) {
			for (T object : list) {
				if ((ignoreCase && name.equalsIgnoreCase(object.getName())) || 
					name.equals(object.getName())) {
					return object;
				}
			}
		}
		return null;
	}
	
}
