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

import java.util.List;

import org.hibernate.Session;

import org.seed.core.api.RestFunction.MethodType;
import org.seed.core.application.ApplicationEntityService;

import org.springframework.web.client.RestTemplate;

public interface RestService extends ApplicationEntityService<Rest> {
	
	Rest findByMapping(String mapping);
	
	RestTemplate createTemplate(String url);
	
	RestFunction createFunction(Rest rest);
	
	void removeFunction(Rest rest, RestFunction function);
	
	List<RestPermission> getAvailablePermissions(Rest rest, Session session);
	
	Object callFunction(RestFunction function, MethodType method, 
						Object body, String[] parameters);
	
}
