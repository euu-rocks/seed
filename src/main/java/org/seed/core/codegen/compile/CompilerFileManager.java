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

import static org.seed.core.util.CollectionUtils.*;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

import org.seed.C;
import org.seed.core.codegen.SourceCode;
import org.seed.core.util.Assert;
import org.seed.core.util.SafeZipInputStream;
import org.seed.core.util.StreamUtils;

import static org.seed.core.codegen.CodeUtils.*;

class CompilerFileManager extends ForwardingJavaFileManager<JavaFileManager> {
	
	private final Map<String, JavaClassFileObject> classFileObjectMap = new HashMap<>();
	
	private List<CustomJarInfo> customJarInfos;
	
	CompilerFileManager(StandardJavaFileManager standardFileManager) {
		super(standardFileManager);
	}
	
	List<JavaClassFileObject> getClassFileObjects() {
		synchronized (classFileObjectMap) {
			return valueList(classFileObjectMap);
		}
	}
	
	JavaClassFileObject removeClassFileObject(String qualifiedName) {
		Assert.notNull(qualifiedName, C.QUALIFIEDNAME);
		
		synchronized (classFileObjectMap) {
			return classFileObjectMap.remove(qualifiedName); 
		}
	}
	
	List<JavaSourceFileObject> createSourceFileObjects(List<SourceCode> sourceCodes) {
		Assert.notNull(sourceCodes, "sourceCodes");
		
		return convertedList(sourceCodes, JavaSourceFileObject::new);
	}
	
	void setCustomJars(List<CustomJar> customJars) {
		customJarInfos = customJars != null 
			? convertedList(customJars, CustomJarInfo::new) 
			: null;
	}
	
	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName,
											   Kind kind, FileObject sibling) throws IOException  {
		if (kind == Kind.CLASS) {
			final JavaClassFileObject classFileObject = new JavaClassFileObject(qualifiedName);
			synchronized (classFileObjectMap) {
				classFileObjectMap.put(qualifiedName, classFileObject);
			}
			return classFileObject;
		}
		return super.getJavaFileForOutput(location, qualifiedName, kind, sibling);
	}
	
	@Override
	public String inferBinaryName(Location location, JavaFileObject fileObject) {
		if (fileObject instanceof DependencyFileObject) {
			return ((DependencyFileObject) fileObject).getQualifiedName();
		}
		if (fileObject instanceof JavaClassFileObject) {
			return ((JavaClassFileObject) fileObject).getQualifiedName();
		}
		return super.inferBinaryName(location, fileObject);
	}
	
	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName, 
										 Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
		if (location == StandardLocation.CLASS_PATH && kinds.contains(Kind.CLASS)) {
			if (packageName.contains(".generated.")) {
				return listGeneratedClasses(packageName);
			}
			if (!packageName.startsWith("java.")) {
				return listDependencies(packageName);
			}
		}
		return super.list(location, packageName, kinds, recurse);
	}
	
	private Iterable<JavaFileObject> listGeneratedClasses(String packageName) {
		synchronized (classFileObjectMap) {
			return filterAndConvert(classFileObjectMap.entrySet(), 
									entry -> packageName.equals(extractPackageName(entry.getKey())), 
									Entry::getValue);
		}
	}
	
	private Iterable<JavaFileObject> listDependencies(String packageName) throws IOException {
		final List<JavaFileObject> result = new ArrayList<>();
		
		// list class loader resources 
		final Enumeration<URL> urlEnum = getClass().getClassLoader().getResources(getPackagePath(packageName));
		while (urlEnum.hasMoreElements()) {
			final URL packageURL = urlEnum.nextElement();
			final File packageURLFile = new File(packageURL.getFile());
			if (packageURLFile.isDirectory()) {
				listDirectory(result, packageName, packageURLFile);
			} 
			else {
				listJar(result, packageURL);
			}
		}
		
		// list custom libraries
		filterAndForEach(customJarInfos, 
						 jarInfo -> jarInfo.containsPackage(packageName), 
						 jarInfo -> listCustomJar(result, packageName, jarInfo));
		return result;
	}
	
	private void listDirectory(List<JavaFileObject> result, String packageName, File directory) {
		filterAndForEach(directory.listFiles(), 
						 file -> file.isFile() && isClassFile(file.getName()),
						 file -> result.add(new DependencyFileObject(
								 getQualifiedName(packageName, removeClassExtension(file.getName())), file.toURI())));
	}
	
	private void listJar(List<JavaFileObject> result, URL packageURL) throws IOException {
		final JarURLConnection jarCon = (JarURLConnection) packageURL.openConnection();
		final String packagePath = jarCon.getEntryName();
		final Enumeration<JarEntry> entryEnum = jarCon.getJarFile().entries();
		while (entryEnum.hasMoreElements()) {
			final String entryName = entryEnum.nextElement().getName();
			if (isClassFile(entryName) &&
				entryName.startsWith(packagePath) && 
				!isSubPackage(entryName, packagePath)) {
					result.add(new DependencyFileObject(getQualifiedName(entryName), 
														createJarURI(packageURL, entryName)));
			}
		}
	}
	
	private void listCustomJar(List<JavaFileObject> result, String packageName, CustomJarInfo customJar) {
		final String packagePath = getPackagePath(packageName);
		try (SafeZipInputStream zis = StreamUtils.getZipStream(customJar.getContent())) {
			ZipEntry entry;
			while ((entry = zis.getNextEntrySafe()) != null) {
				final String entryName = entry.getName();
				if (isClassFile(entryName) &&
					entryName.startsWith(packagePath) && 
					!isSubPackage(entryName, packagePath)) {
						result.add(new JavaClassFileObject(getQualifiedName(entryName), zis.readSafe(entry)));
				}
			}
		}
		catch (IOException ioex) {
			throw new CompilerException(ioex);
		}
	}
	
}
