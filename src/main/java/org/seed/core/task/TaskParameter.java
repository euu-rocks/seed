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
package org.seed.core.task;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;

import org.seed.core.application.TransferableObject;
import org.seed.core.data.AbstractSystemObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "sys_task_param")
public class TaskParameter extends AbstractSystemObject
	implements TransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
	@JsonIgnore
	private TaskMetadata task;
	
	private String uid;
	
	private String name;
	
	private String value;
	
	@XmlTransient
	public Task getTask() {
		return task;
	}
	
	public void setTask(Task task) {
		this.task = (TaskMetadata) task;
	}
	
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
	
	@XmlAttribute
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !TaskParameter.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final TaskParameter otherParameter = (TaskParameter) other;
		return new EqualsBuilder()
			.append(name, otherParameter.name)
			.append(value, otherParameter.value)
			.isEquals();
	}
	
}
