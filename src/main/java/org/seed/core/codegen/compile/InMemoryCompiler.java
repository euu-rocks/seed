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

import javax.annotation.PostConstruct;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.seed.C;
import org.seed.core.codegen.Compiler;
import org.seed.core.codegen.GeneratedCode;
import org.seed.core.codegen.SourceCode;
import org.seed.core.util.Assert;
import org.seed.core.util.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InMemoryCompiler implements Compiler {
	
	private static final Logger log = LoggerFactory.getLogger(InMemoryCompiler.class);
	
	@Autowired
	private CustomJarProvider customJarProvider;
	
	private Map<String, Class<GeneratedCode>> mapClasses;
	
	private JavaCompiler javaCompiler;
	
	private CompilerFileManager fileManager;
	
	private List<CustomJar> customJars;
	
	@PostConstruct
	private void init() {
		javaCompiler = ToolProvider.getSystemJavaCompiler();
		if (javaCompiler == null) {
			throw new CompilerException("Java compiler not available. Use JDK instead of JRE");
		}
		fileManager = new CompilerFileManager(javaCompiler.getStandardFileManager(null, null, null));
		log.info("Found Java compiler: {}", javaCompiler.getClass());
	}
	
	@Override
	public synchronized Class<GeneratedCode> getGeneratedClass(String qualifiedName) {
		Assert.notNull(qualifiedName, C.QUALIFIEDNAME);
		Assert.stateAvailable(mapClasses, "class map");
		
		return mapClasses.get(qualifiedName);
	}
	
	@Override
	public synchronized List<Class<GeneratedCode>> getGeneratedClasses(Class<?> typeClass) {
		Assert.notNull(typeClass, C.TYPECLASS);
		Assert.stateAvailable(mapClasses, "class map");
		
		return CollectionUtils.subList(mapClasses.values(), typeClass::isAssignableFrom);
	}
	
	@Override
	public synchronized void resetCustomJars() {
		customJars = null;
		fileManager.setCustomJars(null);
	}
	
	@Override
	public synchronized void compile(List<SourceCode> sourceCodes) {
		Assert.notNull(sourceCodes, "sourceCodes");
		if (sourceCodes.isEmpty()) {
			return;
		}
		
		log.info("Compiling: {}", sourceCodes);
		getCustomJars(); // load jars and init fileManager
		final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		final CompilationTask task = javaCompiler.getTask(null, fileManager, diagnostics, null, null, 
														  fileManager.createSourceFileObjects(sourceCodes));
		final Boolean result = task.call();
		if (result == null || !result) {
			sourceCodes.forEach(sourceCode -> 
				fileManager.removeClassFileObject(sourceCode.getQualifiedName()));
			throw new CompilerException(diagnostics.getDiagnostics());
		}
	}
	
	@Override
	public synchronized ClassLoader createClassLoader() {
		final ClassLoader parent = getCustomJars().isEmpty() 
				? getClass().getClassLoader() 
				: new CustomJarClassLoader(getCustomJars(), getClass().getClassLoader());
		final GeneratedCodeClassLoader classLoader 
				= new GeneratedCodeClassLoader(fileManager.getClassFileObjects(), parent);
		mapClasses = classLoader.getClassMap();
		return classLoader;
	}
	
	private List<CustomJar> getCustomJars() {
		if (customJars == null) {
			customJars = customJarProvider.getCustomJars();
			if (!customJars.isEmpty()) {
				fileManager.setCustomJars(customJars);
				log.info("Custom libraries loaded.");
			}
			else {
				fileManager.setCustomJars(null);
			}
		}
		return customJars;
	}
	
}
