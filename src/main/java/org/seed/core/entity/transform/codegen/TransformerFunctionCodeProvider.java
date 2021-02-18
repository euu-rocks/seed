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

import java.util.ArrayList;
import java.util.List;

import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.SourceCodeBuilder;
import org.seed.core.codegen.SourceCodeBuilder.BuildMode;
import org.seed.core.codegen.SourceCodeProvider;
import org.seed.core.entity.transform.Transformer;
import org.seed.core.entity.transform.TransformerFunction;
import org.seed.core.entity.transform.TransformerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class TransformerFunctionCodeProvider implements SourceCodeProvider<TransformerFunction> {
	
	@Autowired
	private TransformerRepository transformerRepository;
	
	public String getFunctionTemplate(TransformerFunction function) {
		Assert.notNull(function, "function is null");
		
		return new TransformerFunctionCodeBuilder(function).build(BuildMode.TEMPLATE).getContent();
	}
	
	public SourceCode<TransformerFunction> getFunctionSource(TransformerFunction function) {
		Assert.notNull(function, "function is null");
		
		return new TransformerFunctionCodeBuilder(function).build();
	}
	
	@Override
	public List<SourceCodeBuilder<TransformerFunction>> getSourceCodeBuilders() {
		final List<SourceCodeBuilder<TransformerFunction>> result = new ArrayList<>();
		for (Transformer transformer : transformerRepository.find()) {
			if (transformer.hasFunctions()) {
				for (TransformerFunction function : transformer.getFunctions()) {
					if (function.isActiveBeforeTransformation() ||
						function.isActiveAfterTransformation()) {
						result.add(new TransformerFunctionCodeBuilder(function));
					}
				}
			}
		}
		return result;
	}
	
}
