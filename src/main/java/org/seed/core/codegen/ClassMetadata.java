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

import org.springframework.util.Assert;

final class ClassMetadata {
	
	final boolean isAbstract;
	
	final String packageName;
	
	final String className;
	
	final TypeClass superClass;
	
	final TypeClass[] interfaceClasses;
	
	final AnnotationMetadata[] annotations;
	
	ClassMetadata(String qualifiedName) {
		this(CodeUtils.extractPackageName(qualifiedName), 
			 CodeUtils.extractClassName(qualifiedName));
	}
	
	ClassMetadata(String packageName, String className) {
		this(packageName, className, false, null, null);
	}
	
	ClassMetadata(GeneratedObject generatedObject, boolean isAbstract,
				  TypeClass superClass, TypeClass[] interfaceClasses, 
				  AnnotationMetadata ...annotations) {
		this(generatedObject.getGeneratedPackage(), generatedObject.getGeneratedClass(),
			 isAbstract, superClass, interfaceClasses, annotations);
	}
	
	private ClassMetadata(String packageName, String className, boolean isAbstract,
						  TypeClass superClass, TypeClass[] interfaceClasses, 
						  AnnotationMetadata ...annotations) {
		Assert.notNull(packageName, "packageName is null");
		Assert.notNull(className, "className is null");
		
		this.packageName = packageName;
		this.className = className;
		this.isAbstract = isAbstract;
		this.superClass = superClass;
		this.interfaceClasses = interfaceClasses;
		this.annotations = annotations;
	}
	
	public String getQualifiedName() {
		return CodeUtils.getQualifiedName(packageName, className);
	}
	
}
