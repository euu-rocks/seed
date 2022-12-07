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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ParameterMode;

import org.hibernate.procedure.ProcedureCall;
import org.hibernate.procedure.ProcedureOutputs;

import org.seed.core.api.StoredProcedureCall;
import org.seed.core.util.Assert;

class DefaultStoredProcedureCall implements StoredProcedureCall {
	
	private final Set<String> registeredOutParameters = new HashSet<>();
	
	private final ProcedureCall procedureCall;
	
	private ProcedureOutputs procedureOutputs;
	
	DefaultStoredProcedureCall(ProcedureCall procedureCall) {
		Assert.notNull(procedureCall, "procedure call");
		
		this.procedureCall = procedureCall;
	}

	@Override
	public void setParameter(int paramIndex, Object paramValue) {
		procedureCall.registerParameter(paramIndex, paramValue.getClass(), ParameterMode.IN)
					 .enablePassingNulls(true);
		procedureCall.setParameter(paramIndex, paramValue);
	}
	
	@Override
	public void setParameter(String paramName, Object paramValue) {
		procedureCall.registerParameter(paramName, paramValue.getClass(), ParameterMode.IN)
					 .enablePassingNulls(true);
		procedureCall.setParameter(paramName, paramValue);
	}
	
	@Override
	public void awaitOutput(int paramIndex, Class<?> outputClass) {
		procedureCall.registerParameter(paramIndex, outputClass, ParameterMode.INOUT)
					 .enablePassingNulls(true);
		procedureCall.setParameter(paramIndex, null);
		registeredOutParameters.add(String.valueOf(paramIndex));
	}
	
	@Override
	public void awaitOutput(String paramName, Class<?> outputClass) {
		procedureCall.registerParameter(paramName, outputClass, ParameterMode.INOUT)
					 .enablePassingNulls(true);
		procedureCall.setParameter(paramName, null);
		registeredOutParameters.add(paramName);
	}
	
	@Override
	public void execute() {
		procedureCall.execute();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getOutput(int paramIndex) {
		Assert.state(registeredOutParameters.contains(String.valueOf(paramIndex)), "call awaitOutput first");
		
		return (T) getOutputs().getOutputParameterValue(paramIndex);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getOutput(String paramName) {
		Assert.state(registeredOutParameters.contains(paramName), "call awaitOutput first");
		
		return (T) getOutputs().getOutputParameterValue(paramName);
	}
	
	private ProcedureOutputs getOutputs() {
		if (procedureOutputs == null) {
			procedureOutputs = procedureCall.getOutputs();
		}
		return procedureOutputs;
	}
	
}
