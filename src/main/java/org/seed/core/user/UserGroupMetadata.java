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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.util.Assert;

import org.springframework.util.ObjectUtils;

@Entity
@Table(name = "sys_usergroup")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserGroupMetadata extends AbstractApplicationEntity implements UserGroup {
	
	@OneToMany(mappedBy = "userGroup",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private List<UserGroupAuthorisation> authorisations;
	
	@ManyToMany(mappedBy = "userGroups")
	private Set<UserMetadata> users;
	
	private boolean isSystemGroup;
	
	@Transient
	private List<Long> originalUserIds;
	
	@XmlTransient
	public List<Long> getOriginalUserIds() {
		return originalUserIds;
	}

	public void setOriginalUserIds(List<Long> originalUserIds) {
		this.originalUserIds = originalUserIds;
	}
	
	@Override
	@XmlTransient
	public boolean isSystemGroup() {
		return isSystemGroup;
	}
	
	@Override
	public void setSystemGroup(boolean isSystemGroup) {
		this.isSystemGroup = isSystemGroup;
	}
	
	@Override
	@XmlElement(name="authorisation")
	@XmlElementWrapper(name="authorisations")
	public List<UserGroupAuthorisation> getAuthorisations() {
		return authorisations;
	}
	
	public void setAuthorisations(List<UserGroupAuthorisation> authorisations) {
		this.authorisations = authorisations;
	}

	@Override
	public boolean isAuthorised(Authorisation authorisation) {
		Assert.notNull(authorisation, "authorisation");
		
		return hasAuthorisations() &&
			   getAuthorisations().stream()
			   	.anyMatch(auth -> auth.getAuthorisation() == authorisation);
	}
	
	@Override
	public boolean hasAuthorisations() {
		return !ObjectUtils.isEmpty(getAuthorisations());
	}
	
	@Override
	public UserGroupAuthorisation getAuthorisationByUid(String uid) {
		return getObjectByUid(getAuthorisations(), uid);
	}

	@Override
	public boolean hasUsers() {
		return !ObjectUtils.isEmpty(getUsers());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Set<User> getUsers() {
		final Set<?> usrs = users;
		return (Set<User>) usrs;
	}
	
	public void setUsers(Set<UserMetadata> users) {
		this.users = users;
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final UserGroup otherGroup = (UserGroup) other;
		if (!new EqualsBuilder()
				.append(getName(), otherGroup.getName())
				.append(isSystemGroup, isSystemGroup)
				.isEquals()) {
			return false;
		}
		return isEqualAuthorisations(otherGroup);
	}
	
	private boolean isEqualAuthorisations(UserGroup otherGroup) {
		if (hasAuthorisations()) {
			for (UserGroupAuthorisation auth : getAuthorisations()) {
				if (!auth.isEqual(otherGroup.getAuthorisationByUid(auth.getUid()))) {
					return false;
				}
			}
		}
		if (otherGroup.hasAuthorisations()) {
			for (UserGroupAuthorisation otherAuth : otherGroup.getAuthorisations()) {
				if (getAuthorisationByUid(otherAuth.getUid()) == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getAuthorisations());
	}
	
	@Override
	public void initUid() {
		super.initUid();
		initUids(getAuthorisations());
	}
	
	void createLists() {
		authorisations = new ArrayList<>();
		users = new HashSet<>();
	}
	
}
