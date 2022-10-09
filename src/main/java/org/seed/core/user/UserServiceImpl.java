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

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.data.AbstractSystemEntityService;
import org.seed.core.data.Options;
import org.seed.core.data.ValidationException;
import org.seed.core.mail.MailBuilder;
import org.seed.core.mail.MailService;
import org.seed.core.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends AbstractSystemEntityService<User> 
	implements UserService, UserGroupDependent<User>, ApplicationListener<AuthenticationSuccessEvent> {
	
	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Autowired
	private UserRepository repository;
	
	@Autowired
	private UserValidator validator;
	
	@Autowired
	private UserGroupService userGroupService;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public void initDefaults() {
		// if no user exist, create default user und group
		if (!repository.exist()) {
			createDefaultUserAndGroup();
		}
	}
	
	@Override
	public User createInstance(@Nullable Options options) {
		final UserMetadata user = (UserMetadata) super.createInstance(options);
		user.createLists();
		user.setEnabled(true);
		user.setPasswordChange(true);
		return user;
	}
	
	@Override
	public User getCurrentUser() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null
				? getUser(authentication)
				: null;
	}
	
	@Override
	public User getCurrentUser(Session session) {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null
				? getUser(authentication, session)
				: null;
	}
	
	@Override // called after successful login
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		final String userName = event.getAuthentication().getName();
		if (log.isDebugEnabled()) {
			log.debug("login user: {}", userName);
		}
		final User user = getUser(event.getAuthentication());
		Assert.state(user != null, "user not available: " + userName);
		((UserMetadata) user).setLastLogin(new Date());
		try {
			super.saveObject(user);
		} 
		catch (ValidationException vex) {
			throw new InternalException(vex);
		}
	}
	
	@Override
	public List<UserGroup> getAvailableUserGroups(User user) {
		Assert.notNull(user, C.USER);
		
		return subList(userGroupService.getObjects(), 
					   group -> noneMatch(user.getUserGroups(), group::equals));
	}
	
	@Override
	public List<User> findUsage(UserGroup userGroup) {
		Assert.notNull(userGroup, C.USERGROUP);
		
		return subList(getObjects(), 
					   user -> anyMatch(user.getUserGroups(), userGroup::equals));
	}
	
	@Override
	public void setPassword(User user, String password, String passwordRepeated) throws ValidationException {
		Assert.notNull(user, C.USER);
		
		validator.validatePassword(password, passwordRepeated);
		
		final UserMetadata userMeta = (UserMetadata) user;
		userMeta.setPasswordChange(true);
		userMeta.setPassword(password);
	}
	
	@Override
	@Secured("ROLE_ADMIN_USER")
	public void saveObject(User user) throws ValidationException {
		Assert.notNull(user, C.USER);
		
		String pwd = null;
		final boolean isInsert = user.isNew();
		final UserMetadata userMeta = (UserMetadata) user;
		// password change
		if (userMeta.isPasswordChange()) {
			pwd = user.getPassword() != null 
					? user.getPassword() 
					: user.getName();
			userMeta.setPassword(passwordEncoder.encode(pwd));
		}
		
		super.saveObject(user);
		
		// password change
		if (userMeta.isPasswordChange()) {
			userMeta.setPasswordChange(false);
			
			if (mailService.isMailingEnabled()) {
				sendPasswordMail(user, pwd, isInsert);
			}
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_USER")
	public void deleteObject(User user) throws ValidationException {
		super.deleteObject(user);
	}
	
	@Override
	protected UserRepository getRepository() {
		return repository;
	}

	@Override
	protected UserValidator getValidator() {
		return validator;
	}
	
	private User getUser(Authentication authentication) {
		return findByName(authentication.getName());
	}
	
	private User getUser(Authentication authentication, Session session) {
		return findByName(authentication.getName(), session);
	}
	
	private void sendPasswordMail(User user, String pwd, boolean registration) {
		final StringBuilder buf = new StringBuilder()
				.append("Benutzername: ").append(user.getName()).append('\n')
				.append("Passwort: ").append(pwd).append('\n');
		final MailBuilder mailBuilder = mailService.getMailBuilder()
				.setToAddress(user.getEmail())
				.setSubject(registration ? "Registrierung bei seed" : "Passwort-Änderung bei seed")
				.setText(buf.toString());
		mailService.sendMail(mailBuilder.build());
	}
	
	private void createDefaultUserAndGroup() {
		try (Session session = repository.openSession()) {
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				
				// create admin user group
				final UserGroupMetadata groupAdmin = new UserGroupMetadata();
				groupAdmin.createLists();
				groupAdmin.setName("Administration");
				groupAdmin.setSystemGroup(true);
				
				// grant authorisations
				for (Authorisation authorisation : Authorisation.values()) {
					final UserGroupAuthorisation groupAuthorisation = new UserGroupAuthorisation();
					groupAuthorisation.setUserGroup(groupAdmin);
					groupAuthorisation.setAuthorisation(authorisation);
					groupAdmin.getAuthorisations().add(groupAuthorisation);
				}
				userGroupService.saveObject(groupAdmin, session);
				log.info("User group 'Administration' created.");
				
				// create defaut user
				final UserMetadata user = (UserMetadata) createInstance(null);
				user.setEnabled(true);
				user.setName(C.SEED);
				user.setEmail("seed@seed.org");
				user.setPassword("$2a$10$yiUz8SpvqJNy4hVocA8EKOjcdsLg/POsJMg.b0YXmIHqFbUVFeS1S");
				user.getUserGroups().add(groupAdmin);
				saveObject(user, session);
				log.info("Default user 'seed' created.");
				
				tx.commit();
			}
			catch (Exception ex) {
				if (tx != null) {
					tx.rollback();
				}
				throw new InternalException(ex);
			}
		}
	}
	
}
