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

import org.seed.core.application.AbstractRestController;
import org.seed.core.entity.EntityAccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/rest/transform")
public class TransformerRestController extends AbstractRestController<Transformer> {
	
	@Autowired
	private TransformerService transformerService;
	
	@Override
	protected TransformerService getService() {
		return transformerService;
	}
	
	@Override
	public List<Transformer> findAll() {
		return findAll(t -> checkPermissions(t.getSourceEntity(), EntityAccess.READ) &&
							checkPermissions(t.getTargetEntity(), EntityAccess.READ));
	}
	
	@Override
	public Transformer get(@PathVariable("id") Long id) {
		final Transformer transformer = super.get(id);
		if (!(checkPermissions(transformer.getSourceEntity(), EntityAccess.READ) &&
			  checkPermissions(transformer.getTargetEntity(), EntityAccess.READ))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		return transformer;
	}

}
