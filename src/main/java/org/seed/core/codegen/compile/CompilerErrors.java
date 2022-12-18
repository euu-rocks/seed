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
package org.seed.core.codegen.compile;

import static org.seed.core.util.CollectionUtils.convertedList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.seed.core.util.Assert;

public class CompilerErrors {
	
	public class CompilerError {
		
		private final String className;
		
		private final String errorMessage;

		private CompilerError(String className, String errorMessage) {
			this.className = className;
			this.errorMessage = errorMessage;
		}

		public String getClassName() {
			return className;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

	}
	
	private final List<CompilerError> errors;
	
	CompilerErrors(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
		Assert.notNull(diagnostics, "diagnostics");
		final Map<String, String> errorMap = new HashMap<>();
		
		for (Diagnostic<?> diagnostic : diagnostics) {
			final String msg = diagnostic.toString();
			final String name = msg.substring(0, msg.indexOf('.'));
			final String msgBuf = errorMap.get(name);
			errorMap.put(name, msgBuf == null ? msg : msgBuf + "\n\n" + msg);
		}
		errors = convertedList(errorMap.entrySet(), 
							   entry -> new CompilerError(entry.getKey(), 
									   					  entry.getValue()));
	}

	public List<CompilerError> getErrors() {
		return errors;
	}
	
}
