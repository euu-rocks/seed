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
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.seed.C;
import org.seed.core.util.Assert;
import org.seed.core.util.StreamUtils;

final class CustomJarInfo {
	
	private final CustomJar customJar;
	
	private final Set<String> packageNames;

	CustomJarInfo(CustomJar customJar) {
		Assert.notNull(customJar, "customJar");
		
		this.customJar = customJar;
		this.packageNames = extractPackageNames(customJar);
	}
	
	byte[] getContent() {
		return customJar.getContent();
	}
	
	boolean containsPackage(String packageName) {
		Assert.notNull(packageName, C.PACKAGENAME);
		
		return packageNames.contains(packageName);
	}
	
	private static Set<String> extractPackageNames(CustomJar customJar) {
		final Set<String> packageNames = new HashSet<>();
		try (ZipInputStream zis = StreamUtils.getZipStream(customJar.getContent())) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory() && !entry.getName().startsWith("META-INF")) {
                	packageNames.add(entry.getName()
                						  .substring(0, entry.getName().length() - 1)
                						  .replace('/', '.'));
				}
            }
            return packageNames;
        }
		catch (IOException ioex) {
			throw new CompilerException(ioex);
		}
	}
	
}
