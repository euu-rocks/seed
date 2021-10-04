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

import java.util.HashMap;
import java.util.Map;

import org.seed.C;
import org.seed.Seed;
import org.seed.core.api.RestClient;
import org.seed.core.api.RestProvider;
import org.seed.core.util.Assert;

public class DefaultRestProvider implements RestProvider {
	
	private final RestService restService;
	
	private final Map<String, RestClient> mapClients = new HashMap<>();
	
	public DefaultRestProvider() {
		restService = Seed.getBean(RestService.class);
	}
	
	@Override
	public RestClient getClient(String url) {
		Assert.notNull(url, C.URL);
		
		mapClients.computeIfAbsent(url, this::createClient);
		return mapClients.get(url);
	}
	
	private RestClient createClient(String url) {
		return new DefaultRestClient(restService.createTemplate(url));
	}
	
}
