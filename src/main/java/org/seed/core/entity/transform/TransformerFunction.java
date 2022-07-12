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
package org.seed.core.entity.transform;

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
import org.seed.core.codegen.CodeManagerImpl;
import org.seed.core.codegen.GeneratedObject;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "sys_entity_transform_func")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TransformerFunction extends AbstractContentObject 
	implements GeneratedObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transform_id")
	@JsonIgnore
	private TransformerMetadata transformer;
	
	private boolean isActiveBeforeTransformation;
	
	private boolean isActiveAfterTransformation;
	
	@XmlTransient
	public Transformer getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = (TransformerMetadata) transformer;
	}
	
	@XmlAttribute
	public boolean isActiveBeforeTransformation() {
		return isActiveBeforeTransformation;
	}

	public void setActiveBeforeTransformation(boolean isActiveBeforeTransformation) {
		this.isActiveBeforeTransformation = isActiveBeforeTransformation;
	}
	
	@XmlAttribute
	public boolean isActiveAfterTransformation() {
		return isActiveAfterTransformation;
	}

	public void setActiveAfterTransformation(boolean isActiveAfterTransformation) {
		this.isActiveAfterTransformation = isActiveAfterTransformation;
	}
	
	@Override
	@JsonIgnore
	public String getGeneratedPackage() {
		return CodeManagerImpl.GENERATED_TRANSFORM_PACKAGE + '.' + 
				getTransformer().getInternalName().toLowerCase();
	}
	
	@Override
	@JsonIgnore
	public String getGeneratedClass() {
		return StringUtils.capitalize(getInternalName());
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final TransformerFunction otherFunction = (TransformerFunction) other;
		return new EqualsBuilder()
				.append(getName(), otherFunction.getName())
				.append(getContent(), otherFunction.getContent())
				.append(isActiveBeforeTransformation, otherFunction.isActiveBeforeTransformation)
				.append(isActiveAfterTransformation, otherFunction.isActiveAfterTransformation)
				.isEquals();
	}

}
