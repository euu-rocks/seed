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
package org.seed.core.application.module;

import static org.seed.core.util.CollectionUtils.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.InternalException;
import org.seed.Seed;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.codegen.CodeUtils;
import org.seed.core.config.ApplicationProperties;
import org.seed.core.config.SchemaVersion;
import org.seed.core.config.SessionProvider;
import org.seed.core.config.SystemLog;
import org.seed.core.config.UpdatableConfiguration;
import org.seed.core.customcode.CustomLib;
import org.seed.core.customcode.CustomLibMetadata;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.transfer.TransferFormat;
import org.seed.core.entity.transfer.TransferService;
import org.seed.core.util.Assert;
import org.seed.core.util.StreamUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;

@Component
public class ModuleTransfer {
	
	private static final Logger log = LoggerFactory.getLogger(ModuleTransfer.class);
	
	static final String MODULE_XML_FILENAME = "module.xml";
	
	static final String MODULE_FILE_EXTENSION = ".seed";
	
	@Autowired
	private ApplicationProperties applicationProperties;
	
	@Autowired
	private ModuleRepository moduleRepository;
	
	@Autowired
	private TransferService transferService;
	
	@Autowired
	private SessionProvider sessionProvider;
	
	@Autowired
	private SystemLog systemLog;
	
	@Autowired
	private List<ApplicationEntityService<?>> applicationServices;
	
	private File externalModuleDir;	// application.properties module.external.dir
	
	private Jaxb2Marshaller marshaller;
	
	@PostConstruct
	private void init() {
		final String propExternalModuleDir = applicationProperties.getProperty(Seed.PROP_MODULE_EXT_ROOT_DIR);
		if (propExternalModuleDir == null) {
			return;
		}
		
		log.info("Enable external module files at {}", propExternalModuleDir);
		final File fileExternalModuleDir = new File(propExternalModuleDir);
		if (fileExternalModuleDir.exists() && fileExternalModuleDir.isDirectory()) {
			externalModuleDir = fileExternalModuleDir;
		}
		else {
			log.warn("Location {} does not exist or is not a directory", fileExternalModuleDir);
		}
	}
	
	boolean isExternalDirEnabled() {
		return externalModuleDir != null;
	}
	
	Module readModule(InputStream inputStream) throws IOException {
		Assert.notNull(inputStream, "input stream");
		var mapJars = new HashMap<String, byte[]>();
		var mapTransferContents = new HashMap<String, byte[]>();
		var mapNestedModules = new HashMap<String, Module>();
		
		Module module = null;
		try (final var stream = StreamUtils.getZipStream(inputStream)) {
			ZipEntry entry;
			while ((entry = stream.getNextEntrySafe()) != null) {
				final var name = entry.getName();
				final var content = stream.readSafe(entry);
				// read nested modules
				if (name.endsWith(MODULE_FILE_EXTENSION)) {
					var nestedModule = readModule(new ByteArrayInputStream(content));
					mapNestedModules.put(nestedModule.getUid(), nestedModule);
				}
				// read module
				else if (MODULE_XML_FILENAME.equals(name)) {
					module = (Module) getMarshaller().unmarshal(
						new StreamSource(new ByteArrayInputStream(content)));
				}
				// read jar files
				else if (CodeUtils.isJarFile(name)) {
					mapJars.put(name, content);
				}
				// read transfer files
				else if (isTransferFile(name)) {
					mapTransferContents.put(name, content);
				}
				// ignore other entries
			}
		}
		if (module != null) {
			initModuleContent(module, mapJars, mapTransferContents, mapNestedModules);
		}
		return module;
	}
	
	Module readModuleFromDir(String moduleDirName) throws IOException {
		Assert.notNull(moduleDirName, "module dir name");
		Assert.stateAvailable(externalModuleDir, "external module dir");
		
		return readModuleFromDir(moduleDirName, externalModuleDir);
	}
	
	private Module readModuleFromDir(String moduleDirName, File parentDir) throws IOException {
		final var mapJars = new HashMap<String, byte[]>();
		final var mapTransferContents = new HashMap<String, byte[]>();
		final var mapNestedModules = new HashMap<String, Module>();
		final var moduleDir = new File(parentDir, moduleDirName);
		
		Module module = null;
		if (!moduleDir.exists() || !moduleDir.isDirectory()) {
			return null;
		}
		for (File file : subList(moduleDir.listFiles(), not(File::isHidden))) {
			// nested module
			if (file.isDirectory()) {
				var nestedModule = readModuleFromDir(file.getName(), moduleDir);
				mapNestedModules.put(nestedModule.getUid(), nestedModule);
				continue;
			}
			// module files
			try (InputStream fis = new FileInputStream(file)) {
				// read module
				if (MODULE_XML_FILENAME.equals(file.getName())) {
					module = (Module) getMarshaller().unmarshal(new StreamSource(fis));
				}
				// read jar files
				else if (CodeUtils.isJarFile(file.getName())) {
					mapJars.put(file.getName(), fis.readAllBytes());
				}
				// read transfer files
				else if (isTransferFile(file.getName())) {
					mapTransferContents.put(file.getName(), fis.readAllBytes());
				}
				// ignore other entries
			}
		}
		if (module != null) {
			initModuleContent(module, mapJars, mapTransferContents, mapNestedModules);
		}
		return module;
	}
	
	byte[] exportModule(Module module) throws IOException {
		Assert.notNull(module, C.MODULE);
		
		try (final var stream = new FastByteArrayOutputStream()) {
			exportModule(module, stream);
			return stream.toByteArray();
		}
	}
	
	private void exportModule(Module module, OutputStream out) throws IOException {
		try (final var stream = new ZipOutputStream(out)) {
			// write nested modules
			if (module.hasNesteds()) {
				for (NestedModule nested : module.getNesteds()) {
					writeZipEntry(stream, nested.getFileName(), 
								  exportModule(nested.getNestedModule()));
				}
			}
			
			// write module
		    writeZipEntry(stream, MODULE_XML_FILENAME, getModuleContent(module));
		    
		    // write custom libs
		    for (CustomLib customLib : module.getCustomLibs()) {
		    	writeZipEntry(stream, customLib.getFilename(), customLib.getContent());
		    }
		    
		    // write transferable objects
		    for (Entity entity : module.getTransferableEntities()) {
		    	writeZipEntry(stream, entity.getInternalName() + TransferFormat.CSV.fileExtension, 
		    				  transferService.doExport(entity));
		    }
	    }
	}
	
	void exportModuleToDir(Module module) throws IOException {
		Assert.notNull(module, C.MODULE);
		Assert.stateAvailable(externalModuleDir, "external module dir");
		
		exportModuleToDir(module, externalModuleDir);
	}
	
	private void exportModuleToDir(Module module, File parentDir) throws IOException {
		// create module directory
		final var moduleDir = new File(parentDir, module.getInternalName());
		moduleDir.mkdir();
		
		// write nested modules
		if (module.hasNesteds()) {
			for (NestedModule nested : module.getNesteds()) {
				exportModuleToDir(nested.getNestedModule(), moduleDir);
			}
		}
		
		// write module
		writeToExternalDir(moduleDir, MODULE_XML_FILENAME, getModuleContent(module));
		
		// write custom libs
	    for (final var customLib : module.getCustomLibs()) {
	    	writeToExternalDir(moduleDir, customLib.getFilename(), customLib.getContent());
	    }
	    
	    // write transferable objects
	    for (final var entity : module.getTransferableEntities()) {
	    	writeToExternalDir(moduleDir, entity.getInternalName() + TransferFormat.CSV.fileExtension, 
	    					   transferService.doExport(entity));
	    }
	}
	
	void analyzeModule(Module module, ImportAnalysis analysis) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(analysis, "analysis");
		final Module currentVersionModule = getCurrentVersionModule(module);
		
		// nested modules
		if (module.hasNesteds()) {
			analyzeNesteds(module, analysis, currentVersionModule);
		}
		
		// module parameters
		if (module.hasParameters()) {
			analyzeParameters(module, analysis, currentVersionModule);
		}
		
		// module objects
		sortByDependencies(applicationServices).forEach(service -> service.analyzeObjects(analysis, currentVersionModule));
	}
	
	private void analyzeNesteds(Module module, ImportAnalysis analysis, Module currentVersionModule) {
		for (final var nested : module.getNesteds()) {
			if (currentVersionModule == null) {
				analysis.addChangeNew(nested);
			}
			else {
				final var currentVersionNested =
					currentVersionModule.getNestedByUid(nested.getUid());
				if (currentVersionNested == null) {
					analysis.addChangeNew(nested);
				}
				else if (!nested.isEqual(currentVersionNested)) {
					analysis.addChangeModify(nested);
				}
			}
			// analyze nested
			analysis.setModule(nested.getNestedModule());
			analyzeModule(nested.getNestedModule(), analysis);
		}
		analysis.setModule(module);
	}
	
	private void analyzeParameters(Module module, ImportAnalysis analysis, Module currentVersionModule) {
		for (final var parameter : module.getParameters()) {
			if (currentVersionModule == null) {
				analysis.addChangeNew(parameter);
			}
			else {
				final var currentVersionParameter =
					currentVersionModule.getParameterByUid(parameter.getUid());
				if (currentVersionParameter == null) {
					analysis.addChangeNew(parameter);
				}
				else if (!parameter.isEqual(currentVersionParameter)) {
					analysis.addChangeModify(parameter);
				}
			}
		}
	}
	
	void importModule(Module module) throws ValidationException {
		final var sortedServices = sortByDependencies(applicationServices);
		try (Session session = sessionProvider.getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				importModule(module, session, sortedServices);
				tx.commit();
				systemLog.logInfo("systemlog.info.moduleimported", module.getName());
			}
			catch (Exception ex) {
				if (tx != null) {
					tx.rollback();
				}
				if (ex instanceof ValidationException) {
					throw (ValidationException) ex;
				}
				systemLog.logError("systemlog.error.moduleimport", ex, module.getName());
				throw new InternalException(ex);
			}
		}
		
		// update configuration
		Seed.getBean(UpdatableConfiguration.class).updateConfiguration();
		
		// import content
		importModuleContent(module);
	}
	
	private void importModule(Module module, Session session, List<ApplicationEntityService<?>> services) 
		throws ValidationException {
		Assert.notNull(module, C.MODULE);
		final var context = new DefaultTransferContext(module);
		final var currentVersionModule = getCurrentVersionModule(module);
		
		// nested modules
		if (module.hasNesteds()) {
			importNestedModules(module, currentVersionModule);
			for (NestedModule nested : module.getNesteds()) {
				importModule(nested.getNestedModule(), session, services);
			}
		}
		
		// init module
		if (currentVersionModule != null) {
			removeObjects(session, module, currentVersionModule);
			((ModuleMetadata) currentVersionModule).copySystemFieldsTo(module);
			session.detach(currentVersionModule);
		}

		// parameters
		if (module.hasParameters()) {
			importModuleParameters(module, currentVersionModule);
		}
		
		// schema update handling
		handleSchemaUpdates(module, context, services);

		// save module
		moduleRepository.save(module, session);
		
		// import objects
		for (final var service : services) {
			service.importObjects(context, session);
		}
		// create changelogs
		if (module.getEntities() != null || module.getDBObjects() != null) {
			services.forEach(service -> service.createChangeLogs(context, session));
		}
	}
	
	private void importModuleParameters(Module module, Module currentVersionModule) {
		for (final var parameter : module.getParameters()) {
			parameter.setModule(module);
			if (currentVersionModule != null) {
				final var currentVersionParam = currentVersionModule.getParameterByUid(parameter.getUid());
				if (currentVersionParam != null) {
					currentVersionParam.copySystemFieldsTo(parameter);
				}
			}
		}
	}
	
	private void importNestedModules(Module module, Module currentVersionModule) {
		for (final var nested : module.getNesteds()) {
			nested.setParentModule(module);
			if (currentVersionModule != null) {
				final var currentVersionNested = currentVersionModule.getNestedByUid(nested.getUid());
				if (currentVersionNested != null) {
					currentVersionNested.copySystemFieldsTo(nested);
				}
			}
		}
	}
	
	private void importModuleContent(Module module) throws ValidationException {
		// nested modules
		if (module.hasNesteds()) {
			for (NestedModule nested : module.getNesteds()) {
				importModuleContent(nested.getNestedModule());
			}
		}
		
		// import transferable entity content
		if (module.getTransferableEntities() != null) {
			for (final var entity : module.getTransferableEntities()) {
				final var content = module.getTransferContent(entity);
				if (content != null) {
					transferService.doImport(entity, content);
				}
			}
		}
	}
	
	private Module getCurrentVersionModule(Module module) {
		return moduleRepository.findByUid(module.getUid());
	}
	
	private void removeObjects(Session session, Module module, Module currentVersionModule) {
		final var services = sortByDependencies(applicationServices);
		Collections.reverse(services);
		services.forEach(service -> service.removeObjects(module, currentVersionModule, session));
	}
	
	private byte[] getModuleContent(Module module) {
		try (final var stream = new FastByteArrayOutputStream()) {
			((ModuleMetadata) module).setSchemaVersion(SchemaVersion.currentVersion());
			getMarshaller().marshal(module, new StreamResult(stream));
			return stream.toByteArray();
		}
	}
	
	private synchronized Jaxb2Marshaller getMarshaller() {
		if (marshaller == null) {
			try {
				marshaller = new Jaxb2Marshaller();
				marshaller.setPackagesToScan("org.seed.core");
				marshaller.setMarshallerProperties(Collections.singletonMap(Marshaller.JAXB_FORMATTED_OUTPUT, true));
				marshaller.afterPropertiesSet();
			} 
			catch (Exception ex) {
				SystemLog.logError(ex);
				throw new InternalException(ex);
			}
		}
		return marshaller;
	}
	
	private static boolean isTransferFile(String fileName) {
		return fileName != null && fileName.toLowerCase().endsWith(TransferFormat.CSV.fileExtension);
	}
	
	private static void initModuleContent(Module module, 
										  Map<String, byte[]> mapJars, 
										  Map<String, byte[]> mapTransferContents,
										  Map<String, Module> mapNestedModules) {
		// init custom libs content
		if (notEmpty(mapJars) && notEmpty(module.getCustomLibs())) {
			for (final var customLib : module.getCustomLibs()) {
				final var content = mapJars.get(customLib.getFilename()); 
				Assert.stateAvailable(content, customLib.getFilename());
				((CustomLibMetadata) customLib).setContent(content);
			}
		}
		
		// init nested modules
		if (notEmpty(mapNestedModules) && notEmpty(module.getNesteds())) {
			for (final var nestedModule : module.getNesteds()) {
				final var nested = mapNestedModules.get(nestedModule.getNestedModuleUid());
				Assert.stateAvailable(nested, "nested module " + nestedModule.getNestedModuleUid());
				nestedModule.setNestedModule(nested);
			}
		}
		
		// store transfer file content in module
		if (notEmpty(mapTransferContents) && notEmpty(module.getTransferableEntities())) {
			for (final var entity : module.getTransferableEntities()) {
				final var content = mapTransferContents.get(entity.getInternalName().concat(TransferFormat.CSV.fileExtension));
				if (content != null) {
					module.addTransferContent(entity, content);
				}
			}
		}
	}
	
	private static void handleSchemaUpdates(Module module, TransferContext context,
											List<ApplicationEntityService<?>> services) {
		// module schema version
		SchemaVersion moduleSchemaVersion = module.getSchemaVersion();
		if (moduleSchemaVersion == null) {
			moduleSchemaVersion = SchemaVersion.V_0_9_21;
		}

		// schema update
		if (moduleSchemaVersion != SchemaVersion.currentVersion()) {
			for (int v = moduleSchemaVersion.ordinal() + 1; v <= SchemaVersion.currentVersion().ordinal(); v++) {
				final var version = SchemaVersion.getVersion(v);
				services.forEach(service -> service.handleSchemaUpdate(context, version));
			}
		}
	}
	
	private static void writeToExternalDir(File moduleDir, String name, byte[] content) throws IOException {
		try (final var stream = new FileOutputStream(new File(moduleDir, name))) {
			stream.write(content);
		}
	}
	
	private static void writeZipEntry(ZipOutputStream stream, String name, byte[] content) throws IOException {
		final var entry = new ZipEntry(name);
		entry.setSize(content.length);
		stream.putNextEntry(entry);
		stream.write(content);
		stream.closeEntry();
	}
	
	private static List<ApplicationEntityService<?>> sortByDependencies(List<ApplicationEntityService<?>> applicationServices) {
		final var result = new ArrayList<ApplicationEntityService<?>>(applicationServices.size());
		while (result.size() < applicationServices.size()) {
			result.addAll(subList(applicationServices, service -> !result.contains(service) && dependenciesResolved(service, result)));
		}
		return result;
	}
	
	private static boolean dependenciesResolved(ApplicationEntityService<?> applicationService, List<ApplicationEntityService<?>> resolvedServices) {
		for (final var dependencyClass : applicationService.getImportDependencies()) {
			// check already resolved services
			if (noneMatch(resolvedServices, service -> dependencyClass.isAssignableFrom(service.getClass()))) {
				return false;
			}
		}
		return true;
	}

}
