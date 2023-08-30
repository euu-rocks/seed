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
package org.seed.core.user;

import static org.seed.core.util.CollectionUtils.*;

import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.config.SchemaVersion;
import org.seed.core.data.Options;
import org.seed.core.data.QueryParameter;
import org.seed.core.data.ValidationException;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.core.util.UID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class UserGroupServiceImpl extends AbstractApplicationEntityService<UserGroup> 
	implements UserGroupService {
	
	@Autowired
	private UserGroupRepository repository;
	
	@Autowired
	private UserGroupValidator validator;
	
	@Autowired
	private UserRepository userRepository; 
	
	@Override
	protected UserGroupRepository getRepository() {
		return repository;
	}

	@Override
	protected UserGroupValidator getValidator() {
		return validator;
	}
	
	@Override
	public UserGroup createInstance(@Nullable Options options) {
		final UserGroupMetadata group = (UserGroupMetadata) super.createInstance(options);
		group.createLists();
		return group;
	}
	
	@Override
	public List<UserGroup> findNonSystemGroups() {
		return repository.find(nonSystemGroup());
	}
	
	@Override
	public List<UserGroup> findNonSystemGroups(Session session) {
		return repository.find(session, nonSystemGroup());
	}
	
	@Override
	public List<UserGroup> findNonSystemGroupsWithoutModule(Session session) {
		return repository.find(session, nonSystemGroup(),
							   			queryParam(C.MODULE, QueryParameter.IS_NULL));
	}
	
	@Override
	public List<UserGroupAuthorisation> getAvailableAuthorisations(UserGroup userGroup) {
		Assert.notNull(userGroup, C.USERGROUP);
		
		return filterAndConvert(Authorisation.values(), 
								not(userGroup::isAuthorised), 
								auth -> createAuthorization(userGroup, auth));
	}
	
	@Override
	public List<User> getAvailableUsers(UserGroup userGroup, Session session) {
		Assert.notNull(userGroup, C.USERGROUP);
		Assert.notNull(session, C.SESSION);
		
		return subList(userRepository.find(session), 
					   user -> noneMatch(userGroup.getUsers(), user::equals));
	}
	
	@Override
	public void handleSchemaUpdate(TransferContext context, SchemaVersion schemaVersion) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(schemaVersion, "schema version");
		
		if (ObjectUtils.isEmpty(context.getModule().getUserGroups())) {
			return;
		}
		if (schemaVersion == SchemaVersion.V_0_9_32) {
			for (UserGroup userGroup : context.getModule().getUserGroups()) {
				filterAndForEach(userGroup.getAuthorisations(), 
								 auth -> auth.getUid() == null, 
								 auth -> auth.setUid(UID.createUID()));
			}
		}
	}
	
	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (analysis.getModule().getUserGroups() != null) {
			for (UserGroup group : analysis.getModule().getUserGroups()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(group);
				}
				else {
					final UserGroup currentVersionGroup = 
						currentVersionModule.getUserGroupByUid(group.getUid());
					if (currentVersionGroup == null) {
						analysis.addChangeNew(group);
					}
					else if (!group.isEqual(currentVersionGroup)) {
						analysis.addChangeModify(group);
					}
				}
			}
		}
	}
	
	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		filterAndForEach(currentVersionModule.getUserGroups(), 
						 group -> analysis.getModule().getUserGroupByUid(group.getUid()) == null, 
						 analysis::addChangeDelete);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return MiscUtils.toArray(); // independent
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) throws ValidationException {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		
		if (context.getModule().getUserGroups() != null) {
			for (UserGroup group : context.getModule().getUserGroups()) {
				((UserGroupMetadata) group).setModule(context.getModule());
				final UserGroup currentVersionGroup = findByUid(session, group.getUid());
				if (currentVersionGroup != null) {
					((UserGroupMetadata) currentVersionGroup).copySystemFieldsTo(group);
					session.detach(currentVersionGroup);
				}
				if (group.hasAuthorisations()) {
					initAuthorisations(group, currentVersionGroup);
				}
				saveObject(group, session);
			}
		}
 	}
	
	private void initAuthorisations(UserGroup group, UserGroup currentVersionGroup) {
		for (UserGroupAuthorisation auth : group.getAuthorisations()) {
			auth.setUserGroup(group);
			final UserGroupAuthorisation currentVersionAuth =
					currentVersionGroup != null
						? currentVersionGroup.getAuthorisationByUid(auth.getUid())
						: null;
			if (currentVersionAuth != null) {
				currentVersionAuth.copySystemFieldsTo(auth);
			}
		}
	}
	
	@Override
	public void removeObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		filterAndForEach(currentVersionModule.getUserGroups(), 
						 group -> module.getUserGroupByUid(group.getUid()) == null, 
						 group -> session.saveOrUpdate(removeModule(group)));
	}
	
	@Override
	public void saveObject(UserGroup userGroup) throws ValidationException {
		Assert.notNull(userGroup, C.USERGROUP);
		
		final List<Long> originalUserIds = ((UserGroupMetadata) userGroup).getOriginalUserIds();
		Assert.notNull(originalUserIds != null, "originalUserIds");
		
		try (Session session = repository.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				saveObject(userGroup, session);
				// add users
				filterAndForEach(userGroup.getUsers(), 
								 user -> !user.getUserGroups().contains(userGroup), 
								 user -> { 
									 user.getUserGroups().add(userGroup);
								 	 userRepository.save(user, session); 
								 });
				// remove users
				filterAndForEach(originalUserIds, 
								 userId -> noneMatch(userGroup.getUsers(), user -> userId.equals(user.getId())), 
								 userId -> {
									 User user = userRepository.get(userId, session);
									 user.getUserGroups().remove(userGroup);
									 userRepository.save(user, session);
								 });
				tx.commit();
			}
			catch (Exception ex) {
				handleException(tx, ex);
			}
		}
	}
	
	private static QueryParameter nonSystemGroup() {
		return queryParam("isSystemGroup", false);
	}
	
	private static UserGroupAuthorisation createAuthorization(UserGroup userGroup, Authorisation authorisation) {
		final var groupAuthorisation = new UserGroupAuthorisation();
		groupAuthorisation.setUserGroup(userGroup);
		groupAuthorisation.setAuthorisation(authorisation);
		return groupAuthorisation;
	}

}
