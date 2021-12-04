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
package org.seed.core.customcode;

import java.util.List;

import org.hibernate.Session;

import org.seed.C;
import org.seed.InternalException;
import org.seed.Seed;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.codegen.Compiler;
import org.seed.core.codegen.compile.CustomJar;
import org.seed.core.codegen.compile.CustomJarProvider;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class CustomLibServiceImpl extends AbstractApplicationEntityService<CustomLib>
	implements CustomLibService, CustomJarProvider {
	
	@Autowired
	private CustomLibRepository repository;
	
	@Autowired
	private CustomLibValidator validator;
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return MiscUtils.toArray(); // independent
	}
	
	@Override
	public List<CustomJar> getCustomJars() {
		final List<CustomJar> jars = MiscUtils.castList(getObjects());
		jars.sort((CustomJar jar1, CustomJar jar2) -> 
					Integer.compare(jar1.getOrder() != null ? jar1.getOrder() : 0, 
									jar2.getOrder() != null ? jar2.getOrder() : 0));
		return jars;
	}
	
	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (analysis.getModule().getCustomLibs() != null) {
			for (CustomLib customLib : analysis.getModule().getCustomLibs()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(customLib);
				}
				else {
					final CustomLib currentVersionLib =
						currentVersionModule.getCustomLibByUid(customLib.getUid());
					if (currentVersionLib == null) {
						analysis.addChangeNew(customLib);
					}
					else if (!customLib.isEqual(currentVersionLib)) {
						analysis.addChangeModify(customLib);
					}
				}
			}
		}
	}

	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (currentVersionModule.getCustomLibs() != null) {
			for (CustomLib currentVersionLib : currentVersionModule.getCustomLibs()) {
				if (analysis.getModule().getCustomLibByUid(currentVersionLib.getUid()) == null) {
					analysis.addChangeDelete(currentVersionLib);
				}
			}
		}
	}

	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		try {
			if (context.getModule().getCustomLibs() != null) {
				for (CustomLib customLib : context.getModule().getCustomLibs()) {
					final CustomLib currentVersionLib = findByUid(session, customLib.getUid());
					((CustomLibMetadata) customLib).setModule(context.getModule());
					if (currentVersionLib != null) {
						((CustomLibMetadata) currentVersionLib).copySystemFieldsTo(customLib);
						session.detach(currentVersionLib);
					}
					saveObject(customLib, session);
				}
				resetCustomJars();
			}
		}
		catch (ValidationException vex) {
			throw new InternalException(vex);
		}
	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		if (currentVersionModule.getCustomLibs() != null) {
			for (CustomLib currentVersionLib : currentVersionModule.getCustomLibs()) {
				if (module.getCustomLibByUid(currentVersionLib.getUid()) == null) {
					session.delete(currentVersionLib);
				}
			}
		}
	}
	
	@Override
	public void reportError(CustomJar customJar, String error) {
		Assert.notNull(error, C.ERROR);
		
		saveError(customJar, error);
	}
	
	@Override
	public void resetError(CustomJar customJar) {
		saveError(customJar, null);
	}
	
	@Override
	@Secured("ROLE_ADMIN_SOURCECODE")
	public void saveObject(CustomLib customLib) throws ValidationException {
		Assert.notNull(customLib, "customLib");
		
		super.saveObject(customLib);
		resetCustomJars();
	}
	
	@Override
	@Secured("ROLE_ADMIN_SOURCECODE")
	public void deleteObject(CustomLib customLib) throws ValidationException {
		Assert.notNull(customLib, "customLib");
		
		super.deleteObject(customLib);
		resetCustomJars();
	}
	
	@Override
	protected CustomLibRepository getRepository() {
		return repository;
	}

	@Override
	protected CustomLibValidator getValidator() {
		return validator;
	}
	
	private void resetCustomJars() {
		Seed.getBean(Compiler.class).resetCustomJars();
	}
	
	private void saveError(CustomJar customJar, String error) {
		Assert.notNull(customJar, "customJar");
		final CustomLibMetadata libMeta = (CustomLibMetadata) customJar;
		Assert.state(!libMeta.isNew(), "customJar is new");
		
		libMeta.setError(error);
		saveObjectDirectly(libMeta);
	}
	
}
