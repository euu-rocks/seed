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

import org.seed.core.application.AbstractContentObject;
import org.seed.core.codegen.GeneratedObject;

import org.springframework.util.StringUtils;

@Entity
@Table(name = "sys_rest_mapping")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RestMapping extends AbstractContentObject
	implements GeneratedObject {
	
	static final String PACKAGE_NAME = "org.seed.generated.rest";
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rest_id")
	private RestMetadata rest;
	
	private String mapping;
	
	private RestMethodType method;
	
	@XmlTransient
	public RestMetadata getRest() {
		return rest;
	}

	public void setRest(Rest rest) {
		this.rest = (RestMetadata) rest;
	}

	@XmlAttribute
	public String getMapping() {
		return mapping;
	}

	public void setMapping(String mapping) {
		this.mapping = mapping;
	}
	
	@XmlAttribute
	public RestMethodType getMethod() {
		return method;
	}

	public void setMethod(RestMethodType method) {
		this.method = method;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !RestMapping.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final RestMapping otherMapping = (RestMapping) other;
		return new EqualsBuilder()
				.append(getName(), otherMapping.getName())
				.append(mapping, otherMapping.getMapping())
				.append(method, otherMapping.getMethod())
				.append(getContent(), otherMapping.getContent())
				.isEquals();
	}

	@Override
	public String getGeneratedPackage() {
		return PACKAGE_NAME + '.' + rest.getInternalName();
	}

	@Override
	public String getGeneratedClass() {
		return StringUtils.capitalize(getInternalName());
	}

}
