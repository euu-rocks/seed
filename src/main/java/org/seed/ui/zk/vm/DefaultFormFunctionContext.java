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
package org.seed.ui.zk.vm;

import org.hibernate.Session;

import org.seed.C;
import org.seed.core.api.FormFunctionContext;
import org.seed.core.entity.value.event.ValueObjectFunctionContext;
import org.seed.core.util.Assert;
import org.seed.ui.ViewModelProperty;

import org.zkoss.zk.ui.Component;

class DefaultFormFunctionContext extends ValueObjectFunctionContext
	implements FormFunctionContext {
	
	private final AbstractFormViewModel viewModel;
	
	private final Component component;
	
	private final Object parameter;
	
	DefaultFormFunctionContext(AbstractFormViewModel viewModel, Component component, 
							   Object parameter, Session session) {
		super(session, viewModel.getForm().getModule());
		this.viewModel = viewModel;
		this.component = component;
		this.parameter = parameter;
	}

	@Override
	public AbstractFormViewModel getViewModel() {
		return viewModel;
	}

	@Override
	public Component getComponent() {
		return component;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getParameter() {
		return (T) parameter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getFormProperty(String name) {
		return (T) property(name).getValue();
	}

	@Override
	public void setFormProperty(String name, Object value) {
		property(name).setValue(value);
	}
	
	private ViewModelProperty property(String name) {
		Assert.notNull(name, C.NAME);
		
		return new ViewModelProperty(viewModel.getTab(), name);
	}

}
