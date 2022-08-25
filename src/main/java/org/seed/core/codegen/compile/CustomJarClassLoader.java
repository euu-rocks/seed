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
import java.util.List;
import java.util.zip.ZipEntry;

import javax.tools.JavaFileObject.Kind;

import org.seed.core.codegen.CodeUtils;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.SafeZipInputStream;
import org.seed.core.util.StreamUtils;

class CustomJarClassLoader extends ClassLoader {
	
	private final List<CustomJar> customJars;

	CustomJarClassLoader(List<CustomJar> customJars, ClassLoader parent) {
		super(parent);
		Assert.notNull(customJars, "custom jars");
		
		this.customJars = customJars;
	}
	
	@Override
    protected final Class<?> findClass(String className) throws ClassNotFoundException {
		final String resourceName = CodeUtils.getPackagePath(className).concat(Kind.CLASS.extension);
		for (CustomJar customJar : customJars) {
			final byte[] byteCode = findCustomJarClass(customJar, resourceName);
			if (byteCode != MiscUtils.EMPTY_BYTE_ARRAY) {
				return defineClass(className, byteCode, 0, byteCode.length);
			}
		}
		throw new ClassNotFoundException(className);
	}
	
	private static byte[] findCustomJarClass(CustomJar customJar, String resourceName) {
		try (SafeZipInputStream zipStream = StreamUtils.getZipStream(customJar.getContent())) {
			ZipEntry entry;
			while ((entry = zipStream.getNextEntry()) != null) {
				if (resourceName.equals(entry.getName())) {
					return zipStream.readSafe(entry);
				}
			}
			return MiscUtils.EMPTY_BYTE_ARRAY;
		}
		catch (IOException ioex) {
			throw new CompilerException(ioex);
		}
	}
	
}
