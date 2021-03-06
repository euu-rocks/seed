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
package org.seed.core.entity.filter;

import java.util.List;

import javax.annotation.Nullable;

import org.seed.core.application.ApplicationEntityService;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;

public interface FilterService extends ApplicationEntityService<Filter> {
	
	List<Filter> findFilters(Entity entity);
	
	Filter getFilterByName(Entity entity, String name);
	
	Filter createFieldFilter(Entity entity, EntityField entityField, Object value);
	
	Filter createStatusFilter(Entity entity, EntityStatus status);
	
	List<FilterElement> getFilterElements(Filter filter, @Nullable NestedEntity nestedEntity);
	
}
