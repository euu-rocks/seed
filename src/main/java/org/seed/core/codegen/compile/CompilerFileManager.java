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
import java.util.stream.Collectors;

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

import static org.seed.core.codegen.CodeUtils.*;

class CompilerFileManager extends ForwardingJavaFileManager<JavaFileManager> {
	
	private final Map<String, JavaClassFileObject> classFileObjectMap = new HashMap<>();
	
	CompilerFileManager(StandardJavaFileManager standardFileManager) {
		super(standardFileManager);
	}
	
	List<JavaClassFileObject> getClassFileObjects() {
		synchronized (classFileObjectMap) {
			return classFileObjectMap.values().stream().collect(Collectors.toList());
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
		
		return sourceCodes.stream().map(JavaSourceFileObject::new)
								   .collect(Collectors.toList());
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
		if (location == StandardLocation.CLASS_PATH && 
			kinds.contains(Kind.CLASS)) {
			if (packageName.contains(".generated")) {
				return listGeneratedClasses(packageName);
			}
			if (!packageName.startsWith("java.")) {
				return listDependencies(packageName);
			}
		}
		return super.list(location, packageName, kinds, recurse);
	}
	
	private Iterable<JavaFileObject> listGeneratedClasses(String packageName) {
		final List<JavaFileObject> result = new ArrayList<>();
		synchronized (classFileObjectMap) {
			for (Entry<String, JavaClassFileObject> entry : classFileObjectMap.entrySet()) {
				if (packageName.equals(extractPackageName(entry.getKey()))) {
					result.add(entry.getValue());
				}
			}
		}
		return result;
	}
	
	private Iterable<JavaFileObject> listDependencies(String packageName) throws IOException {
		final List<JavaFileObject> result = new ArrayList<>();
		final Enumeration<URL> urlEnum = getClass().getClassLoader().getResources(packageName.replace('.', '/'));
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
		return result;
	}
	
	private void listDirectory(List<JavaFileObject> result, String packageName, File directory) {
		for (File file : directory.listFiles()) {
			if (file.isFile() && isClassFile(file.getName())) {
				final String qualifiedName = getQualifiedName(packageName, removeClassExtension(file.getName()));
				result.add(new DependencyFileObject(qualifiedName, file.toURI()));
			}
		}
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
					final String qualifiedName = removeClassExtension(entryName.replace('/', '.'));
					result.add(new DependencyFileObject(qualifiedName, createJarURI(packageURL, entryName)));
			}
		}
	}
	
}
