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

import static org.seed.core.util.CollectionUtils.*;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.C;
import org.seed.core.data.AbstractSystemEntity;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

@Entity
@Table(name = "sys_user")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserMetadata extends AbstractSystemEntity implements User {
	
	@ManyToMany(fetch = FetchType.LAZY, 
				cascade = CascadeType.ALL)
	@JoinTable(name = "sys_user_group", 
			   joinColumns = { 
					   @JoinColumn(name = "user_id", nullable = false, updatable = false) }, 
			   inverseJoinColumns = { 
					   @JoinColumn(name = "group_id", nullable = false, updatable = false) })
	private Set<UserGroupMetadata> userGroups;
	
	private String password;
	
	private String email;
	
	private String firstname;
	
	private String lastname;
	
	private Date lastLogin;
	
	private boolean isEnabled;
	
	@Transient
	private boolean passwordChange;
	
	@Override
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	@Override
	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	@Override
	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	@Override
	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	public boolean isPasswordChange() {
		return passwordChange;
	}

	public void setPasswordChange(boolean passwordChange) {
		this.passwordChange = passwordChange;
	}

	@Override
	public boolean hasUserGroups() {
		return notEmpty(getUserGroups());
	}
	
	@Override
	public boolean belongsTo(UserGroup userGroup) {
		Assert.notNull(userGroup, C.USERGROUP);
		
		return containsObject(getUserGroups(), userGroup);
	}
	
	@Override
	public boolean belongsToSystemGroup() {
		return anyMatch(getUserGroups(), UserGroup::isSystemGroup);
	}
	
	@Override
	public boolean belongsToOneOf(Collection<? extends UserGroup> groups) {
		Assert.notNull(groups, "user groups");
		
		return anyMatch(groups, this::belongsTo);
	}
	
	@Override
	public Set<UserGroup> getUserGroups() {
		return MiscUtils.castSet(userGroups);
	}
	
	public void setUserGroups(Set<UserGroupMetadata> userGroups) {
		this.userGroups = userGroups;
	}

	@Override
	public boolean isAuthorised(Authorisation authorisation) {
		Assert.notNull(authorisation, "authorisation");
		
		return isEnabled && 
			   anyMatch(getUserGroups(), group -> group.isAuthorised(authorisation));
	}
	
	@Override
	public boolean hasAdminAuthorisations() {
		return anyMatch(Authorisation.values(), auth -> auth.isAdminAuthorisation() && 
														isAuthorised(auth));
	}
	
	void createLists() {
		userGroups = new HashSet<>();
	}
	
}
