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
package org.seed.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.zkoss.zk.au.http.DHtmlUpdateServlet;
import org.zkoss.zk.ui.http.HttpSessionListener;

public class ZKCEConfig {

	@Bean
	public ViewResolver zulViewResolver() {
		return new InternalResourceViewResolver("/zkau/web/zul/", ".zul");
	}
	
	@Bean
	public ServletRegistrationBean<DHtmlUpdateServlet> dHtmlUpdateServlet() {
		return new ServletRegistrationBean<>(new DHtmlUpdateServlet(), "/zkau/*");
	}
	
	@Bean
	public HttpSessionListener httpSessionListener() {
		return new HttpSessionListener();
	}
	
}
