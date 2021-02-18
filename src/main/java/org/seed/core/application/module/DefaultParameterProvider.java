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
package org.seed.core.application.module;

import javax.annotation.Nullable;

import org.seed.core.api.ParameterProvider;

public class DefaultParameterProvider implements ParameterProvider {

	private final Module module;
	
	public DefaultParameterProvider(@Nullable Module module) {
		this.module = module;
	}

	@Override
	public boolean hasModuleParameter(String name) {
		return module != null && module.getParameter(name) != null;
	}

	@Override
	public String getModuleParameter(String name) {
		final ModuleParameter parameter = module != null ? module.getParameter(name) : null;
		return parameter != null ? parameter.getValue() : null;
	}

	@Override
	public String getModuleParameter(String name, String defaultValue) {
		final String parameter = getModuleParameter(name);
		return parameter != null ? parameter : defaultValue;
	}

}
