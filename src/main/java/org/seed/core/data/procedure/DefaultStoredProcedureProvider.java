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
package org.seed.core.data.procedure;

import org.seed.C;
import org.seed.core.api.StoredProcedureCall;
import org.seed.core.api.StoredProcedureProvider;
import org.seed.core.entity.value.event.ValueObjectFunctionContext;
import org.seed.core.util.Assert;

public class DefaultStoredProcedureProvider implements StoredProcedureProvider {

	private final ValueObjectFunctionContext functionContext;
	
	public DefaultStoredProcedureProvider(ValueObjectFunctionContext functionContext) {
		Assert.notNull(functionContext, C.CONTEXT);
		
		this.functionContext = functionContext;
	}

	@Override
	public StoredProcedureCall createCall(String procedureName) {
		Assert.notNull(procedureName, "procedure name");
		
		return new DefaultStoredProcedureCall(functionContext.getSession()
							.createStoredProcedureCall(procedureName));
	}
	
}
