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

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.seed.C;
import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.codegen.CodeUtils;
import org.seed.core.util.Assert;
import org.seed.core.util.CDATAXmlAdapter;

@Entity
@Table(name = "sys_customcode")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CustomCodeMetadata extends AbstractApplicationEntity 
	implements CustomCode {
	
	private String content;
	
	@Override
	@XmlJavaTypeAdapter(CDATAXmlAdapter.class)
	public String getContent() {
		return content;
	}
	
	@Override
	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public String getQualifiedName() {
		Assert.hasText(content, C.CONTENT);
		
		return CodeUtils.extractQualifiedName(content);
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final CustomCode otherCode = (CustomCode) other;
		return new EqualsBuilder()
				.append(getName(), otherCode.getName())
				.append(content, otherCode.getContent())
				.isEquals();
	}

}
