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
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.config.SessionFactoryProvider;
import org.seed.core.config.UpdatableConfiguration;
import org.seed.core.customcode.CustomLib;
import org.seed.core.customcode.CustomLibMetadata;
import org.seed.core.util.Assert;

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
	
	static final String MODULE_META_FILENAME = "module.xml";
	
	static final String MODULE_FILE_EXTENSION = ".seed"; 
	
	@Autowired
	private ModuleRepository moduleRepository;
	
	@Autowired
	private UpdatableConfiguration configuration;
	
	@Autowired
	private SessionFactoryProvider sessionFactoryProvider;
	
	@Autowired
	private List<ApplicationEntityService<?>> applicationServices;
	
	private List<ApplicationEntityService<?>> sortedServices; // sorted by dependencies
	
	private final Jaxb2Marshaller marshaller = new Jaxb2Marshaller(); // thread-safe
	
	@PostConstruct
	private void init() throws Exception {
		marshaller.setPackagesToScan("org.seed.core");
		marshaller.afterPropertiesSet();
		sortedServices = sortByDependencies(applicationServices);
	}
	
	
	
	public Module readModule(InputStream inputStream) throws IOException {
		Assert.notNull(inputStream, "input stream");
		Map<String, byte[]> mapJars = null;
		Module module = null;
		
		try (ZipInputStream zis = new ZipInputStream(inputStream)) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				// read module
				if (MODULE_META_FILENAME.equals(entry.getName())) {
					module = (Module) marshaller.unmarshal(
							new StreamSource(new ByteArrayInputStream(zis.readAllBytes())));
				}
				// read jar
				else if (isJarFile(entry.getName())) {
					if (mapJars == null) {
						mapJars = new HashMap<>();
					}
					mapJars.put(entry.getName(), zis.readAllBytes());
				}
			}
		}
		Assert.stateAvailable(module, MODULE_META_FILENAME);
		
		// init custom libs content
		if (module != null && mapJars != null && module.getCustomLibs() != null) {
			for (CustomLib customLib : module.getCustomLibs()) {
				Assert.stateAvailable(mapJars.containsKey(customLib.getFilename()), customLib.getFilename());
				((CustomLibMetadata) customLib).setContent(mapJars.get(customLib.getFilename()));
			}
		}
		return module;
	}
	
	public byte[] exportModule(Module module) throws IOException {
		Assert.notNull(module, C.MODULE);
		
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
		    // write module
		    writeEntry(zos, MODULE_META_FILENAME, getModuleContent(module));
		    // write custom libs
		    for (CustomLib customLib : module.getCustomLibs()) {
		    	writeEntry(zos, customLib.getFilename(), customLib.getContent());
		    }
	    }
	    return baos.toByteArray();
	}
	
	public ImportAnalysis analyzeModule(Module module) {
		Assert.notNull(module, C.MODULE);
		final ImportAnalysis analysis = new ImportAnalysis(module);
		final Module currentVersionModule = getCurrentVersionModule(module);
		
		sortedServices.forEach(service -> service.analyzeObjects(analysis, currentVersionModule));
		return analysis;
	}
	
	public void importModule(Module module) {
		Assert.notNull(module, C.MODULE);
		final Module currentVersionModule = getCurrentVersionModule(module);
		final TransferContext context = new DefaultTransferContext(module);
		
		try (Session session = sessionFactoryProvider.getSessionFactory().openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				
				// module
				if (currentVersionModule != null) {
					deleteObjects(session, module, currentVersionModule);
					((ModuleMetadata) currentVersionModule).copySystemFieldsTo(module);
					session.detach(currentVersionModule);
				}
				moduleRepository.save(module, session);
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
				throw ex;
			}
		}
		
		// update configuration
		if (module.getEntities() != null) {
			configuration.updateConfiguration();
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
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		marshaller.marshal(module, new StreamResult(out));
		return out.toByteArray();
	}
	
	private static void writeEntry(ZipOutputStream zos, String name, byte[] content) throws IOException {
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
