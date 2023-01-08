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
import org.seed.core.config.ApplicationProperties;
import org.seed.core.config.SchemaVersion;
import org.seed.core.config.SessionProvider;
import org.seed.core.config.UpdatableConfiguration;
import org.seed.core.customcode.CustomLib;
import org.seed.core.customcode.CustomLibMetadata;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.transfer.TransferFormat;
import org.seed.core.entity.transfer.TransferService;
import org.seed.core.util.Assert;
import org.seed.core.util.SafeZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.seed.core.codegen.CodeUtils.isJarFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;

/**
 * In case of "NPE while unmarshalling" 
 * check if public setter for collection exists (e.g. setElements(List<?> ...))
 */
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
		Module module = null;
		
		try (final var stream = new SafeZipInputStream(inputStream)) {
			ZipEntry entry;
			while ((entry = stream.getNextEntrySafe()) != null) {
				// read module
				if (MODULE_XML_FILENAME.equals(entry.getName())) {
					module = (Module) getMarshaller().unmarshal(
						new StreamSource(
							new ByteArrayInputStream(stream.readSafe(entry))));
				}
				// read jar files
				else if (isJarFile(entry.getName())) {
					mapJars.put(entry.getName(), stream.readSafe(entry));
				}
				// read transfer files
				else if (isTransferFile(entry.getName())) {
					mapTransferContents.put(entry.getName(), stream.readSafe(entry));
				}
				// ignore other entries
			}
		}
		if (module != null) {
			initModuleContent(module, mapJars, mapTransferContents);
		}
		return module;
	}
	
	Module readModuleFromDir(String moduleName) throws IOException {
		Assert.notNull(moduleName, "module name");
		Assert.stateAvailable(externalModuleDir, "external module dir");
		var mapJars = new HashMap<String, byte[]>();
		var mapTransferContents = new HashMap<String, byte[]>();
		Module module = null;
		
		final var moduleDir = new File(externalModuleDir, moduleName);
		if (!moduleDir.exists() || !moduleDir.isDirectory()) {
			return null;
		}
		for (File file : moduleDir.listFiles()) {
			if (!file.isFile() || file.isHidden()) {
				continue;
			}
			try (InputStream fis = new FileInputStream(file)) {
				// read module
				if (MODULE_XML_FILENAME.equals(file.getName())) {
					module = (Module) getMarshaller().unmarshal(new StreamSource(fis));
				}
				// read jar files
				else if (isJarFile(file.getName())) {
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
			initModuleContent(module, mapJars, mapTransferContents);
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
		
		final var moduleDir = new File(externalModuleDir, module.getName());
		moduleDir.mkdir();
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
	
	ImportAnalysis analyzeModule(Module module) {
		Assert.notNull(module, C.MODULE);
		final ImportAnalysis analysis = new ImportAnalysis(module);
		final Module currentVersionModule = getCurrentVersionModule(module);
		
		// module parameters
		if (module.hasParameters()) {
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
		
		// module objects
		sortByDependencies(applicationServices).forEach(service -> service.analyzeObjects(analysis, currentVersionModule));
		return analysis;
	}
	
	void importModule(Module module) throws ValidationException {
		Assert.notNull(module, C.MODULE);
		final Module currentVersionModule = getCurrentVersionModule(module);
		final var context = new DefaultTransferContext(module);
		final var sortedServices = sortByDependencies(applicationServices);
		try (Session session = sessionProvider.getSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				
				// module
				if (currentVersionModule != null) {
					deleteObjects(session, module, currentVersionModule);
					((ModuleMetadata) currentVersionModule).copySystemFieldsTo(module);
					session.detach(currentVersionModule);
				}
				
				// parameters
				if (module.hasParameters()) {
					importModuleParameters(module, currentVersionModule);
				}
				
				// module schema version
				SchemaVersion moduleSchemaVersion = module.getSchemaVersion();
				if (moduleSchemaVersion == null) {
					moduleSchemaVersion = SchemaVersion.V_0_9_21;
				}
				// schema update
				if (moduleSchemaVersion != SchemaVersion.currentVersion()) {
					for (int v = moduleSchemaVersion.ordinal() + 1; v <= SchemaVersion.currentVersion().ordinal(); v++) {
						final SchemaVersion version = SchemaVersion.getVersion(v);
						sortedServices.forEach(service -> service.handleSchemaUpdate(context, version));
					}
				}
				
				// save module
				moduleRepository.save(module, session);
				// init components
				for (final var service : sortedServices) {
					service.importObjects(context, session);
				}
				// changelogs
				if (module.getEntities() != null || module.getDBObjects() != null) {
					sortedServices.forEach(service -> service.createChangeLogs(context, session));
				}
				tx.commit();
			}
			catch (Exception ex) {
				if (tx != null) {
					tx.rollback();
				}
				if (ex instanceof ValidationException) {
					throw (ValidationException) ex;
				}
				throw new InternalException(ex);
			}
		}
		
		// update configuration
		if (module.getEntities() != null) {
			Seed.getBean(UpdatableConfiguration.class).updateConfiguration();
		}
		
		importModuleContent(module);
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
	
	private void importModuleContent(Module module) {
		// import transferable entity content
		if (module.getTransferableEntities() != null) {
			for (final var entity : module.getTransferableEntities()) {
				final var content = module.getTransferContent(entity);
				if (content != null) {
					try {
						transferService.doImport(entity, content);
					} 
					catch (ValidationException vex) {
						throw new InternalException(vex);
					}
				}
			}
		}
	}
	
	private Module getCurrentVersionModule(Module module) {
		return moduleRepository.findByUid(module.getUid());
	}
	
	private void deleteObjects(Session session, Module module, Module currentVersionModule) {
		final var services = sortByDependencies(applicationServices);
		Collections.reverse(services);
		services.forEach(service -> service.deleteObjects(module, currentVersionModule, session));
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
										  Map<String, byte[]> mapTransferContents) {
		// init custom libs content
		if (notEmpty(mapJars) && module.getCustomLibs() != null) {
			for (final var customLib : module.getCustomLibs()) {
				Assert.stateAvailable(mapJars.containsKey(customLib.getFilename()), customLib.getFilename());
				((CustomLibMetadata) customLib).setContent(mapJars.get(customLib.getFilename()));
			}
		}
		// store transfer file content in module
		if (notEmpty(mapTransferContents) && module.getTransferableEntities() != null) {
			for (final var entity : module.getTransferableEntities()) {
				final var content = mapTransferContents.get(entity.getInternalName().concat(TransferFormat.CSV.fileExtension));
				if (content != null) {
					module.addTransferContent(entity, content);
				}
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
			result.addAll(subList(applicationServices, 
								  service -> !result.contains(service) &&
											 dependenciesResolved(service, result)));
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
