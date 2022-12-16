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

import static org.seed.core.codegen.CodeUtils.*;
import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import org.hibernate.Session;

import org.seed.core.codegen.compile.CompilerException;
import org.seed.core.config.SessionProvider;
import org.seed.core.config.SystemLog;
import org.seed.core.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CodeManagerImpl implements CodeManager {
	
	private class CompilerStatusCheck extends TimerTask {

		@Override
		public void run() {
			if (compilerError) {
				compilerError = !compileAllCodeBuilders();
			}
		}
	}
	
	private static final Logger log = LoggerFactory.getLogger(CodeManagerImpl.class);
	
	public static final String API_PACKAGE				   = "org.seed.core.api";
	
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
	private SessionProvider sessionProvider;
	
	@Autowired
	private List<SourceCodeProvider> codeProviders;
	
	private volatile boolean compilerError = false;
	
	private Date lastCompilerRun;
	
	@PostConstruct
	private void init() {
		new Timer("CodeManagerTask")
			.schedule(new CompilerStatusCheck(), 0, 60 * 1000); // every minute
	}
	
	@Override
	public boolean existsCompilerError() {
		return compilerError;
	}
	
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
	public void removeClass(String qualifiedName) {
		compiler.removeClass(qualifiedName);
	}
	
	@Override
	public void testCompile(SourceCode sourceCode) {
		Assert.notNull(sourceCode, "source code");
		
		compiler.compile(Collections.singletonList(sourceCode));
	}
	
	@Override
	public void generateClasses() {
		final List<SourceCode> sourceCodeList = buildSources(collectCodeBuilders(false));
		if (sourceCodeList.isEmpty()) {
			return;
		}
		compilerError = false;
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
	
	private void compile(SourceCode sourceCode) {
		compile(Collections.singletonList(sourceCode));
	}
	
	private void compile(List<SourceCode> sourceCodeList) {
		compiler.compile(sourceCodeList);
		lastCompilerRun = new Date();
	}
	
	private boolean compileAllCodeBuilders() {
		try {
			compiler.compileSeparately(buildSources(collectCodeBuilders(true)));
			return true;
		}
		catch (CompilerException cex) {
			return false;
		}
	}
	
	private boolean compileAllClasses(List<SourceCode> sourceCodeList) {
		try {
			compile(sourceCodeList);
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
			compile(subList(sourceCodeList, this::isEntitySource));
		}
		catch (CompilerException cex) {
			log.warn("Error while compiling entity classes {}", cex.getMessage());
			systemLog.logError("systemlog.error.compileentities", cex);
			compilerError = true;
		}
	}
	
	private void compileTransformClasses(List<SourceCode> sourceCodeList) {
		for (SourceCode source : subList(sourceCodeList, this::isTransformSource)) {
			try {
				compile(source);
			}
			catch (CompilerException cex) {
				log.warn("Error while compiling entity transformation class {}", cex.getMessage());
				systemLog.logError("systemlog.error.compiletransform", cex);
				compilerError = true;
			}
		}
	}
	
	private void compileRestClasses(List<SourceCode> sourceCodeList) {
		for (SourceCode source : subList(sourceCodeList, this::isRestSource)) {
			try {
				compile(source);
			}
			catch (CompilerException cex) {
				log.warn("Error while compiling rest class {}", cex.getMessage());
				systemLog.logError("systemlog.error.compilerest", cex);
				compilerError = true;
			}
		}
	}
	
	private void compileTaskClasses(List<SourceCode> sourceCodeList) {
		for (SourceCode source : subList(sourceCodeList, this::isTaskSource)) {
			try {
				compile(source);
			}
			catch (CompilerException cex) {
				log.warn("Error while compiling task class {}", cex.getMessage());
				systemLog.logError("systemlog.error.compiletask", cex);
				compilerError = true;
			}
		}
	}
	
	private void compileCustomClasses(List<SourceCode> sourceCodeList) {
		try {
			compile(subList(sourceCodeList, this::isCustomSource));
		}
		catch (CompilerException cex) {
			log.warn("Error while compiling custom classes {}", cex.getMessage());
			systemLog.logError("systemlog.error.compilecustom", cex);
			compilerError = true;
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
	
	private boolean isCustomSource(SourceCode code) {
		return !(isEntitySource(code) ||
				 isTransformSource(code) ||
				 isRestSource(code) ||
				 isTaskSource(code));
	}
	
	private List<SourceCodeBuilder> collectCodeBuilders(boolean all) {
		final List<SourceCodeBuilder> builderList = new ArrayList<>();
		try (Session session = sessionProvider.getSession()) {
			for (SourceCodeProvider codeProvider : codeProviders) {
                // compile only if code has changed since last compiler run
				builderList.addAll(subList(codeProvider.getSourceCodeBuilders(session), 
										   builder -> all || lastCompilerRun == null || 
										   			  builder.getLastModified().after(lastCompilerRun)));
			}
		}
		return builderList;
	}
	
	private static List<SourceCode> buildSources(List<SourceCodeBuilder> builderList) {
		final List<SourceCode> sourceCodeList = convertedList(builderList, SourceCodeBuilder::build);
		if (log.isDebugEnabled()) {
			sourceCodeList.forEach(sourceCode -> 
				log.debug("generated source: {}", System.lineSeparator() + sourceCode.getContent()));
		}
		return sourceCodeList;
	}
	
}
