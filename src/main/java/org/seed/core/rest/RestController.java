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

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seed.core.api.RestFunction.MethodType;
import org.seed.core.user.Authorisation;
import org.seed.core.user.User;
import org.seed.core.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/seed/rest")
public class RestController {
	
	@Autowired
	private RestService restService;
	
	@Autowired
	private UserService userService;
	
	@GetMapping(value = "**")
    public ResponseEntity<Object> get(HttpServletRequest request, HttpServletResponse response) {
		return handleRequest(MethodType.GET, request, null);
	}
	
	@PostMapping(value = "**")
    public ResponseEntity<Object> post(HttpServletRequest request, HttpServletResponse response, @RequestBody Object body) {
		return handleRequest(MethodType.POST, request, body);
	}
	
	private ResponseEntity<Object> handleRequest(MethodType method, HttpServletRequest request, Object body) {
		final String uri = request.getRequestURI().substring(10); // "/seed/rest" - length
		if (!uri.isEmpty()) {
			final String[] uriParts = uri.substring(1).split("/"); 
			if (uriParts.length >= 2) {
				final Rest rest = restService.findByMapping(uriParts[0]);
				if (rest != null) {
					return callRest(rest, uriParts[1], method, body, 
									Arrays.copyOfRange(uriParts, 2, uriParts.length));
				}
			}
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	private ResponseEntity<Object> callRest(Rest rest, String functionMapping, 
											MethodType method, Object body, String[] parameters) {
		// check access
		final User user = userService.getCurrentUser();
		if (user == null || !user.isAuthorised(Authorisation.CALL_REST) ||
			!rest.checkPermissions(user)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		// call function
		final RestFunction function = rest.getFunctionByMapping(functionMapping);
		if (function != null && function.getMethod() == method) {
			try {
				final Object result = restService.callFunction(function, method, body, parameters);
				return new ResponseEntity<>(result, HttpStatus.OK);
			}
			catch (Exception ex) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
}
