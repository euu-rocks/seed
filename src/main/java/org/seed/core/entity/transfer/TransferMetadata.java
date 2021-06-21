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
package org.seed.core.entity.transfer;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.data.Order;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityMetadata;

import org.springframework.util.ObjectUtils;

@javax.persistence.Entity
@Table(name = "sys_entity_transfer")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TransferMetadata extends AbstractApplicationEntity implements Transfer {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id")
	private EntityMetadata entity;
	
	@OneToMany(mappedBy = "transfer",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OrderBy("order")
	private List<TransferElement> elements;
	
	private TransferFormat format;
	
	private String separatorChar;
	
	private String quoteChar;
	
	private String escapeChar;
	
	private CharEncoding encoding;
	
	private Newline newline;
	
	private boolean quoteAll;
	
	private boolean header;
	
	@Transient
	private String entityUid;
	
	@Override
	@XmlTransient
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = (EntityMetadata) entity;
	}
	
	@Override
	public boolean hasElements() {
		return !ObjectUtils.isEmpty(getElements());
	}
	
	@Override
	@XmlElement(name="element")
	@XmlElementWrapper(name="elements")
	public List<TransferElement> getElements() {
		return elements;
	}
	
	public void setElements(List<TransferElement> elements) {
		this.elements = elements;
	}
	
	@Override
	@XmlAttribute
	public TransferFormat getFormat() {
		return format;
	}

	public void setFormat(TransferFormat format) {
		this.format = format;
	}
	
	@Override
	@XmlAttribute
	public String getSeparatorChar() {
		return separatorChar;
	}

	public void setSeparatorChar(String separatorChar) {
		this.separatorChar = separatorChar;
	}
	
	@Override
	@XmlAttribute
	public String getQuoteChar() {
		return quoteChar;
	}

	public void setQuoteChar(String quoteChar) {
		this.quoteChar = quoteChar;
	}
	
	@Override
	@XmlAttribute
	public String getEscapeChar() {
		return escapeChar;
	}

	public void setEscapeChar(String escapeChar) {
		this.escapeChar = escapeChar;
	}
	
	@Override
	@XmlAttribute
	public CharEncoding getEncoding() {
		return encoding;
	}

	public void setEncoding(CharEncoding encoding) {
		this.encoding = encoding;
	}

	@Override
	@XmlAttribute
	public Newline getNewline() {
		return newline;
	}

	public void setNewline(Newline newline) {
		this.newline = newline;
	}
	
	@Override
	@XmlAttribute
	public boolean isQuoteAll() {
		return quoteAll;
	}

	public void setQuoteAll(boolean quoteAll) {
		this.quoteAll = quoteAll;
	}
	
	@Override
	@XmlAttribute
	public boolean isHeader() {
		return header;
	}

	public void setHeader(boolean header) {
		this.header = header;
	}
	
	@Override
	@XmlAttribute
	public String getEntityUid() {
		return entity != null ? entity.getUid() : entityUid;
	}

	public void setEntityUid(String entityUid) {
		this.entityUid = entityUid;
	}
	
	@Override
	public boolean containsField(EntityField entityField) {
		if (hasElements()) {
			for (TransferElement element : getElements()) {
				if (entityField.equals(element.getEntityField())) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !Transfer.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final Transfer otherTransfer = (Transfer) other;
		if (!new EqualsBuilder()
			.append(format, otherTransfer.getFormat())
			.append(separatorChar, otherTransfer.getSeparatorChar())
			.append(quoteChar, otherTransfer.getQuoteChar())
			.append(escapeChar, otherTransfer.getEscapeChar())
			.append(encoding, otherTransfer.getEncoding())
			.append(newline, otherTransfer.getNewline())
			.append(quoteAll, otherTransfer.getQuoteChar())
			.append(header, otherTransfer.isHeader())
			.isEquals()) {
			return false;
		}
		return isEqualElements(otherTransfer);
	}
	
	private boolean isEqualElements(Transfer otherTransfer) {
		if (hasElements()) {
			for (TransferElement element : getElements()) {
				if (!element.isEqual(otherTransfer.getElementByUid(element.getUid()))) {
					return false;
				}
			}
		}
		if (otherTransfer.hasElements()) {
			for (TransferElement otherElement : otherTransfer.getElements()) {
				if (getElementByUid(otherElement.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public EntityField getIdentifierField() {
		if (hasElements()) {
			for (TransferElement element : getElements()) {
				if (element.isIdentifier()) {
					return element.getEntityField();
				}
			}
		}
		return null;
	}
	
	@Override
	public List<EntityField> getElementFields() {
		final List<EntityField> result = new ArrayList<>();
		if (hasElements()) {
			for (TransferElement element : getElements()) {
				result.add(element.getEntityField());
			}
		}
		return result;
	}
	
	@Override
	public TransferElement getElementByUid(String uid) {
		return getObjectByUid(getElements(), uid);
	}

	@Override
	protected void setOrderIndexes() {
		Order.setOrderIndexes(getElements());
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getElements());
	}
	
	@Override
	public void initUids() {
		super.initUids();
		initUids(getElements());
	}
	
	void createLists() {
		elements = new ArrayList<>();
	}

}
