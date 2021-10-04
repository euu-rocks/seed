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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.seed.C;
import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.util.Assert;
import org.seed.core.util.CDATAXmlAdapter;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "sys_task")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TaskMetadata extends AbstractApplicationEntity 
	implements Task {
	
	static final String PACKAGE_NAME = "org.seed.generated.task";
	
	private Date startTime;
	
	private Integer repeatInterval;
	
	private IntervalUnit repeatIntervalUnit; 
	
	private String cronExpression;
	
	@JsonIgnore
	private String content;
	
	private boolean isActive;
	
	@Transient
	private boolean contentChanged;
	
	@OneToMany(mappedBy = "task",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	private List<TaskParameter> parameters;
	
	@OneToMany(mappedBy = "task",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	private List<TaskPermission> permissions;
	
	@OneToMany(mappedBy = "task",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	private List<TaskNotification> notifications;
	
	@OneToMany(mappedBy = "task",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@OrderBy("startTime")
	private List<TaskRun> runs;
	
	@Override
	@XmlAttribute
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	@Override
	@XmlAttribute
	public Integer getRepeatInterval() {
		return repeatInterval;
	}

	public void setRepeatInterval(Integer repeatInterval) {
		this.repeatInterval = repeatInterval;
	}
	
	@Override
	@XmlAttribute
	public IntervalUnit getRepeatIntervalUnit() {
		return repeatIntervalUnit;
	}

	public void setRepeatIntervalUnit(IntervalUnit repeatIntervalUnit) {
		this.repeatIntervalUnit = repeatIntervalUnit;
	}

	@Override
	@XmlAttribute
	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	
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
	@XmlAttribute
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	@XmlTransient
	public boolean isContentChanged() {
		return contentChanged;
	}

	public void setContentChanged(boolean contentChanged) {
		this.contentChanged = contentChanged;
	}
	
	@Override
	@JsonIgnore
	public String getGeneratedPackage() {
		return PACKAGE_NAME;
	}

	@Override
	@JsonIgnore
	public String getGeneratedClass() {
		return StringUtils.capitalize(getInternalName());
	}
	
	@Override
	public boolean hasParameters() {
		return !ObjectUtils.isEmpty(getParameters());
	}
	
	@Override
	public TaskParameter getParameterByUid(String uid) {
		return getObjectByUid(getParameters(), uid);
	}
	
	@Override
	@XmlElement(name="parameter")
	@XmlElementWrapper(name="parameters")
	public List<TaskParameter> getParameters() {
		return parameters;
	}
	
	@Override
	public void addParameter(TaskParameter parameter) {
		Assert.notNull(parameter, C.PARAMETER);
		
		if (parameters == null) {
			parameters = new ArrayList<>();
		}
		parameter.setTask(this);
		parameters.add(parameter);
	}
	
	@Override
	public void removeParameter(TaskParameter parameter) {
		Assert.notNull(parameter, C.PARAMETER);
		
		getParameters().remove(parameter);
	}

	public void setParameters(List<TaskParameter> params) {
		this.parameters = params;
	}
	
	@Override
	public boolean hasPermissions() {
		return !ObjectUtils.isEmpty(getPermissions());
	}
	
	@Override
	@XmlElement(name="permission")
	@XmlElementWrapper(name="permissions")
	public List<TaskPermission> getPermissions() {
		return permissions;
	}
	
	@Override
	public TaskPermission getPermissionByUid(String uid) {
		return getObjectByUid(getPermissions(), uid);
	}

	public void setPermissions(List<TaskPermission> permissions) {
		this.permissions = permissions;
	}
	
	@Override
	public boolean hasNotifications() {
		return !ObjectUtils.isEmpty(getNotifications());
	}
	
	@Override
	@XmlTransient
	@JsonIgnore
	public List<TaskNotification> getNotifications() {
		return notifications;
	}
	
	@Override
	public void addNotification(TaskNotification notification) {
		Assert.notNull(notification, "notification");
		
		if (notifications == null) {
			notifications = new ArrayList<>();
		}
		notification.setTask(this);
		notifications.add(notification);
	}
	
	@Override
	public void removeNotification(TaskNotification notification) {
		Assert.notNull(notification, "notification");
		
		getNotifications().remove(notification);
	}

	public void setNotifications(List<TaskNotification> notifications) {
		this.notifications = notifications;
	}

	@Override
	@XmlTransient
	@JsonIgnore
	public List<TaskRun> getRuns() {
		return runs;
	}
	
	@Override
	public TaskRun getRunById(Long id) {
		return getObjectById(runs, id);
	}
	
	@Override
	public void addRun(TaskRun run) {
		Assert.notNull(run, "run");
		
		if (runs == null) {
			runs = new ArrayList<>();
		}
		run.setTask(this);
		runs.add(run);
	}

	public void setRuns(List<TaskRun> runs) {
		this.runs = runs;
	}

	@Override
	public TaskResult getLastResult() {
		return !ObjectUtils.isEmpty(getRuns()) 
				? getRuns().get(getRuns().size() - 1).getResult() 
				: null;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !Task.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final Task otherTask = (Task) other;
		if (!new EqualsBuilder()
				.append(getName(), otherTask.getName())
				.append(startTime, otherTask.getStartTime())
				.append(repeatInterval, otherTask.getRepeatInterval())
				.append(repeatIntervalUnit, otherTask.getRepeatIntervalUnit())
				.append(cronExpression, otherTask.getCronExpression())
				.append(content, otherTask.getContent())
				.append(isActive, otherTask.isActive())
				.isEquals()) {
			return false;
		}
		return isEqualParameters(otherTask) &&
			   isEqualPermissions(otherTask);
	}
	
	private boolean isEqualParameters(Task otherTask) {
		if (hasParameters()) {
			for (TaskParameter parameter : getParameters()) {
				if (!parameter.isEqual(otherTask.getParameterByUid(parameter.getUid()))) {
					return false;
				}
			}
		}
		if (otherTask.hasParameters()) {
			for (TaskParameter otherParameter : otherTask.getParameters()) {
				if (getParameterByUid(otherParameter.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean isEqualPermissions(Task otherTask) {
		if (hasPermissions()) {
			for (TaskPermission permission : getPermissions()) {
				if (!permission.isEqual(otherTask.getPermissionByUid(permission.getUid()))) {
					return false;
				}
			}
		}
		if (otherTask.hasPermissions()) {
			for (TaskPermission otherPermission : otherTask.getPermissions()) {
				if (getPermissionByUid(otherPermission.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getParameters());
		removeNewObjects(getPermissions());
		removeNewObjects(getNotifications());
	}
	
	@Override
	public void initUid() {
		super.initUid();
		initUids(getParameters());
		initUids(getPermissions());
	}
	
	void createLists() {
		permissions = new ArrayList<>();
	}

}