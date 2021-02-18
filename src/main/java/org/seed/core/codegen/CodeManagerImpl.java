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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.core.config.SessionFactoryProvider;
import org.seed.core.config.UpdatableConfiguration;
import org.seed.core.util.MiscUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import static java.nio.file.StandardWatchEventKinds.*;

import static org.seed.core.codegen.CodeUtils.*;

@Component
public class CodeManagerImpl implements CodeManager {
	
	protected static final Logger log = LoggerFactory.getLogger(CodeManagerImpl.class);
	
	@Autowired
	private SessionFactoryProvider sessionFactoryProvider;
	
	@Autowired
	private UpdatableConfiguration configuration;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private Compiler compiler;
	
	@Autowired
	private List<SourceCodeProvider<?>> codeProviders;
	
	@Autowired
	private List<CodeChangeAware> changeAwareObjects;
	
	private Date lastCompilerRun;
	
	private File externalSourcesRootDir;	// application.properties codegen.external.rootdir
	
	private WatchService watchService;
	
	@PostConstruct
	private void init() {
		// init external sources
		final String propExternalRootDir = environment.getProperty("codegen.external.rootdir");
		if (propExternalRootDir != null) {
			log.info("Enable external source files at " + propExternalRootDir);
			final File fileExternalRootDir = new File(propExternalRootDir);
			if (fileExternalRootDir.exists() && fileExternalRootDir.isDirectory()) {
				externalSourcesRootDir = fileExternalRootDir;
				try {
					FileUtils.cleanDirectory(externalSourcesRootDir);
				} catch (IOException ioex) {
					log.warn("Could not delete dir content " + externalSourcesRootDir, ioex);
				}
			}
			else {
				log.warn("Location " + fileExternalRootDir + " does not exist or is not a directory");
				return;
			}
			// init upload changes
			final String propUploadChanges = environment.getProperty("codegen.external.uploadchanges");
			if (MiscUtils.booleanProperty(propUploadChanges)) {
				log.info("Enable code uploads on change");
				try {
					watchService = FileSystems.getDefault().newWatchService();
				} 
				catch (IOException ioex) {
					log.warn("Setting up WatchService failed", ioex);
				}
			}
		}
	}
	
	@Scheduled(fixedRate=10000)
	private void pollWatchServiceEvents() {
		if (watchService != null) {
			try {
				final WatchKey watchKey = watchService.take();
				final Path dir = (Path) watchKey.watchable();
				Thread.sleep(50); // prevent double event detection (https://stackoverflow.com/questions/16777869/java-7-watchservice-ignoring-multiple-occurrences-of-the-same-event)
				
				boolean changesProcessed = false;
				final Session session = getSession();
				if (session != null) {
					Transaction tx = null;
					try {
						tx = session.beginTransaction();
						for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
							final Path file = (Path) watchEvent.context();
							if (isSourceFile(file.toString())) {
								try {
									if (onExternalSourceFileChanged(dir, file, session)) {
										changesProcessed = true;
									}
								}
								catch (Exception ex) {
									log.warn("source file upload failed: " + ex.getMessage());
								}
							}
						}
						try {
							tx.commit();
						} 
						catch (Exception ex) {
							// ignore if session is closed
						}
					}
					catch (Exception ex) {
						try {
							tx.rollback();
						}
						catch (Exception ex1) {
							// ignore if session is closed
						}
						log.warn("pollWatchServiceEvents failed: " + ex.getMessage());
					}
					finally {
						try {
							session.close();
						}
						catch (Exception ex1) {
							// ignore if session is closed
						}
					}
				}
				watchKey.reset();
				if (changesProcessed) {
					configuration.updateConfiguration();
				}
			} 
			catch (InterruptedException e) {
				log.warn("WatchServiceTask interuppted");
			}
		}
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
		return getGeneratedClass(getQualifiedName(generatedObject.getGeneratedPackage(), 
												  generatedObject.getGeneratedClass()));
	}
	
	@Override
	public List<Class<GeneratedCode>> getGeneratedClasses(Class<?> type) {
		return compiler.getGeneratedClasses(type);
	}
	
	@Override
	public void testCompile(SourceCode<?> sourceCode) {
		Assert.notNull(sourceCode, "sourceCode is null");
		
		compiler.compile(Collections.singletonList(sourceCode));
	}
	
	@Override
	public void generateClasses() {
		final List<SourceCode<?>> sourceCodeList = new ArrayList<>();
		final List<SourceCodeBuilder<?>> builderList = new ArrayList<>();
		
		// collect relevant code builders
		for (SourceCodeProvider<?> codeProvider : codeProviders) {
			for (SourceCodeBuilder<?> builder : codeProvider.getSourceCodeBuilders()) {
				// compile only if code has changed since last compiler run
				if (lastCompilerRun == null || builder.getLastModified().after(lastCompilerRun)) {
					builderList.add(builder);
				}
			}
		}
		
		// build sources
		for (SourceCodeBuilder<?> codeBuilder : builderList) {
			final SourceCode<?> sourceCode = codeBuilder.build();
			if (log.isDebugEnabled()) {
				log.debug("generated source: " + sourceCode.getQualifiedName() + System.lineSeparator() + sourceCode.getContent());
			}
			sourceCodeList.add(sourceCode);
		}
		
		// compile
		if (!sourceCodeList.isEmpty()) {
			compiler.compile(sourceCodeList);
			lastCompilerRun = new Date();
		}
		
		// file storage
		if (externalSourcesRootDir != null) {
			for (SourceCode<?> sourceCode : sourceCodeList) {
				try {
					storeToFileSystem(sourceCode);
				} 
				catch (IOException e) {
					log.warn("Couldn't write external source file for " + sourceCode.getQualifiedName(), e);
				}
			}
			// watch service
			if (watchService != null) {
				try {
					registerExternalSourcePathTree();
				} catch (IOException ioex) {
					log.warn("Register path tree failed", ioex);
				}
			}
		}
	}
	
	private Session getSession() {
		try {
			return sessionFactoryProvider.getSessionFactory().openSession();
		}
		catch (Exception ex) {
			return null;
		}
	}
	
	private void registerExternalSourcePathTree() throws IOException {
		Assert.state(externalSourcesRootDir != null, "externalSourcesRootDir not available");
		Assert.state(watchService != null, "watchService not available");
		
		Files.walkFileTree(externalSourcesRootDir.toPath(), new SimpleFileVisitor<Path>() {

	        @Override
	        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
	                throws IOException {
	            dir.register(watchService, ENTRY_MODIFY);
	            return FileVisitResult.CONTINUE;
	        }

	    });
	}
	
	private void storeToFileSystem(SourceCode<?> sourceCode) throws IOException {
		Assert.state(externalSourcesRootDir != null, "externalSourcesRootDir not available");
		
		final File file = new File(externalSourcesRootDir, getSourceFileName(sourceCode.getQualifiedName()));
		final File dir = file.getParentFile();
		if (!dir.exists() && !dir.mkdirs()) {
			log.warn("Couldn't create external source dir " + dir.getAbsolutePath());
			return;
		}
		try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
			osw.append(sourceCode.getContent());
		}
	}
	
	private boolean onExternalSourceFileChanged(Path dir, Path file, Session session) {
		String code;
		try {
			code = MiscUtils.getFileAsText(new File(dir.toFile(), file.toFile().getName()));
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		} 
		
		final String packageName = getPackageName(externalSourcesRootDir, dir.toFile());
		final String className = getClassName(file.toFile());
		final SourceCode<?> sourceCode = new SourceCodeImpl<>(new ClassMetadata(packageName, className), code);
		try {
			testCompile(sourceCode);
		}
		catch (Exception ex) {
			log.warn("Compilation failed", ex);
			return false;
		}
		
		boolean changeProcessed = false;
		for (CodeChangeAware changeAware : changeAwareObjects) {
			if (changeAware.processCodeChange(sourceCode, session)) {
				changeProcessed = true;
				break;
			}
		}
		return changeProcessed;
	}

}
