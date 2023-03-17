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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.seed.core.data.FieldType;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldConstraint;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityMetadata;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.EntityRepository;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityServiceImpl;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.EntityStatusTransition;
import org.seed.core.entity.EntityStatusTransitionFunction;
import org.seed.core.user.User;
import org.seed.core.user.UserMetadata;
import org.seed.core.util.MiscUtils;

class EntityServiceTest {
	
	@Test
	void testCreateInstance() {
		final EntityService service = createService();
		assertNotNull(service.createInstance(null));
		assertTrue(service.createInstance(null) instanceof Entity);
	}
	
	@Test
	void testCreateField() {
		final EntityService service = createService();
		final Entity entity = new EntityMetadata();
		final EntityField field = service.createField(entity);
		
		assertNotNull(field);
		assertTrue(entity.hasFields());
		assertSame(entity, field.getEntity());
	}
	
	@Test
	void testCreateFieldGroup() {
		final EntityService service = createService();
		final Entity entity = new EntityMetadata();
		final EntityFieldGroup group = service.createFieldGroup(entity);
		
		assertNotNull(group);
		assertTrue(entity.hasFieldGroups());
		assertSame(entity, group.getEntity());
	}
	
	@Test
	void testCreateFunction() {
		final EntityService service = createService();
		final Entity entity = new EntityMetadata();
		final EntityFunction function = service.createFunction(entity, true);
		
		assertNotNull(function);
		assertTrue(function.isCallback());
		assertTrue(function.isActive());
		assertTrue(entity.hasFunctions());
		assertSame(entity, function.getEntity());
	}
	
	@Test
	void testCreateRelation() {
		final EntityService service = createService();
		final Entity entity = new EntityMetadata();
		final EntityRelation relation = service.createRelation(entity);
		
		assertNotNull(relation);
		assertTrue(entity.hasRelations());
		assertSame(entity, relation.getEntity());
	}
	
	@Test
	void testCreateStatus() {
		final EntityService service = createService();
		final Entity entity = new EntityMetadata();
		final EntityStatus status = service.createStatus(entity);
		
		assertNotNull(status);
		assertTrue(entity.hasStatus());
		assertSame(entity, status.getEntity());
	}
	
	@Test
	void testCreateFieldConstraint() {
		final EntityService service = createService();
		final Entity entity = new EntityMetadata();
		final EntityFieldConstraint constraint = service.createFieldConstraint(entity);
		
		assertNotNull(constraint);
		assertTrue(entity.hasFieldConstraints());
		assertSame(entity, constraint.getEntity());
	}
	
	@Test
	void testCreateStatusTransition() {
		final EntityService service = createService();
		final Entity entity = new EntityMetadata();
		final EntityStatusTransition transition = service.createStatusTransition(entity);
		
		assertNotNull(transition);
		assertTrue(entity.hasStatusTransitions());
		assertSame(entity, transition.getEntity());
	}
	
	@Test
	void testFindUsageFieldGroup() {
		final EntityServiceImpl service = (EntityServiceImpl) createService();
		final Entity entity = new EntityMetadata();
		final EntityField field = service.createField(entity);
		final EntityFieldConstraint constraint = service.createFieldConstraint(entity);
		final EntityFieldGroup group = service.createFieldGroup(entity);
		assertTrue(service.findUsage(group).isEmpty());
		
		field.setFieldGroup(group);
		assertFalse(service.findUsage(group).isEmpty());
		assertSame(entity, service.findUsage(group).get(0));
		
		field.setFieldGroup(null);
		constraint.setFieldGroup(group);
		assertFalse(service.findUsage(group).isEmpty());
		assertSame(entity, service.findUsage(group).get(0));
	}
	
	@Test 
	void testGetAvailableFieldTypes() {
		final EntityService service = createService();
		final EntityMetadata entity = new EntityMetadata();
		final EntityField field = new EntityField();
		entity.addField(field);
		assertArrayEquals(MiscUtils.toArray(), service.getAvailableFieldTypes(entity, null));
		assertArrayEquals(FieldType.values(), service.getAvailableFieldTypes(entity, field));
		
		entity.setGeneric(true);
		assertArrayEquals(FieldType.nonAutonumTypes(), service.getAvailableFieldTypes(entity, field));
		entity.setGeneric(false);
		
		entity.setTransferable(true);
		assertArrayEquals(FieldType.transferableTypes(), service.getAvailableFieldTypes(entity, field));
		entity.setTransferable(false);
		
		field.setId(23L);
		field.setType(FieldType.AUTONUM);
		assertArrayEquals(MiscUtils.toArray(FieldType.AUTONUM), service.getAvailableFieldTypes(entity, field));
		field.setType(FieldType.BINARY);
		assertArrayEquals(MiscUtils.toArray(FieldType.BINARY), service.getAvailableFieldTypes(entity, field));
		field.setType(FieldType.BOOLEAN);
		assertArrayEquals(MiscUtils.toArray(FieldType.BOOLEAN, FieldType.TEXT, FieldType.TEXTLONG), service.getAvailableFieldTypes(entity, field));
		field.setType(FieldType.DATE);
		assertArrayEquals(MiscUtils.toArray(FieldType.DATE, FieldType.DATETIME), service.getAvailableFieldTypes(entity, field));
		field.setType(FieldType.DATETIME);
		assertArrayEquals(MiscUtils.toArray(FieldType.DATETIME, FieldType.DATE), service.getAvailableFieldTypes(entity, field));
		field.setType(FieldType.DECIMAL);
		assertArrayEquals(MiscUtils.toArray(FieldType.DECIMAL, FieldType.LONG, FieldType.DOUBLE), service.getAvailableFieldTypes(entity, field));
		field.setType(FieldType.DOUBLE);
		assertArrayEquals(MiscUtils.toArray(FieldType.DOUBLE, FieldType.LONG, FieldType.DECIMAL), service.getAvailableFieldTypes(entity, field));
		field.setType(FieldType.FILE);
		assertArrayEquals(MiscUtils.toArray(FieldType.FILE), service.getAvailableFieldTypes(entity, field));
		field.setType(FieldType.INTEGER);
		assertArrayEquals(MiscUtils.toArray(FieldType.INTEGER, FieldType.LONG, FieldType.DOUBLE, FieldType.DECIMAL), service.getAvailableFieldTypes(entity, field));
		field.setType(FieldType.LONG);
		assertArrayEquals(MiscUtils.toArray(FieldType.LONG, FieldType.DOUBLE, FieldType.DECIMAL), service.getAvailableFieldTypes(entity, field));
		field.setType(FieldType.REFERENCE);
		assertArrayEquals(MiscUtils.toArray(FieldType.REFERENCE), service.getAvailableFieldTypes(entity, field));
		field.setType(FieldType.TEXT);
		assertArrayEquals(MiscUtils.toArray(FieldType.TEXT, FieldType.TEXTLONG), service.getAvailableFieldTypes(entity, field));
		field.setType(FieldType.TEXTLONG);
		assertArrayEquals(MiscUtils.toArray(FieldType.TEXTLONG), service.getAvailableFieldTypes(entity, field));
	}
	
	@Test
	void testGetAvailableStatusList() {
		final EntityServiceImpl service = (EntityServiceImpl) createService();
		final User user = new UserMetadata();
		final EntityMetadata entity = new EntityMetadata();
		final EntityStatus statusA = service.createStatus(entity);
		final EntityStatus statusB = service.createStatus(entity);
		final EntityStatus statusC = service.createStatus(entity);
		final EntityStatusTransition transAB = service.createStatusTransition(entity);
		transAB.setSourceStatus(statusA);
		transAB.setTargetStatus(statusB);
		final EntityStatusTransition transBC = service.createStatusTransition(entity);
		transBC.setSourceStatus(statusB);
		transBC.setTargetStatus(statusC);
		
		List<EntityStatus> list = service.getAvailableStatusList(entity, statusA, user);
		assertFalse(list.isEmpty());
		assertSame(2, list.size());
		assertSame(statusA, list.get(0));
		assertSame(statusB, list.get(1));
		
		list = service.getAvailableStatusList(entity, statusB, user);
		assertFalse(list.isEmpty());
		assertSame(2, list.size());
		assertSame(statusB, list.get(0));
		assertSame(statusC, list.get(1));
		
		list = service.getAvailableStatusList(entity, statusC, user);
		assertFalse(list.isEmpty());
		assertSame(1, list.size());
		assertSame(statusC, list.get(0));
	}
	
	@Test
	void testGetAvailableStatusTransitionFunctions() {
		final EntityServiceImpl service = (EntityServiceImpl) createService();
		final Entity entity = new EntityMetadata();
		final EntityFunction function = service.createFunction(entity, true);
		final EntityStatusTransition transition = service.createStatusTransition(entity);
		assertTrue(service.getAvailableStatusTransitionFunctions(entity, transition).isEmpty());
		
		function.setActiveOnStatusTransition(true);
		assertFalse(service.getAvailableStatusTransitionFunctions(entity, transition).isEmpty());
		assertTrue(service.getAvailableStatusTransitionFunctions(entity, transition).get(0) instanceof EntityStatusTransitionFunction);
	}
	
	private static EntityService createService() {
		final EntityService service = new EntityServiceImpl();
		((EntityServiceImpl) service).setEntityRepository(new EntityRepository());
		return service;
	}
	
}
