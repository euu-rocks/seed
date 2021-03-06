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

import org.seed.core.application.Permission;
import org.seed.core.application.TransferableObject;
import org.seed.core.data.AbstractSystemObject;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupMetadata;

@Entity
@Table(name = "sys_report_permission")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ReportPermission extends AbstractSystemObject
	implements Permission, TransferableObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
	private ReportMetadata report;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usergroup_id")
	private UserGroupMetadata userGroup;
	
	private String uid;
	
	@Transient
	private String userGroupUid;
	
	@Override
	@XmlAttribute
	public String getUid() {
		return uid;
	}

	@Override
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	@XmlTransient
	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = (ReportMetadata) report;
	}
	
	@Override
	@XmlTransient
	public UserGroup getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = (UserGroupMetadata) userGroup;
	}
	
	@Override
	public Enum<?> getAccess() {
		return null;
	}
	
	public String getUserGroupUid() {
		return userGroup != null ? userGroup.getUid() : userGroupUid;
	}

	public void setUserGroupUid(String userGroupUid) {
		this.userGroupUid = userGroupUid;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (other == null || !ReportPermission.class.isAssignableFrom(other.getClass())) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final ReportPermission otherPermission = (ReportPermission) other;
		return new EqualsBuilder()
			.append(userGroupUid, otherPermission.getUserGroupUid())
			.isEquals();
	}

}
