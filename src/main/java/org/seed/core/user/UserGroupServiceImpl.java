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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.data.Options;
import org.seed.core.data.QueryParameter;
import org.seed.core.data.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
		return repository.find(queryParam("isSystemGroup", false));
	}
	
	@Override
	public List<UserGroup> findNonSystemGroupsWithoutModule() {
		return repository.find(queryParam("isSystemGroup", false),
							   queryParam("module", QueryParameter.IS_NULL));
	}
	
	@Override
	public List<UserGroupAuthorisation> getAvailableAuthorisations(UserGroup userGroup) {
		Assert.notNull(userGroup, "userGroup is null");
		
		final List<UserGroupAuthorisation> result = new ArrayList<>();
		for (Authorisation authorisation : Authorisation.values()) {
			if (!userGroup.isAuthorised(authorisation)) {
				final UserGroupAuthorisation groupAuthorisation = new UserGroupAuthorisation();
				groupAuthorisation.setUserGroup(userGroup);
				groupAuthorisation.setAuthorisation(authorisation);
				result.add(groupAuthorisation);
			}
		}
		return result;
	}
	
	@Override
	public List<User> getAvailableUsers(UserGroup userGroup) {
		Assert.notNull(userGroup, "userGroup is null");
		
		final List<User> result = new ArrayList<>();
		for (User user : userRepository.find()) {
			boolean found = false;
			if (userGroup.hasUsers()) {
				for (User existingUser : userGroup.getUsers()) {
					if (existingUser.equals(user)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				result.add(user);
			}
		}
		return result;
	}
	
	@Override
	public void analyzeObjects(ImportAnalysis analysis, Module currentVersionModule) {
		Assert.notNull(analysis, "analysis is null");
		
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
		if (currentVersionModule != null && currentVersionModule.getUserGroups() != null) {
			for (UserGroup currentVersionGroup : currentVersionModule.getUserGroups()) {
				if (analysis.getModule().getUserGroupByUid(currentVersionGroup.getUid()) == null) {
					analysis.addChangeDelete(currentVersionGroup);
				}
			}
		}

	}
	
	@Override
	public Class<? extends ApplicationEntityService<? extends ApplicationEntity>>[] getImportDependencies() {
		return null; // independent
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, "context is null");
		Assert.notNull(session, "session is null");
		try {
			if (context.getModule().getUserGroups() != null) {
				for (UserGroup group : context.getModule().getUserGroups()) {
					((UserGroupMetadata) group).setModule(context.getModule());
					final UserGroup currentVersionGroup = findByUid(session, group.getUid());
					if (currentVersionGroup != null) {
						((UserGroupMetadata) currentVersionGroup).copySystemFieldsTo(group);
						session.detach(currentVersionGroup);
					}
					if (group.getAuthorisations() != null) {
						for (UserGroupAuthorisation auth : group.getAuthorisations()) {
							auth.setUserGroup(group);
						}
					}
					saveObject(group, session);
				}
			}
		}
		catch (ValidationException vex) {
			throw new RuntimeException(vex);
		}
 	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, "module is null");
		Assert.notNull(currentVersionModule, "currentVersionModule is null");
		Assert.notNull(session, "session is null");
		
		if (currentVersionModule.getUserGroups() != null) {
			for (UserGroup currentVersionGroup : currentVersionModule.getUserGroups()) {
				if (module.getUserGroupByUid(currentVersionGroup.getUid()) == null) {
					session.delete(currentVersionGroup);
				}
			}
		}
	}
	
	@Override
	public void saveObject(UserGroup userGroup) throws ValidationException {
		Assert.notNull(userGroup, "userGroup is null");
		
		final List<Long> originalUserIds = ((UserGroupMetadata) userGroup).getOriginalUserIds();
		Assert.state(originalUserIds != null, "originalUserIds is null");
		
		try (Session session = repository.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				saveObject(userGroup, session);

				if (userGroup.hasUsers()) {
					for (User user : userGroup.getUsers()) {
						if (!user.getUserGroups().contains(userGroup)) {
							user.getUserGroups().add(userGroup);
							userRepository.save(user, session);
						}
					}
				}
				
				for (Long userId : originalUserIds) {
					boolean found = false;
					for (User user : userGroup.getUsers()) {
						if (userId.equals(user.getId())) {
							found = true;
							break;
						}
					}
					if (!found) {
						final User user = userRepository.get(userId, session);
						user.getUserGroups().remove(userGroup);
						userRepository.save(user, session);
					}
				}
				
				tx.commit();
			}
			catch (Exception ex) {
				handleException(tx, ex);
			}
		}
	}

}
