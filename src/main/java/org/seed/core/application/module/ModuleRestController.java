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

import java.util.List;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.application.AbstractRestController;
import org.seed.core.config.OpenSessionInViewFilter;
import org.seed.core.data.ValidationException;
import org.seed.core.user.Authorisation;
import org.seed.core.user.User;
import org.seed.core.user.UserService;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/seed/rest/module")
public class ModuleRestController {
	
	@Autowired
	private ModuleService service;
	
	@Autowired
	private UserService userService;
	
	@ApiOperation(value = "getAllModules", notes = "returns a list of all modules")
	@GetMapping
	public List<Module> getAll(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session) {
		return service.getObjects(session);
	}
	
	@ApiOperation(value = "exportModule", notes="exports the module with the given id")
	@GetMapping(value = "/{id}/export")
	public ResponseEntity<ByteArrayResource> exportModule(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
														  @PathVariable(C.ID) Long id) {
		checkAuthorisation(session);
		final Module module = service.getObject(id, session);
		if (module == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, C.MODULE + ' ' + id);
		}
		return AbstractRestController.download(module.getFileName(), service.exportModule(module));
	}
	
	@ApiOperation(value = "importModule", notes="imports the module file specified by parameter 'file'")
	@PostMapping("/import")
	public ResponseEntity<String> importModule(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
											   @RequestParam("file") MultipartFile file) {
		checkAuthorisation(session);
		if (file == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file not found");
		}
		try {
			service.importModule(service.readModule(file.getInputStream()));
		}
		catch (ValidationException vex) {
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, vex.getMessage());
		}
		catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
		return new ResponseEntity<>(file.getOriginalFilename() + " imported", HttpStatus.OK);
	}
	
	private void checkAuthorisation(Session session) {
		Assert.notNull(session, C.SESSION);
		final User user = userService.getCurrentUser(session);
		Assert.stateAvailable(user, C.USER);
		if (!user.isAuthorised(Authorisation.ADMIN_MODULE)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
	}

}
