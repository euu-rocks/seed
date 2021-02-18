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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.TransferableObject;
import org.seed.core.codegen.GeneratedObject;
import org.seed.core.data.AbstractOrderedSystemObject;
import org.seed.core.util.CDATAXmlAdapter;
import org.seed.core.util.NameUtils;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "sys_entity_transform_func")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TransformerFunction extends AbstractOrderedSystemObject 
	implements GeneratedObject, TransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transform_id")
	private TransformerMetadata transformer;
	
	private String uid;
	
	private String name;
	
	private String content;
	
	private boolean isActiveBeforeTransformation;
	
	private boolean isActiveAfterTransformation;
	
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
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlJavaTypeAdapter(CDATAXmlAdapter.class)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

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
	
	@JsonIgnore
	public String getInternalName() {
		return NameUtils.getInternalName(getName());
	}
	
	@Override
	@JsonIgnore
	public String getGeneratedPackage() {
		return Transformer.PACKAGE_NAME + '.' + getTransformer().getInternalName().toLowerCase();
	}
	
	@Override
	@JsonIgnore
	public String getGeneratedClass() {
		return StringUtils.capitalize(getInternalName());
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !TransformerFunction.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final TransformerFunction otherFunction = (TransformerFunction) other;
		return new EqualsBuilder()
				.append(name, otherFunction.name)
				.append(content, otherFunction.content)
				.append(isActiveBeforeTransformation, otherFunction.isActiveBeforeTransformation)
				.append(isActiveAfterTransformation, otherFunction.isActiveAfterTransformation)
				.isEquals();
	}

}
