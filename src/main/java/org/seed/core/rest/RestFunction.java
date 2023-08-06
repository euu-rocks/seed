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

import org.seed.core.api.RestFunction.MethodType;
import org.seed.core.application.AbstractContentObject;
import org.seed.core.codegen.CodeManagerImpl;
import org.seed.core.codegen.GeneratedObject;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "sys_rest_function")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RestFunction extends AbstractContentObject
	implements GeneratedObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rest_id")
	@JsonIgnore
	private RestMetadata rest;
	
	private String mapping;
	
	private MethodType method;
	
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
	
	public String getNameMapping() {
		return RestMetadata.getMapping(getInternalName());
	}
	
	@XmlAttribute
	public MethodType getMethod() {
		return method;
	}

	public void setMethod(MethodType method) {
		this.method = method;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final RestFunction otherFunction = (RestFunction) other;
		return new EqualsBuilder()
				.append(getOrder(), otherFunction.getOrder())
				.append(getName(), otherFunction.getName())
				.append(mapping, otherFunction.getMapping())
				.append(method, otherFunction.getMethod())
				.append(getContent(), otherFunction.getContent())
				.isEquals();
	}

	@Override
	public String getGeneratedPackage() {
		return CodeManagerImpl.GENERATED_REST_PACKAGE + '.' + rest.getInternalName().toLowerCase();
	}

	@Override
	public String getGeneratedClass() {
		return StringUtils.capitalize(getInternalName());
	}

}
