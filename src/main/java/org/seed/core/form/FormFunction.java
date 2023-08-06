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
package org.seed.core.form;

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
import org.seed.core.data.Options;
import org.seed.core.data.SystemEntity;
import org.springframework.util.StringUtils;

@javax.persistence.Entity
@Table(name = "sys_form_function")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FormFunction extends AbstractContentObject
	implements SystemEntity, GeneratedObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
	private FormMetadata form;
	
	private boolean isInitial;
	
	@XmlTransient
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = (FormMetadata) form;
	}
	
	@XmlAttribute
	public boolean isInitial() {
		return isInitial;
	}

	public void setInitial(boolean isInitial) {
		this.isInitial = isInitial;
	}

	@Override
	public String getGeneratedPackage() {
		return CodeManagerImpl.GENERATED_FORM_PACKAGE + '.' + form.getInternalName().toLowerCase();
	}

	@Override
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
		final FormFunction otherFunction = (FormFunction) other;
		return new EqualsBuilder()
				.append(getOrder(), otherFunction.getOrder())
				.append(getName(), otherFunction.getName())
				.append(getContent(), otherFunction.getContent())
				.append(isInitial, otherFunction.isInitial())
				.isEquals();
	}

	@Override
	public <T extends Options> T getOptions() {
		return null;
	}

	@Override
	public void removeNewObjects() {
		// do nothing
	}
	
}
