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

import java.util.Collections;
import java.util.Map;

import org.seed.C;
import org.seed.core.api.RestClient;
import org.seed.core.util.Assert;

import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

class DefaultRestClient implements RestClient {
	
	private RestTemplate template;

	DefaultRestClient(RestTemplate template) {
		Assert.notNull(template, "template");
		
		this.template = template;
	}
	
	@Override
	public String get(String path) {
		return get(path, null);
	}
	
	@Override
	public String get(String path, Map<String, Object> params) {
		Assert.notNull(path, C.PATH);
		
		return template.getForObject(path, String.class, emptyMapIfNull(params));
	}
	
	@Override
	public <T> T get(String path, Class<T> responseType, Map<String, Object> params) {
		Assert.notNull(path, C.PATH);
		Assert.notNull(responseType, "responseType");
		
		return template.getForObject(path, responseType, emptyMapIfNull(params));
	}
	
	@Override
	public <T> T post(String path, Class<T> responseType, @Nullable Object request) {
		return post(path, responseType, request, null);
	}
	
	@Override
	public <T> T post(String path, Class<T> responseType, @Nullable Object request, Map<String, Object> params) {
		Assert.notNull(path, C.PATH);
		Assert.notNull(responseType, "responseType");
		
		return template.postForObject(path, request, responseType, emptyMapIfNull(params));
	}
	
	private static Map<String, Object> emptyMapIfNull(Map<String, Object> params) {
		return params != null ? params : Collections.emptyMap();
	}
	
}
