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

import org.hibernate.Session;

import org.seed.core.api.RestFunction.MethodType;
import org.seed.core.api.ClientProvider;
import org.seed.core.api.RestFunctionContext;
import org.seed.core.application.module.Module;
import org.seed.core.entity.value.event.ValueObjectFunctionContext;

class DefaultRestFunctionContext extends ValueObjectFunctionContext
	implements RestFunctionContext {
	
	private final MethodType methodType;
	
	private final Object body; 
	
	private final String[] parameters;

	DefaultRestFunctionContext(MethodType methodType, String[] parameters, Object body, 
							   Session session, Module module) {
		super(session, module);
		this.methodType = methodType;
		this.body = body;
		this.parameters = parameters;
	}
	
	@Override
	public ClientProvider getClientProvider() {
		throw new UnsupportedOperationException("client not available in rest context");
	}
	
	@Override
	public MethodType getMethodType() {
		return methodType;
	}
	
	@Override
	public Object getBody() {
		return body;
	}

	@Override
	public String[] getParameters() {
		return parameters;
	}

}
