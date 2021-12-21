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
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.Seed;
import org.seed.core.config.SessionProvider;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;
import org.seed.core.util.NameUtils;
import org.seed.core.util.StreamUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExternalCodeManager implements ApplicationContextAware {
	
	private static final Logger log = LoggerFactory.getLogger(ExternalCodeManager.class);
	
	@Autowired
	private SessionProvider sessionProvider;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private Compiler compiler;
	
	private ApplicationContext applicationContext;
	
	private WatchService watchService;
	
	private File externalSourcesRootDir;	// application.properties codegen.external.rootdir
	
	private List<CodeChangeAware> changeAwareObjects;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	@PostConstruct
	private void init() {
		final String propDownloadSources = environment.getProperty("codegen.external.downloadsources");
		if (!NameUtils.booleanValue(propDownloadSources)) {
			return;	// abort
		}
		
		// init download
		final String propExternalRootDir = environment.getProperty("codegen.external.rootdir");
		if (propExternalRootDir == null) {
			log.warn("property codegen.external.rootdir is not defined");
			return;
		}
		
		log.info("Enable external source files at {}", propExternalRootDir);
		final File fileExternalRootDir = new File(propExternalRootDir);
		if (fileExternalRootDir.exists() && fileExternalRootDir.isDirectory()) {
			externalSourcesRootDir = fileExternalRootDir;
			try {
				FileUtils.cleanDirectory(externalSourcesRootDir);
			} 
			catch (IOException ioex) {
				log.warn("Could not delete dir content {}", externalSourcesRootDir, ioex);
			}
		}
		else {
			log.warn("Location {} does not exist or is not a directory", fileExternalRootDir);
			return;
		}
		
		// init upload
		final String propUploadChanges = environment.getProperty("codegen.external.uploadchanges");
		if (NameUtils.booleanValue(propUploadChanges)) {
			log.info("Enable code uploads on change");
			try {
				watchService = FileSystems.getDefault().newWatchService();
			} 
			catch (IOException ioex) {
				log.warn("Setting up WatchService failed", ioex);
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
				
				boolean changesProcessed = proccessWatchEvents(watchKey, dir);
				watchKey.reset();
				
				if (changesProcessed) {
					Seed.updateConfiguration();
				}
			} 
			catch (InterruptedException e) {
				log.warn("WatchServiceTask interuppted");
				Thread.currentThread().interrupt();
			}
		}
	}
	
	boolean isDownloadEnabled() {
		return externalSourcesRootDir != null;
	}
	
	boolean isUploadEnabled() {
		return watchService != null;
	}
	
	void storeToFileSystem(SourceCode sourceCode) {
		Assert.stateAvailable(externalSourcesRootDir, "externalSourcesRootDir");
		
		final File file = new File(externalSourcesRootDir, CodeUtils.getSourceFileName(sourceCode.getQualifiedName()));
		final File dir = file.getParentFile();
		if (!dir.exists() && !dir.mkdirs()) {
			log.warn("Couldn't create external source dir {}", dir.getAbsolutePath());
			return;
		}
		try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), StreamUtils.CHARSET)) {
			osw.append(sourceCode.getContent());
		}
		catch (IOException ioex) {
			log.warn("Couldn't write external source file for {}", sourceCode.getQualifiedName(), ioex);
		}
	}
	
	void registerExternalSourcePathTree() {
		Assert.stateAvailable(externalSourcesRootDir, "externalSourcesRootDir");
		Assert.stateAvailable(watchService, "watchService");
		try {
			Files.walkFileTree(externalSourcesRootDir.toPath(), new SimpleFileVisitor<Path>() {
	
		        @Override
		        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
		                throws IOException {
		            dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
		            return FileVisitResult.CONTINUE;
		        }
	
		    });
		} 
		catch (IOException ioex) {
			log.warn("Register path tree failed", ioex);
		}
	}
	
	private boolean proccessWatchEvents(WatchKey watchKey, Path dir) {
		boolean changesProcessed = false;
		if (sessionProvider.isSessionAvailable()) {
			try (Session session = sessionProvider.getSession()) {
				Transaction tx = session.beginTransaction();
				changesProcessed = pollEvents(watchKey, dir, session);
				tx.commit();
			}
			catch (Exception ex) {
				log.warn("pollWatchEvents failed: {}", ex.getMessage());
			}
		}
		return changesProcessed;
	}
	
	private boolean pollEvents(WatchKey watchKey, Path dir, Session session) {
		boolean changesProcessed = false;
		for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
			final Path file = (Path) watchEvent.context();
			try {
				if (CodeUtils.isSourceFile(file.toString()) &&
					onSourceFileChange(dir, file, session)) {
					changesProcessed = true;
				}
			}
			catch (Exception ex) {
				log.warn("source file upload failed: {}", ex.getMessage());
				return false;
			}
		}
		return changesProcessed;
	}
	
	private boolean onSourceFileChange(Path dir, Path file, Session session) throws IOException {
		final String code = StreamUtils.getFileAsText(new File(dir.toFile(), file.toFile().getName()));
		final String packageName = CodeUtils.getPackageName(externalSourcesRootDir, dir.toFile());
		final String className = CodeUtils.getClassName(file.toFile());
		final SourceCode sourceCode = new SourceCodeImpl(new ClassMetadata(packageName, className), code);
		try {
			compiler.compile(Collections.singletonList(sourceCode));
		}
		catch (Exception ex) {
			log.warn("Compilation failed: {}", ex.getMessage());
			return false;
		}
		
		boolean changeProcessed = false;
		for (CodeChangeAware changeAware : getChangeAwareObjects()) {
			if (changeAware.processCodeChange(sourceCode, session)) {
				changeProcessed = true;
				break;
			}
		}
		return changeProcessed;
	}
	
	private List<CodeChangeAware> getChangeAwareObjects() {
		if (changeAwareObjects == null) {
			changeAwareObjects = BeanUtils.getBeans(applicationContext, CodeChangeAware.class);

		}
		return changeAwareObjects;
	}
	
}
