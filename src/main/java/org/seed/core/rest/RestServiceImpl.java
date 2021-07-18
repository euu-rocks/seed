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
package org.seed.core.rest;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.user.UserGroupService;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RestServiceImpl extends AbstractApplicationEntityService<Rest>
	implements RestService {
	
	@Autowired
	private RestRepository repository;
	
	@Autowired
	private RestValidator validator;
	
	@Autowired
	private UserGroupService userGroupService;
	
	@Override
	public RestMapping createMapping(Rest rest) {
		Assert.notNull(rest, "rest");
		
		final RestMapping mapping = new RestMapping();
		rest.addMapping(mapping);
		return mapping;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { UserGroupService.class };
	}

	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		
		for (Rest rest : context.getModule().getRests()) {
			final Rest currentVersionRest = findByUid(session, rest.getUid());
			((RestMetadata) rest).setModule(context.getModule());
			if (currentVersionRest != null) {
				((RestMetadata) currentVersionRest).copySystemFieldsTo(rest);
				session.detach(currentVersionRest);
			}
			initRest(rest, currentVersionRest, session);
			repository.save(rest, session);
		}
	}
	
	private void initRest(Rest rest, Rest currentVersionRest, Session session) {
		if (rest.hasMappings()) {
			for (RestMapping mapping : rest.getMappings()) {
				initRestMapping(mapping, rest, currentVersionRest);
			}
		}
		if (rest.hasPermissions()) {
			for (RestPermission permission : rest.getPermissions()) {
				initRestPermission(permission, rest, currentVersionRest, session);
			}
		}
	}
	
	private void initRestMapping(RestMapping mapping, Rest rest, Rest currentVersionRest) {
		mapping.setRest(rest);
		final RestMapping currentVersionMapping = 
				currentVersionRest != null 
				? currentVersionRest.getMappingByUid(mapping.getUid())
				: null;
		if (currentVersionMapping != null) {
			currentVersionMapping.copySystemFieldsTo(mapping);
		}
	}
	
	private void initRestPermission(RestPermission permission, Rest rest, Rest currentVersionRest, Session session) {
		permission.setRest(rest);
		permission.setUserGroup(userGroupService.findByUid(session, permission.getUserGroupUid()));
		final RestPermission currentVersionPermission =
				currentVersionRest != null
					? currentVersionRest.getPermissionByUid(permission.getUid())
					: null;
		if (currentVersionPermission != null) {
			currentVersionPermission.copySystemFieldsTo(permission);
		}
	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		if (currentVersionModule.getRests() != null) {
			for (Rest currentVersionRest : currentVersionModule.getRests()) {
				if (module.getReportByUid(currentVersionRest.getUid()) == null) {
					session.delete(currentVersionRest);
				}
			}
		}
	}

	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (currentVersionModule.getRests() != null) {
			for (Rest currentVersionRest : currentVersionModule.getRests()) {
				if (analysis.getModule().getRestByUid(currentVersionRest.getUid()) == null) {
					analysis.addChangeDelete(currentVersionRest);
				}
			}
		}
	}

	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (analysis.getModule().getRests() != null) {
			for (Rest rest : analysis.getModule().getRests()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(rest);
				}
				else {
					final Rest currentVersionRest = currentVersionModule.getRestByUid(rest.getUid());
					if (currentVersionRest == null) {
						analysis.addChangeNew(rest);
					}
					else if (!rest.isEqual(currentVersionRest)) {
						analysis.addChangeModify(rest);
					}
				}
			}
		}
	}

	@Override
	protected RestRepository getRepository() {
		return repository;
	}

	@Override
	protected RestValidator getValidator() {
		return validator;
	}

}
