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

import static org.seed.core.util.CollectionUtils.*;

import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.api.RestFunction.MethodType;
import org.seed.core.api.RestFunctionContext;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.codegen.CodeManager;
import org.seed.core.codegen.GeneratedCode;
import org.seed.core.data.Options;
import org.seed.core.data.ValidationException;
import org.seed.core.user.UserGroup;
import org.seed.core.user.UserGroupDependent;
import org.seed.core.user.UserGroupService;
import org.seed.core.util.Assert;
import org.seed.core.util.BeanUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Service
public class RestServiceImpl extends AbstractApplicationEntityService<Rest>
	implements RestService, UserGroupDependent<Rest> {
	
	@Autowired
	private RestRepository repository;
	
	@Autowired
	private RestValidator validator;
	
	@Autowired
	private UserGroupService userGroupService;
	
	@Autowired
	private CodeManager codeManager;
	
	@Autowired
	private HttpComponentsClientHttpRequestFactory clientHttpRequestFactory;
	
	@Override
	public Rest createInstance(@Nullable Options options) {
		final RestMetadata rest = (RestMetadata) super.createInstance(options);
		rest.createLists();
		return rest;
	}
	
	@Override
	public RestTemplate createTemplate(String url) {
		Assert.notNull(url, C.URL);
		
		final RestTemplate template = new RestTemplate(clientHttpRequestFactory);
		template.setUriTemplateHandler(new DefaultUriBuilderFactory(url));
		return template;
	}
	
	@Override
	public RestFunction createFunction(Rest rest) {
		Assert.notNull(rest, C.REST);
		
		final RestFunction function = new RestFunction();
		rest.addFunction(function);
		return function;
	}
	
	@Override
	public void removeFunction(Rest rest, RestFunction function) {
		Assert.notNull(rest, C.REST);
		
		rest.removeFunction(function);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { UserGroupService.class };
	}
	
	@Override
	public Rest findByMapping(String mapping) {
		Assert.notNull(mapping, "mapping");
		
		Rest rest = repository.findUnique(queryParam("mapping", '/' + mapping));
		if (rest == null) {
			rest = firstMatch(repository.find(), 
							  res -> mapping.equalsIgnoreCase(res.getInternalName()));
		}
		return rest;
	}
	
	@Override
	public List<Rest> findUsage(UserGroup userGroup) {
		Assert.notNull(userGroup, C.USERGROUP);
		
		return subList(getObjects(), rest -> anyMatch(rest.getPermissions(), 
													  perm -> userGroup.equals(perm.getUserGroup())));
	}
	
	@Override
	public Object callFunction(RestFunction function, MethodType method, 
							   Object body, String[] parameters) {
		Assert.notNull(function, C.FUNCTION);
		Assert.notNull(method, "method");
		
		final Class<GeneratedCode> functionClass = codeManager.getGeneratedClass(function);
		Assert.stateAvailable(functionClass, "function class");
		
		Object result = null;
		try (Session session = repository.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				
				// create context
				final RestFunctionContext context = 
						new DefaultRestFunctionContext(method, parameters, body, 
													   session, function.getRest().getModule());
				// create instance
				final org.seed.core.api.RestFunction functionInstance = 
						(org.seed.core.api.RestFunction) BeanUtils.instantiate(functionClass);
				
				// call function
				result = functionInstance.call(context);
				
				tx.commit();
			}
			catch (Exception ex) {
				if (tx != null) {
					tx.rollback();
				}
				throw new InternalException(ex);
			}
		}
		return result;
	}
	
	@Override
	public List<RestPermission> getAvailablePermissions(Rest rest) {
		Assert.notNull(rest, C.REST);
		
		return filterAndConvert(userGroupService.findNonSystemGroups(), 
								group -> !rest.containsPermission(group), 
								group -> createPermission(rest, group));
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
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		filterAndForEach(currentVersionModule.getRests(), 
						 rest -> module.getRestByUid(rest.getUid()) == null, 
						 session::delete);
	}
	
	@Override
	@Secured("ROLE_ADMIN_JOB")
	public void saveObject(Rest rest) throws ValidationException {
		Assert.notNull(rest, C.REST);
		
		super.saveObject(rest);
		updateConfiguration();
	}

	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		filterAndForEach(currentVersionModule.getRests(), 
						 rest -> analysis.getModule().getRestByUid(rest.getUid()) == null, 
						 analysis::addChangeDelete);
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
	
	private void initRest(Rest rest, Rest currentVersionRest, Session session) {
		if (rest.hasFunctions()) {
			rest.getFunctions().forEach(function -> initRestFunction(function, rest, currentVersionRest));
		}
		if (rest.hasPermissions()) {
			rest.getPermissions().forEach(perm -> initRestPermission(perm, rest, currentVersionRest, session));
		}
	}
	
	private void initRestFunction(RestFunction function, Rest rest, Rest currentVersionRest) {
		function.setRest(rest);
		final RestFunction currentVersionFunction = 
				currentVersionRest != null 
				? currentVersionRest.getFunctionByUid(function.getUid())
				: null;
		if (currentVersionFunction != null) {
			currentVersionFunction.copySystemFieldsTo(function);
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
	
	private static RestPermission createPermission(Rest rest, UserGroup group) {
		final RestPermission permission = new RestPermission();
		permission.setRest(rest);
		permission.setUserGroup(group);
		return permission;
	}

}
