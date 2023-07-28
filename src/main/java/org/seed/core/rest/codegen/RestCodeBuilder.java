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
import org.seed.core.api.RestFunctionContext;
import org.seed.core.codegen.AbstractSourceCodeBuilder;
import org.seed.core.codegen.CodeManagerImpl;
import org.seed.core.codegen.ParameterMetadata;
import org.seed.core.codegen.SourceCode;
import org.seed.core.codegen.TypeClass;
import org.seed.core.rest.RestFunction;
import org.seed.core.util.Assert;

class RestCodeBuilder extends AbstractSourceCodeBuilder {
	
	private final RestFunction function;

	RestCodeBuilder(RestFunction function) {
		super(function, 
			  false, 
			  null, 
			  getInterfaceTypes());
		this.function = function;
	}

	@Override
	public Date getLastModified() {
		return function.getLastModified();
	}

	@Override
	public SourceCode build(BuildMode buildMode) {
		Assert.notNull(buildMode, "buildMode");
		
		switch (buildMode) {
			case TEMPLATE:
				addImportPackage(CodeManagerImpl.API_PACKAGE);
				addImportPackage(CodeManagerImpl.GENERATED_ENTITY_PACKAGE);
				addMethod(newTypeClass(Object.class), "call", 
						  new ParameterMetadata[] {
							newParameter(C.CONTEXT, newTypeClass(RestFunctionContext.class))
						  }, 
						  CODE_PLACEHOLDER + "\n\t\treturn \"TODO\";\n",
						  newAnnotation(Override.class));
				return buildSourceCode();
				
			case COMPLETE:
				return createSourceCode(function.getContent());
				
			default:
				throw new UnsupportedOperationException(buildMode.name());
		}
	}
	
	private static TypeClass[] getInterfaceTypes() {
		return new TypeClass[] { newTypeClass(org.seed.core.api.RestFunction.class) };
	}

}
