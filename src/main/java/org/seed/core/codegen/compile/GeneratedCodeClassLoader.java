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
package org.seed.core.codegen.compile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.seed.core.codegen.GeneratedCode;
import org.seed.core.util.Assert;

class GeneratedCodeClassLoader extends ClassLoader {
	
	private final Map<String, Class<GeneratedCode>> mapClasses;
	
	GeneratedCodeClassLoader(List<JavaClassFileObject> classFileObjects, ClassLoader parent) {
		super(parent);
		Assert.notNull(classFileObjects, "classFileObjects");
		
		mapClasses = classFileObjects.stream()
						.collect(Collectors.toMap(JavaClassFileObject::getQualifiedName,
												  this::defineClass));		
	}
	
	Map<String, Class<GeneratedCode>> getClassMap() {
		return mapClasses;
	}
	
	@SuppressWarnings("unchecked")
	private Class<GeneratedCode> defineClass(JavaClassFileObject classFileObject) {
		return (Class<GeneratedCode>) 
					defineClass(classFileObject.getQualifiedName(), 
								classFileObject.getByteCode(), 0,
								classFileObject.getByteCode().length);
	}
	
}
