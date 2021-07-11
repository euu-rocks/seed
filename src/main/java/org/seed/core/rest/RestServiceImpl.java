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

import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.user.UserGroupService;

import org.springframework.beans.factory.annotation.Autowired;

public class RestServiceImpl extends AbstractApplicationEntityService<Rest> {
	
	@Autowired
	private RestRepository repository;
	
	@Autowired
	private RestValidator validator;
	
	@Autowired
	private UserGroupService userGroupService;
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { UserGroupService.class };
	}

	@Override
	public void importObjects(TransferContext context, Session session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		// TODO Auto-generated method stub
		
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
