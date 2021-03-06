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
package org.seed.core.application;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.seed.core.application.module.Module;
import org.seed.core.application.module.ModuleMetadata;
import org.seed.core.data.AbstractSystemEntity;
import org.seed.core.user.User;
import org.seed.core.util.ReferenceJsonSerializer;
import org.seed.core.util.UID;

import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@MappedSuperclass
public abstract class AbstractApplicationEntity extends AbstractSystemEntity 
	implements ApplicationEntity  {
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
	@JsonSerialize(using = ReferenceJsonSerializer.class)
	private ModuleMetadata module;
	
	private String uid;
	
	@Override
	@XmlAttribute
	public String getUid() {
		return uid;
	}
	
	@Override
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	@Override
	@XmlTransient
	public Module getModule() {
		return module;
	}

	public void setModule(Module module) {
		this.module = (ModuleMetadata) module;
	}
	
	@Override
	public final boolean checkPermissions(User user, @Nullable Enum<?> access) {
		Assert.notNull(user, "user is null");
		Assert.state(this instanceof ApprovableObject, "object is not approvable");
		
		final ApprovableObject<?> approvable = (ApprovableObject<?>) this;
		// as long as there are no permissions, 
		// every access is permitted
		if (!approvable.hasPermissions()) {
			return true;
		}
		for (Permission permission : approvable.getPermissions()) {
			if (user.belongsTo(permission.getUserGroup()) && 
				(access == null || // access doesn't exist or granted
				 permission.getAccess().ordinal() >= access.ordinal())) {
					return true;
			}
		}
		return false;
	}
	
	protected void initUids() {
		initUid(this);
	}
	
	public static <T extends TransferableObject> T getObjectByUid(Collection<T> list, String uid) {
		Assert.notNull(uid, "uid is null");
		
		if (list != null) {
			for (T object : list) {
				if (uid.equals(object.getUid())) {
					return object;
				}
			}
		}
		return null;
	}
	
	protected static void initUids(List<? extends TransferableObject> transferableList) {
		if (transferableList != null) {
			transferableList.forEach(t -> initUid(t));
		}
	}
	
	protected static void initUid(TransferableObject transferable) {
		if (transferable.getUid() == null) {
			transferable.setUid(UID.createUID());
		}
	}
	
}
