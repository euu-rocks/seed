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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {
	
	@Autowired
	private Environment environment;
	
	@Bean
    public Docket api() { 
        return new Docket(DocumentationType.OAS_30)
						  .select()                                  
						  .apis(RequestHandlerSelectors.any())              
						  .paths(PathSelectors.ant("/seed/**"))
						  .build()
						  .ignoredParameterTypes(org.seed.core.customcode.CustomCode.class,
								  				 org.seed.core.customcode.CustomLib.class,
								  				 org.seed.core.data.dbobject.DBObject.class,
								  				 org.seed.core.data.datasource.IDataSource.class,
								  				 org.seed.core.data.datasource.DataSourceParameter.class,
								  				 org.seed.core.form.Form.class,
								  				 org.seed.core.form.navigation.Menu.class,
								  				 org.seed.core.application.module.Module.class,
								  				 org.seed.core.data.Options.class,
								  				 org.seed.core.user.User.class,
								  				 org.seed.core.user.UserGroup.class,
								  				 org.seed.core.entity.value.ValueObject.class);                                           
    }
	
	@Bean
	public WebMvcEndpointHandlerMapping 
				endpointHandlerMapping(WebEndpointsSupplier webEndpointsSupplier, 
									   ServletEndpointsSupplier servletEndpointsSupplier, 
									   ControllerEndpointsSupplier controllerEndpointsSupplier, 
									   EndpointMediaTypes endpointMediaTypes, 
									   CorsEndpointProperties corsEndpointProperties, 
									   WebEndpointProperties webEndpointProperties) {
		final String basePath = webEndpointProperties.getBasePath();
		final List<ExposableEndpoint<?>> endpoints = new ArrayList<>();
        endpoints.addAll(webEndpointsSupplier.getEndpoints());
        endpoints.addAll(servletEndpointsSupplier.getEndpoints());
        endpoints.addAll(controllerEndpointsSupplier.getEndpoints());
        return new WebMvcEndpointHandlerMapping(new EndpointMapping(basePath), 
        										webEndpointsSupplier.getEndpoints(), 
        										endpointMediaTypes, 
        										corsEndpointProperties.toCorsConfiguration(), 
        										new EndpointLinksResolver(endpoints, basePath), 
        										shouldRegisterLinksMapping(webEndpointProperties, basePath));
    }

	private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled() && 
        		(StringUtils.hasText(basePath) || 
        		 ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
    }
	
}
