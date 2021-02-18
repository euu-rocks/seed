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

import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.SourceCodeBuilder;
import org.seed.core.codegen.SourceCodeProvider;
import org.seed.core.codegen.SourceCodeBuilder.BuildMode;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityFunction;
import org.seed.core.entity.EntityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class EntityFunctionCodeProvider implements SourceCodeProvider<EntityFunction> {
	
	@Autowired
	private EntityRepository entityRepository;
	
	public String getFunctionTemplate(EntityFunction function) {
		Assert.notNull(function, "function is null");
		
		if (function.isCallback()) {
			return new EntityFunctionCodeBuilder(function).build(BuildMode.TEMPLATE).getContent();
		}
		return "\n\n\tpublic void " + function.getInternalName() + "() {\n\n\t}\n";
	}
	
	public SourceCode<EntityFunction> getFunctionSource(EntityFunction function) {
		Assert.notNull(function, "function is null");
		
		return new EntityFunctionCodeBuilder(function).build();
	}
	
	@Override
	public List<SourceCodeBuilder<EntityFunction>> getSourceCodeBuilders() {
		final List<SourceCodeBuilder<EntityFunction>> result = new ArrayList<>();
		for (Entity entity : entityRepository.find()) {
			if (entity.hasFunctions()) {
				for (EntityFunction function : entity.getCallbackFunctions()) {
					if (function.isActive()) {
						result.add(new EntityFunctionCodeBuilder(function));
					}
				}
			}
		}
		return result;
	}
	
}
