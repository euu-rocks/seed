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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.seed.core.codegen.GeneratedCode;
import org.seed.core.util.Assert;

import static org.seed.core.codegen.CodeUtils.*;

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
	
	final void defineJar(CustomJar customJar) {
		Assert.notNull(customJar, "customJar");
		
		final List<JavaClassFileObject> notDefinedClasses = defineJarClasses(customJar);
		int lastErrorNum = notDefinedClasses.size();
		// if errors exist -> try again
		while (lastErrorNum > 0) {
			notDefinedClasses.removeIf(this::defineClassFile);
			if (notDefinedClasses.size() < lastErrorNum) {
				lastErrorNum = notDefinedClasses.size();
			}
			else { // no progress -> quit
				throw new CustomJarException(notDefinedClasses);
			}
		}
	}
	
	private List<JavaClassFileObject> defineJarClasses(CustomJar customJar) {
		final List<JavaClassFileObject> notDefinedClasses = new ArrayList<>();
		try (ZipInputStream zis = createZipStream(customJar.getContent())) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (isClassFile(entry.getName())) {
					final String qualifiedName = getQualifiedName(entry.getName());
					if (findLoadedClass(qualifiedName) == null) {
						final JavaClassFileObject classFile = 
								new JavaClassFileObject(qualifiedName, zis.readAllBytes());
						if (!defineClassFile(classFile)) {
							notDefinedClasses.add(classFile);
						}
					}
				}
			}
			return notDefinedClasses;
		}
		catch (IOException ioex) {
			throw new CompilerException(ioex);
		}
	}
	
	private boolean defineClassFile(JavaClassFileObject classFile) {
		try {
			defineClass(classFile);
			return true;
		}
		catch (LinkageError error) {
			return false;
		}
	}
	
}
