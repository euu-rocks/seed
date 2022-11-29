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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.seed.core.codegen.GeneratedCode;
import org.seed.core.util.Assert;

class GeneratedCodeClassLoader extends ClassLoader {
	
	private final Map<String, Class<GeneratedCode>> mapClasses = new HashMap<>();
	
	GeneratedCodeClassLoader(List<JavaClassFileObject> classFileObjects, ClassLoader parent) {
		super(parent);
		Assert.notNull(classFileObjects, "classFileObjects");
		final List<JavaClassFileObject> objectsToDefine = new ArrayList<>(classFileObjects);
		
		while (!objectsToDefine.isEmpty()) {
			final int initialSize = objectsToDefine.size();
			for (Iterator<JavaClassFileObject> it = objectsToDefine.iterator(); it.hasNext();) {
				final JavaClassFileObject classFileObject = it.next();
				try {
					final Class<GeneratedCode> definedClass = defineClass(classFileObject);
					mapClasses.put(classFileObject.getQualifiedName(), definedClass);
					it.remove();
				}
				catch (NoClassDefFoundError err) {
					// object remains in list
				}
			}
			Assert.state(objectsToDefine.size() < initialSize, "no progress");
		}
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
