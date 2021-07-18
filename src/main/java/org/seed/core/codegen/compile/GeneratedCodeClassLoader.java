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

import org.seed.core.codegen.GeneratedCode;
import org.seed.core.util.Assert;

class GeneratedCodeClassLoader extends ClassLoader {
	
	GeneratedCodeClassLoader(ClassLoader parent) {
		super(parent);
	}
	
	@SuppressWarnings("unchecked")
	final Class<GeneratedCode> defineClass(JavaClassFileObject classFileObject) {
		Assert.notNull(classFileObject, "classFileObject");
		
		return (Class<GeneratedCode>) 
					defineClass(classFileObject.getQualifiedName(), 
								classFileObject.getByteCode(), 0, 
								classFileObject.getByteCode().length);
	}
	
}
