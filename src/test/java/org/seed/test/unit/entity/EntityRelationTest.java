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

import org.junit.jupiter.api.Test;

import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityMetadata;
import org.seed.core.entity.EntityRelation;

class EntityRelationTest {
	
	@Test
	void testCreateDescendantRelation() {
		final EntityRelation relation = new EntityRelation();
		final Entity relatedEntity = new EntityMetadata();
		final Entity descendantEntity = new EntityMetadata();
		relation.setRelatedEntity(relatedEntity);
		
		final EntityRelation descandantRelation = relation.createDescendantRelation(descendantEntity);
		assertNotNull(descandantRelation);
		assertSame(descendantEntity, descandantRelation.getEntity());
		assertSame(relatedEntity, descandantRelation.getRelatedEntity());
	}
	
	@Test
	void testCreateInverseRelation() {
		final EntityRelation relation = new EntityRelation();
		final Entity relatedEntity = new EntityMetadata();
		final Entity entity = new EntityMetadata();
		relation.setEntity(entity);
		
		final EntityRelation inverseRelation = relation.createInverseRelation(relatedEntity);
		assertNotNull(inverseRelation);
		assertSame(entity, inverseRelation.getEntity());
		assertSame(relatedEntity, inverseRelation.getRelatedEntity());
	}
	
	@Test
	void testGetInverseJoinColumnName() {
		final EntityRelation relation = new EntityRelation();
		final Entity relatedEntity = new EntityMetadata();
		relation.setRelatedEntity(relatedEntity);
		
		relatedEntity.setName("TÄST");
		assertEquals("taest_id", relation.getInverseJoinColumnName());
		
		((EntityMetadata) relatedEntity).setTableName("TÄBLE");
		assertEquals("täble_id", relation.getInverseJoinColumnName());
	}
	
	@Test
	void testGetInternalName() {
		final EntityRelation relation = new EntityRelation();
		assertNull(relation.getInternalName());
		
		relation.setName("TÄST");
		assertEquals("taest", relation.getInternalName());
	}
	
	@Test
	void testGetJoinColumnName() {
		final EntityRelation relation = new EntityRelation();
		final Entity entity = new EntityMetadata();
		relation.setEntity(entity);
		
		entity.setName("TÄST");
		assertEquals("taest_id", relation.getJoinColumnName());
		
		((EntityMetadata) entity).setTableName("TÄBLE");
		assertEquals("täble_id", relation.getJoinColumnName());
	}
	
	@Test
	void testGetJoinTableName() {
		final EntityRelation relation = new EntityRelation();
		final Entity entity = new EntityMetadata();
		final Entity relatedEntity = new EntityMetadata();
		relation.setEntity(entity);
		relation.setRelatedEntity(relatedEntity);
		
		entity.setName("TÄST");
		relatedEntity.setName("RÄLTEST");
		assertEquals("taest_raeltest", relation.getJoinTableName());
		
		((EntityMetadata) entity).setTableName("TÄBLE");
		((EntityMetadata) relatedEntity).setTableName("RÄLTABLE");
		assertEquals("täble_rältable", relation.getJoinTableName());
	}
	
	@Test
	void testGetRelatedEntityUid() {
		final EntityRelation relation = new EntityRelation();
		final Entity relatedEntity = new EntityMetadata();
		relation.setRelatedEntityUid("test");
		assertEquals("test", relation.getRelatedEntityUid());
		
		relatedEntity.setUid("related");
		relation.setRelatedEntity(relatedEntity);
		assertEquals("related", relation.getRelatedEntityUid());
	}
	
	@Test
	void testIsEqual() {
		final EntityRelation relation1 = new EntityRelation();
		final EntityRelation relation2 = new EntityRelation();
		assertTrue(relation1.isEqual(relation2));
		
		relation1.setName("name");
		relation1.setRelatedEntityUid("relation");
		assertFalse(relation1.isEqual(relation2));
		
		relation2.setName("name");
		relation2.setRelatedEntityUid("relation");
		assertTrue(relation1.isEqual(relation2));
	}
	
	@Test
	void testIsRelated() {
		final EntityRelation relation = new EntityRelation();
		final Entity entity = new EntityMetadata();
		final Entity relatedEntity = new EntityMetadata();
		relation.setRelatedEntity(relatedEntity);
		
		assertFalse(relation.isRelated(entity));
		assertTrue(relation.isRelated(relatedEntity));
	}
	
}
