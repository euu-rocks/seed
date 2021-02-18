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
package org.seed.core.report;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.data.Order;
import org.seed.core.data.datasource.DataSourceParameter;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

@Entity
@Table(name = "sys_report")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ReportMetadata extends AbstractApplicationEntity 
	implements Report {
	
	@OneToMany(mappedBy = "report",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@OrderBy("order")
	private List<ReportDataSource> dataSources;
	
	@OneToMany(mappedBy = "report",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	private List<ReportPermission> permissions;
	
	@Override
	public boolean hasDataSources() {
		return !ObjectUtils.isEmpty(getDataSources());
	}
	
	@Override
	@XmlElement(name="datasource")
	@XmlElementWrapper(name="datasources")
	public List<ReportDataSource> getDataSources() {
		return dataSources;
	}
	
	@Override
	public ReportDataSource getDataSourceByUid(String uid) {
		return getObjectByUid(getDataSources(), uid);
	}
	
	@Override
	public void addDataSource(ReportDataSource reportDataSource) {
		Assert.notNull(reportDataSource, "reprtDataSource is null");
		
		if (dataSources == null) {
			dataSources = new ArrayList<>();
		}
		reportDataSource.setReport(this);
		dataSources.add(reportDataSource);
	}
	
	@Override
	public void removeDataSource(ReportDataSource reprtDataSource) {
		Assert.notNull(reprtDataSource, "reprtDataSource is null");
		
		getDataSources().remove(reprtDataSource);
	}

	public void setDataSources(List<ReportDataSource> dataSources) {
		this.dataSources = dataSources;
	}

	@Override
	public boolean hasPermissions() {
		return !ObjectUtils.isEmpty(getPermissions());
	}
	
	@Override
	@XmlElement(name="permission")
	@XmlElementWrapper(name="permissions")
	public List<ReportPermission> getPermissions() {
		return permissions;
	}
	
	@Override
	public ReportPermission getPermissionByUid(String uid) {
		return getObjectByUid(getPermissions(), uid);
	}

	public void setPermissions(List<ReportPermission> permissions) {
		this.permissions = permissions;
	}
	
	@Override
	public Map<String, List<DataSourceParameter>> getDataSourceParameterMap() {
		final Map<String, List<DataSourceParameter>> paramMap = new LinkedHashMap<>();
		if (hasDataSources()) {
			for (ReportDataSource dataSource : getDataSources()) {
				if (dataSource.getDataSource().hasParameters()) {
					paramMap.put(dataSource.getName(), 
								 dataSource.getDataSource().getParameters());
				}
 			}
		}
		return paramMap;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !Report.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final Report otherReport = (Report) other;
		if (!new EqualsBuilder()
				.append(getName(), otherReport.getName())
				.isEquals()) {
			return false;
		}
		
		if (hasDataSources()) {
			for (ReportDataSource dataSource : getDataSources()) {
				if (!dataSource.isEqual(otherReport.getDataSourceByUid(dataSource.getUid()))) {
					return false;
				}
			}
		}
		if (otherReport.hasDataSources()) {
			for (ReportDataSource otherDataSource : otherReport.getDataSources()) {
				if (getDataSourceByUid(otherDataSource.getUid()) == null) {
					return false;
				}
			}
		}
		if (hasPermissions()) {
			for (ReportPermission permission : getPermissions()) {
				if (!permission.isEqual(otherReport.getPermissionByUid(permission.getUid()))) {
					return false;
				}
			}
		}
		if (otherReport.hasPermissions()) {
			for (ReportPermission otherPermission : otherReport.getPermissions()) {
				if (getPermissionByUid(otherPermission.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getDataSources());
		removeNewObjects(getPermissions());
	}
	
	@Override
	public void initUids() {
		super.initUids();
		initUids(getDataSources());
		initUids(getPermissions());
	}
	
	@Override
	protected void setOrderIndexes() {
		Order.setOrderIndexes(getDataSources());
	}
	
	void createLists() {
		permissions = new ArrayList<>();
	}
	
}
