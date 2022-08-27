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
package org.seed.test.unit.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.EntityStatusTransition;
import org.seed.core.entity.EntityStatusTransitionFunction;
import org.seed.core.entity.EntityStatusTransitionPermission;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupMetadata;
import org.seed.core.user.UserMetadata;

class EntityStatusTransitionTest {
	
	@Test
	void testAddFunction() {
		final EntityStatusTransition transition = new EntityStatusTransition();
		final EntityStatusTransitionFunction function = new EntityStatusTransitionFunction();
		assertFalse(transition.hasFunctions());
		
		transition.addFunction(function);
		
		assertEquals(transition, function.getStatusTransition());
		assertTrue(transition.hasFunctions());
		assertSame(1, transition.getFunctions().size());
		assertSame(function, transition.getFunctions().get(0));
	}
	
	@Test
	void testContainsEntityFunction() {
		final EntityStatusTransition transition = new EntityStatusTransition();
		final EntityFunction entityFunction = new EntityFunction();
		final EntityStatusTransitionFunction function = new EntityStatusTransitionFunction();
		transition.addFunction(function);
		
		assertFalse(transition.containsEntityFunction(entityFunction));
		
		function.setFunction(entityFunction);
		
		assertTrue(transition.containsEntityFunction(entityFunction));
	}
	
	@Test
	void testGetFunctionByUid() {
		final EntityStatusTransition transition = new EntityStatusTransition();
		final EntityStatusTransitionFunction function = new EntityStatusTransitionFunction();
		function.setUid("other");
		transition.addFunction(function);
		
		assertNull(transition.getFunctionByUid("test"));
		
		function.setUid("test");
		
		assertSame(function, transition.getFunctionByUid("test"));
	}
	
	@Test
	void testContainsPermission() {
		final EntityStatusTransition transition = new EntityStatusTransition();
		final EntityStatusTransitionPermission permission = new EntityStatusTransitionPermission();
		final List<EntityStatusTransitionPermission> permissions = new ArrayList<>();
		final UserGroup group = new UserGroupMetadata();
		permissions.add(permission);
		transition.setPermissions(permissions);
		
		assertFalse(transition.containsPermission(group));
		
		permission.setUserGroup(group);
		
		assertTrue(transition.containsPermission(group));
	}
	
	@Test
	void testGetPermissionByUid( ) {
		final EntityStatusTransition transition = new EntityStatusTransition();
		final EntityStatusTransitionPermission permission = new EntityStatusTransitionPermission();
		final List<EntityStatusTransitionPermission> permissions = new ArrayList<>();
		permission.setUid("other");
		permissions.add(permission);
		transition.setPermissions(permissions);
		
		assertNull(transition.getPermissionByUid("test"));
		
		permission.setUid("test");
		
		assertSame(permission, transition.getPermissionByUid("test"));
	}
	
	@Test
	void testGetSourceStatusUid() {
		final EntityStatusTransition transition = new EntityStatusTransition();
		final EntityStatus status = new EntityStatus();
		transition.setSourceStatusUid("test");
		assertEquals("test", transition.getSourceStatusUid());
		
		status.setUid("source");
		transition.setSourceStatus(status);
		assertEquals("source", transition.getSourceStatusUid());
	}
	
	@Test
	void testGetTargetStatusUid() {
		final EntityStatusTransition transition = new EntityStatusTransition();
		final EntityStatus status = new EntityStatus();
		transition.setTargetStatusUid("test");
		assertEquals("test", transition.getTargetStatusUid());
		
		status.setUid("target");
		transition.setTargetStatus(status);
		assertEquals("target", transition.getTargetStatusUid());
	}
	
	@Test
	void testIsAuthorized() {
		final EntityStatusTransition transition = new EntityStatusTransition();
		final EntityStatusTransitionPermission permission = new EntityStatusTransitionPermission();
		final UserMetadata user = new UserMetadata();
		final UserGroup group = new UserGroupMetadata();
		final List<EntityStatusTransitionPermission> permissions = new ArrayList<>();
		final Set<UserMetadata> users = new HashSet<>();
		final Set<UserGroupMetadata> groups = new HashSet<>();
		users.add(user);
		user.setUserGroups(groups);
		((UserGroupMetadata)group).setUsers(users);
		permission.setUserGroup(group);
		permissions.add(permission);
		transition.setPermissions(permissions);
		
		assertFalse(transition.isAuthorized(user));
		
		groups.add((UserGroupMetadata) group);
		
		assertTrue(transition.isAuthorized(user));
	}
	
	@Test
	void testIsEqual() {
		final EntityStatusTransition transition1 = new EntityStatusTransition();
		final EntityStatusTransition transition2 = new EntityStatusTransition();
		assertTrue(transition1.isEqual(transition2));
		
		transition1.setDescription("desc");
		transition1.setSourceStatusUid("source");
		transition1.setTargetStatusUid("traget");
		assertFalse(transition1.isEqual(transition2));
		
		transition2.setDescription("desc");
		transition2.setSourceStatusUid("source");
		transition2.setTargetStatusUid("traget");
		assertTrue(transition1.isEqual(transition2));
	}
	
	@Test
	void testIsEqualFunctions() {
		final EntityStatusTransition transition1 = new EntityStatusTransition();
		final EntityStatusTransition transition2 = new EntityStatusTransition();
		final EntityStatusTransitionFunction function1 = new EntityStatusTransitionFunction();
		final EntityStatusTransitionFunction function2 = new EntityStatusTransitionFunction();
		assertTrue(transition1.isEqual(transition2));
		
		function1.setUid("function");
		function2.setUid("function");
		transition1.addFunction(function1);
		assertFalse(transition1.isEqual(transition2));
		
		transition2.addFunction(function2);
		assertTrue(transition1.isEqual(transition2));
		
		function2.setUid("other");
		assertFalse(transition1.isEqual(transition2));
	}
	
	@Test
	void testIsEqualPermissions() {
		final EntityStatusTransition transition1 = new EntityStatusTransition();
		final EntityStatusTransition transition2 = new EntityStatusTransition();
		final EntityStatusTransitionPermission permission1 = new EntityStatusTransitionPermission();
		final EntityStatusTransitionPermission permission2 = new EntityStatusTransitionPermission();
		final List<EntityStatusTransitionPermission> permissions1 = new ArrayList<>();
		final List<EntityStatusTransitionPermission> permissions2 = new ArrayList<>();
		permission1.setUid("permission");
		permission2.setUid("permission");
		permissions1.add(permission1);
		permissions2.add(permission2);
		transition1.setPermissions(permissions1);
		transition2.setPermissions(permissions2);
		assertTrue(transition1.isEqual(transition2));
		
		permission2.setUid("other");
		assertFalse(transition1.isEqual(transition2));
	}
	
	@Test
	void testRemoveFunction() {
		final EntityStatusTransition transition = new EntityStatusTransition();
		final EntityStatusTransitionFunction function = new EntityStatusTransitionFunction();
		transition.addFunction(function);
		assertSame(1, transition.getFunctions().size());
		
		transition.removeFunction(function);
		
		assertFalse(transition.hasFunctions());
		assertSame(0, transition.getFunctions().size());
	}
	
}
