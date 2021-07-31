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
package org.seed.core.config;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorHandler implements ErrorController {
	
	@GetMapping("/error")
    public String handleErrorGet(HttpServletRequest request, HttpServletResponse response) {
		return handleError(request);
	}
	
	@PostMapping("/error")
    public String handleErrorPost(HttpServletRequest request, HttpServletResponse response) {
		return handleError(request);
	}
	
	private static String handleError(HttpServletRequest request) {
        return "Error <b>" + getAttribute(request, RequestDispatcher.ERROR_STATUS_CODE)  + "</b>" + 
        		getAttribute(request, RequestDispatcher.ERROR_MESSAGE) +
        		getAttribute(request, RequestDispatcher.ERROR_EXCEPTION);
	}
	
	private static String getAttribute(HttpServletRequest request, String name) {
		final Object attribute = request.getAttribute(name);
		return attribute != null ? attribute.toString() : "";
	}
	
}
