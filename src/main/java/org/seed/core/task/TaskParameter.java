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
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractParameterObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "sys_task_param")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TaskParameter extends AbstractParameterObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
	@JsonIgnore
	private TaskMetadata task;
	
	@XmlTransient
	public Task getTask() {
		return task;
	}
	
	public void setTask(Task task) {
		this.task = (TaskMetadata) task;
	}
	
}
