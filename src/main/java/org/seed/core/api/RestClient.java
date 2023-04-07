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
package org.seed.core.api;

import java.util.Map;

import org.springframework.lang.Nullable;

/**
 * A <code>RestClient</code> is a client for REST services.
 * It can be used to query data from the service or send data to the service.
 * 
 * @author seed-master
 *
 */
public interface RestClient {
	
	/**
	 * Sends a GET request for the given path and returns the response as String.
	 * @param path the request path
	 * @return the response as String
	 */
	String get(String path);
	
	/**
	 * Sends a GET request for the given path with the given parameters
	 * and returns the response as String.
	 * @param path the request path
	 * @param params a map of named parameters
	 * @return the response as String
	 */
	String get(String path, Map<String, Object> params);
	
	/**
	 * Sends a GET request for the given path with the given parameters
	 * and returns the response as an object of the given response type
	 * @param <T> the type of the response object
	 * @param path the request path
	 * @param responseType the class of the response
	 * @param params a map of named parameters
	 * @return the response as an object of the given response type
	 */
	<T> T get(String path, Class<T> responseType, Map<String, Object> params);
	
	/**
	 * Sends an object via a POST request for the given path
	 * and returns the response as an object of the given response type
	 * @param <T> the type of the response object
	 * @param path the request path
	 * @param responseType the class of the response
	 * @param requestObject the object to be POSTed (may be <code>null</code>)
	 * @return the response as an object of the given response type
	 */
	<T> T post(String path, Class<T> responseType, @Nullable Object requestObject);
	
	/**
	 * Sends an object via a POST request for the given path with the given parameters
	 * and returns the response as an object of the given response type
	 * @param <T> the type of the response object
	 * @param path the request path
	 * @param responseType
	 * @param requestObject the class of the response
	 * @param params
	 * @return the response as an object of the given response type
	 */
	<T> T post(String path, Class<T> responseType, @Nullable Object requestObject, Map<String, Object> params);
	
}
