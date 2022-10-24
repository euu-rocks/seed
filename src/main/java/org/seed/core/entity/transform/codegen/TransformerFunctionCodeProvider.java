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
package org.seed.core.entity.transform.codegen;

import static org.seed.core.util.CollectionUtils.filterAndForEach;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.SourceCodeBuilder;
import org.seed.core.codegen.SourceCodeBuilder.BuildMode;
import org.seed.core.codegen.SourceCodeProvider;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerFunction;
import org.seed.core.entity.transform.TransformerRepository;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransformerFunctionCodeProvider implements SourceCodeProvider {
	
	@Autowired
	private TransformerRepository transformerRepository;
	
	public String getFunctionTemplate(TransformerFunction function) {
		Assert.notNull(function, C.FUNCTION);
		
		return new TransformerFunctionCodeBuilder(function).build(BuildMode.TEMPLATE).getContent();
	}
	
	public SourceCode getFunctionSource(TransformerFunction function) {
		Assert.notNull(function, C.FUNCTION);
		
		return new TransformerFunctionCodeBuilder(function).build();
	}
	
	@Override
	public List<SourceCodeBuilder> getSourceCodeBuilders(Session session) {
		final List<SourceCodeBuilder> result = new ArrayList<>();
		for (Transformer transformer : transformerRepository.find(session)) {
			filterAndForEach(transformer.getFunctions(), 
							 function -> function.isActiveBeforeTransformation() ||
							 			 function.isActiveAfterTransformation(), 
							 function -> result.add(new TransformerFunctionCodeBuilder(function)));
		}
		return result;
	}
	
}
