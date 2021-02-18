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

import org.seed.core.data.AbstractSystemEntity;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

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
		return !ObjectUtils.isEmpty(getUserGroups());
	}
	
	@Override
	public boolean belongsTo(UserGroup userGroup) {
		Assert.notNull(userGroup, "userGroup is null");
		
		return hasUserGroups() && getUserGroups().contains(userGroup);
	}
	
	@Override
	public boolean belongsToOneOf(Collection<? extends UserGroup> userGroups) {
		if (!ObjectUtils.isEmpty(userGroups)) {
			for (UserGroup group : userGroups) {
				if (belongsTo(group)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Set<UserGroup> getUserGroups() {
		final Set<?> groups = userGroups;
		return (Set<UserGroup>) groups;
	}
	
	@Override
	public boolean isAuthorised(Authorisation authorisation) {
		Assert.notNull(authorisation, "authorisation is null");
		
		if (isEnabled && hasUserGroups()) {
			for (UserGroup userGroup : getUserGroups()) {
				if (userGroup.isAuthorised(authorisation)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean hasAdminAuthorisations() {
		for (Authorisation authorisation : Authorisation.values()) {
			if (authorisation.name().startsWith("ADMIN") && 
				isAuthorised(authorisation)) {
					return true;
			}
		}
		return false;
	}
	
	void createLists() {
		userGroups = new HashSet<>();
	}
	
}
