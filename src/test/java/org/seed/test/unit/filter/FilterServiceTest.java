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
package org.seed.test.unit.filter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.seed.core.data.FieldType;
import org.seed.core.data.SystemField;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityMetadata;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterCriterion;
import org.seed.core.entity.filter.FilterElement;
import org.seed.core.entity.filter.FilterMetadata;
import org.seed.core.entity.filter.FilterRepository;
import org.seed.core.entity.filter.FilterService;
import org.seed.core.entity.filter.FilterServiceImpl;

class FilterServiceTest {
	
	@Test
	void testCreateInstance() {
		final FilterService service = getService();
		assertNotNull(service.createInstance(null));
		assertTrue(service.createInstance(null) instanceof Filter);
	}
	
	@Test
	void testCreateFieldFilter() {
		final FilterService service = getService();
		final Entity entity = new EntityMetadata();
		final EntityField field = new EntityField();
		field.setType(FieldType.TEXT);
		final Filter filter = service.createFieldFilter(entity, field, "test");
		assertNotNull(filter);
		assertSame(entity, filter.getEntity());
		assertTrue(filter.hasCriteria());
		
		final FilterCriterion criterion = filter.getCriteria().get(0);
		assertSame(filter, criterion.getFilter());
		assertSame(field, criterion.getEntityField());
		assertEquals("test", criterion.getValue());
	}
	
	@Test
	void testCreateStatusFilter() {
		final FilterService service = getService();
		final Entity entity = new EntityMetadata();
		final EntityStatus status = new EntityStatus();
		status.setUid("test");
		final Filter filter = service.createStatusFilter(entity, status);
		assertNotNull(filter);
		assertSame(entity, filter.getEntity());
		assertTrue(filter.hasCriteria());
		
		final FilterCriterion criterion = filter.getCriteria().get(0);
		assertEquals(SystemField.ENTITYSTATUS, criterion.getSystemField());
		assertEquals("test", criterion.getReferenceUid());
	}
	
	@Test
	void testGetFilterElements() {
		final FilterService service = getService();
		final Entity entity = new EntityMetadata();
		final Entity nestedEntity = new EntityMetadata();
		final NestedEntity nested = new NestedEntity();
		nested.setNestedEntity(nestedEntity);
		final EntityField field = new EntityField();
		final EntityField nestedField = new EntityField();
		field.setType(FieldType.TEXT);
		nestedField.setType(FieldType.TEXT);
		entity.addField(field);
		nestedEntity.addField(nestedField);
		final Filter filter = new FilterMetadata();
		((FilterMetadata) filter).setEntity(entity);
		List<FilterElement> elements = service.getFilterElements(filter, null);
		assertFalse(elements.isEmpty());
		FilterElement element = elements.get(0);
		assertSame(field, element.getEntityField());
		
		elements = service.getFilterElements(filter, nested);
		assertFalse(elements.isEmpty());
		element = elements.get(0);
		assertSame(nestedField, element.getEntityField());
	}
	
	private static FilterService getService() {
		final FilterServiceImpl service = new FilterServiceImpl();
		service.setRepository(new FilterRepository());
		return service;
	}
	
}
