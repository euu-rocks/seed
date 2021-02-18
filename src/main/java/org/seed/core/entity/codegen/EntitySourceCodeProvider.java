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
package org.seed.core.entity.codegen;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.SourceCodeBuilder;
import org.seed.core.codegen.SourceCodeProvider;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class EntitySourceCodeProvider implements SourceCodeProvider<Entity> {
	
	@Autowired
	private EntityRepository entityRepository;
	
	@Override
	public List<SourceCodeBuilder<Entity>> getSourceCodeBuilders() {
		final List<SourceCodeBuilder<Entity>> result = new ArrayList<>();
		try (Session session = entityRepository.getSession()) {
			for (Entity entity : entityRepository.find(session)) {
				result.add(new EntitySourceCodeBuilder(entity));
			}
		}
		return result;
	}
	
	public SourceCode<Entity> getEntitySource(Entity entity) {
		Assert.notNull(entity, "entity is null");
		
		return new EntitySourceCodeBuilder(entity).build();
	}

}
