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
package org.seed.core.entity.value.event;

import javax.annotation.Nullable;

import org.hibernate.Session;

import org.seed.core.api.AbstractFunctionContext;
import org.seed.core.api.CallbackFunctionContext;
import org.seed.core.api.ClientProvider;
import org.seed.core.api.DataSourceProvider;
import org.seed.core.api.EntityObjectProvider;
import org.seed.core.api.MailProvider;
import org.seed.core.api.ParameterProvider;
import org.seed.core.api.Status;
import org.seed.core.application.module.DefaultParameterProvider;
import org.seed.core.application.module.Module;
import org.seed.core.config.ApplicationContextProvider;
import org.seed.core.data.datasource.DefaultDataSourceProvider;
import org.seed.core.entity.EntityStatusTransition;
import org.seed.core.mail.DefaultMailProvider;

import org.springframework.util.Assert;

public class ValueObjectFunctionContext extends AbstractFunctionContext 
	implements CallbackFunctionContext {
	
	private final Session session;
	
	private final Module module;
	
	private final EntityStatusTransition statusTransition;
	
	private ClientProvider clientProvider;
	
	private ParameterProvider parameterProvider;
	
	private MailProvider mailProvider;
	
	private EntityObjectProvider objectProvider;
	
	private DataSourceProvider dataSourceProvider;
	
	public ValueObjectFunctionContext(Session session, Module module) {
		this(session, module, null);
	}
	
	protected ValueObjectFunctionContext(Session session, @Nullable Module module, 
										@Nullable EntityStatusTransition statusTransition) {
		Assert.notNull(session, "session is null");
		
		this.session = session;
		this.module = module;
		this.statusTransition = statusTransition;
	}
	
	@Override
	public Session getSession() {
		return session;
	}
	
	@Override
	public ClientProvider getClientProvider() {
		if (clientProvider == null) {
			clientProvider = ApplicationContextProvider.getBean(ClientProvider.class);
			Assert.state(clientProvider != null, "client provider not available");
		}
		return clientProvider;
	}
	
	@Override
	public ParameterProvider getParameterProvider() {
		if (parameterProvider == null) {
			parameterProvider = new DefaultParameterProvider(module);
		}
		return parameterProvider;
	}
	
	@Override
	public MailProvider getMailProvider() {
		if (mailProvider == null) {
			mailProvider = new DefaultMailProvider();
		}
		return mailProvider;
	}
	
	@Override
	public EntityObjectProvider getObjectProvider() {
		if (objectProvider == null) {
			objectProvider = new ValueObjectProvider(this); 
		}
		return objectProvider;
	}
	
	@Override
	public DataSourceProvider getDataSourceProvider() {
		if (dataSourceProvider == null) {
			dataSourceProvider = new DefaultDataSourceProvider(this);
		}
		return dataSourceProvider;
	}
	
	@Override
	public Status getSourceStatus() {
		return statusTransition != null ? statusTransition.getSourceStatus() : null;
	}
	
	@Override
	public Status getTargetStatus() {
		return statusTransition != null ? statusTransition.getTargetStatus() : null;
	}

}