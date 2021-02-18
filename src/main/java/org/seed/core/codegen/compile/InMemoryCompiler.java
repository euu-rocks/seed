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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.seed.core.codegen.Compiler;
import org.seed.core.codegen.GeneratedCode;
import org.seed.core.codegen.SourceCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class InMemoryCompiler implements Compiler {
	
	private static final Logger log = LoggerFactory.getLogger(InMemoryCompiler.class);
	
	private final Map<String, Class<GeneratedCode>> mapClasses = Collections.synchronizedMap(new HashMap<>());
	
	private JavaCompiler javaCompiler;
	
	private CompilerFileManager fileManager;
	
	@PostConstruct
	private void init() {
		javaCompiler = ToolProvider.getSystemJavaCompiler();
		if (javaCompiler == null) {
			throw new CompilerException("Java compiler not available. Use JDK instead of JRE");
		}
		fileManager = new CompilerFileManager(javaCompiler.getStandardFileManager(null, null, null));
		log.info("Found Java compiler: " + javaCompiler.getClass());
	}
	
	@Override
	public ClassLoader createClassLoader() {
		final GeneratedCodeClassLoader classLoader = new GeneratedCodeClassLoader(getClass().getClassLoader());
		for (JavaClassFileObject classFileObject : fileManager.getClassFileObjects()) {
			final Class<GeneratedCode> generatedClass = classLoader.defineClass(classFileObject);
			mapClasses.put(classFileObject.getQualifiedName(), generatedClass);
		}
		return classLoader;
	}
	
	@Override
	public Class<GeneratedCode> getGeneratedClass(String qualifiedName) {
		Assert.notNull(qualifiedName, "qualifiedName is null");
		Assert.state(!mapClasses.isEmpty(), "no classes available");
		
		return mapClasses.get(qualifiedName);
	}
	
	@Override
	public List<Class<GeneratedCode>> getGeneratedClasses(Class<?> typeClass) {
		Assert.notNull(typeClass, "typeClass is null");
		
		final List<Class<GeneratedCode>> generatedClasses = new ArrayList<>();
		for (Class<GeneratedCode> generatedClass : mapClasses.values()) {
			if (typeClass.isAssignableFrom(generatedClass)) {
				generatedClasses.add(generatedClass);
			}
		}
		return generatedClasses;
	}
	
	@Override
	public void compile(List<SourceCode<?>> sourceCodes) {
		Assert.notNull(sourceCodes, "sourceCodes is null");
		
		log.info("Compiling: " + Arrays.asList(sourceCodes));
		final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		final CompilationTask task = javaCompiler.getTask(null, fileManager, diagnostics, null, null, 
														  fileManager.createSourceFileObjects(sourceCodes));
		if (!task.call()) {
			for (SourceCode<?> sourceCode : sourceCodes) {
				fileManager.removeClassFileObject(sourceCode.getQualifiedName());
			}
			throw new CompilerException(diagnostics.getDiagnostics());
		}
	}
	
}
