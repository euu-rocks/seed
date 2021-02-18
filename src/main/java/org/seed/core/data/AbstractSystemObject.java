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

import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlTransient;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

@MappedSuperclass
public abstract class AbstractSystemObject implements SystemObject {
	
	@Id
	@SequenceGenerator(name="seqGen", sequenceName="seed_id_seq", initialValue=1000, allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seqGen")
	private Long id;
	
	@Version
	private int version;
	
	private Date createdOn;
	
	private String createdBy;
	
	private Date modifiedOn;
	
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
		return getId() == null;
	}
	
	@Override
	public boolean isEqual(Object other) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int hashCode() {
		return isNew() ? super.hashCode() : getId().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this ||
				(!isNew() && obj instanceof SystemObject && 
				 getId().equals(((SystemObject) obj).getId()));
	}
	
	public void copySystemFieldsTo(SystemObject object) {
		Assert.notNull(object, "object is null");
		
		final AbstractSystemObject other = (AbstractSystemObject) object;
		other.id = id;
		other.version = version;
		other.createdBy = createdBy;
		other.createdOn = createdOn;
		other.modifiedBy = modifiedBy;
		other.modifiedOn = modifiedOn;
	}
	
	protected void setOrderIndexes() {
		// do nothing by default
	}
	
	protected static <T extends SystemObject> T getObjectById(List<T> list, Long id) {
		Assert.notNull(id, "id is null");
		
		if (list != null) {
			for (T object : list) {
				if (id.equals(object.getId())) {
					return object;
				}
			}
		}
		return null;
	}
	
	protected static <T extends SystemObject> List<T> subList(List<T> list, Predicate<T> predicate) {
		Assert.notNull(predicate, "predicate is null");
		
		return list != null 
				? list.stream()
					  .filter(predicate)
					  .collect(Collectors.toList()) 
				: null;
	}
	
	protected static <T extends SystemObject> void removeNewObjects(List<T> list) {
		if (list != null) {
			try {
				list.removeIf(o -> o.isNew());
			}
			catch (Exception ex) {
				// ignore exceptions
			}
		}
	}

}
