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

import org.hibernate.Session;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.codegen.CodeChangeAware;
import org.seed.core.codegen.SourceCode;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class CustomCodeServiceImpl extends AbstractApplicationEntityService<CustomCode>
	implements CustomCodeService, CodeChangeAware {

	@Autowired
	private CustomCodeRepository repository;
	
	@Autowired
	private CustomCodeValidator validator;
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return MiscUtils.toArray(); // independent
	}
	
	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (analysis.getModule().getCustomCodes() != null) {
			for (CustomCode customCode : analysis.getModule().getCustomCodes()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(customCode);
				}
				else {
					final CustomCode currentVersionCode =
						currentVersionModule.getCustomCodeByUid(customCode.getUid());
					if (currentVersionCode == null) {
						analysis.addChangeNew(customCode);
					}
					else if (!customCode.isEqual(currentVersionCode)) {
						analysis.addChangeModify(customCode);
					}
				}
			}
		}
	}
	
	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (currentVersionModule.getCustomCodes() != null) {
			for (CustomCode currentVersionCode : currentVersionModule.getCustomCodes()) {
				if (analysis.getModule().getCustomCodeByUid(currentVersionCode.getUid()) == null) {
					analysis.addChangeDelete(currentVersionCode);
				}
			}
		}
	}

	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		try {
			if (context.getModule().getCustomCodes() != null) {
				for (CustomCode customCode : context.getModule().getCustomCodes()) {
					final CustomCode currentVersionCode = findByUid(session, customCode.getUid());
					((CustomCodeMetadata) customCode).setModule(context.getModule());
					if (currentVersionCode != null) {
						((CustomCodeMetadata) currentVersionCode).copySystemFieldsTo(customCode);
						session.detach(currentVersionCode);
					}
					saveObject(customCode, session);
				}
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
		
		if (currentVersionModule.getCustomCodes() != null) {
			for (CustomCode currentVersionCode : currentVersionModule.getCustomCodes()) {
				if (module.getCustomCodeByUid(currentVersionCode.getUid()) == null) {
					session.delete(currentVersionCode);
				}
			}
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_SOURCECODE")
	public void saveObject(CustomCode customCode) throws ValidationException {
		Assert.notNull(customCode, "customCode");
		
		super.saveObject(customCode);
		updateConfiguration();
	}
	
	@Override
	@Secured("ROLE_ADMIN_SOURCECODE")
	public void deleteObject(CustomCode customCode) throws ValidationException {
		Assert.notNull(customCode, "customCode");
		
		super.deleteObject(customCode);
		updateConfiguration();
	}
	
	@Override
	public boolean processCodeChange(SourceCode sourceCode, Session session) {
		Assert.notNull(sourceCode, "sourceCode");
		Assert.notNull(session, C.SESSION);
		
		final CustomCode customCode = findByName(sourceCode.getQualifiedName(), session);
		if (customCode != null && !customCode.getContent().equals(sourceCode.getContent())) {
			
			customCode.setContent(sourceCode.getContent());
			repository.save(customCode);
			return true;
		}
		return false;
	}

	@Override
	protected CustomCodeRepository getRepository() {
		return repository;
	}

	@Override
	protected CustomCodeValidator getValidator() {
		return validator;
	}

}
