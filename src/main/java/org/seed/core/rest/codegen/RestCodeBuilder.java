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
package org.seed.core.rest.codegen;

import java.util.Date;

import org.seed.C;
import org.seed.core.api.RestFunction;
import org.seed.core.api.RestFunctionContext;
import org.seed.core.codegen.AbstractSourceCodeBuilder;
import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.TypeClass;
import org.seed.core.rest.RestMapping;
import org.seed.core.util.Assert;

class RestCodeBuilder extends AbstractSourceCodeBuilder {
	
	private final RestMapping mapping;

	RestCodeBuilder(RestMapping mapping) {
		super(mapping, 
			  false, 
			  null, 
			  new TypeClass[] { newTypeClass(RestFunction.class) });
		this.mapping = mapping;
	}

	@Override
	public Date getLastModified() {
		return mapping.getLastModified();
	}

	@Override
	public SourceCode build(BuildMode buildMode) {
		Assert.notNull(buildMode, "buildMode");
		
		switch (buildMode) {
			case TEMPLATE:
				addMethod(newTypeClass(Object.class), "call", 
						  new ParameterMetadata[] {
							newParameter(C.CONTEXT, newTypeClass(RestFunctionContext.class))
						  }, 
						  CODE_PLACEHOLDER, newAnnotation(Override.class));
				return super.build(false);
				
			case COMPLETE:
				return createSourceCode(mapping.getContent());
				
			default:
				throw new UnsupportedOperationException(buildMode.name());
		}
	}

}
