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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.seed.core.codegen.CodeUtils.isJarFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

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
	
	private List<ApplicationEntityService<?>> sortedServices; // sorted by dependencies
	
	private File externalModuleDir;	// application.properties module.external.dir
	
	private final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
	
	@PostConstruct
	private void init() throws Exception {
		marshaller.setPackagesToScan("org.seed.core");
		marshaller.setMarshallerProperties(Collections.singletonMap(Marshaller.JAXB_FORMATTED_OUTPUT, true));
		marshaller.afterPropertiesSet();
		sortedServices = sortByDependencies(applicationServices);
		
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
		Map<String, byte[]> mapJars = null;
		Map<String, byte[]> mapTransferContents = null;
		Module module = null;
		
		try (ZipInputStream zis = new ZipInputStream(inputStream)) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				// read module
				if (MODULE_XML_FILENAME.equals(entry.getName())) {
					module = (Module) marshaller.unmarshal(
							new StreamSource(new ByteArrayInputStream(zis.readAllBytes())));
				}
				// read jar files
				else if (isJarFile(entry.getName())) {
					if (mapJars == null) {
						mapJars = new HashMap<>();
					}
					mapJars.put(entry.getName(), zis.readAllBytes());
				}
				// read transfer files
				else if (isTransferFile(entry.getName())) {
					if (mapTransferContents == null) {
						mapTransferContents = new HashMap<>();
					}
					mapTransferContents.put(entry.getName(), zis.readAllBytes());
				}
				// ignore other entries
			}
		}
		Assert.stateAvailable(module, MODULE_XML_FILENAME);
		if (module != null) {
			initModuleContent(module, mapJars, mapTransferContents);
		}
		return module;
	}
	
	Module readModuleFromDir(String moduleName) throws IOException {
		Assert.notNull(moduleName, "module name");
		Assert.stateAvailable(externalModuleDir, "external module dir");
		Map<String, byte[]> mapJars = null;
		Map<String, byte[]> mapTransferContents = null;
		Module module = null;
		
		final File moduleDir = new File(externalModuleDir, moduleName);
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
					module = (Module) marshaller.unmarshal(new StreamSource(fis));
				}
				// read jar files
				else if (isJarFile(file.getName())) {
					if (mapJars == null) {
						mapJars = new HashMap<>();
					}
					mapJars.put(file.getName(), fis.readAllBytes());
				}
				// read transfer files
				else if (isTransferFile(file.getName())) {
					if (mapTransferContents == null) {
						mapTransferContents = new HashMap<>();
					}
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
		
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
		    // write module
		    writeZipEntry(zos, MODULE_XML_FILENAME, getModuleContent(module));
		    // write custom libs
		    for (CustomLib customLib : module.getCustomLibs()) {
		    	writeZipEntry(zos, customLib.getFilename(), customLib.getContent());
		    }
		    // write transferable objects
		    for (Entity entity : module.getTransferableEntities()) {
		    	writeZipEntry(zos, entity.getInternalName() + TransferFormat.CSV.fileExtension, 
		    				  transferService.doExport(entity));
		    }
	    }
	    return baos.toByteArray();
	}
	
	void exportModuleToDir(Module module) throws IOException {
		Assert.notNull(module, C.MODULE);
		Assert.stateAvailable(externalModuleDir, "external module dir");
		
		final File moduleDir = new File(externalModuleDir, module.getName());
		moduleDir.mkdir();
		// write module
		writeToExternalDir(moduleDir, MODULE_XML_FILENAME, getModuleContent(module));
		// write custom libs
	    for (CustomLib customLib : module.getCustomLibs()) {
	    	writeToExternalDir(moduleDir, customLib.getFilename(), customLib.getContent());
	    }
	    // write transferable objects
	    for (Entity entity : module.getTransferableEntities()) {
	    	writeToExternalDir(moduleDir, entity.getInternalName() + TransferFormat.CSV.fileExtension, 
	    					   transferService.doExport(entity));
	    }
	}
	
	ImportAnalysis analyzeModule(Module module) {
		Assert.notNull(module, C.MODULE);
		final ImportAnalysis analysis = new ImportAnalysis(module);
		final Module currentVersionModule = getCurrentVersionModule(module);
		
		sortedServices.forEach(service -> service.analyzeObjects(analysis, currentVersionModule));
		return analysis;
	}
	
	void importModule(Module module) {
		Assert.notNull(module, C.MODULE);
		final Module currentVersionModule = getCurrentVersionModule(module);
		final TransferContext context = new DefaultTransferContext(module);
		
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
				sortedServices.forEach(service -> service.importObjects(context, session));
				
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
				throw new InternalException(ex);
			}
		}
		
		// update configuration
		if (module.getEntities() != null) {
			Seed.getBean(UpdatableConfiguration.class).updateConfiguration();
		}
		
		importModuleContent(module);
	}
	
	private void importModuleContent(Module module) {
		// import transferable entity content
		if (module.getTransferableEntities() != null) {
			for (Entity entity : module.getTransferableEntities()) {
				final byte[] content = module.getTransferContent(entity);
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
		final List<ApplicationEntityService<?>> services = new ArrayList<>(sortedServices);
		Collections.reverse(services);
		services.forEach(service -> service.deleteObjects(module, currentVersionModule, session));
	}
	
	private byte[] getModuleContent(Module module) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		((ModuleMetadata) module).setSchemaVersion(SchemaVersion.currentVersion());
		marshaller.marshal(module, new StreamResult(baos));
		return baos.toByteArray();
	}
	
	private static boolean isTransferFile(String fileName) {
		return fileName != null && fileName.toLowerCase().endsWith(TransferFormat.CSV.fileExtension);
	}
	
	private static void initModuleContent(Module module, 
								   Map<String, byte[]> mapJars, 
								   Map<String, byte[]> mapTransferContents) {
		// init custom libs content
		if (mapJars != null && module.getCustomLibs() != null) {
			for (CustomLib customLib : module.getCustomLibs()) {
				Assert.stateAvailable(mapJars.containsKey(customLib.getFilename()), customLib.getFilename());
				((CustomLibMetadata) customLib).setContent(mapJars.get(customLib.getFilename()));
			}
		}
		// store transfer file content in module
		if (mapTransferContents != null && module.getTransferableEntities() != null) {
			for (Entity entity : module.getTransferableEntities()) {
				final byte[] content = mapTransferContents.get(entity.getInternalName() + TransferFormat.CSV.fileExtension);
				if (content != null) {
					module.addTransferContent(entity, content);
				}
			}
		}
	}
	
	private static void writeToExternalDir(File moduleDir, String name, byte[] content) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(new File(moduleDir, name))) {
			fos.write(content);
		}
	}
	
	private static void writeZipEntry(ZipOutputStream zos, String name, byte[] content) throws IOException {
		final ZipEntry entry = new ZipEntry(name);
		entry.setSize(content.length);
		zos.putNextEntry(entry);
		zos.write(content);
		zos.closeEntry();
	}
	
	private static List<ApplicationEntityService<?>> sortByDependencies(List<ApplicationEntityService<?>> applicationServices) {
		final List<ApplicationEntityService<?>> result = new ArrayList<>(applicationServices.size());
		while (result.size() < applicationServices.size()) {
			for (ApplicationEntityService<?> applicationService : applicationServices) {
				if (result.contains(applicationService)) {
					continue;
				}
				if (dependenciesResolved(applicationService, result)) {
					result.add(applicationService);
				}
			}
		}
		return result;
	}
	
	private static boolean dependenciesResolved(ApplicationEntityService<?> applicationService,
										  		List<ApplicationEntityService<?>> resolvedServices) {
		if (applicationService.getImportDependencies() != null) {
			for (Class<?> dependency : applicationService.getImportDependencies()) {
				boolean dependencyResolved = false;
				// check already resolved services
				for (ApplicationEntityService<?> service : resolvedServices) {
					if (dependency.isAssignableFrom(service.getClass())) {
						dependencyResolved = true;
						break;
					}
				}
				if (!dependencyResolved) {
					return false;
				}
			}
		}
		return true;
	}

}
