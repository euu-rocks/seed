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
package org.seed.core.entity.transform;

import java.util.List;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.application.AbstractRestController;
import org.seed.core.config.OpenSessionInViewFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/seed/rest/transform")
public class TransformerRestController extends AbstractRestController<Transformer> {
	
	@Autowired
	private TransformerService transformerService;
	
	@Override
	protected TransformerService getService() {
		return transformerService;
	}
	
	@Override
	@ApiOperation(value = "getAllTransformations", notes="returns a list of all authorized transformations")
	@GetMapping
	public List<Transformer> getAll(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session) {
		return getAll(session, transformer -> checkPermissions(session, transformer));
	}
	
	@Override
	@ApiOperation(value = "getTransformationById", notes="returns the transformation with the given id")
	@GetMapping(value = "/{id}")
	public Transformer get(@RequestAttribute(OpenSessionInViewFilter.ATTR_SESSION) Session session,
						   @PathVariable(C.ID) Long id) {
		final Transformer transformer = super.get(session, id);
		if (transformer != null && !checkPermissions(session, transformer)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		return transformer;
	}

}
