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
package org.seed.core.data;

import static org.seed.core.util.CollectionUtils.firstMatch;

import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.Hibernate;

import org.seed.C;
import org.seed.core.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties("hibernateLazyInitializer")
@MappedSuperclass
public abstract class AbstractSystemObject implements SystemObject {
	
	@Id
	@SequenceGenerator(name = "seqGen", sequenceName = "seed_id_seq", 
					   initialValue = 1000, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqGen")
	private Long id;
	
	@Version
	@JsonIgnore
	private int version;
	
	@JsonIgnore
	private Date createdOn;
	
	@JsonIgnore
	private String createdBy;
	
	@JsonIgnore
	private Date modifiedOn;
	
	@JsonIgnore
	private String modifiedBy;
	
	@Override
	@XmlTransient
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	@XmlTransient
	public int getVersion() {
		return version;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public Date getCreatedOn() {
		return createdOn;
	}

	void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	
	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	@Override
	public Date getModifiedOn() {
		return modifiedOn;
	}
	
	void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}
	
	@Override
	public String getModifiedBy() {
		return modifiedBy;
	}
	
	void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	
	@Override
	@JsonIgnore
	public Date getLastModified() {
		return modifiedOn != null ? modifiedOn : createdOn;
	}
	
	@Override
	@JsonIgnore
	public final boolean isNew() {
		return getId() == null; // don't touch
	}
	
	@Override
	public boolean isEqual(Object other) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final int hashCode() {
		return isNew() ? super.hashCode() : getId().hashCode(); // don't touch
	}
	
	@Override
	public final boolean equals(Object obj) { // don't touch
		return obj == this ||
				(!isNew() && obj instanceof SystemObject && 
				 getId().equals(((SystemObject) obj).getId()));
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " id:" + id;
	}
	
	public final void copySystemFieldsTo(SystemObject object) {
		Assert.notNull(object, C.OBJECT);
		final var thisObject = (AbstractSystemObject) Hibernate.unproxy(this);
		final var targetObject = (AbstractSystemObject) object;
		
		targetObject.id = thisObject.id;
		targetObject.version = thisObject.version;
		targetObject.createdBy = thisObject.createdBy;
		targetObject.createdOn = thisObject.createdOn;
		targetObject.modifiedBy = thisObject.modifiedBy;
		targetObject.modifiedOn = thisObject.modifiedOn;
	}
	
	public final void resetId() {
		id = null;
	}
	
	protected void setOrderIndexes() {
		// do nothing by default
	}
	
	protected final boolean isInstance(Object otherObject) {
		return getClass().isInstance(otherObject);
	}
	
	protected static <T extends SystemObject> T getObjectById(List<T> list, Long id) {
		Assert.notNull(id, C.ID);
		
		return firstMatch(list, object -> id.equals(object.getId()));
	}
	
	protected static <T extends SystemObject> void removeNewObjects(List<T> list) {
		if (list != null) {
			list.removeIf(SystemObject::isNew);
		}
	}

}
