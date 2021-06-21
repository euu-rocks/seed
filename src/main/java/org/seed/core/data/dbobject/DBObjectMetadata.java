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
package org.seed.core.data.dbobject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.util.CDATAXmlAdapter;

@Entity
@Table(name = "sys_dbobject")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DBObjectMetadata extends AbstractApplicationEntity 
	implements DBObject {
	
	private DBObjectType type;
	
	@Column(name = "ordernum")
	private Integer order;
	
	private String content;
	
	@Override
	@XmlAttribute
	public DBObjectType getType() {
		return type;
	}

	public void setType(DBObjectType type) {
		this.type = type;
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
	@XmlJavaTypeAdapter(CDATAXmlAdapter.class)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !DBObject.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final DBObject otherObject = (DBObject) other;
		return new EqualsBuilder()
				.append(getName(), otherObject.getName())
				.append(type, otherObject.getType())
				.append(order, otherObject.getOrder())
				.append(content, otherObject.getContent())
				.isEquals();
	}
	
}
