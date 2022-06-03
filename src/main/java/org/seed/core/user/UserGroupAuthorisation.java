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
package org.seed.core.user;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.data.AbstractSystemObject;

@Entity
@Table(name = "sys_usergroup_auth")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserGroupAuthorisation extends AbstractSystemObject {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
	private UserGroupMetadata userGroup;
	
	private Authorisation authorisation;
	
	private String roleName;
	
	@XmlTransient
	public UserGroup getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = (UserGroupMetadata) userGroup;
	}
	
	@XmlAttribute
	public Authorisation getAuthorisation() {
		return authorisation;
	}

	public void setAuthorisation(Authorisation authorisation) {
		this.authorisation = authorisation;
		setRoleName(authorisation != null ? authorisation.roleName() : null);
	}
	
	@XmlTransient
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
}
