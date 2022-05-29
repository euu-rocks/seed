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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@Table(name = "sys_revision")
@org.hibernate.envers.RevisionEntity(Revision.RevisionListener.class)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RevisionEntity implements Revision {
	
	@Id
	@SequenceGenerator(name="seqGen", sequenceName="seed_id_seq", initialValue=1000, allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seqGen")
	@RevisionNumber
	private int id;

	@RevisionTimestamp
	private long revisiontime;
	
	private String author;

	@Override
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public long getRevisiontime() {
		return revisiontime;
	}

	public void setRevisiontime(long revisiontime) {
		this.revisiontime = revisiontime;
	}
	
	@Override
	@Transient
	public Date getRevisionDate() {
		return new Date(revisiontime);
	}

	@Override
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this ||
				(obj instanceof RevisionEntity && 
				 getId() == ((RevisionEntity) obj).getId());
	}
	
	@Override
	public int hashCode() {
		return Integer.valueOf(id).hashCode();
	}

}
