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
package org.seed.core.entity.transform;

import java.util.List;
import java.util.Set;

import org.seed.core.api.EntityTransformer;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityStatus;
import org.seed.core.user.User;
import org.seed.core.user.UserGroup;

public interface Transformer extends ApplicationEntity, EntityTransformer {
	
	String getSourceEntityUid();
	
	String getTargetEntityUid();
	
	Entity getSourceEntity();
	
	Entity getTargetEntity();
	
	boolean hasElements();
	
	boolean containsElement(TransformerElement element);
	
	TransformerElement getElementByUid(String uid);
	
	List<TransformerElement> getElements();
	
	void addElement(TransformerElement element);
	
	void removeElement(TransformerElement element);
	
	boolean hasFunctions();
	
	List<TransformerFunction> getFunctions();
	
	TransformerFunction getFunctionByUid(String uid);
	
	void addFunction(TransformerFunction function);
	
	void removeFunction(TransformerFunction function);
	
	boolean hasUserGroups();
	
	boolean containsUserGroup(UserGroup userGroup);
	
	UserGroup getUserGroupByUid(String uid);
	
	Set<UserGroup> getUserGroups();
	
	boolean isAuthorized(User user);
	
	boolean isEnabled(EntityStatus entityStatus);
	
	boolean hasStatus();
	
	boolean containsStatus(EntityStatus status);
	
	EntityStatus getStatusByUid(String uid);
	
	Set<EntityStatus> getStatus();
	
}
