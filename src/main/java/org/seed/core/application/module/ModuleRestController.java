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

import org.seed.core.config.OpenSessionInViewFilter;
import org.seed.core.data.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
	
	@ApiOperation(value = "getAllModules", notes = "returns a list of all modules")
	@GetMapping
	public List<Module> getAll(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session) {
		return service.getObjects(session);
	}
	
	@ApiOperation(value = "importModule", notes="imports module file specified by parameter 'file'")
	@PostMapping("/import")
	public ResponseEntity<String> importModule(@RequestParam("file") MultipartFile file) {
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

}
