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
package org.seed.core.codegen;

import java.util.Date;

import org.springframework.util.Assert;

public interface SourceCodeBuilder<T> {
	
	enum BuildMode {
		
		COMPLETE,
		TEMPLATE
	}
	
	class MemberMetadata {
		
		final TypeClass typeClass;
		
		final String name;
		
		MemberMetadata(TypeClass typeClass, String name) {
			Assert.notNull(typeClass, "typeClass is null");
			Assert.notNull(name, "name is null");
			
			this.typeClass = typeClass;
			this.name = name;
		}
		
	}
	
	class ParameterMetadata {
		
		final TypeClass typeClass;
		
		final AnnotationMetadata annotation;
		
		final String name;
		
		ParameterMetadata(String name, TypeClass typeClass) {
			this(name, typeClass, null);
		}
		
		ParameterMetadata(String name, TypeClass typeClass, AnnotationMetadata annotation) {
			Assert.notNull(name, "name is null");
			Assert.notNull(typeClass, "typeClass is null");
			
			this.name = name;
			this.typeClass = typeClass;
			this.annotation = annotation;
		}
		
	}
	
	Date getLastModified();
	
	SourceCode<T> build();
	
	SourceCode<T> build(BuildMode buildMode);
	
}
