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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpenSessionInViewFilter implements Filter {
	
	private static final Logger log = LoggerFactory.getLogger(OpenSessionInViewFilter.class);
	
	public static final String ATTR_SESSION = "HIBERNATE_SESSION";
	
	@Autowired
	private SessionProvider provider;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		try (Session session = provider.getSession()) {
			request.setAttribute(ATTR_SESSION, session);
			chain.doFilter(request, response);
			// if session changed due to configuration update
			if (!session.equals(request.getAttribute(ATTR_SESSION))) {
				// close new session
				((Session) request.getAttribute(ATTR_SESSION)).close();
			}
		}
		catch (IllegalStateException isex) {
			log.warn("{} {}", isex.getMessage(), ((HttpServletRequest) request).getRequestURI());
		}
	}

}
