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

import org.seed.core.application.AbstractOrderedTransferableObject;

@Entity
@Table(name = "sys_form_printout")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FormPrintout extends AbstractOrderedTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
	private FormMetadata form;
	
	private String name;
	
	private String fileName;
	
	private String contentType;
	
	private byte[] content;
	
	@XmlTransient
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = (FormMetadata) form;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}
	
	@XmlAttribute
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final FormPrintout otherPrintout = (FormPrintout) other;
		return new EqualsBuilder()
			.append(getOrder(), otherPrintout.getOrder())
			.append(name, otherPrintout.name)
			.append(fileName, otherPrintout.fileName)
			.append(contentType, otherPrintout.contentType)
			.append(content, otherPrintout.content)
			.isEquals();
	}
	
}
