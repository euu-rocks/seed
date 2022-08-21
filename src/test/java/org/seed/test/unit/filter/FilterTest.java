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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterCriterion;
import org.seed.core.entity.filter.FilterMetadata;
import org.seed.core.entity.filter.FilterPermission;

class FilterTest {
	
	@Test
	void testAddCriterion() {
		final Filter filter = new FilterMetadata();
		final FilterCriterion criterion = new FilterCriterion();
		assertFalse(filter.hasCriteria());
		
		filter.addCriterion(criterion);
		
		assertSame(criterion.getFilter(), filter);
		
		assertTrue(filter.hasCriteria());
		assertSame(filter.getCriteria().size(), 1);
		assertSame(filter.getCriteria().get(0), criterion);
	}
	
	@Test
	void testGetCriterionByUid() {
		final Filter filter = new FilterMetadata();
		final FilterCriterion criterion = new FilterCriterion();
		criterion.setUid("other");
		filter.addCriterion(criterion);
		
		assertNull(filter.getCriterionByUid("test"));
		
		criterion.setUid("test");
		
		assertSame(filter.getCriterionByUid("test"), criterion);
	}
	
	@Test
	void testGetPermissionByUid() {
		final Filter filter = new FilterMetadata();
		final FilterPermission permission = new FilterPermission();
		final List<FilterPermission> permissions = new ArrayList<>();
		permission.setUid("other");
		permissions.add(permission);
		((FilterMetadata) filter).setPermissions(permissions);
		
		assertNull(filter.getPermissionByUid("test"));
		
		permission.setUid("test");
		
		assertSame(filter.getPermissionByUid("test"), permission);
	}
	
	@Test
	void testIsEqual() {
		final Filter filter1 = new FilterMetadata();
		final Filter filter2 = new FilterMetadata();
		assertTrue(filter1.isEqual(filter2));
		
		((FilterMetadata) filter1).setHqlQuery("query");
		assertFalse(filter1.isEqual(filter2));
		
		((FilterMetadata) filter2).setHqlQuery("query");
		assertTrue(filter1.isEqual(filter2));
	}
	
	@Test
	void testIsEqualCriteria() {
		final Filter filter1 = new FilterMetadata();
		final Filter filter2 = new FilterMetadata();
		final FilterCriterion criterion1 = new FilterCriterion();
		final FilterCriterion criterion2 = new FilterCriterion();
		criterion1.setUid("test");
		criterion2.setUid("test");
		filter1.addCriterion(criterion1);
		assertFalse(filter1.isEqual(filter2));
		
		filter2.addCriterion(criterion2);
		assertTrue(filter1.isEqual(filter2));
		
		criterion2.setUid("other");
		assertFalse(filter1.isEqual(filter2));
	}
	
	@Test
	void testIsEqualPermissions() {
		final Filter filter1 = new FilterMetadata();
		final Filter filter2 = new FilterMetadata();
		final FilterPermission permission1 = new FilterPermission();
		final FilterPermission permission2 = new FilterPermission();
		final List<FilterPermission> permissions1 = new ArrayList<>();
		final List<FilterPermission> permissions2 = new ArrayList<>();
		permissions1.add(permission1);
		permissions2.add(permission2);
		permission1.setUid("test");
		permission2.setUid("test");
		((FilterMetadata) filter1).setPermissions(permissions1);
		assertFalse(filter1.isEqual(filter2));
		
		((FilterMetadata) filter2).setPermissions(permissions2);
		assertTrue(filter1.isEqual(filter2));
		
		permission2.setUid("other");
		assertFalse(filter1.isEqual(filter2));
	}
	
	@Test
	void testRemoveCriterion() {
		final Filter filter = new FilterMetadata();
		final FilterCriterion criterion = new FilterCriterion();
		filter.addCriterion(criterion);
		assertTrue(filter.hasCriteria());
		assertSame(filter.getCriteria().size(), 1);
		
		filter.removeCriterion(criterion);
		
		assertFalse(filter.hasCriteria());
		assertSame(filter.getCriteria().size(), 0);
	}
	
}
