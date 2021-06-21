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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractOrderedTransferableObject;
import org.seed.core.data.datasource.IDataSource;
import org.seed.core.data.datasource.DataSourceMetadata;

@Entity
@Table(name = "sys_report_datasource")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ReportDataSource extends AbstractOrderedTransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
	private ReportMetadata report;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datasource_id")
	private DataSourceMetadata dataSource;
	
	private String label;
	
	@Transient
	private String dataSourceUid;
	
	@XmlAttribute
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@XmlTransient
	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = (ReportMetadata) report;
	}
	
	@XmlTransient
	public IDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(IDataSource dataSource) {
		this.dataSource = (DataSourceMetadata) dataSource;
	}
	
	@XmlAttribute
	public String getDataSourceUid() {
		return dataSource != null ? dataSource.getUid() : dataSourceUid;
	}

	public void setDataSourceUid(String dataSourceUid) {
		this.dataSourceUid = dataSourceUid;
	}
	
	public String getName() {
		return label != null ? label : dataSource.getName();
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !ReportDataSource.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final ReportDataSource otherDataSource = (ReportDataSource) other;
		return new EqualsBuilder()
			.append(label, otherDataSource.getLabel())
			.append(dataSourceUid, otherDataSource.getDataSourceUid())
			.isEquals();
	}
	
}
