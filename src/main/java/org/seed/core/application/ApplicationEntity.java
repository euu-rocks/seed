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

import org.seed.core.application.module.Module;
import org.seed.core.data.SystemEntity;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;

/** 
 * An ApplicationEntity represents a SystemEntity, 
 * that is transferable and can be part of a module.
 * It also supports a permission management to restrict access.
 * 
 * @author seed-master
 *
 */
public interface ApplicationEntity extends SystemEntity, TransferableObject {
	
	Module getModule();
	
	boolean checkPermissions(User user);
	
	boolean checkPermissions(User user, Enum<?> access);
	
	boolean containsPermission(UserGroup group);
	
}
