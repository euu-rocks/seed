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

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.Collections;
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
import org.seed.core.util.Assert;

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
		return notEmpty(getDataSources());
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
		Assert.notNull(reportDataSource, "reportDataSource");
		
		if (dataSources == null) {
			dataSources = new ArrayList<>();
		}
		reportDataSource.setReport(this);
		dataSources.add(reportDataSource);
	}
	
	@Override
	public void removeDataSource(ReportDataSource reportDataSource) {
		Assert.notNull(reportDataSource, "reportDataSource");
		
		getDataSources().remove(reportDataSource);
	}

	public void setDataSources(List<ReportDataSource> dataSources) {
		this.dataSources = dataSources;
	}

	@Override
	public boolean hasPermissions() {
		return notEmpty(getPermissions());
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
		return hasDataSources()
				? getDataSources().stream()
					.filter(dataSource -> dataSource.getDataSource().hasParameters())
					.collect(linkedMapCollector(ReportDataSource::getName, 
												dataSource -> dataSource.getDataSource().getParameters()))
				: Collections.emptyMap();
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
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
		return isEqualDataSources(otherReport) &&
			   isEqualPermissions(otherReport);
	}
	
	private boolean isEqualDataSources(Report otherReport) {
		return !(anyMatch(dataSources, dataSource -> !dataSource.isEqual(otherReport.getDataSourceByUid(dataSource.getUid()))) || 
				 anyMatch(otherReport.getDataSources(), dataSource -> getDataSourceByUid(dataSource.getUid()) == null));
	}
	
	private boolean isEqualPermissions(Report otherReport) {
		return !(anyMatch(permissions, permission -> !permission.isEqual(otherReport.getPermissionByUid(permission.getUid()))) || 
				 anyMatch(otherReport.getPermissions(), permission -> getPermissionByUid(permission.getUid()) == null));
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getDataSources());
		removeNewObjects(getPermissions());
	}
	
	@Override
	public void initUid() {
		super.initUid();
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
