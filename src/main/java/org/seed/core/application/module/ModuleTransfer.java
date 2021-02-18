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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.config.SessionFactoryProvider;
import org.seed.core.config.UpdatableConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * In case of "NPE while unmarshalling" 
 * check if public setter for collection exists (e.g. setElements(List<?> ...))
 */
@Component
public class ModuleTransfer {
	
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
	
	public Module readModule(InputStream inputStream) {
		Assert.notNull(inputStream, "inputStream is null");
		
		return (Module) marshaller.unmarshal(new StreamSource(inputStream));
	}
	
	public byte[] exportModule(Module module) {
		Assert.notNull(module, "module is null");
		
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		marshaller.marshal(module, new StreamResult(out));
		return out.toByteArray();
	}
	
	public ImportAnalysis analyzeModule(Module module) {
		Assert.notNull(module, "module is null");
		
		final ImportAnalysis analysis = new ImportAnalysis(module);
		final Module currentVersionModule = getCurrentVersionModule(module);
		
		sortedServices.forEach(service -> service.analyzeObjects(analysis, currentVersionModule));

		return analysis;
	}
	
	public void importModule(Module module) {
		Assert.notNull(module, "module is null");
		
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
				session.saveOrUpdate(module);
				
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
	
	private static List<ApplicationEntityService<?>> sortByDependencies(List<ApplicationEntityService<?>> applicationServices) {
		final List<ApplicationEntityService<?>> result = new ArrayList<>(applicationServices.size());
		while (result.size() < applicationServices.size()) {
			for (ApplicationEntityService<?> applicationService : applicationServices) {
				if (result.contains(applicationService)) {
					continue;
				}
				boolean doAddService = true;
				// check dependencies
				if (applicationService.getImportDependencies() != null) {
					for (Class<?> dependency : applicationService.getImportDependencies()) {
						boolean dependencyResolved = false;
						for (ApplicationEntityService<?> service : result) {
							if (dependency.isAssignableFrom(service.getClass())) {
								dependencyResolved = true;
								break;
							}
						}
						if (!dependencyResolved) {
							doAddService = false;
							break;
						}
					}
				}
				if (doAddService) {
					result.add(applicationService);
				}
			}
		}
		return result;
	}

}
