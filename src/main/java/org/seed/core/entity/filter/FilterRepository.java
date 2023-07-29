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

import org.hibernate.Session;

import org.seed.core.data.AbstractSystemEntityRepository;

import org.springframework.stereotype.Repository;

@Repository
public class FilterRepository extends AbstractSystemEntityRepository<Filter> {
	
	public FilterRepository() {
		super(FilterMetadata.class);
	}
	
	@Override
	public Session getSession() {
		return super.getSession();
	}
	
}