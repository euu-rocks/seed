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

import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;

import org.seed.C;
import org.seed.core.api.CallbackFunctionContext;
import org.seed.core.api.TransformationFunction;
import org.seed.core.codegen.AbstractSourceCodeBuilder;
import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.TypeClass;
import org.seed.core.entity.transform.TransformerFunction;
import org.seed.core.util.Assert;

class TransformerFunctionCodeBuilder extends AbstractSourceCodeBuilder {
	
	private final TransformerFunction transformerFunction;
	
	TransformerFunctionCodeBuilder(TransformerFunction transformerFunction) {
		super(transformerFunction,
			  false,
			  null,
			  new TypeClass[] {
				  newTypeClass(TransformationFunction.class,
							   newTypeClass(transformerFunction.getTransformer().getSourceEntity()),
							   newTypeClass(transformerFunction.getTransformer().getTargetEntity()))
			  },
			  ArrayUtils.toArray());
		
		this.transformerFunction = transformerFunction;
	}
	
	@Override
	public Date getLastModified() {
		return transformerFunction.getLastModified();
	}
	
	@Override
	public SourceCode build(BuildMode buildMode) {
		Assert.notNull(buildMode, "buildMode");
		
		switch (buildMode) {
			case TEMPLATE:
				addMethod(null, "transform", 
						new ParameterMetadata[] { 
								newParameter("sourceObject", newTypeClass(transformerFunction.getTransformer().getSourceEntity())),
								newParameter("targetObject", newTypeClass(transformerFunction.getTransformer().getTargetEntity())),
								newParameter(C.CONTEXT, newTypeClass(CallbackFunctionContext.class))
							  },
						  CODE_PLACEHOLDER);
				return super.build(false);
			
			case COMPLETE:
				return createSourceCode(transformerFunction.getContent());
				
			default:
				throw new UnsupportedOperationException(buildMode.name());
		}
	}
	
}
