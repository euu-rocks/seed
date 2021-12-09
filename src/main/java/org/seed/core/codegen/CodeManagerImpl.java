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

import org.seed.core.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.seed.core.codegen.CodeUtils.*;

@Component
public class CodeManagerImpl implements CodeManager {
	
	private static final Logger log = LoggerFactory.getLogger(CodeManagerImpl.class);
	
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
		Assert.notNull(generatedObject, "generatedObject is null");
		
		return getGeneratedClass(getQualifiedName(generatedObject.getGeneratedPackage(), 
												  generatedObject.getGeneratedClass()));
	}
	
	@Override
	public List<Class<GeneratedCode>> getGeneratedClasses(Class<?> type) {
		return compiler.getGeneratedClasses(type);
	}
	
	@Override
	public void testCompile(SourceCode sourceCode) {
		Assert.notNull(sourceCode, "sourceCode is null");
		
		compiler.compile(Collections.singletonList(sourceCode));
	}
	
	@Override
	public void generateClasses() {
		final List<SourceCode> sourceCodeList = buildSources(collectCodeBuilders());
		
		// compile
		if (!sourceCodeList.isEmpty()) {
			compiler.compile(sourceCodeList);
			lastCompilerRun = new Date();
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
