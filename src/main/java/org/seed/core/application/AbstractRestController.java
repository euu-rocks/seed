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
package org.seed.core.application;

import static org.seed.core.util.CollectionUtils.subList;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import org.hibernate.Session;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.data.FileObject;
import org.seed.core.user.Authorisation;
import org.seed.core.user.User;
import org.seed.core.user.UserService;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

public abstract class AbstractRestController<T extends ApplicationEntity> {
	
	@Autowired
	private UserService userService;
	
	protected List<T> getAll(Session session) {
		Assert.notNull(session, C.SESSION);
		
		return getService().getObjects(session);
	}
	
	protected List<T> getAll(Session session, Predicate<T> filter) {
		Assert.notNull(session, C.SESSION);
		
		return subList(getService().getObjects(session), filter);
	}
	
	protected T get(Session session, Long id) {
		Assert.notNull(session, C.SESSION);
		
		final T object = getService().getObject(id, session);
		if (object == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return object;
	}
	
	protected boolean checkPermissions(Session session, ApplicationEntity object) {
		Assert.notNull(session, C.SESSION);
		
		return checkPermissions(session, object, null);
	}
	
	protected boolean checkPermissions(Session session, ApplicationEntity object, Enum<?> access) {
		Assert.notNull(session, C.SESSION);
		
		return object.checkPermissions(getUser(session), access);
	}
	
	protected boolean isAuthorised(Session session, Authorisation authorisation) {
		Assert.notNull(session, C.SESSION);
		
		return getUser(session).isAuthorised(authorisation);
	}
	
	protected User getUser(Session session) {
		Assert.notNull(session, C.SESSION);
		final User user = userService.getCurrentUser(session);
		Assert.stateAvailable(user, C.USER);
		return user;
	}
	
	public static FileObject toFileObject(MultipartFile multipartFile) {
		Assert.notNull(multipartFile, "multipart file");
		try {
			final FileObject file = new FileObject();
			file.setName(multipartFile.getOriginalFilename());
			file.setContentType(multipartFile.getContentType());
			file.setContent(multipartFile.getBytes());
			return file;
		}
		catch (IOException ex) {
			throw new InternalException(ex);
		}
	}
	
	public static ResponseEntity<ByteArrayResource> download(String fileName, byte[] content) {
		Assert.notNull(fileName, "file name");
		Assert.notNull(content, C.CONTENT);
		
		return ResponseEntity
				.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + '\"')
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(createResource(content));
	}
	
	public static ResponseEntity<ByteArrayResource> stream(String contentType, byte[] content) {
		Assert.notNull(contentType, "content type");
		Assert.notNull(content, C.CONTENT);
		
		return ResponseEntity
				.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.body(createResource(content));
	}
	
	private static ByteArrayResource createResource(byte[] content) {
		return new ByteArrayResource(content);
	}
	
	protected abstract ApplicationEntityService<T> getService();
	
}
