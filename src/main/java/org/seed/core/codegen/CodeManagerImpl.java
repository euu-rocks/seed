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
package org.seed.core.codegen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.seed.core.codegen.compile.CompilerException;
import org.seed.core.config.SystemLog;
import org.seed.core.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.seed.core.codegen.CodeUtils.*;

@Component
public class CodeManagerImpl implements CodeManager {
	
	private static final Logger log = LoggerFactory.getLogger(CodeManagerImpl.class);
	
	public static final String GENERATED_ENTITY_PACKAGE    = "org.seed.generated.entity";
	public static final String GENERATED_REST_PACKAGE      = "org.seed.generated.rest";
	public static final String GENERATED_TASK_PACKAGE      = "org.seed.generated.task";
	public static final String GENERATED_TRANSFORM_PACKAGE = "org.seed.generated.transform";
	
	@Autowired
	private SystemLog systemLog;
	
	@Autowired
	private Compiler compiler;
	
	@Autowired
	private ExternalCodeManager externalCodeManager;
	
	@Autowired
	private List<SourceCodeProvider> codeProviders;
	
	private Date lastCompilerRun;
	
	@Override
	public ClassLoader getClassLoader() {
		return compiler.createClassLoader();
	}
	
	@Override
	public Class<GeneratedCode> getGeneratedClass(String qualifiedName) {
		return compiler.getGeneratedClass(qualifiedName);
	}
	
	@Override
	public Class<GeneratedCode> getGeneratedClass(GeneratedObject generatedObject) {
		Assert.notNull(generatedObject, "generated object");
		
		return getGeneratedClass(getQualifiedName(generatedObject.getGeneratedPackage(), 
												  generatedObject.getGeneratedClass()));
	}
	
	@Override
	public List<Class<GeneratedCode>> getGeneratedClasses(Class<?> type) {
		return compiler.getGeneratedClasses(type);
	}
	
	@Override
	public void testCompile(SourceCode sourceCode) {
		Assert.notNull(sourceCode, "source code");
		
		compiler.compile(Collections.singletonList(sourceCode));
	}
	
	@Override
	public void generateClasses() {
		final List<SourceCode> sourceCodeList = buildSources(collectCodeBuilders());
		if (sourceCodeList.isEmpty()) {
			return;
		}
		// compile
		if (!compileAllClasses(sourceCodeList)) {
			// fallback: compile classes separately
			compileEntityClasses(sourceCodeList);
			compileCustomClasses(sourceCodeList);
			compileTransformClasses(sourceCodeList);
			compileRestClasses(sourceCodeList);
			compileTaskClasses(sourceCodeList);
		}
		
		// external file storage
		if (externalCodeManager.isDownloadEnabled()) {
			for (SourceCode sourceCode : sourceCodeList) {
				externalCodeManager.storeToFileSystem(sourceCode);
			}
			// watch service
			if (externalCodeManager.isUploadEnabled()) {
				externalCodeManager.registerExternalSourcePathTree();
			}
		}
	}
	
	private boolean compileAllClasses(List<SourceCode> sourceCodeList) {
		try {
			compiler.compile(sourceCodeList);
			lastCompilerRun = new Date();
			return true;
		}
		catch (CompilerException cex) {
			log.warn("Compiling all classes failed");
			systemLog.logWarn("systemlog.warn.compileallfail");
			return false;
		}
	}
	
	private void compileEntityClasses(List<SourceCode> sourceCodeList) {
		try {
			compiler.compile(sourceCodeList.stream().filter(this::isEntitySource)
										   .collect(Collectors.toList()));
			lastCompilerRun = new Date();
		}
		catch (CompilerException cex) {
			log.warn("Error while compiling entity classes {}", cex.getMessage());
			systemLog.logError("systemlog.error.compileentities", cex);
		}
	}
	
	private void compileTransformClasses(List<SourceCode> sourceCodeList) {
		for (SourceCode source : sourceCodeList.stream().filter(this::isTransformSource)
											   .collect(Collectors.toList())) {
			try {
				compiler.compile(Collections.singletonList(source));
				lastCompilerRun = new Date();
			}
			catch (CompilerException cex) {
				log.warn("Error while compiling entity transformation class {}", cex.getMessage());
				systemLog.logError("systemlog.error.compiletransform", cex);
			}
		}
	}
	
	private void compileRestClasses(List<SourceCode> sourceCodeList) {
		for (SourceCode source : sourceCodeList.stream().filter(this::isRestSource)
											   .collect(Collectors.toList())) {
			try {
				compiler.compile(Collections.singletonList(source));
				lastCompilerRun = new Date();
			}
			catch (CompilerException cex) {
				log.warn("Error while compiling rest class {}", cex.getMessage());
				systemLog.logError("systemlog.error.compilerest", cex);
			}
		}
	}
	
	private void compileTaskClasses(List<SourceCode> sourceCodeList) {
		for (SourceCode source : sourceCodeList.stream().filter(this::isTaskSource)
											   .collect(Collectors.toList())) {
			try {
				compiler.compile(Collections.singletonList(source));
				lastCompilerRun = new Date();
			}
			catch (CompilerException cex) {
				log.warn("Error while compiling task class {}", cex.getMessage());
				systemLog.logError("systemlog.error.compiletask", cex);
			}
		}
	}
	
	private void compileCustomClasses(List<SourceCode> sourceCodeList) {
		try {
			compiler.compile(sourceCodeList.stream().filter(this::isOtherSource)
										   .collect(Collectors.toList()));
			lastCompilerRun = new Date();
		}
		catch (CompilerException cex) {
			log.warn("Error while compiling custom classes {}", cex.getMessage());
			systemLog.logError("systemlog.error.compilecustom", cex);
		}
	}
	
	private boolean isEntitySource(SourceCode code) {
		return code.getPackageName().equals(GENERATED_ENTITY_PACKAGE);
	}
	
	private boolean isTransformSource(SourceCode code) {
		return code.getPackageName().equals(GENERATED_TRANSFORM_PACKAGE);
	}
	
	private boolean isRestSource(SourceCode code) {
		return code.getPackageName().equals(GENERATED_REST_PACKAGE);
	}
	
	private boolean isTaskSource(SourceCode code) {
		return code.getPackageName().equals(GENERATED_TASK_PACKAGE);
	}
	
	private boolean isOtherSource(SourceCode code) {
		return !(isEntitySource(code) ||
				 isTransformSource(code) ||
				 isRestSource(code) ||
				 isTaskSource(code));
	}
	
	private List<SourceCodeBuilder> collectCodeBuilders() {
		final List<SourceCodeBuilder> builderList = new ArrayList<>();
		for (SourceCodeProvider codeProvider : codeProviders) {
			for (SourceCodeBuilder builder : codeProvider.getSourceCodeBuilders()) {
				// compile only if code has changed since last compiler run
				if (lastCompilerRun == null || builder.getLastModified().after(lastCompilerRun)) {
					builderList.add(builder);
				}
			}
		}
		return builderList;
	}
	
	private List<SourceCode> buildSources(List<SourceCodeBuilder> builderList) {
		final List<SourceCode> sourceCodeList = new ArrayList<>(builderList.size());
		for (SourceCodeBuilder codeBuilder : builderList) {
			final SourceCode sourceCode = codeBuilder.build();
			if (log.isDebugEnabled()) {
				log.debug("generated source: {}", sourceCode.getQualifiedName() + System.lineSeparator() + sourceCode.getContent());
			}
			sourceCodeList.add(sourceCode);
		}
		return sourceCodeList;
	}

}
