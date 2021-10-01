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
package org.seed.core.customcode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractApplicationEntity;

@Entity
@Table(name = "sys_customlib")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CustomLibMetadata extends AbstractApplicationEntity
	implements CustomLib {
	
	private String filename;
	
	private byte[] content;
	
	private String error;
	
	@Column(name = "ordernum")
	private Integer order;
	
	@Override
	@XmlAttribute
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	@Override
	@XmlTransient
	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}
	
	@Override
	@XmlTransient
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@Override
	@XmlAttribute
	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !CustomLib.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final CustomLib otherLib = (CustomLib) other;
		return new EqualsBuilder()
				.append(getName(), otherLib.getName())
				.append(filename, otherLib.getFilename())
				.append(content, otherLib.getContent())
				.append(order, otherLib.getOrder())
				.isEquals();
	}
	
}
